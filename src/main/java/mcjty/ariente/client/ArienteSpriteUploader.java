package mcjty.ariente.client;

import mcjty.ariente.Ariente;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;

/** The game's atlas manager owns loading, animation and resource reloads. */
public final class ArienteSpriteUploader {

    public static final ArienteSpriteUploader INSTANCE = new ArienteSpriteUploader();
    public static final Identifier ARIENTE_ATLAS = Identifier.fromNamespaceAndPath(Ariente.MODID, "textures/atlas/ariente.png");
    public static final Identifier ATLAS_ID = Identifier.fromNamespaceAndPath(Ariente.MODID, "ariente");

    private ArienteSpriteUploader() {
    }

    public TextureAtlasSprite getSprite(Identifier location) {
        return Minecraft.getInstance().getAtlasManager().get(new SpriteId(ARIENTE_ATLAS, location));
    }
}
