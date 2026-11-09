package mcjty.ariente;

import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.blocks.ExtremeMachineTile;
import mcjty.ariente.blocks.utility.ILockable;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.items.armor.PacketPersonalForcefield;
import mcjty.ariente.items.armor.PersonalForcefield;
import mcjty.ariente.items.armor.PowerSuit;
import mcjty.ariente.items.armor.PowerSuitEffects;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.FluxLevitatorSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (ExtremeMachineTile.hasExtremeCircuit(event.getItemStack())) {
            event.getToolTip().add(Component.translatable("message.ariente.extreme_circuit.upgraded"));
        }
    }

    @SubscribeEvent
    public void onWorldTick(LevelTickEvent.Pre event) {
        if (!event.getLevel().isClientSide() && event.getLevel().dimension() == Level.OVERWORLD) {
            PowerSystem.getPowerSystem(event.getLevel()).tick();
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide()) {
            PowerSuitEffects.tick(event.getEntity());
            if (event.getEntity() instanceof ServerPlayer player && player.isAlive() && !player.isPassenger()
                    && player.getItemBySlot(EquipmentSlot.CHEST).is(Registration.POWERSUIT_CHEST.get())
                    && ModuleSupport.hasWorkingUpgrade(player.getItemBySlot(EquipmentSlot.CHEST), ArmorUpgradeType.FLIGHT)) {
                player.connection.aboveGroundTickCount = 0;
                player.fallDistance = 0;
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PowerSuitEffects.clear(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        var original = event.getOriginal().getPersistentData();
        if (original.contains("arienteReturn")) {
            event.getEntity().getPersistentData().put("arienteReturn", original.getCompoundOrEmpty("arienteReturn").copy());
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        PowerSuitEffects.clear(event.getEntity());
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        ItemStack feetStack = event.getEntity().getItemBySlot(EquipmentSlot.FEET);
        if (feetStack.getItem() == Registration.POWERSUIT_FEET.get()) {
            if (ModuleSupport.hasWorkingUpgrade(feetStack, ArmorUpgradeType.FEATHERFALLING)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamage(LivingDamageEvent.Pre event) {
        PowerSuit.absorbEnergyDamage(event);
        PersonalForcefield.absorb(event);
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof LivingEntity entity
                && PersonalForcefield.isCoolingDown(entity)) {
            PacketDistributor.sendToPlayer(player,
                    new PacketPersonalForcefield(entity.getId(), PersonalForcefield.getCooldownUntil(entity),
                            PersonalForcefield.getLastHit(entity)));
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        syncPersonalField(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncPersonalField(event.getEntity());
    }

    private void syncPersonalField(LivingEntity entity) {
        if (entity instanceof ServerPlayer player && PersonalForcefield.isCoolingDown(entity)) {
            PacketDistributor.sendToPlayer(player,
                    new PacketPersonalForcefield(entity.getId(), PersonalForcefield.getCooldownUntil(entity),
                            PersonalForcefield.getLastHit(entity)));
        }
    }

    private void onBlockBreakNormal(BreakBlockEvent event) {
        LevelAccessor world = event.getLevel();
        BlockPos pos = event.getPos();
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ILockable) {
            if (((ILockable) te).isLocked()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        Level world = event.getLevel();
        if (world.isClientSide()) {
            Entity entity = event.getEntity();
            if (entity instanceof FluxLevitatorEntity) {
                FluxLevitatorSounds.playMovingSoundClient((FluxLevitatorEntity) entity);
            }
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BreakBlockEvent event) {
        onBlockBreakNormal(event);
    }

}
