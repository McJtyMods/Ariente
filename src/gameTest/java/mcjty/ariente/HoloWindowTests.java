package mcjty.ariente;

import com.mojang.authlib.GameProfile;
import mcjty.ariente.gui.GuiHelp;
import mcjty.ariente.gui.ModGuis;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.CloseStrategy;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.gui.HoloGuiEntity;
import mcjty.hologui.gui.components.HoloWindowPanel;
import mcjty.lib.setup.ModSetup;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

import static mcjty.hologui.setup.Registration.HOLOGUI_ENTITY_TYPE;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class HoloWindowTests {

    @GameTest(template = "empty")
    public static void reopeningRepositionsOnlyTheSamePlayersPanelAndKeepsItsPin(GameTestHelper helper) {
        BlockPos pos = blockPanel(helper).getGuiTile();
        ServerPlayer first = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "panel-first"));
        ServerPlayer second = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "panel-second"));
        first.setPos(pos.getX() + .5, pos.getY(), pos.getZ() - 2);
        second.setPos(pos.getX() - 2, pos.getY(), pos.getZ() + .5);
        second.setYRot(-90);
        HoloGuiEntity original = (HoloGuiEntity) Ariente.guiHandler.openHoloGuiEntity(helper.getLevel(), pos, first, "default", 1);
        HoloGuiEntity peer = (HoloGuiEntity) Ariente.guiHandler.openHoloGuiEntity(helper.getLevel(), pos, second, "default", 1);
        helper.assertTrue(original != peer && original.getOwnerUUID().equals(first.getUUID())
                        && peer.getOwnerUUID().equals(second.getUUID()), "Each player opens their own panel for the same block");
        original.setPinned(true);
        Vec3 old = original.position();
        Vec3 peerPosition = peer.position();
        first.setPos(pos.getX() + .5, pos.getY() + 1, pos.getZ() + 2);
        first.setYRot(180);
        HoloGuiEntity reopened = (HoloGuiEntity) Ariente.guiHandler.openHoloGuiEntity(helper.getLevel(), pos, first, IGuiTile.TAG_HELP, 1.5);
        helper.assertTrue(reopened == original && reopened.isPinned() && reopened.position().distanceToSqr(old) > 1,
                "Reopening repositions the existing entity and retains a selected pin");
        helper.assertTrue(reopened.getTag().equals(IGuiTile.TAG_HELP) && reopened.getYRot() == 180
                        && Math.abs(reopened.getZ() - (first.getZ() - 1.5)) < .0001,
                "The requested page, distance and new facing apply to the reused panel");
        helper.assertTrue(peer.position().equals(peerPosition) && !peer.isPinned(), "Reopening never moves another player's panel");
        CompoundTag saved = new CompoundTag();
        original.saveWithoutId(saved);
        HoloGuiEntity restored = new HoloGuiEntity(HOLOGUI_ENTITY_TYPE.get(), helper.getLevel());
        restored.load(saved);
        helper.assertTrue(first.getUUID().equals(restored.getOwnerUUID()), "Panel ownership survives chunk save/load");

        original.setPinned(false);
        original.setMaxTimeout(4);
        original.setTimeout(1);
        original.setCloseStrategy(CloseStrategy.TIMEOUT);
        helper.assertTrue(Ariente.guiHandler.openHoloGuiEntity(helper.getLevel(), pos, first, "default", 1) == original
                        && !original.isPinned(), "An unpinned panel is also reused without selecting its pin");
        for (int i = 0; i < 3; i++) {
            original.tick();
        }
        helper.assertTrue(!original.isRemoved(), "Reopening restarts the full timeout");
        original.tick();
        helper.assertTrue(original.isRemoved(), "An unpinned reused panel still times out normally");
        peer.discard();
        helper.succeed();
    }

    private static HoloGuiEntity blockPanel(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos, Registration.LEVEL_MARKER.get().defaultBlockState());
        HoloGuiEntity holo = new HoloGuiEntity(HOLOGUI_ENTITY_TYPE.get(), helper.getLevel());
        holo.setPos(pos.getX(), pos.getY(), pos.getZ());
        holo.setGuiTile(pos);
        holo.setTag(IGuiTile.TAG_MAIN);
        return holo;
    }

    private static IGuiComponent<?> control(HoloGuiEntity holo, GameTestHelper helper, String name) {
        return holo.getGui(helper.makeMockPlayer()).findChild("hologui." + name).orElseThrow();
    }

    private static void click(HoloGuiEntity holo, GameTestHelper helper, String name) {
        var player = helper.makeMockPlayer();
        IGuiComponent<?> button = control(holo, helper, name);
        holo.getGui(player).hit(player, holo, button.getX() + button.getW() / 2, button.getY() + button.getH() / 2);
    }

    @GameTest(template = "empty")
    public static void pinSurvivesTimeoutAndPagesThenUnpinRestartsTimeout(GameTestHelper helper) {
        HoloGuiEntity holo = blockPanel(helper);
        holo.setCloseStrategy(CloseStrategy.TIMEOUT);
        holo.setMaxTimeout(4);
        holo.setTimeout(1);
        click(holo, helper, "pin");
        helper.assertTrue(holo.isPinned(), "The pin button toggles server state");
        for (int i = 0; i < 100; i++) {
            holo.tick();
        }
        helper.assertTrue(!holo.isRemoved(), "Pinned block panels survive their automatic timeout");
        holo.switchTag(IGuiTile.TAG_HELP);
        helper.assertTrue(holo.isPinned() && control(holo, helper, "pin") != null,
                "Pin state and controls survive page changes");
        holo.switchTag(IGuiTile.TAG_MAIN);
        click(holo, helper, "pin");
        helper.assertTrue(!holo.isPinned(), "Clicking again unpins");
        for (int i = 0; i < 3; i++) {
            holo.tick();
        }
        helper.assertTrue(!holo.isRemoved(), "Unpinning gives the full timeout again");
        holo.tick();
        helper.assertTrue(holo.isRemoved(), "An unpinned panel resumes normal automatic closing");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pinnedPanelsCloseAndOnlyRestoreWithinTheirServerSession(GameTestHelper helper) {
        HoloGuiEntity holo = blockPanel(helper);
        holo.setPinned(true);
        CompoundTag saved = new CompoundTag();
        holo.saveWithoutId(saved);
        HoloGuiEntity restored = new HoloGuiEntity(HOLOGUI_ENTITY_TYPE.get(), helper.getLevel());
        restored.load(saved);
        helper.assertTrue(restored.isPinned(), "Reloading a chunk within the same server session keeps its pin");
        saved.putUUID("pinSession", UUID.randomUUID());
        restored.load(saved);
        helper.assertTrue(!restored.isPinned() && restored.canPin(), "A pin from another server session is not restored");
        click(holo, helper, "close");
        helper.assertTrue(holo.isRemoved(), "X closes a pinned panel");

        HoloGuiEntity rightClick = blockPanel(helper);
        rightClick.setPinned(true);
        rightClick.interact(helper.makeMockPlayer(), InteractionHand.MAIN_HAND);
        helper.assertTrue(rightClick.isRemoved(), "Right-click can also manually close a pinned panel");

        HoloGuiEntity removedBlock = blockPanel(helper);
        removedBlock.setPinned(true);
        helper.getLevel().setBlockAndUpdate(removedBlock.getGuiTile(), Blocks.AIR.defaultBlockState());
        for (int i = 0; i < 6; i++) {
            removedBlock.tick();
        }
        helper.assertTrue(removedBlock.isRemoved(), "Pinning does not leave orphaned panels for destroyed blocks");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void controlsMatchAvailableManualAndMountedEquipmentExceptions(GameTestHelper helper) {
        HoloGuiEntity holo = blockPanel(helper);
        var player = helper.makeMockPlayer();
        IGuiComponent<?> root = holo.getGui(player);
        helper.assertTrue(root.findChild("hologui.help").isPresent() == ModSetup.patchouli,
                "Manual buttons require Patchouli");
        IGuiComponent<?> close = control(holo, helper, "close");
        IGuiComponent<?> pin = control(holo, helper, "pin");
        helper.assertTrue(close.getW() == pin.getW() && close.getH() == pin.getH(), "Controls have identical dimensions");
        helper.assertTrue(Math.abs(close.getX() + close.getW() - 9.0) < .0001 && close.getY() == -.6,
                "Close sits flush against the top-right border");
        root.findChild("hologui.help").ifPresent(help -> {
            helper.assertTrue(Math.abs(help.getX() + help.getW() - close.getX()) < .0001,
                    "Manual sits directly left of close");
            helper.assertTrue(Math.abs(pin.getX() + pin.getW() - help.getX()) < .0001,
                    "Pin sits directly left of manual");
        });

        holo.setGuiId(ModGuis.GUI_ARMOR);
        holo.setGuiTile(null);
        holo.switchGui(ModGuis.GUI_ARMOR_CHEST);
        helper.assertTrue(holo.getGui(player).findChild("hologui.close").isPresent()
                        && holo.getGui(player).findChild("hologui.pin").isEmpty(),
                "Equipment panels have close but no pin");
        holo.setPinned(true);
        helper.assertTrue(!holo.isPinned(), "Equipment cannot be pinned through the API either");

        holo.switchGui(ModGuis.GUI_LEVITATOR);
        holo.startRiding(EntityType.PIG.create(helper.getLevel()), true);
        helper.assertTrue(holo.getGui(player).findChild("hologui.close").isEmpty()
                        && holo.getGui(player).findChild("hologui.pin").isEmpty(),
                "Mounted controls omit both close and pin");
        helper.assertTrue(holo.getGui(player).findChild("hologui.help").isPresent() == ModSetup.patchouli,
                "Mounted panels can still offer their manual");
        holo.close();
        holo.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(!holo.isRemoved(), "A mounted panel cannot be dismissed independently");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void barePanelsGetCloseAndPinWithoutAHelpGap(GameTestHelper helper) {
        HoloGuiEntity holo = blockPanel(helper);
        helper.getLevel().setBlockAndUpdate(holo.getGuiTile(), Registration.POSIRITE_TANK.get().defaultBlockState());
        IGuiComponent<?> root = holo.getGui(helper.makeMockPlayer());
        IGuiComponent<?> close = control(holo, helper, "close");
        IGuiComponent<?> pin = control(holo, helper, "pin");
        helper.assertTrue(root.findChild("hologui.help").isEmpty(), "Panels without a manual omit help");
        helper.assertTrue(Math.abs(pin.getX() + pin.getW() - close.getX()) < .0001,
                "Missing help leaves no empty cell");
        helper.assertTrue(root.findHoveringWidget(close.getX(), close.getY() + .2) == close,
                "The shared edge targets only the right-hand button");
        click(holo, helper, "pin");
        helper.assertTrue(holo.isPinned() && !holo.isRemoved(), "Pin clicks do not trigger the adjacent close button");

        var registry = Ariente.guiHandler.getComponentRegistry();
        IGuiComponent<?> withoutManual = new HoloWindowPanel(registry.panel(0, 0, 8, 8), holo);
        helper.assertTrue(withoutManual.findChild("hologui.help").isEmpty(), "A plain secondary page still omits help");
        helper.assertTrue((GuiHelp.EQUIPMENT.createPanel(registry).getHelpEvent() != null) == ModSetup.patchouli,
                "The shared help builder registers only an available manual action");
        helper.succeed();
    }
}
