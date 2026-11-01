package mcjty.ariente.display.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.display.DisplayBaseTile;
import mcjty.ariente.display.DisplayGeometry;
import mcjty.ariente.display.InstalledModule;
import mcjty.ariente.display.PacketDisplayGesture;
import mcjty.ariente.network.ArienteMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/** Local drag preview; authoritative state changes only when the server accepts the release. */
public class DisplayModuleDrag {

    private static Gesture gesture;

    public static boolean active() {
        return gesture != null;
    }

    public static void begin(DisplayGeometry.ModuleHit hit) {
        Minecraft mc = Minecraft.getInstance();
        DisplayGeometry.Hit start = DisplayGeometry.project(hit.owner(), mc.player,
                Math.min(8, mc.player.getBlockReach()), mc.player.getLookAngle());
        if (start == null) {
            return;
        }
        gesture = new Gesture(hit.owner(), hit.module(), start);
        send(PacketDisplayGesture.Action.BEGIN);
    }

    private static void send(PacketDisplayGesture.Action action) {
        Minecraft mc = Minecraft.getInstance();
        if (gesture != null && mc.getConnection() != null && mc.player != null) {
            ArienteMessages.INSTANCE.sendToServer(new PacketDisplayGesture(gesture.source.getBlockPos(), gesture.module.id(),
                    gesture.ticket, action, mc.player.getYRot(), mc.player.getXRot(), gesture.dragged, gesture.valid));
        }
    }

    public static void cancel() {
        send(PacketDisplayGesture.Action.CANCEL);
        gesture = null;
    }

    private static boolean contextValid(Minecraft mc) {
        return mc.player != null && mc.level == gesture.source.getLevel() && mc.screen == null
                && !mc.player.isSpectator() && !gesture.source.isRemoved();
    }

    public static void tick() {
        if (gesture == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!contextValid(mc)) {
            cancel();
            return;
        }
        if (gesture.pending) {
            if (++gesture.waitTicks > 40 || gesture.confirmed && synchronizedPosition()) {
                gesture = null;
            }
            return;
        }
        InstalledModule current = gesture.source.findModule(gesture.module.id());
        if (current == null || current.x() != gesture.module.x() || current.y() != gesture.module.y()
                || !ItemStack.isSameItemSameTags(current.stack(), gesture.originalStack)
                || gesture.source.getHeight() != gesture.height || DisplayGeometry.facing(gesture.source) != gesture.facing) {
            cancel();
            return;
        }
        update(mc);
        if (!mc.options.keyUse.isDown()) {
            send(PacketDisplayGesture.Action.RELEASE);
            if (gesture.dragged && gesture.valid) {
                gesture.pending = true;
            } else {
                gesture = null;
            }
        } else if (++gesture.ticks % 200 == 0) {
            send(PacketDisplayGesture.Action.KEEP_ALIVE);
        }
    }

    private static void update(Minecraft mc) {
        if (gesture == null || gesture.pending) {
            return;
        }
        DisplayGeometry.Hit cursor = DisplayGeometry.project(gesture.source, mc.player,
                Math.min(8, mc.player.getBlockReach()), mc.player.getLookAngle());
        if (cursor == null) {
            gesture.dragged = true;
            gesture.valid = false;
            return;
        }
        gesture.dragged |= Math.abs(cursor.x() - gesture.start.x()) >= DisplayGeometry.SNAP
                || Math.abs(cursor.y() - gesture.start.y()) >= DisplayGeometry.SNAP;
        if (gesture.dragged) {
            InstalledModule current = gesture.source.findModule(gesture.module.id());
            if (current == null) {
                gesture.valid = false;
                return;
            }
            gesture.position = DisplayGeometry.dragPosition(gesture.source, current, gesture.start, cursor);
        }
        gesture.valid = gesture.position.valid();
    }

    private static boolean synchronizedPosition() {
        Minecraft mc = Minecraft.getInstance();
        BlockPos pos = gesture.destination != null ? gesture.destination : gesture.position.target().getBlockPos();
        if (mc.level == null || !mc.level.hasChunkAt(pos)
                || !(mc.level.getBlockEntity(pos) instanceof DisplayBaseTile target) || target.isRemoved()) {
            return false;
        }
        InstalledModule module = target.findModule(gesture.module.id());
        return module != null && module.x() == gesture.position.x() && module.y() == gesture.position.y()
                && (target == gesture.source || gesture.source.findModule(module.id()) == null);
    }

    public static void complete(UUID ticket, BlockPos target, int x, int y) {
        if (gesture != null && gesture.ticket.equals(ticket)) {
            if (target == null) {
                gesture = null;
            } else {
                gesture.destination = target;
                Direction right = DisplayGeometry.facing(gesture.source).getCounterClockWise();
                BlockPos delta = target.subtract(gesture.source.getBlockPos());
                int offset = delta.getX() * right.getStepX() + delta.getZ() * right.getStepZ();
                gesture.position = new DisplayGeometry.DragPosition(null, x, y, offset * DisplayGeometry.PIXELS + x, true);
                gesture.confirmed = true;
                if (synchronizedPosition()) {
                    gesture = null;
                }
            }
        }
    }

    public static boolean hides(UUID module) {
        return gesture != null && gesture.dragged && gesture.module.id().equals(module);
    }

    public static void render(PoseStack poses, MultiBufferSource.BufferSource buffer, Vec3 camera) {
        if (gesture == null || !contextValid(Minecraft.getInstance())) {
            return;
        }
        update(Minecraft.getInstance());
        DisplayGeometry.DragPosition position = gesture.position;
        InstalledModule current = gesture.source.findModule(gesture.module.id());
        InstalledModule module = current == null ? gesture.module : current;
        poses.pushPose();
        poses.translate(gesture.source.getBlockPos().getX() - camera.x, gesture.source.getBlockPos().getY() - camera.y,
                gesture.source.getBlockPos().getZ() - camera.z);
        DisplayRenderer.transform(poses, gesture.source);
        poses.translate(0, 0, .3);
        DisplayCanvas canvas = new DisplayCanvas(poses, buffer);
        if (gesture.dragged) {
            poses.pushPose();
            poses.translate(position.sourceX(), position.y(), 0);
            DisplayModuleRenderers.render(canvas, module);
            poses.popPose();
        }
        poses.translate(0, 0, .2);
        int rgb = gesture.valid ? 0x54ffb1 : 0xff596c;
        poses.pushPose();
        poses.translate(0, 0, -.01);
        canvas.fill(position.sourceX(), position.y(), module.size().width(), module.size().height(), 0x18000000 | rgb);
        poses.popPose();
        canvas.border(position.sourceX(), position.y(), module.size().width(), module.size().height(), 0xff000000 | rgb);
        if (gesture.dragged) {
            canvas.text(gesture.valid ? "MOVE" : "NO ROOM", position.sourceX() + 2,
                    position.y() + module.size().height() + 1, rgb, .55f, module.size().width() - 4);
        }
        poses.popPose();
        buffer.endBatch();
    }

    private static class Gesture {

        private final UUID ticket = UUID.randomUUID();
        private final DisplayBaseTile source;
        private final InstalledModule module;
        private final ItemStack originalStack;
        private final DisplayGeometry.Hit start;
        private final int height;
        private final Direction facing;
        private DisplayGeometry.DragPosition position;
        private boolean valid = true;
        private boolean dragged;
        private boolean pending;
        private boolean confirmed;
        private int ticks;
        private int waitTicks;
        private BlockPos destination;

        private Gesture(DisplayBaseTile source, InstalledModule module, DisplayGeometry.Hit start) {
            this.source = source;
            this.module = module;
            this.start = start;
            originalStack = module.stack().copy();
            height = source.getHeight();
            facing = DisplayGeometry.facing(source);
            position = new DisplayGeometry.DragPosition(source, module.x(), module.y(), module.x(), true);
        }
    }
}
