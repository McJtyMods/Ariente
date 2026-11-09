package mcjty.ariente.datagen;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.minecraft.util.ARGB;

final class PngData {

    private PngData() {
    }

    static int abgr(PngImage image, int x, int y) {
        return ARGB.toABGR(image.getPixel(x, y));
    }

    static byte[] encode(PngImage image) throws IOException {
        BufferedImage png = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        png.setRGB(0, 0, image.getWidth(), image.getHeight(), image.getPixels(), 0, image.getWidth());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(png, "PNG", output)) throw new IOException("No PNG encoder");
        return output.toByteArray();
    }
}
