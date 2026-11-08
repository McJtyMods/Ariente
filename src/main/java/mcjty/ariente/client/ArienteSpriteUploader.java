package mcjty.ariente.client;

import mcjty.ariente.Ariente;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;

public class ArienteSpriteUploader extends TextureAtlasHolder {

    public static ArienteSpriteUploader INSTANCE;

    public static final ResourceLocation ARIENTE_ATLAS = ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "textures/atlas/ariente.png");

    public ArienteSpriteUploader(TextureManager textureManager) {
        super(textureManager, ARIENTE_ATLAS, ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "ariente"));
    }

    @Override
    public TextureAtlasSprite getSprite(ResourceLocation location) {
        return super.getSprite(location);
    }
}
