package mcjty.ariente.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.RenderState.TextureState;
import net.minecraft.client.renderer.RenderType.State;

public class ArienteRenderType extends RenderType {

    // Dummy
    public ArienteRenderType(String name, VertexFormat format, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable setup, Runnable clear) {
        super(name, format, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, setup, clear);
    }

    public static final TextureState ARIENTE_TEXTURE_STATE = new TextureState(ArienteSpriteUploader.ARIENTE_ATLAS, false, false);

    public static final RenderType ARIENTE_SOLID = create("ariente_solid", DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 2097152, true, false,
            RenderType.State.builder()
                    .setShadeModelState(SMOOTH_SHADE)
                    .setLightmapState(LIGHTMAP)
                    .setTextureState(ARIENTE_TEXTURE_STATE)
                    .createCompositeState(true));

    public static final RenderType ARIENTE_TRANSLUCENT = create("ariente_translucent", DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 262144, true, true,
            State.builder()
                    .setShadeModelState(SMOOTH_SHADE)
                    .setLightmapState(LIGHTMAP)
                    .setTextureState(BLOCK_SHEET_MIPPED)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(ARIENTE_TEXTURE_STATE)
                    .createCompositeState(true));
}
