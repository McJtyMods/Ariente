package mcjty.ariente.datagen;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import mcjty.ariente.Ariente;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;

/** Keep the artist's first frame and housing intact; rotate only the radial field inside it. */
public class ChargerTextures implements DataProvider {

    private static final int FRAMES = 32;
    private final PackOutput output;

    public ChargerTextures(PackOutput output, ExistingFileHelper helper) {
        this.output = output;
        for (String name : new String[]{"charger_active", "charger_glow", "charger_off"}) {
            helper.trackGenerated(new ResourceLocation(Ariente.MODID, "block/machines/" + name),
                    PackType.CLIENT_RESOURCES, ".png", "textures");
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.runAsync(() -> {
            try (var input = Ariente.class.getResourceAsStream("/assets/ariente/textures/block/machines/charger.png");
                 NativeImage source = NativeImage.read(input);
                 NativeImage active = new NativeImage(source.getWidth(), source.getHeight() * FRAMES, false);
                 NativeImage glow = new NativeImage(source.getWidth(), source.getHeight() * FRAMES, true);
                 NativeImage off = new NativeImage(source.getWidth(), source.getHeight(), false)) {
                int size = source.getWidth();
                int edge = size / 8;
                double center = (size - 1) / 2.0;
                for (int frame = 0; frame < FRAMES; frame++) {
                    double angle = -frame * Math.PI * 2 / FRAMES;
                    for (int y = 0; y < size; y++) {
                        for (int x = 0; x < size; x++) {
                            boolean field = x >= edge && x < size - edge && y >= edge && y < size - edge;
                            int color = source.getPixelRGBA(x, y);
                            if (field && frame != 0) {
                                double dx = x - center;
                                double dy = y - center;
                                color = sample(source, center + dx * Math.cos(angle) - dy * Math.sin(angle),
                                        center + dx * Math.sin(angle) + dy * Math.cos(angle), edge);
                            }
                            active.setPixelRGBA(x, y + frame * size, color);
                            glow.setPixelRGBA(x, y + frame * size, field ? color : 0);
                            if (frame == 0) {
                                off.setPixelRGBA(x, y, field ? 0xff000000 : color);
                            }
                        }
                    }
                }
                write(cache, "charger_active", active);
                write(cache, "charger_glow", glow);
                write(cache, "charger_off", off);
            } catch (IOException exception) {
                throw new UncheckedIOException("Could not animate the charger texture", exception);
            }
        });
    }

    private static int sample(NativeImage image, double x, double y, int edge) {
        x = Math.max(edge, Math.min(image.getWidth() - edge - 1, x));
        y = Math.max(edge, Math.min(image.getHeight() - edge - 1, y));
        int x0 = (int) x;
        int y0 = (int) y;
        int x1 = Math.min(image.getWidth() - edge - 1, x0 + 1);
        int y1 = Math.min(image.getHeight() - edge - 1, y0 + 1);
        int result = 0;
        for (int shift = 0; shift < 32; shift += 8) {
            double top = (1 - (x - x0)) * (image.getPixelRGBA(x0, y0) >>> shift & 255)
                    + (x - x0) * (image.getPixelRGBA(x1, y0) >>> shift & 255);
            double bottom = (1 - (x - x0)) * (image.getPixelRGBA(x0, y1) >>> shift & 255)
                    + (x - x0) * (image.getPixelRGBA(x1, y1) >>> shift & 255);
            result |= (int) Math.round(top * (1 - (y - y0)) + bottom * (y - y0)) << shift;
        }
        return result;
    }

    private void write(CachedOutput cache, String name, NativeImage image) throws IOException {
        byte[] png = image.asByteArray();
        cache.writeIfNeeded(output.getOutputFolder().resolve("assets/ariente/textures/block/machines/" + name + ".png"),
                png, Hashing.sha1().hashBytes(png));
    }

    @Override
    public String getName() {
        return "Ariente charger animation";
    }
}
