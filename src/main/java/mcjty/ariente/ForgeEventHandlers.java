package mcjty.ariente;

import net.minecraft.tags.DamageTypeTags;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.blocks.utility.ILockable;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.items.armor.PowerSuitEffects;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.FluxLevitatorSounds;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeEventHandlers {

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
    public void onDamage(LivingDamageEvent event) {
        Entity entity = event.getEntity();
        Level world = entity.getCommandSenderWorld();
        if (!world.isClientSide && entity instanceof LivingEntity) {
            ItemStack chestStack = ((LivingEntity) entity).getItemBySlot(EquipmentSlot.CHEST);
            if (chestStack.getItem() == Registration.POWERSUIT_CHEST.get()) {
                if (ModuleSupport.hasWorkingUpgrade(chestStack, ArmorUpgradeType.FORCEFIELD)) {
                    float damage = event.getAmount();
                    DamageSource source = event.getSource();
                    if (source.is(DamageTypeTags.IS_EXPLOSION)) {
                        event.setAmount(damage / 5);
                    } else if (source.is(DamageTypeTags.IS_PROJECTILE)) {
                        event.setCanceled(true);
                    } else if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
                        event.setAmount(damage / 2);
                    }
                }
            }
        }
    }

//    @SubscribeEvent
//    public void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
//        PowerSuitFeatureCache.checkCacheClean(event.getEntity().getEntityId(), event.getSlot(), event.getFrom(), event.getTo());
//    }

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
