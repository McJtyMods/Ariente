package mcjty.ariente.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public class ArienteRenderType extends RenderType {

    // Dummy
    public ArienteRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable setup, Runnable clear) {
        super(name, format, mode, p_173181_, p_173182_, p_173183_, setup, clear);
    }

    public static final TextureStateShard ARIENTE_TEXTURE_STATE = new TextureStateShard(ArienteSpriteUploader.ARIENTE_ATLAS, false, false);

    public static final RenderType ARIENTE_SOLID = create("ariente_solid", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, false,
            CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_SMOOTH_CUTOUT_SHADER)
                    .setLightmapState(LIGHTMAP)
                    .setTextureState(ARIENTE_TEXTURE_STATE)
                    .createCompositeState(true));

    public static final RenderType ARIENTE_TRANSLUCENT = create("ariente_translucent", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 262144, true, true,
            CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_SMOOTH_CUTOUT_SHADER)
                    .setLightmapState(LIGHTMAP)
                    .setTextureState(BLOCK_SHEET_MIPPED)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(ARIENTE_TEXTURE_STATE)
                    .createCompositeState(true));
}
