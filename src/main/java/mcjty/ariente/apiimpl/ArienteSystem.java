package mcjty.ariente.apiimpl;

import mcjty.ariente.api.*;
import mcjty.ariente.blocks.utility.wireless.RedstoneChannels;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.items.KeyCardItem;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.power.PowerSenderSupport;
import mcjty.ariente.security.SecuritySystem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.List;

public class ArienteSystem implements IArienteSystem {

    @Nonnull
    @Override
    public List<? extends IFluxLevitatorEntity> getLevitatorsWithinAABB(Level world, AABB aabb) {
        return world.getEntitiesOfClass(FluxLevitatorEntity.class, aabb);
    }

    @Override
    public void addSecurity(ItemStack keyCard, String tag) {
        KeyCardItem.addSecurityTag(keyCard, tag);
    }

    @Override
    public void setDescription(ItemStack keyCard, String description) {
        KeyCardItem.setDescription(keyCard, description);
    }

    @Override
    public void fixNetworks(Level world, BlockPos pos) {
        PowerSenderSupport.fixNetworks(world, pos);
    }

    @Override
    public RailShape getBeamDirection(BlockState state) {
        return FluxLevitatorEntity.getBeamDirection(state);
    }

    @Override
    public boolean hasWorkingUpgrade(ItemStack armor, ArmorUpgradeType type) {
        return ModuleSupport.hasWorkingUpgrade(armor, type);
    }

    @Override
    public ISecuritySystem getSecuritySystem(Level world) {
        return SecuritySystem.getSecuritySystem(world);
    }

    @Override
    public IRedstoneChannels getRedstoneChannels(Level world) {
        return RedstoneChannels.getChannels(world);
    }

    @Override
    public Entity createFluxLevitatorEntity(Level world, double x, double y, double z) {
        return FluxLevitatorEntity.create(world, x, y, z);
    }
}
