package mcjty.ariente.display;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public record ModuleLink(Level level, BlockPos pos, Direction side, BlockEntity tile, String status) {

    public static ModuleLink resolve(Level current, ItemStack stack) {
        CompoundTag config = DisplayModuleItem.config(stack);
        return resolve(current, config.getCompound("Link"));
    }

    public static ModuleLink resolve(Level current, CompoundTag link) {
        if (link.isEmpty()) {
            return new ModuleLink(current, BlockPos.ZERO, Direction.UP, null, "Not linked");
        }
        ResourceLocation dimension = ResourceLocation.tryParse(link.getString("Dimension"));
        Level target = dimension == null || current.getServer() == null ? null
                : current.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
        BlockPos pos = BlockPos.of(link.getLong("Pos"));
        Direction side = Direction.from3DDataValue(link.getInt("Side"));
        if (target == null || !target.hasChunkAt(pos)) {
            return new ModuleLink(current, pos, side, null, "Target unloaded");
        }
        BlockEntity tile = target.getBlockEntity(pos);
        if (tile == null || !link.getString("Block").equals(BuiltInRegistries.BLOCK.getKey(target.getBlockState(pos).getBlock()).toString())) {
            return new ModuleLink(target, pos, side, null, "Target missing");
        }
        return new ModuleLink(target, pos, side, tile, "");
    }
}
