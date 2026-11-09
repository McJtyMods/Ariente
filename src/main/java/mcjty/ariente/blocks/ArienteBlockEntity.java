package mcjty.ariente.blocks;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.RegistryOps;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.minecraft.core.component.DataComponentGetter;
import javax.annotation.Nullable;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.tileentity.TickingTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

/** Ariente machine state layered on McJtyLib's registry-aware block entities. */
public class ArienteBlockEntity extends TickingTileEntity {

    private static final Codec<ArienteBlockEntity> SAVE_CODEC = new Codec<>() {

        @Override
        public <T> DataResult<T> encode(ArienteBlockEntity machine, DynamicOps<T> ops, T prefix) {
            if (!(ops instanceof RegistryOps<T> registryOps)) {
                return DataResult.error(() -> "Ariente machine saves require a registry context");
            }
            CompoundTag tag = new CompoundTag();
            machine.saveAdditional(tag, CommonHooks.extractLookupProvider(registryOps));
            return CompoundTag.CODEC.encode(tag, ops, prefix);
        }

        @Override
        public <T> DataResult<Pair<ArienteBlockEntity, T>> decode(DynamicOps<T> ops, T input) {
            return DataResult.error(() -> "Machine state is loaded into its existing block entity");
        }
    };

    protected ArienteBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Nullable
    public IItemHandler getAutomationHandler(@Nullable Direction side) {
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public ResourceHandler<ItemResource> getItemResourceHandler(@Nullable Direction side) {
        IItemHandler handler = getAutomationHandler(side);
        return handler == null ? null : (ResourceHandler<ItemResource>) handler;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(MapCodec.assumeMapUnsafe(SAVE_CODEC), this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        loadAdditional(input.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseGet(CompoundTag::new), input.lookup());
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        saveInfo(tag, provider);
        if (getAutomationHandler(null) instanceof GenericItemHandler items) {
            tag.put("Items", items.serializeNBT(provider));
        }
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        loadInfo(tag, provider);
        if (getAutomationHandler(null) instanceof GenericItemHandler items) {
            items.deserializeNBT(provider, tag.getCompoundOrEmpty("Items"));
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, level.registryAccess());
        components.set(mcjty.ariente.setup.Registration.MACHINE_DATA.get(), CustomData.of(tag));
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter input) {
        super.applyImplicitComponents(input);
        var data = input.get(mcjty.ariente.setup.Registration.MACHINE_DATA.get());
        if (data != null && level != null) {
            loadAdditional(data.copyTag(), level.registryAccess());
        }
    }

    protected void saveInfo(CompoundTag tag, HolderLookup.Provider provider) {
    }

    protected void loadInfo(CompoundTag tag, HolderLookup.Provider provider) {
    }

    protected static CompoundTag getOrCreateInfo(CompoundTag tag) {
        if (!tag.contains("Info")) {
            tag.put("Info", new CompoundTag());
        }
        return tag.getCompoundOrEmpty("Info");
    }
}
