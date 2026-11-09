package mcjty.ariente.blocks.utility;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import mcjty.ariente.blocks.ExtremeMachineTile;
import mcjty.ariente.entities.fluxship.FluxShipEnergy;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.ariente.power.IPowerUser;
import mcjty.ariente.power.PowerReceiverSupport;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;

import static mcjty.ariente.blocks.BlockProperties.WORKING;
import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

/** A landing pad, fed by either separate polarity cables or a combined power network. */
public class ChargerTile extends ExtremeMachineTile implements IPowerReceiver, IPowerUser {

    private long usingPower;

    public ChargerTile(BlockPos pos, BlockState state) {
        super(mcjty.ariente.setup.Registration.CHARGER_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock(BlockBehaviour.Properties properties) {
        return new BaseBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
                .topDriver(DRIVER)
                .tileEntitySupplier(ChargerTile::new)) {

            {
                registerDefaultState(defaultBlockState().setValue(WORKING, false));
            }

            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }

            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                super.createBlockStateDefinition(builder);
                builder.add(WORKING);
            }
        };
    }

    public boolean isDocked(FluxShipEntity ship) {
        double bottom = ship.getY() - (worldPosition.getY() + 1);
        return Math.abs(ship.getX() - worldPosition.getX() - .5) <= .8
                && Math.abs(ship.getZ() - worldPosition.getZ() - .5) <= .8
                && bottom >= -.01 && bottom <= .4 && ship.getDeltaMovement().lengthSqr() < .01;
    }

    @Override
    public void tickServer() {
        usingPower = 0;
        long available = PowerReceiverSupport.getPowerAvailable(level, worldPosition, true);
        boolean powered = available > 0;
        if (getBlockState().getValue(WORKING) != powered) {
            level.setBlock(worldPosition, getBlockState().setValue(WORKING, powered), Block.UPDATE_CLIENTS);
        }
        if (!powered) {
            return;
        }
        // Only the ship centered on the pad receives power; a flyby above it cannot recharge.
        FluxShipEntity ship = level.getEntitiesOfClass(FluxShipEntity.class, new AABB(worldPosition).inflate(1, 2, 1), this::isDocked)
                .stream().findFirst().orElse(null);
        if (ship != null) {
            int amount = (int) Math.min(Math.min(powerBudget(available), FluxShipEnergy.CAPACITY) * operationSpeed(),
                    Math.min(FluxShipEnergy.CHARGE_PER_TICK * operationSpeed(), FluxShipEnergy.CAPACITY - ship.getCharge()));
            long cost = powerCost((amount + operationSpeed() - 1) / operationSpeed());
            if (amount > 0 && PowerReceiverSupport.consumePower(level, worldPosition, cost, true)) {
                ship.setCharge(ship.getCharge() + amount);
                usingPower = cost;
            }
        }
    }

    @Override
    public long getUsingPower() {
        return usingPower;
    }
}
