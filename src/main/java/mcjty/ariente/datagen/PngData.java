package mcjty.ariente.datagen;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

final class PngData {

    private PngData() {}

    static int swapRedBlue(int color) {
        return (color & 0xff00ff00) | ((color & 255) << 16) | ((color >>> 16) & 255);
    }

    static int abgr(PngImage image, int x, int y) {
        return swapRedBlue(image.getPixel(x, y));
    }

    static byte[] encode(PngImage image) throws IOException {
        BufferedImage png =
                new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        png.setRGB(
                0, 0, image.getWidth(), image.getHeight(), image.getPixels(), 0, image.getWidth());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(png, "PNG", output)) throw new IOException("No PNG encoder");
        return output.toByteArray();
    }
}
