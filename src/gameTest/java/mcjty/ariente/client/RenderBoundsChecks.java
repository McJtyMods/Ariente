package mcjty.ariente.client;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.utility.ElevatorTile;
import mcjty.ariente.blocks.utility.WarperTile;
import mcjty.ariente.blocks.utility.autofield.AutoFieldRenderInfo;
import mcjty.ariente.blocks.utility.autofield.AutoFieldTile;
import mcjty.ariente.blocks.utility.autofield.TransferRender;
import mcjty.ariente.blocks.utility.door.DoorMarkerTile;
import mcjty.ariente.display.DisplayBaseTile;
import mcjty.ariente.entities.LaserEntity;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.setup.Registration;
import mcjty.lib.multipart.PartPos;
import mcjty.lib.multipart.PartSlot;
import mcjty.lib.spline.Spline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Matrix4f;

import java.lang.reflect.Field;

/** Runs against the registered native client renderers in a disposable world. */
@EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public final class RenderBoundsChecks {

    private static Button accepted;
    private static int waiting;
    private static boolean complete;

    private RenderBoundsChecks() {
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) throws ReflectiveOperationException {
        if (!Boolean.getBoolean("ariente.renderBoundsCheck") || complete) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        mc.options.pauseOnLostFocus = false;
        if (mc.screen instanceof PauseScreen) {
            mc.setScreen(null);
        }
        if (mc.player == null && mc.screen != null) {
            for (var child : mc.screen.children()) {
                if (child instanceof Button button && button != accepted && (button.getMessage().getString().equals("Proceed")
                        || button.getMessage().getString().equals("Continue") || button.getMessage().getString().equals("I know what I'm doing!"))) {
                    accepted = button;
                    button.onPress();
                    break;
                }
            }
        }
        if (mc.player == null || mc.level == null || mc.getOverlay() != null) {
            require(++waiting < 2400, "Client never entered the render-bounds fixture");
            return;
        }
        checkAutomation(mc);
        checkBeams(mc);
        checkOtherEffects(mc);
        complete = true;
        Ariente.setup.getLogger().info("RENDER BOUNDS CHECK PASSED: registered automation renderer, off-screen controller, beam width, transfer spline, elevator heights, warper, displays, door markers, lasers and ship exhaust");
        mc.stop();
    }

    private static void checkAutomation(Minecraft mc) throws ReflectiveOperationException {
        BlockPos controller = new BlockPos(0, 64, 0);
        AABB outline = new AABB(0, 64, 0, 24, 72, 16);
        AutoFieldTile field = new AutoFieldTile(controller, Registration.AUTOMATION_FIELD.get().defaultBlockState()) {

            @Override
            public AABB getRenderBox() {
                return outline;
            }
        };
        field.setLevel(mc.level);
        var renderer = mc.getBlockEntityRenderDispatcher().getRenderer(field);
        require(renderer != null, "Automation field renderer must be registered");
        AABB rendered = renderer.getRenderBoundingBox(field);
        require(rendered.contains(new Vec3(24 + (double) .1f, 72 + (double) .1f, 16 + (double) .1f)), "Bounds must include beam width past the far corner");
        for (Vec3 camera : new Vec3[]{new Vec3(23, 68, 20), new Vec3(0, 68, 20), new Vec3(12, 71, 20)}) {
            Frustum view = frustum(camera);
            require(!view.isVisible(new AABB(controller)), "The fixture must put the controller outside the view");
            require(view.isVisible(rendered), "Visible far edges must survive culling while the controller is off screen");
        }
        require(!frustum(new Vec3(60, 68, 20)).isVisible(rendered), "Field bounds must remain finite");
        Field transfers = field(AutoFieldTile.class, "transferRenders");
        for (int sample = 0; sample < 24; sample++) {
            var path = new AutoFieldRenderInfo.TransferPath(PartPos.create(controller, PartSlot.NONE),
                    PartPos.create(controller.offset(64, 0, 8), PartSlot.NONE));
            var transfer = new TransferRender(path, new AutoFieldRenderInfo.Transfer(new ItemStack(Items.IRON_INGOT), 0), controller);
            transfers.set(field, new TransferRender[]{transfer});
            AABB bounds = renderer.getRenderBoundingBox(field);
            Spline<?> spline = (Spline<?>) field(TransferRender.class, "spline").get(transfer);
            for (int step = 0; step <= 1000; step++) {
                spline.calculate(step / 1000.0f);
                Object point = spline.getInterpolated();
                double x = field(point.getClass(), "x").getDouble(point) + controller.getX();
                double y = field(point.getClass(), "y").getDouble(point) + controller.getY();
                double z = field(point.getClass(), "z").getDouble(point) + controller.getZ();
                require(bounds.contains(new Vec3(x - .4, y - .4, z - .4))
                                && bounds.contains(new Vec3(x + .4, y + .4, z + .4)),
                        "Bounds must contain transfer arcs and item radius outside the marker outline");
            }
        }
    }

    private static void checkBeams(Minecraft mc) {
        BlockPos position = new BlockPos(30, 80, -30);
        ElevatorTile elevator = new ElevatorTile(position, Registration.ELEVATOR.get().defaultBlockState());
        elevator.setLevel(mc.level);
        var renderer = mc.getBlockEntityRenderDispatcher().getRenderer(elevator);
        require(renderer != null, "Elevator renderer must be registered");
        for (int height : new int[]{1, 9, 64, 256}) {
            elevator.setHeight(height);
            AABB box = renderer.getRenderBoundingBox(elevator);
            require(box.minY <= position.getY() - 1 && box.maxY >= position.getY() + height + 4,
                    "Elevator bounds must cover the complete animated beam at height " + height);
        }
        WarperTile warper = new WarperTile(position, Registration.WARPER.get().defaultBlockState());
        warper.setLevel(mc.level);
        AABB box = mc.getBlockEntityRenderDispatcher().getRenderer(warper).getRenderBoundingBox(warper);
        require(box.minY <= position.getY() - 1 && box.maxY >= position.getY() + 6,
                "Warper bounds must include its lowest and highest beam segments");
    }

    private static void checkOtherEffects(Minecraft mc) {
        BlockPos position = new BlockPos(0, 80, 0);
        DisplayBaseTile display = new DisplayBaseTile(position, Registration.DISPLAY_BASE.get().defaultBlockState());
        AABB displayBox = mc.getBlockEntityRenderDispatcher().getRenderer(display).getRenderBoundingBox(display);
        require(displayBox.contains(new Vec3(7, 89, 7)), "Display bounds must reach the maximum hologram extent");
        DoorMarkerTile door = new DoorMarkerTile(position, Registration.DOOR_MARKER.get().defaultBlockState());
        AABB doorBox = mc.getBlockEntityRenderDispatcher().getRenderer(door).getRenderBoundingBox(door);
        require(doorBox.contains(new Vec3(-.1, 80.5, .5)), "Door markers must include their displaced geometry");
        LaserEntity laser = new LaserEntity(Registration.ENTITY_LASER.get(), mc.level);
        FluxShipEntity ship = new FluxShipEntity(Registration.ENTITY_FLUX_SHIP.get(), mc.level);
        try {
            laser.setPos(0, 80, 0);
            ship.setPos(0, 80, 0);
            Vec3 laserView = new Vec3(2, 80, 5);
            Vec3 exhaustView = new Vec3(20, 80, 20);
            require(!frustum(laserView).isVisible(laser.getBoundingBox()), "Laser collision box must be off screen");
            require(mc.getEntityRenderDispatcher().getRenderer(laser).shouldRender(laser, frustum(laserView), laserView.x, laserView.y, laserView.z),
                    "Registered laser renderer must retain the visible trail");
            require(!frustum(exhaustView).isVisible(ship.getBoundingBox()), "Ship hull must be off screen");
            require(mc.getEntityRenderDispatcher().getRenderer(ship).shouldRender(ship, frustum(exhaustView), exhaustView.x, exhaustView.y, exhaustView.z),
                    "Registered ship renderer must retain visible exhaust");
        } finally {
            laser.discard();
            ship.discard();
        }
    }

    private static Frustum frustum(Vec3 camera) {
        Frustum frustum = new Frustum(new Matrix4f(), new Matrix4f().setOrtho(-1, 1, -1, 1, .1f, 40));
        frustum.prepare(camera.x, camera.y, camera.z);
        return frustum;
    }

    private static Field field(Class<?> type, String name) throws NoSuchFieldException {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
