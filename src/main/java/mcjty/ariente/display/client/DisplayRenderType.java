package mcjty.ariente.display.client;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public final class DisplayRenderType {

    private static final RenderPipeline IMAGE = imagePipeline("image", BlendFunction.TRANSLUCENT);
    private static final RenderPipeline CAMERA_IMAGE = imagePipeline("camera", BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA);
    private static final RenderPipeline COLOR = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("ariente", "pipeline/display_color"))
            .withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false)).build();
    private static final Function<Identifier, RenderType> CAMERAS = Util.memoize(texture -> image(texture, CAMERA_IMAGE));
    public static final RenderType CAMERA = image(TextureAtlas.LOCATION_BLOCKS, IMAGE);
    public static final RenderType ALIEN_FONT = image(Identifier.fromNamespaceAndPath("ariente", "textures/effects/alientfont.png"), IMAGE);
    public static final RenderType SURFACE = RenderType.create("ariente_display", RenderSetup.builder(COLOR).createRenderSetup());

    private static final RenderPipeline SCENE_SOLID = scenePipeline("solid", false);
    private static final RenderPipeline SCENE_TRANSLUCENT = scenePipeline("translucent", true);

    private static RenderPipeline scenePipeline(String name, boolean translucent) {
        return RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("ariente", "pipeline/snapshot_" + name))
                .withVertexShader("core/position_tex_color").withFragmentShader("core/position_tex_color")
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR).withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withCull(false).withColorTargetState(translucent ? new ColorTargetState(BlendFunction.TRANSLUCENT) : ColorTargetState.DEFAULT)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, !translucent)).build();
    }

    private static final RenderType SCENE_SOLID_TYPE = image(TextureAtlas.LOCATION_BLOCKS, SCENE_SOLID);
    private static final RenderType SCENE_TRANSLUCENT_TYPE = image(TextureAtlas.LOCATION_BLOCKS, SCENE_TRANSLUCENT);

    public static RenderType scene(boolean translucent) {
        return translucent ? SCENE_TRANSLUCENT_TYPE : SCENE_SOLID_TYPE;
    }

    private DisplayRenderType() {
    }

    private static RenderPipeline imagePipeline(String name, BlendFunction blend) {
        return RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("ariente", "pipeline/display_" + name))
                .withVertexShader("core/position_tex_color").withFragmentShader("core/position_tex_color")
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR).withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withCull(false).withColorTargetState(new ColorTargetState(blend))
                .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false)).build();
    }

    private static RenderType image(Identifier texture, RenderPipeline pipeline) {
        return RenderType.create("ariente_display_image", RenderSetup.builder(pipeline).withTexture("Sampler0", texture).createRenderSetup());
    }

    public static RenderType cameraImage(Identifier texture) {
        return CAMERAS.apply(texture);
    }

    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(IMAGE);
        event.registerPipeline(CAMERA_IMAGE);
        event.registerPipeline(COLOR);
        event.registerPipeline(SCENE_SOLID);
        event.registerPipeline(SCENE_TRANSLUCENT);
    }
}
