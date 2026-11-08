package mcjty.ariente.blocks;

import javax.annotation.Nullable;
import mcjty.ariente.setup.Registration;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.tileentity.TickingTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

/** Ariente machine state layered on McJtyLib's registry-aware block entities. */
public class ArienteBlockEntity extends TickingTileEntity {

    protected ArienteBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Nullable
    public IItemHandler getAutomationHandler(@Nullable Direction side) {
        return null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        saveInfo(tag, provider);
        if (getAutomationHandler(null) instanceof GenericItemHandler items) {
            tag.put("Items", items.serializeNBT(provider));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        loadInfo(tag, provider);
        if (getAutomationHandler(null) instanceof GenericItemHandler items) {
            items.deserializeNBT(provider, tag.getCompound("Items"));
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, level.registryAccess());
        components.set(Registration.MACHINE_DATA.get(), CustomData.of(tag));
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        var data = input.get(Registration.MACHINE_DATA.get());
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
        return tag.getCompound("Info");
    }
}
