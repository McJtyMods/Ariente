package mcjty.ariente;

import mcjty.ariente.blocks.utility.door.DoorMarkerTile;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IImage;
import mcjty.hologui.gui.components.HoloToggleIcon;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.function.Function;

import static mcjty.hologui.api.IGuiTile.TAG_MAIN;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class DoorMarkerTests {

    @GameTest(template = "empty")
    public static void patternPreviewsSelectSaveAndSync(GameTestHelper helper) throws Exception {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos, Registration.DOOR_MARKER.get().defaultBlockState());
        DoorMarkerTile tile = (DoorMarkerTile) helper.getLevel().getBlockEntity(pos);
        var registry = Ariente.guiHandler.getComponentRegistry();
        IGuiComponent<?> panel = tile.createGui(TAG_MAIN, registry);
        Player player = helper.makeMockPlayer();
        for (int pattern = 0; pattern < 16; pattern++) {
            IGuiComponent<?> button = panel.findChild("pattern" + pattern).orElseThrow();
            IImage normal = (IImage) field(button, "image");
            IImage selected = (IImage) field(button, "selected");
            checkPreview(helper, normal, pattern, false);
            checkPreview(helper, selected, pattern, true);

            panel.hit(player, null, button.getX() + .5, button.getY() + .5);
            helper.assertTrue(tile.getIconIndex() == pattern, "Click must select pattern " + pattern);
            for (int other = 0; other < 16; other++) {
                helper.assertTrue(isSelected(panel.findChild("pattern" + other).orElseThrow(), player) == (other == pattern),
                        "Only the current pattern must be highlighted");
            }

            CompoundTag saved = new CompoundTag();
            tile.saveAdditional(saved);
            helper.assertTrue(saved.getCompound("Info").getInt("icon") == pattern, "Keep the existing Info.icon save format");
            DoorMarkerTile restored = new DoorMarkerTile(pos, tile.getBlockState());
            restored.load(saved);
            helper.assertTrue(restored.getIconIndex() == pattern, "Pattern must survive save/load");
            DoorMarkerTile client = new DoorMarkerTile(pos, tile.getBlockState());
            client.loadClientDataFromNBT(tile.getUpdateTag());
            helper.assertTrue(client.getIconIndex() == pattern, "Pattern must be included in client updates");
        }

        tile.setLocked(true);
        helper.assertTrue(tile.createGui(TAG_MAIN, registry).findChild("pattern0").isEmpty(), "Locked doors hide the selector");
        panel.findChild("pattern0").orElseThrow().hit(player, null, 1.5, 1.5);
        helper.assertTrue(tile.getIconIndex() == 15, "A panel opened before locking cannot change the pattern");
        tile.setLocked(false);
        panel.findChild("pattern0").orElseThrow().hit(player, null, 1.5, 1.5);
        helper.assertTrue(tile.getIconIndex() == 0, "Unlocking restores pattern selection");
        helper.succeed();
    }

    private static void checkPreview(GameTestHelper helper, IImage image, int pattern, boolean selected) throws Exception {
        helper.assertTrue(image.getImage().toString().equals("ariente:textures/effects/doormarkers.png"),
                "Previews must use the actual door sheet instead of the empty legacy HoloGui palette");
        helper.assertTrue(image.getU() == pattern % 4 * 16 + (selected ? 64 : 0) && image.getV() == pattern / 4 * 16,
                "Preview must match its door pattern and selected state");
        String path = "/assets/" + image.getImage().getNamespace() + "/" + image.getImage().getPath();
        try (var stream = DoorMarkerTests.class.getResourceAsStream(path)) {
            helper.assertTrue(stream != null, "Preview texture must exist");
            BufferedImage texture = ImageIO.read(stream);
            helper.assertTrue(texture.getWidth() == image.getWidth() && texture.getHeight() == image.getHeight(),
                    "Preview dimensions must match the texture");
            boolean visible = false;
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    visible |= (texture.getRGB(image.getU() + x, image.getV() + y) >>> 24) != 0;
                }
            }
            helper.assertTrue(visible, "Every preview must contain visible pixels");
        }
    }

    private static Object field(IGuiComponent<?> button, String name) throws ReflectiveOperationException {
        Field field = HoloToggleIcon.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(button);
    }

    @SuppressWarnings("unchecked")
    private static boolean isSelected(IGuiComponent<?> button, Player player) throws ReflectiveOperationException {
        return ((Function<Player, Boolean>) field(button, "currentValue")).apply(player);
    }
}
