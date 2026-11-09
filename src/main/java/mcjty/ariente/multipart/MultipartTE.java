package mcjty.ariente.multipart;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.util.ProblemReporter;
import java.util.EnumMap;
import java.util.Map;
import mcjty.ariente.blocks.utility.autofield.AutoFieldTile;
import mcjty.lib.multipart.IPartBlock;
import mcjty.lib.multipart.PartSlot;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

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
    public void onDataPacket(Connection connection, ValueInput input) {
        {
            loadAdditional(input);
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(level.registryAccess());
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
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        CompoundTag compound = input.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseGet(CompoundTag::new);
        HolderLookup.Provider provider = input.lookup();
        Map<PartSlot, Part> loaded = new EnumMap<>(PartSlot.class);
        version = compound.getIntOr("version", 0);
        ListTag list = compound.getListOrEmpty("parts");
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompoundOrEmpty(i);
            PartSlot slot = PartSlot.byName(tag.getStringOr("slot", ""));
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, tag);
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
                tile.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, provider, tag.getCompoundOrEmpty("te")));
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, level.registryAccess());
        output.store(tag);
    }

    public void saveAdditional(CompoundTag compound, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        parts.forEach((slot, part) -> {
            CompoundTag tag = NbtUtils.writeBlockState(part.state);
            tag.putString("slot", slot.name());
            if (part.tileEntity != null) {
                tag.put("te", part.tileEntity.saveWithoutMetadata(level.registryAccess()));
            }
            list.add(tag);
        });
        compound.put("parts", list);
        compound.putInt("version", version);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        for (Part part : parts.values()) {
            if (part.getBlockEntity() instanceof GenericTileEntity generic) {
                generic.onReplaced(level, pos, part.getState(), level.getBlockState(pos));
            }
        }
        super.preRemoveSideEffects(pos, state);
    }
}
