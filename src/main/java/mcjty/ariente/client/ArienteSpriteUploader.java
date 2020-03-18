package mcjty.ariente.client;

import mcjty.ariente.Ariente;
import net.minecraft.client.renderer.texture.SpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static mcjty.ariente.blocks.utility.door.DoorMarkerRenderer.DOOR_MARKER_TEXTURE;
import static mcjty.ariente.cables.CableRenderer.*;

public class ArienteSpriteUploader extends SpriteUploader {

    public static ArienteSpriteUploader INSTANCE;

    public static final ResourceLocation ARIENTE_ATLAS = new ResourceLocation(Ariente.MODID, "textures/atlas/ariente.png");

    private final List<ResourceLocation> textures = new ArrayList<>();

    public ArienteSpriteUploader(TextureManager textureManager) {
        super(textureManager, ARIENTE_ATLAS, "effects");

        textures.add(DOOR_MARKER_TEXTURE);
        textures.add(NEGARITE_LASERBEAM_1);
        textures.add(NEGARITE_LASERBEAM_2);
        textures.add(NEGARITE_LASERBEAM_3);
        textures.add(NEGARITE_LASERBEAM_4);
        textures.add(POSIRITE_LASERBEAM_1);
        textures.add(POSIRITE_LASERBEAM_2);
        textures.add(POSIRITE_LASERBEAM_3);
        textures.add(POSIRITE_LASERBEAM_4);
        textures.add(DATA_LASERBEAM_1);
        textures.add(DATA_LASERBEAM_2);
        textures.add(DATA_LASERBEAM_3);
        textures.add(DATA_LASERBEAM_4);
    }

    @Override
    protected Stream<ResourceLocation> getResourceLocations() {
        return textures.stream();
    }

    @Override
    public TextureAtlasSprite getSprite(ResourceLocation location) {
        return super.getSprite(location);
    }
}
