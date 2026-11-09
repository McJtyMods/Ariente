package mcjty.ariente.display.client;

import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.oit.OitPipelineSet;
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
    private static final OitPipelineSet IMAGE_OIT = imageOitPipeline("display_image", false);
    private static final OitPipelineSet CAMERA_IMAGE_OIT = imageOitPipeline("display_camera", true);
    private static final Function<Identifier, RenderType> CAMERAS = Util.memoize(texture -> image(texture, CAMERA_IMAGE));
    public static final RenderType CAMERA = image(TextureAtlas.LOCATION_BLOCKS, IMAGE);
    public static final RenderType ALIEN_FONT = image(Identifier.fromNamespaceAndPath("ariente", "textures/effects/alientfont.png"), IMAGE);
    public static final RenderType SURFACE = RenderType.create("ariente_display", RenderSetup.builder(COLOR).setOitPipelines(RenderPipelines.OIT_DEBUG_QUADS).createRenderSetup());

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

    private static OitPipelineSet imageOitPipeline(String name, boolean premultiplied) {
        var builder = RenderPipeline.builder()
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS).withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                .withVertexShader("core/position_tex_color").withFragmentShader(Identifier.parse("hologui:image"))
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR).withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withCull(false);
        if (premultiplied) builder.withShaderDefine("PREMULTIPLIED_ALPHA");
        return OitPipelineSet.builder(Identifier.fromNamespaceAndPath("ariente", name), builder).build();
    }

    private static RenderType image(Identifier texture, RenderPipeline pipeline) {
        var setup = RenderSetup.builder(pipeline).withTexture("Sampler0", texture);
        if (pipeline == IMAGE) setup.setOitPipelines(IMAGE_OIT);
        if (pipeline == CAMERA_IMAGE) setup.setOitPipelines(CAMERA_IMAGE_OIT);
        return RenderType.create("ariente_display_image", setup.createRenderSetup());
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
        event.registerOitPipelineSet(IMAGE_OIT);
        event.registerOitPipelineSet(CAMERA_IMAGE_OIT);
    }
}
