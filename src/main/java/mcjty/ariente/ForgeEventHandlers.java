package mcjty.ariente;

import mcjty.ariente.blocks.utility.ILockable;
import mcjty.ariente.config.Config;
import mcjty.ariente.config.WorldgenConfiguration;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.sounds.FluxLevitatorSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void onLootLoad(LootTableLoadEvent event) {
        if (WorldgenConfiguration.doDungeonLoot()) {
            if (event.getName().equals(LootTableList.CHESTS_ABANDONED_MINESHAFT) ||
                    event.getName().equals(LootTableList.CHESTS_IGLOO_CHEST) ||
                    event.getName().equals(LootTableList.CHESTS_DESERT_PYRAMID) ||
                    event.getName().equals(LootTableList.CHESTS_JUNGLE_TEMPLE) ||
                    event.getName().equals(LootTableList.CHESTS_NETHER_BRIDGE) ||
                    event.getName().equals(LootTableList.CHESTS_SIMPLE_DUNGEON) ||
                    event.getName().equals(LootTableList.CHESTS_VILLAGE_BLACKSMITH)) {
                LootPool main = event.getTable().getPool("main");
                // Safety, check if the main lootpool is still present
                if (main != null) {
                    if (WorldgenConfiguration.OVERWORLD_LOOT_BLUEPRINTS.get() > 0) {
                        LootFunction lootFunction = new LootFunction(new LootCondition[0]) {
                            @Override
                            public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
                                ConstructorRecipe recipe = BlueprintRecipeRegistry.getRandomRecipes().getRandom();
                                return BlueprintItem.makeBluePrint(recipe.getDestination());
                            }
                        };
                        main.addEntry(new LootEntryItem(ModItems.blueprintItem, WorldgenConfiguration.OVERWORLD_LOOT_BLUEPRINTS.get(), 0, new LootFunction[]{lootFunction},
                                new LootCondition[0], Ariente.MODID + ":loot"));
                    }
                    if (WorldgenConfiguration.OVERWORLD_LOOT_ITEMS.get() > 0) {
                        LootFunction lootFunction = new LootFunction(new LootCondition[0]) {
                            @Override
                            public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
                                ConstructorRecipe recipe = BlueprintRecipeRegistry.getRandomRecipes().getRandom();
                                return recipe.getDestination();
                            }
                        };
                        main.addEntry(new LootEntryItem(ModItems.blueprintItem, WorldgenConfiguration.OVERWORLD_LOOT_ITEMS.get(), 0, new LootFunction[]{lootFunction},
                                new LootCondition[0], Ariente.MODID + ":lootresult"));
                    }

                    if (WorldgenConfiguration.OVERWORLD_LOOT_SUIT.get() > 0) {
                        main.addEntry(new LootEntryItem(ModItems.powerSuitChest, WorldgenConfiguration.OVERWORLD_LOOT_SUIT.get(), 0, new LootFunction[0],
                                new LootCondition[0], Ariente.MODID + ":lootchest"));
                        main.addEntry(new LootEntryItem(ModItems.powerSuitBoots, WorldgenConfiguration.OVERWORLD_LOOT_SUIT.get(), 0, new LootFunction[0],
                                new LootCondition[0], Ariente.MODID + ":lootboots"));
                        main.addEntry(new LootEntryItem(ModItems.powerSuitLegs, WorldgenConfiguration.OVERWORLD_LOOT_SUIT.get(), 0, new LootFunction[0],
                                new LootCondition[0], Ariente.MODID + ":lootlegs"));
                        main.addEntry(new LootEntryItem(ModItems.powerSuitHelmet, WorldgenConfiguration.OVERWORLD_LOOT_SUIT.get(), 0, new LootFunction[0],
                                new LootCondition[0], Ariente.MODID + ":loothelmet"));
                        main.addEntry(new LootEntryItem(ModItems.energySabre, WorldgenConfiguration.OVERWORLD_LOOT_SUIT.get(), 0, new LootFunction[0],
                                new LootCondition[0], Ariente.MODID + ":lootsabre"));
                        main.addEntry(new LootEntryItem(ModItems.enhancedEnergySabreItem, WorldgenConfiguration.OVERWORLD_LOOT_SUIT.get(), 0, new LootFunction[0],
                                new LootCondition[0], Ariente.MODID + ":lootesabre"));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (Config.mainConfig.hasChanged()) {
            Config.mainConfig.save();
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
        if (!world.isRemote && entity instanceof LivingEntity) {
            ItemStack chestStack = ((LivingEntity) entity).getItemStackFromSlot(EntityEquipmentSlot.CHEST);
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
