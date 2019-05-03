package mcjty.ariente;

import mcjty.ariente.blocks.utility.ILockable;
import mcjty.ariente.config.ConfigSetup;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.items.modules.ArmorUpgradeType;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.sounds.FluxLevitatorSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (ConfigSetup.mainConfig.hasChanged()) {
            ConfigSetup.mainConfig.save();
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.world.isRemote && event.world.provider.getDimension() == 0) {
            PowerSystem.getPowerSystem(event.world).tick();
        }
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        ItemStack feetStack = event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.FEET);
        if (feetStack.getItem() == ModItems.powerSuitBoots) {
            if (ModuleSupport.hasWorkingUpgrade(feetStack, ArmorUpgradeType.FEATHERFALLING)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        Entity entity = event.getEntity();
        World world = entity.getEntityWorld();
        if (!world.isRemote && entity instanceof EntityLivingBase) {
            ItemStack chestStack = ((EntityLivingBase) entity).getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (chestStack.getItem() == ModItems.powerSuitChest) {
                if (ModuleSupport.hasWorkingUpgrade(chestStack, ArmorUpgradeType.FORCEFIELD)) {
                    float damage = event.getAmount();
                    DamageSource source = event.getSource();
                    if (source.isExplosion()) {
                        event.setAmount(damage / 5);
                    } else if (source.isProjectile()) {
                        event.setCanceled(true);
                    } else if (!source.isUnblockable()) {
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
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ILockable) {
            if (((ILockable) te).isLocked()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent event) {
        World world = event.getWorld();
        if (world.isRemote) {
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
