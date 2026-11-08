package mcjty.ariente.api;

import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;

public interface IArienteSystem {

    @Nonnull
    List<? extends IFluxLevitatorEntity> getLevitatorsWithinAABB(Level world, AABB aabb);

    Entity createFluxLevitatorEntity(Level world, double x, double y, double z);

    void addSecurity(ItemStack keyCard, String tag);

    void setDescription(ItemStack keyCard, String description);

    void fixNetworks(Level world, BlockPos pos);

    RailShape getBeamDirection(BlockState state);

    boolean hasWorkingUpgrade(ItemStack armor, ArmorUpgradeType type);

    ISecuritySystem getSecuritySystem(Level world);

    IRedstoneChannels getRedstoneChannels(Level world);
}
