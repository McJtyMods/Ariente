package mcjty.ariente;

import net.minecraft.nbt.Tag;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.blocks.ExtremeMachineTile;
import mcjty.ariente.blocks.utility.ILockable;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.entities.LaserEntity;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.items.armor.PacketPersonalForcefield;
import mcjty.ariente.items.armor.PowerSuitEffects;
import mcjty.ariente.items.armor.PersonalForcefield;
import mcjty.ariente.items.armor.PowerSuit;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.FluxLevitatorSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.network.PacketDistributor;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (ExtremeMachineTile.hasExtremeCircuit(event.getItemStack())) {
            event.getToolTip().add(Component.translatable("message.ariente.extreme_circuit.upgraded"));
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.level.isClientSide && event.level.dimension() == Level.OVERWORLD) {
            PowerSystem.getPowerSystem(event.level).tick();
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            PowerSuitEffects.tick(event.player);
            if (event.player instanceof ServerPlayer player && player.isAlive() && !player.isPassenger()
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
        if (original.contains("arienteReturn", Tag.TAG_COMPOUND)) {
            event.getEntity().getPersistentData().put("arienteReturn", original.getCompound("arienteReturn").copy());
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

    @SubscribeEvent
    public void onLanceLooting(LootingLevelEvent event) {
        if (event.getDamageSource() != null && event.getDamageSource().getDirectEntity() instanceof LaserEntity laser && laser.isLanceShot()) {
            event.setLootingLevel(Math.max(event.getLootingLevel(), laser.getLootingLevel()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamage(LivingDamageEvent event) {
        PowerSuit.absorbEnergyDamage(event);
        PersonalForcefield.absorb(event);
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof LivingEntity entity
                && PersonalForcefield.isCoolingDown(entity)) {
            ArienteMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
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
            ArienteMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                    new PacketPersonalForcefield(entity.getId(), PersonalForcefield.getCooldownUntil(entity),
                            PersonalForcefield.getLastHit(entity)));
        }
    }

    private void onBlockBreakNormal(BlockEvent.BreakEvent event) {
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
        if (world.isClientSide) {
            Entity entity = event.getEntity();
            if (entity instanceof FluxLevitatorEntity) {
                FluxLevitatorSounds.playMovingSoundClient((FluxLevitatorEntity) entity);
            }
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        onBlockBreakNormal(event);
    }

}
