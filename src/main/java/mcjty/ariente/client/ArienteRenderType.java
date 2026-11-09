package mcjty.ariente.client;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public final class ArienteRenderType {

    private static final List<RenderPipeline> PIPELINES = new ArrayList<>();
    public static final RenderType SHIP_HULL = RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS);
    public static final RenderType TANK_CONTENTS = type("tank_contents", TextureAtlas.LOCATION_BLOCKS, BlendFunction.TRANSLUCENT, false, true, true);
    public static final RenderType FIELD_MARKER = type("field_marker", TextureAtlas.LOCATION_BLOCKS, null, false, true, true);
    public static final RenderType FIELD_OUTLINE = type("field_outline", ArienteSpriteUploader.ARIENTE_ATLAS, BlendFunction.ADDITIVE, true, false, false);
    public static final RenderType BEAM = type("beam", TextureAtlas.LOCATION_BLOCKS, BlendFunction.ADDITIVE, true, true, false);
    public static final RenderType SHIP_EXHAUST = type("ship_exhaust", TextureAtlas.LOCATION_BLOCKS, BlendFunction.ADDITIVE, true, false, false);
    public static final RenderType ARIENTE_SOLID = type("solid", ArienteSpriteUploader.ARIENTE_ATLAS, null, false, true, true);
    public static final RenderType ARIENTE_TRANSLUCENT = type("translucent", ArienteSpriteUploader.ARIENTE_ATLAS, BlendFunction.TRANSLUCENT, false, true, true);
    public static final RenderType ARIENTE_ADDITIVE = type("additive", ArienteSpriteUploader.ARIENTE_ATLAS, BlendFunction.ADDITIVE, true, false, false);

    private static final RenderPipeline FIELD_TRIANGLES = fieldPipeline("field_triangles", VertexFormat.Mode.TRIANGLES);
    private static final RenderPipeline FIELD_QUADS = fieldPipeline("field_quads", VertexFormat.Mode.QUADS);
    private static final Map<String, RenderType> FIELDS = new HashMap<>();

    private static RenderPipeline fieldPipeline(String name, VertexFormat.Mode mode) {
        var pipeline = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("ariente", "pipeline/" + name))
                .withVertexShader("core/position_tex_color").withFragmentShader("core/position_tex_color")
                .withSampler("Sampler0").withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, mode)
                .withCull(false).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false)).build();
        PIPELINES.add(pipeline);
        return pipeline;
    }

    public static RenderType forceField(Identifier texture, VertexFormat.Mode mode) {
        return FIELDS.computeIfAbsent(texture + "/" + mode, key -> RenderType.create("ariente_forcefield",
                RenderSetup.builder(mode == VertexFormat.Mode.TRIANGLES ? FIELD_TRIANGLES : FIELD_QUADS)
                        .withTexture("Sampler0", texture).createRenderSetup()));
    }

    private ArienteRenderType() {
    }

    private static RenderType type(String name, Identifier texture, BlendFunction blend, boolean glow, boolean writeDepth, boolean lightmap) {
        var builder = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("ariente", "pipeline/" + name))
                .withShaderDefine("NO_CARDINAL_LIGHTING").withShaderDefine("NO_OVERLAY")
                .withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, writeDepth));
        if (blend != null) {
            builder.withColorTargetState(new ColorTargetState(blend));
        }
        if (!lightmap) {
            builder.withShaderDefine("EMISSIVE");
        }
        if (glow) {
            builder.withFragmentShader(Identifier.fromNamespaceAndPath("ariente", "beam"));
        }
        RenderPipeline pipeline = builder.build();
        PIPELINES.add(pipeline);
        var setup = RenderSetup.builder(pipeline).withTexture("Sampler0", texture);
        if (blend == BlendFunction.TRANSLUCENT) {
            setup.sortOnUpload();
        }
        if (lightmap) {
            setup.useLightmap();
        }
        return RenderType.create("ariente_" + name, setup.createRenderSetup());
    }

    public static RenderType displayItem(RenderType type) {
        return type;
    }

    public static void registerShaders(RegisterRenderPipelinesEvent event) {
        PIPELINES.forEach(event::registerPipeline);
    }
}
