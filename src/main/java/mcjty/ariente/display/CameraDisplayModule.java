package mcjty.ariente.display;

import mcjty.ariente.blocks.utility.camera.CameraCapture;
import mcjty.ariente.blocks.utility.camera.CameraTile;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.components.IPanel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class CameraDisplayModule implements DisplayModuleType {

    public static final int VIEW_DISTANCE = 32;
    public static final int MAX_CAMERAS = 4;

    @Override
    public String title() {
        return "CAMERA MODULE";
    }

    @Override
    public int observationDistance() {
        return VIEW_DISTANCE;
    }

    public static int sizeIndex(ItemStack stack) {
        return StorageDisplayModule.setting(stack, "Size", 0, 0, 2);
    }

    public static int yaw(ItemStack stack) {
        return StorageDisplayModule.setting(stack, "Yaw", 45, 0, 360);
    }

    public static int tilt(ItemStack stack) {
        return StorageDisplayModule.setting(stack, "Tilt", 30, 0, 90);
    }

    public static boolean perspective(ItemStack stack) {
        return DisplayModuleItem.config(stack).getBoolean("Perspective");
    }

    public static int livePan(ItemStack stack) {
        return StorageDisplayModule.setting(stack, "LivePan", 0, -180, 180);
    }

    public static int liveTilt(ItemStack stack) {
        return StorageDisplayModule.setting(stack, "LiveTilt", 0, -90, 90);
    }

    private static boolean live(ModuleSettings settings) {
        if (settings.snapshot().getBoolean("Live")) {
            return true;
        }
        CompoundTag link = selectedCamera(settings.stack());
        Level level = settings.level();
        if (level.isClientSide) {
            BlockPos pos = BlockPos.of(link.getLong("Pos"));
            return level.dimension().location().toString().equals(link.getString("Dimension")) && level.hasChunkAt(pos)
                    && level.getBlockEntity(pos) instanceof CameraTile camera && camera.isLive();
        }
        return ModuleLink.resolve(level, link).tile() instanceof CameraTile camera && camera.isLive();
    }

    public static boolean autoRotate(ItemStack stack) {
        return rotationSpeed(stack) > 0;
    }

    public static int rotationSpeed(ItemStack stack) {
        CompoundTag config = DisplayModuleItem.config(stack);
        if (config.contains("AutoRotate") && !config.getBoolean("AutoRotate")) {
            return 0;
        }
        return StorageDisplayModule.setting(stack, "RotationSpeed", config.getBoolean("AutoRotate") ? 15 : 0, 0, 90);
    }

    public static int zoom(ItemStack stack) {
        return StorageDisplayModule.setting(stack, "ViewZoom", 100, 100, 800);
    }

    public static int radius(ItemStack stack) {
        int legacy = StorageDisplayModule.setting(stack, "Zoom", CameraCapture.DEFAULT_RANGE, CameraCapture.MIN_RANGE, CameraCapture.MAX_RANGE);
        return StorageDisplayModule.setting(stack, "Radius", legacy, CameraCapture.MIN_RANGE, CameraCapture.MAX_RANGE);
    }

    /** Elapsed client ticks include the partial tick so animation stays smooth between captures. */
    public static double viewYaw(ItemStack stack, double elapsedTicks) {
        return autoRotate(stack) ? (yaw(stack) + Math.max(0, elapsedTicks) * rotationSpeed(stack) / 20) % 360 : yaw(stack);
    }

    public static List<CompoundTag> cameras(ItemStack stack) {
        CompoundTag config = DisplayModuleItem.config(stack);
        ListTag tags = config.getList("Cameras", Tag.TAG_COMPOUND);
        List<CompoundTag> cameras = new ArrayList<>();
        for (int i = 0; i < Math.min(MAX_CAMERAS, tags.size()); i++) {
            cameras.add(tags.getCompound(i));
        }
        // Modules linked before multiple-camera support keep their original camera.
        if (cameras.isEmpty() && config.contains("Link")) {
            cameras.add(config.getCompound("Link"));
        }
        return cameras;
    }

    public static int cameraIndex(ItemStack stack) {
        return StorageDisplayModule.setting(stack, "CameraIndex", 0, 0, Math.max(0, cameras(stack).size() - 1));
    }

    public static CompoundTag selectedCamera(ItemStack stack) {
        List<CompoundTag> cameras = cameras(stack);
        return cameras.isEmpty() ? new CompoundTag() : cameras.get(cameraIndex(stack));
    }

    @Override
    public Component link(ItemStack stack, CompoundTag link, Component targetName) {
        List<CompoundTag> cameras = cameras(stack);
        int selected = -1;
        for (int i = 0; i < cameras.size(); i++) {
            CompoundTag existing = cameras.get(i);
            if (existing.getLong("Pos") == link.getLong("Pos") && existing.getString("Dimension").equals(link.getString("Dimension"))) {
                selected = i;
                break;
            }
        }
        if (selected < 0) {
            if (cameras.size() == MAX_CAMERAS) {
                return Component.literal("All four camera links are in use. Sneak-right-click in air to clear them.");
            }
            selected = cameras.size();
            cameras.add(link);
        }
        ListTag tags = new ListTag();
        cameras.forEach(camera -> tags.add(camera.copy()));
        CompoundTag config = DisplayModuleItem.config(stack);
        config.put("Cameras", tags);
        config.putInt("CameraIndex", selected);
        config.remove("Link");
        return Component.literal("Camera " + (selected + 1) + "/" + cameras.size() + " linked: " + BlockPos.of(link.getLong("Pos")).toShortString());
    }

    @Override
    public void clearLinks(ItemStack stack) {
        CompoundTag config = DisplayModuleItem.config(stack);
        config.remove("Link");
        config.remove("Cameras");
        config.remove("CameraIndex");
    }

    @Override
    public void appendLinkTooltip(ItemStack stack, List<Component> lines) {
        List<CompoundTag> cameras = cameras(stack);
        for (int i = 0; i < cameras.size(); i++) {
            CompoundTag link = cameras.get(i);
            lines.add(Component.literal("Camera " + (i + 1) + "/" + cameras.size() + ": " + BlockPos.of(link.getLong("Pos")).toShortString()
                    + " (" + link.getString("Dimension") + ")").withStyle(i == cameraIndex(stack) ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        }
        lines.add(Component.literal("Link up to 4 cameras; select one in the module GUI.").withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("Radius: " + radius(stack) + " blocks").withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("Zoom: " + zoom(stack) + "%").withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal(perspective(stack) ? "Projection: Perspective" : "Projection: Orthographic").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean refreshOnConfiguration(ItemStack before, ItemStack after) {
        return !selectedCamera(before).equals(selectedCamera(after)) || radius(before) != radius(after);
    }

    @Override
    public Size size(ItemStack stack) {
        int factor = sizeIndex(stack) + 1;
        return new Size(64 * factor, 48 * factor + 12);
    }

    @Override
    public boolean canLink(Level level, BlockPos pos, Direction side) {
        return level.getBlockEntity(pos) instanceof CameraTile;
    }

    @Override
    public CompoundTag sample(Level level, ItemStack stack, CompoundTag previous) {
        ModuleLink link = ModuleLink.resolve(level, selectedCamera(stack));
        if (link.tile() instanceof CameraTile camera) {
            return camera.capture(radius(stack));
        }
        CompoundTag result = new CompoundTag();
        result.putString("Status", link.tile() == null ? link.status() : "Camera missing");
        return result;
    }

    @Override
    public void configure(IGuiComponentRegistry r, IPanel panel, ModuleSettings settings) {
        IPanel holographic = r.panel(0, 0, 8, 7.2).visible(() -> !live(settings));
        IPanel live = r.panel(0, 0, 8, 7.2).visible(() -> live(settings));
        holographic.add(r.textChoice(5.5, .15, 2, .6).name("setting_Perspective").scale(.28f)
                .addText("Orthographic").addText("Perspective").getter(p -> perspective(settings.stack()) ? 1 : 0)
                .hitEvent((c, p, e, x, y) -> settings.toggle(p, e, "Perspective", false)));
        live.add(r.text(5.5, .2, 2, .6).text("LIVE CAMERA").scale(.25f).color(0x76eaff));
        live.add(r.integerSlider(.3, 2, 7.2, .95).name("setting_LivePan").label("Pan").suffix(" deg")
                .range(-180, 180).getter((p, e) -> livePan(settings.stack()))
                .changed((p, e, value) -> settings.setNumber(p, e, "LivePan", value)));
        live.add(r.integerSlider(.3, 3, 7.2, .95).name("setting_LiveTilt").label("Tilt").suffix(" deg")
                .range(-90, 90).getter((p, e) -> liveTilt(settings.stack()))
                .changed((p, e, value) -> settings.setNumber(p, e, "LiveTilt", value)));
        settings.addNumber(r, holographic, 2, "Yaw", "View rotation", 45, 0, 360);
        settings.addNumber(r, holographic, 3, "Tilt", "View tilt", 30, 0, 90);
        panel.add(r.textChoice(.3, 1, 3.4, .7).name("setting_Size").scale(.38f)
                .addText("Size: Small").addText("Size: Medium").addText("Size: Large")
                .getter(p -> sizeIndex(settings.stack()))
                .hitEvent((c, p, e, x, y) -> settings.setNumber(p, e, "Size", (sizeIndex(settings.stack()) + 1) % 3)));
        panel.add(r.button(3.9, 1, .6, .7).name("camera_previous").text("<").scale(.4f)
                .hitEvent((c, p, e, x, y) -> {
                    int count = cameras(settings.stack()).size();
                    if (count > 0) {
                        settings.setNumber(p, e, "CameraIndex", Math.floorMod(cameraIndex(settings.stack()) - 1, count));
                    }
                }));
        panel.add(r.text(4.6, 1.1, 2.1, .5).name("camera_selected").text(() -> {
            int count = cameras(settings.stack()).size();
            return count == 0 ? "Not linked" : "Camera " + (cameraIndex(settings.stack()) + 1) + "/" + count;
        }).scale(.3f));
        panel.add(r.button(6.9, 1, .6, .7).name("camera_next").text(">").scale(.4f)
                .hitEvent((c, p, e, x, y) -> {
                    int count = cameras(settings.stack()).size();
                    if (count > 0) {
                        settings.setNumber(p, e, "CameraIndex", (cameraIndex(settings.stack()) + 1) % count);
                    }
                }));
        panel.add(r.integerSlider(.3, 4, 7.2, .95).name("setting_Radius").label("Radius").suffix(" blocks")
                .range(CameraCapture.MIN_RANGE, CameraCapture.MAX_RANGE).getter((p, e) -> radius(settings.stack()))
                .changed((p, e, value) -> settings.edit(p, e, config -> {
                    config.putInt("Radius", value);
                    config.remove("Zoom");
                })));
        panel.add(r.integerSlider(.3, 5, 7.2, .95).name("setting_Zoom").label("Zoom").suffix("%")
                .range(100, 800).step(10).getter((p, e) -> zoom(settings.stack()))
                .changed((p, e, value) -> settings.setNumber(p, e, "ViewZoom", value)));
        panel.add(r.integerSlider(.3, 6, 7.2, .95).name("setting_RotationSpeed").label("Rotation speed").suffix(" deg/s")
                .range(0, 90).getter((p, e) -> rotationSpeed(settings.stack()))
                .changed((p, e, value) -> settings.edit(p, e, config -> {
                    config.remove("AutoRotate");
                    config.putInt("RotationSpeed", value);
                })));
        panel.add(holographic, live);
    }
}
