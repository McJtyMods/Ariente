package mcjty.ariente.display;

import java.util.HashSet;
import java.util.Set;
import mcjty.ariente.blocks.generators.NegariteGeneratorTile;
import mcjty.ariente.blocks.generators.NegariteTankTile;
import mcjty.ariente.blocks.generators.PosiriteGeneratorTile;
import mcjty.ariente.blocks.generators.PosiriteTankTile;
import mcjty.ariente.power.IPowerBlob;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.ariente.power.IPowerUser;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.power.PowerType;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.components.IPanel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PowerDisplayModule implements DisplayModuleType {

    private static final String[] MODES = {"Numbers", "Meter", "History"};

    @Override
    public String title() {
        return "POWER MODULE";
    }

    public static boolean flag(ItemStack stack, String key, boolean fallback) {
        CompoundTag config = DisplayModuleItem.config(stack);
        return config.contains(key) ? config.getBoolean(key) : fallback;
    }

    public static int mode(ItemStack stack) {
        return Mth.clamp(DisplayModuleItem.config(stack).getInt("Mode"), 0, 2);
    }

    @Override
    public Size size(ItemStack stack) {
        int rows = (flag(stack, "Available", true) ? 1 : 0) + (flag(stack, "Usage", true) ? 1 : 0)
                + (flag(stack, "Output", false) ? 1 : 0) + (flag(stack, "Fuel", false) ? 1 : 0);
        return new Size(mode(stack) == 0 ? 64 : 128, 24 + Math.max(1, rows) * 10 + (mode(stack) == 0 ? 0 : mode(stack) == 1 ? 12 : 40));
    }

    @Override
    public boolean canLink(Level level, BlockPos pos, Direction side) {
        BlockEntity tile = level.getBlockEntity(pos);
        return tile instanceof IPowerBlob || tile instanceof IPowerReceiver || tile instanceof IPowerUser
                || tile instanceof NegariteTankTile || tile instanceof PosiriteTankTile;
    }

    @Override
    public CompoundTag sample(Level level, ItemStack stack, CompoundTag previous) {
        ModuleLink link = ModuleLink.resolve(level, stack);
        CompoundTag data = new CompoundTag();
        if (link.tile() == null) {
            data.putString("Status", link.status());
            return data;
        }
        BlockEntity target = link.tile();
        Level world = link.level();
        // Tanks are generation chambers in Ariente, not persistent energy buffers.
        // Resolve a vertical tank column to its generator and report the actual network flux.
        if (target instanceof NegariteTankTile || target instanceof PosiriteTankTile) {
            boolean negative = target instanceof NegariteTankTile;
            BlockPos pos = target.getBlockPos();
            while (world.hasChunkAt(pos) && (negative ? world.getBlockEntity(pos) instanceof NegariteTankTile
                    : world.getBlockEntity(pos) instanceof PosiriteTankTile)) {
                pos = pos.below();
            }
            target = world.hasChunkAt(pos) ? world.getBlockEntity(pos) : null;
        }
        if (target == null || !(target instanceof IPowerBlob || target instanceof IPowerReceiver || target instanceof IPowerUser)) {
            data.putString("Status", "No connected generator");
            return data;
        }
        data.putString("Name", world.getBlockState(target.getBlockPos()).getBlock().getName().getString());
        if (target instanceof IPowerBlob blob) {
            PowerSystem system = PowerSystem.getPowerSystem(world);
            long negative = blob.getCableId() < 0 ? 0 : system.getTotalPower(blob.getCableId(), PowerType.NEGARITE);
            long positive = blob.getCableId() < 0 ? 0 : system.getTotalPower(blob.getCableId(), PowerType.POSIRITE);
            data.putLong("Available", negative + positive);
        } else {
            data.putLong("Available", availableFlux(world, target.getBlockPos()));
        }
        if (target instanceof IPowerUser user) {
            data.putLong("Usage", user.getUsingPower());
        }
        boolean generator = target instanceof NegariteGeneratorTile || target instanceof PosiriteGeneratorTile;
        if (generator) {
            boolean negative = target instanceof NegariteGeneratorTile;
            boolean working = negative ? ((NegariteGeneratorTile) target).isWorking() : ((PosiriteGeneratorTile) target).isWorking();
            int fuel = negative ? ((NegariteGeneratorTile) target).getFuelTicks() : ((PosiriteGeneratorTile) target).getFuelTicks();
            int tanks = 0;
            BlockPos pos = target.getBlockPos().above();
            while (world.hasChunkAt(pos) && (negative ? world.getBlockEntity(pos) instanceof NegariteTankTile
                    : world.getBlockEntity(pos) instanceof PosiriteTankTile)) {
                tanks++;
                pos = pos.above();
            }
            data.putLong("Output", working ? (long) tanks * NegariteGeneratorTile.POWERGEN : 0);
            data.putInt("Fuel", fuel);
        }
        if (mode(stack) == 2) {
            // One sample per second; history is bounded and never saved to chunk NBT.
            long value = flag(stack, "Usage", true) && data.contains("Usage") ? data.getLong("Usage")
                    : data.contains("Output") ? data.getLong("Output") : data.getLong("Available");
            long[] old = previous.getLongArray("History");
            int length = Math.min(32, old.length + 1);
            long[] history = new long[length];
            int keep = Math.min(old.length, length - 1);
            System.arraycopy(old, old.length - keep, history, 0, keep);
            history[length - 1] = value;
            data.putLongArray("History", history);
        }
        return data;
    }

    private static long availableFlux(Level level, BlockPos pos) {
        // Sampling must not make the consumer helper load adjacent chunks at a chunk boundary.
        PowerSystem system = PowerSystem.getPowerSystem(level);
        Set<Integer> handled = new HashSet<>();
        long negative = 0;
        long positive = 0;
        for (Direction side : Direction.values()) {
            BlockPos neighbor = pos.relative(side);
            if (level.hasChunkAt(neighbor) && level.getBlockEntity(neighbor) instanceof IPowerBlob blob
                    && blob.canSendPower() && blob.getCableId() >= 0 && handled.add(blob.getCableId())) {
                switch (blob.getCableColor()) {
                    case NEGARITE -> negative += system.getTotalPower(blob.getCableId(), PowerType.NEGARITE);
                    case POSIRITE -> positive += system.getTotalPower(blob.getCableId(), PowerType.POSIRITE);
                    case COMBINED -> {
                        negative += system.getTotalPower(blob.getCableId(), PowerType.NEGARITE);
                        positive += system.getTotalPower(blob.getCableId(), PowerType.POSIRITE);
                    }
                }
            }
        }
        return Math.min(negative, positive);
    }

    @Override
    public void configure(IGuiComponentRegistry r, IPanel panel, ModuleSettings settings) {
        var choice = r.textChoice(.3, 1, 7.2, .85).name("setting_Mode").scale(.5f)
                .getter(p -> Mth.clamp(settings.number("Mode", 0), 0, 2))
                .hitEvent((c, p, e, x, y) -> settings.setNumber(p, e, "Mode", (settings.number("Mode", 0) + 1) % 3));
        for (String mode : MODES) {
            choice.addText("Style: " + mode);
        }
        panel.add(choice);
        settings.addToggle(r, panel, 2, "Available", "Network flux", true);
        settings.addToggle(r, panel, 3, "Usage", "Consumption / tick", true);
        settings.addToggle(r, panel, 4, "Output", "Generation / tick", false);
        settings.addToggle(r, panel, 5, "Fuel", "Fuel remaining", false);
        panel.add(r.text(.3, 6.2, 8, .6).text("History: last 32 seconds of usage / output.").scale(.35f));
    }
}
