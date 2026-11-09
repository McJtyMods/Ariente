package mcjty.ariente.datagen;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import net.minecraft.util.ARGB;

/** PNG generation runs on the server data generator without loading client native-image classes. */
final class PngImage implements AutoCloseable {

    private final BufferedImage image;

    PngImage(int width, int height, boolean clear) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    private PngImage(BufferedImage image) {
        this.image = image;
    }

    static PngImage read(InputStream input) throws IOException {
        BufferedImage image = ImageIO.read(input);
        if (image == null) throw new IOException("Invalid source PNG");
        return new PngImage(image);
    }

    int getWidth() {
        return image.getWidth();
    }

    int getHeight() {
        return image.getHeight();
    }

    int getPixel(int x, int y) {
        return image.getRGB(x, y);
    }

    int[] getPixels() {
        return image.getRGB(0, 0, getWidth(), getHeight(), null, 0, getWidth());
    }

    void setPixelABGR(int x, int y, int color) {
        image.setRGB(x, y, ARGB.fromABGR(color));
    }

    @Override
    public void close() {
        image.flush();
    }
}
