package mcjty.ariente.multipart;

import mcjty.ariente.blocks.utility.autofield.AutoFieldTile;
import mcjty.lib.multipart.IPartBlock;
import mcjty.lib.multipart.PartSlot;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.EnumMap;
import java.util.Map;

import static mcjty.ariente.setup.Registration.TYPE_MULTIPART;

public class MultipartTE extends BlockEntity {

    public static final ModelProperty<Map<PartSlot, Part>> PARTS = new ModelProperty<>();

    public static class Part {
        private final BlockState state;
        private final BlockEntity tileEntity;

        public Part(BlockState state, BlockEntity tileEntity) {
            this.state = state;
            this.tileEntity = tileEntity;
        }

        public BlockState getState() {
            return state;
        }

        public BlockEntity getBlockEntity() {
            return tileEntity;
        }
    }

    private Map<PartSlot, Part> parts = new EnumMap<>(PartSlot.class);
    private int version;

    public MultipartTE(BlockPos pos, BlockState state) {
        super(TYPE_MULTIPART.get(), pos, state);
    }

    public void addPart(PartSlot slot, BlockState state, BlockEntity tile) {
        if (parts.containsKey(slot)) {
            throw new IllegalArgumentException("Multipart slot already occupied: " + slot);
        }
        parts.put(slot, new Part(state, tile));
        if (tile != null && level != null) {
            tile.setLevel(level);
            if (tile instanceof GenericTileEntity generic) {
                generic.onPartAdded(slot, state, this);
            }
        }
        version++;
        markDirtyClient();
    }

    public void removePart(BlockState state) {
        PartSlot slot = parts.entrySet().stream().filter(e -> e.getValue().state == state)
                .map(Map.Entry::getKey).findFirst().orElse(null);
        if (slot == null) {
            return;
        }
        Part removed = parts.remove(slot);
        if (removed.tileEntity instanceof GenericTileEntity generic && level != null) {
            generic.onReplaced(level, worldPosition, state, Blocks.AIR.defaultBlockState());
        }
        if (removed.tileEntity != null) {
            removed.tileEntity.setRemoved();
        }
        version++;
        markDirtyClient();
        if (level != null) {
            AutoFieldTile.notifyField(level, worldPosition);
        }
    }

    public Map<PartSlot, Part> getParts() {
        return parts;
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(PARTS, parts).build();
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) {
            load(packet.getTag());
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void markDirtyClient() {
        setChanged();
        if (level != null) {
            requestModelDataUpdate();
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    public void markDirtyQuick() {
        setChanged();
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        Map<PartSlot, Part> loaded = new EnumMap<>(PartSlot.class);
        version = compound.getInt("version");
        ListTag list = compound.getList("parts", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            PartSlot slot = PartSlot.byName(tag.getString("slot"));
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag);
            if (slot == null || !(state.getBlock() instanceof IPartBlock) || loaded.containsKey(slot)) {
                continue;
            }
            Part previous = parts.get(slot);
            BlockEntity tile = previous != null && previous.state == state ? previous.tileEntity : null;
            if (tile == null && state.getBlock() instanceof EntityBlock block) {
                tile = block.newBlockEntity(worldPosition, state);
            }
            if (tile != null) {
                if (level != null) {
                    tile.setLevel(level);
                }
                tile.load(tag.getCompound("te"));
            }
            loaded.put(slot, new Part(state, tile));
        }
        for (Map.Entry<PartSlot, Part> entry : parts.entrySet()) {
            Part replacement = loaded.get(entry.getKey());
            BlockEntity tile = entry.getValue().tileEntity;
            if (tile != null && (replacement == null || replacement.tileEntity != tile)) {
                tile.setRemoved();
            }
        }
        parts = loaded;
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        parts.values().forEach(part -> {
            if (part.tileEntity != null) {
                part.tileEntity.setLevel(level);
            }
        });
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        parts.values().forEach(part -> {
            if (part.tileEntity != null) {
                part.tileEntity.clearRemoved();
            }
        });
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        parts.values().forEach(part -> {
            if (part.tileEntity != null) {
                part.tileEntity.setRemoved();
            }
        });
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        ListTag list = new ListTag();
        parts.forEach((slot, part) -> {
            CompoundTag tag = NbtUtils.writeBlockState(part.state);
            tag.putString("slot", slot.name());
            if (part.tileEntity != null) {
                tag.put("te", part.tileEntity.saveWithoutMetadata());
            }
            list.add(tag);
        });
        compound.put("parts", list);
        compound.putInt("version", version);
    }
}
