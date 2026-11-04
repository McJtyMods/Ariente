package mcjty.ariente;

import mcjty.ariente.blocks.utility.ChargerTile;
import mcjty.ariente.entities.fluxship.FluxShipEnergy;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.items.FluxShipItem;
import mcjty.ariente.power.IPowerBlob;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.power.PowerType;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static mcjty.ariente.blocks.BlockProperties.WORKING;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class FluxShipChargingTests {

    @GameTest(template = "empty")
    public static void freshCreativeAndCraftedItemsDeployWithoutPower(GameTestHelper helper) {
        BlockPos origin = helper.absolutePos(new BlockPos(1, 0, 1));
        BlockPos floor = new BlockPos(origin.getX(), 239, origin.getZ());
        helper.getLevel().setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
        Player player = helper.makeMockSurvivalPlayer();
        for (boolean creative : new boolean[]{false, true}) {
            player.getAbilities().instabuild = creative;
            ItemStack stack = new ItemStack(Registration.FLUX_SHIP.get());
            // Unrelated item tags must not cause a fresh ship to inherit the entity's charged default.
            stack.setHoverName(Component.literal("Fresh ship"));
            helper.assertTrue(FluxShipItem.getCharge(stack) == 0, "Fresh ships must report an empty battery");
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(floor).add(0, .5, 0), Direction.UP, floor, false));
            try {
                helper.assertTrue(stack.useOn(context).consumesAction(), "A fresh empty ship must still deploy");
                var ships = helper.getLevel().getEntitiesOfClass(FluxShipEntity.class, new AABB(floor).inflate(5));
                helper.assertTrue(ships.size() == 1 && ships.get(0).getCharge() == 0
                                && FluxShipItem.getCharge(ships.get(0).asItem()) == 0,
                        "New survival and creative ships must deploy and recover with no power");
                helper.assertTrue(creative ? !stack.isEmpty() : stack.isEmpty(),
                        "Creative placement retains the item; survival placement consumes it");
            } finally {
                helper.getLevel().getEntitiesOfClass(FluxShipEntity.class, new AABB(floor).inflate(5)).forEach(FluxShipEntity::discard);
            }
        }
        helper.getLevel().removeBlock(floor, false);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aFullChargeFliesTwentyMinutesAndPowerLossEasesIntoDescent(GameTestHelper helper) {
        FluxShipEntity ship = FluxShipEntity.create(helper.getLevel(), 183000.5, 220, 181000.5);
        helper.getLevel().addFreshEntity(ship);
        Player pilot = helper.makeMockPlayer();
        pilot.startRiding(ship);
        Vec3 position = ship.position();
        ship.setDeltaMovement(0, 0, 5.2);
        try {
            for (int tick = 0; tick < 24_000; tick++) {
                ship.setPos(position);
                ship.handleInput(pilot, 1, 0, 0);
                ship.tick();
                if (tick == 23_998) {
                    helper.assertTrue(ship.getCharge() == 50, "Full-speed flight must retain its last tick of power at twenty minutes");
                }
            }
            helper.assertTrue(ship.getCharge() == 0 && ship.getSpeed() > 103.9,
                    "The complete charge must supply exactly 24,000 full-speed ticks");
            ship.tick();
            helper.assertTrue(ship.getDeltaMovement().z > 4.8 && ship.getDeltaMovement().z < 5.2,
                    "An exhausted engine must brake gradually, without an instant stop");
            for (int tick = 0; tick < 150; tick++) {
                ship.setPos(position);
                ship.handleInput(pilot, 1, 0, 1);
                ship.tick();
            }
            helper.assertTrue(ship.getCharge() == 0 && ship.getDeltaMovement().horizontalDistance() < .001
                            && Math.abs(ship.getDeltaMovement().y + .05) < .001,
                    "Empty ships cannot generate thrust and must gently descend even with a pilot");
            helper.assertTrue(FluxShipEnergy.flightCost(0, 0) <= FluxShipEnergy.FULL_THRUST_COST / 10
                            && FluxShipEnergy.flightCost(0, -.05) <= FluxShipEnergy.FULL_THRUST_COST / 10,
                    "Hovering and a gentle descent must cost substantially less than full-speed flight");
        } finally {
            pilot.stopRiding();
            ship.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chargerNeedsBothPolaritiesAndFillsOneStationaryShipInOneMinute(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos, Registration.CHARGER.get().defaultBlockState());
        ChargerTile charger = (ChargerTile) helper.getLevel().getBlockEntity(pos);
        FluxShipEntity ship = FluxShipEntity.create(helper.getLevel(), pos.getX() + .5, pos.getY() + 1.0625, pos.getZ() + .5);
        ship.setCharge(0);
        helper.getLevel().addFreshEntity(ship);
        PowerSystem power = PowerSystem.getPowerSystem(helper.getLevel());
        try {
            helper.getLevel().setBlockAndUpdate(pos.north(), Registration.NEGARITE_GENERATOR.get().defaultBlockState());
            int negative = power.allocateId();
            ((IPowerBlob) helper.getLevel().getBlockEntity(pos.north())).fillCableId(negative);
            power.addPower(negative, FluxShipEnergy.CAPACITY, PowerType.NEGARITE);
            charger.tickServer();
            helper.assertTrue(ship.getCharge() == 0 && !charger.getBlockState().getValue(WORKING)
                            && power.getTotalPower(negative, PowerType.NEGARITE) == FluxShipEnergy.CAPACITY,
                    "One polarity cannot activate the pad, charge a ship or waste the other polarity");
            helper.getLevel().setBlockAndUpdate(pos.south(), Registration.POSIRITE_GENERATOR.get().defaultBlockState());
            int positive = power.allocateId();
            ((IPowerBlob) helper.getLevel().getBlockEntity(pos.south())).fillCableId(positive);
            power.addPower(positive, FluxShipEnergy.CAPACITY, PowerType.POSIRITE);
            for (int tick = 0; tick < 1199; tick++) {
                charger.tickServer();
            }
            helper.assertTrue(ship.getCharge() == FluxShipEnergy.CAPACITY - FluxShipEnergy.CHARGE_PER_TICK
                            && charger.getBlockState().getValue(WORKING), "Charging must require the final tick of its minute");
            charger.tickServer();
            helper.assertTrue(ship.getCharge() == FluxShipEnergy.CAPACITY
                            && power.getTotalPower(negative, PowerType.NEGARITE) == 0
                            && power.getTotalPower(positive, PowerType.POSIRITE) == 0,
                    "A full recharge consumes matching amounts of both power types without overflow");
            charger.tickServer();
            helper.assertTrue(!charger.getBlockState().getValue(WORKING), "An empty network must switch the top to its black texture");
            power.addPower(negative, 1000, PowerType.NEGARITE);
            power.addPower(positive, 1000, PowerType.POSIRITE);
            ship.setCharge(0);
            ship.setDeltaMovement(0, 0, .5);
            charger.tickServer();
            helper.assertTrue(ship.getCharge() == 0 && charger.getUsingPower() == 0, "A moving flyby must not charge");
            ship.setDeltaMovement(Vec3.ZERO);
            ship.setPos(ship.position().add(2, 0, 0));
            charger.tickServer();
            helper.assertTrue(ship.getCharge() == 0, "Only a ship centered above the pad may charge");
            ItemStack blueprint = BlueprintItem.makeBluePrint(new ItemStack(Registration.CHARGER.get()));
            helper.assertTrue(BlueprintRecipeRegistry.findRecipe(BlueprintItem.getDestination(blueprint)) != null,
                    "The new landing pad must have a usable Constructor blueprint");
        } finally {
            ship.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chargeSurvivesSavingRecoveryAndLegacyShipsRemainUsable(GameTestHelper helper) {
        FluxShipEntity ship = FluxShipEntity.create(helper.getLevel(), 0, 220, 0);
        try {
            ship.setCharge(123456);
            CompoundTag saved = ship.saveWithoutId(new CompoundTag());
            ship.setCharge(0);
            ship.load(saved);
            helper.assertTrue(ship.getCharge() == 123456
                            && ItemStack.of(ship.asItem().save(new CompoundTag())).getTag().getInt(FluxShipEnergy.CHARGE_TAG) == 123456,
                    "Saving and recovering a ship must retain the charge without refilling it");
            saved.remove(FluxShipEnergy.CHARGE_TAG);
            ship.load(saved);
            helper.assertTrue(ship.getCharge() == FluxShipEnergy.CAPACITY, "Existing ships from before batteries must start charged");
            ship.setCharge(-1);
            helper.assertTrue(ship.getCharge() == 0, "Charge must not underflow");
            ship.setCharge(Integer.MAX_VALUE);
            helper.assertTrue(ship.getCharge() == FluxShipEnergy.CAPACITY, "Charge must not overflow");
        } finally {
            ship.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void placingARecoveredShipPreservesItsNameAndRemainingCharge(GameTestHelper helper) {
        // Keep the placed entity in the helper's ticking chunks, above the other fixtures.
        BlockPos origin = helper.absolutePos(new BlockPos(1, 0, 1));
        BlockPos floor = new BlockPos(origin.getX(), 219, origin.getZ());
        helper.getLevel().setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
        FluxShipEntity original = FluxShipEntity.create(helper.getLevel(), floor.getX() + .5, floor.getY() + 1.0625, floor.getZ() + .5);
        original.setCharge(4321);
        original.setCustomName(Component.literal("Captured scout"));
        Player player = helper.makeMockSurvivalPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, original.asItem());
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(floor).add(0, .5, 0), Direction.UP, floor, false));
        try {
            helper.assertTrue(player.getMainHandItem().useOn(context).consumesAction() && player.getMainHandItem().isEmpty(),
                    "A recovered ship must place normally and consume its survival item");
            var ships = helper.getLevel().getEntitiesOfClass(FluxShipEntity.class, new AABB(floor).inflate(5));
            helper.assertTrue(ships.size() == 1 && ships.get(0).getCharge() == 4321
                            && "Captured scout".equals(ships.get(0).getName().getString()),
                    "Replacing the recovered item must retain the ship's exact battery and name: "
                            + ships.stream().map(ship -> ship.getUUID() + ": " + ship.getName().getString() + ", charge=" + ship.getCharge()
                            + ", removed=" + ship.isRemoved()).toList());
        } finally {
            original.discard();
            helper.getLevel().getEntitiesOfClass(FluxShipEntity.class, new AABB(floor).inflate(5)).forEach(FluxShipEntity::discard);
            helper.getLevel().removeBlock(floor, false);
        }
        helper.succeed();
    }
}
