package mcjty.ariente.datagen;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;
import mcjty.ariente.Ariente;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/** Projects the existing straight-beam stripe onto a continuous west-to-south turn. */
public class FluxBeamTextures implements DataProvider {

    private final PackOutput output;

    public FluxBeamTextures(PackOutput output, ExistingFileHelper helper) {
        this.output = output;
        helper.trackGenerated(ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "block/machines/flux_bend_beam"),
                PackType.CLIENT_RESOURCES, ".png", "textures");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.runAsync(() -> {
            try (var input = Ariente.class.getResourceAsStream("/assets/ariente/textures/block/machines/flux_beam.png");
                 NativeImage strip = NativeImage.read(input);
                 NativeImage bend = new NativeImage(64, 64, false)) {
                for (int x = 0; x < 64; x++) {
                    for (int z = 0; z < 64; z++) {
                        double dx = (x + .5) / 4 - 8;
                        double dz = (z + .5) / 4 - 8;
                        double corner = Math.hypot(dx, dz);
                        double distance = Math.min(dx <= 0 ? Math.abs(dz) : corner, dz >= 0 ? Math.abs(dx) : corner);
                        int row = Math.min(strip.getHeight() - 1, (int) (8 + distance * 16 / 6));
                        bend.setPixelRGBA(x, z, strip.getPixelRGBA(8, row));
                    }
                }
                byte[] png = bend.asByteArray();
                cache.writeIfNeeded(output.getOutputFolder().resolve("assets/ariente/textures/block/machines/flux_bend_beam.png"),
                        png, Hashing.sha1().hashBytes(png));
            } catch (IOException exception) {
                throw new UncheckedIOException("Could not generate the flux bend texture", exception);
            }
        });
    }

    @Override
    public String getName() {
        return "Ariente flux bend texture";
    }
}
