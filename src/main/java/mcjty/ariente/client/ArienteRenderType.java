package mcjty.ariente.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

public class ArienteRenderType extends RenderType {

    // Dummy
    public ArienteRenderType(String name, VertexFormat format, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable setup, Runnable clear) {
        super(name, format, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, setup, clear);
    }

    public static final RenderType ARIENTE_SOLID = makeType("ariente_solid", DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 2097152, true, false,
            RenderType.State.getBuilder()
                    .shadeModel(SHADE_ENABLED)
                    .lightmap(LIGHTMAP_ENABLED)
                    .texture(new TextureState(ArienteSpriteUploader.ARIENTE_ATLAS, false, false)).build(true));
}
