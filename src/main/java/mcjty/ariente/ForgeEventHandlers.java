package mcjty.ariente;

import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.blocks.utility.ILockable;
import mcjty.ariente.config.WorldgenConfiguration;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.FluxLevitatorSounds;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.DimensionType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void onLootLoad(LootTableLoadEvent event) {
        if (WorldgenConfiguration.doDungeonLoot()) {
            if (event.getName().equals(LootTables.ABANDONED_MINESHAFT) ||
                    event.getName().equals(LootTables.IGLOO_CHEST) ||
                    event.getName().equals(LootTables.DESERT_PYRAMID) ||
                    event.getName().equals(LootTables.JUNGLE_TEMPLE) ||
                    event.getName().equals(LootTables.NETHER_BRIDGE) ||
                    event.getName().equals(LootTables.SIMPLE_DUNGEON) ||
                    event.getName().equals(LootTables.VILLAGE_TOOLSMITH)) {
                LootPool main = event.getTable().getPool("main");
                // Safety, check if the main lootpool is still present
                if (main != null) {
                    if (WorldgenConfiguration.OVERWORLD_LOOT_BLUEPRINTS.get() > 0) {
                        LootFunction lootFunction = new LootFunction(new ILootCondition[0]) {
                            @Override
                            protected ItemStack run(ItemStack stack, LootContext context) {
                                ConstructorRecipe recipe = BlueprintRecipeRegistry.getRandomRecipes().getRandom();
                                return BlueprintItem.makeBluePrint(recipe.getDestination());
                            }

                            @Override
                            public LootFunctionType getType() {
                                // TODO Auto-generated method stub
                                return null;
                            }
                        };
                        // @todo 1.14
//                        main.addEntry(new LootEntryItem(ModItems.blueprintItem, WorldgenConfiguration.OVERWORLD_LOOT_BLUEPRINTS.get(), 0, new LootFunction[]{lootFunction},
//                                new LootCondition[0], Ariente.MODID + ":loot"));
                    }
                    if (WorldgenConfiguration.OVERWORLD_LOOT_ITEMS.get() > 0) {
                        LootFunction lootFunction = new LootFunction(new ILootCondition[0]) {
                            @Override
                            public ItemStack run(ItemStack stack, LootContext context) {
                                ConstructorRecipe recipe = BlueprintRecipeRegistry.getRandomRecipes().getRandom();
                                return recipe.getDestination();
                            }

                            @Override
                            public LootFunctionType getType() {
                                // TODO Auto-generated method stub
                                return null;
                            }
                        };
                        // @todo 1.14
//                        main.addEntry(new LootEntryItem(ModItems.blueprintItem, WorldgenConfiguration.OVERWORLD_LOOT_ITEMS.get(), 0, new LootFunction[]{lootFunction},
//                                new LootCondition[0], Ariente.MODID + ":lootresult"));
                    }

                    if (WorldgenConfiguration.OVERWORLD_LOOT_SUIT.get() > 0) {
                        // @todo 1.14
//                        main.addEntry(new LootEntryItem(ModItems.powerSuitChest, WorldgenConfiguration.OVERWORLD_LOOT_SUIT.get(), 0, new LootFunction[0],
//                                new LootCondition[0], Ariente.MODID + ":lootchest"));
//                        main.addEntry(new LootEntryItem(ModItems.powerSuitBoots, WorldgenConfiguration.OVERWORLD_LOOT_SUIT.get(), 0, new LootFunction[0],
//                                new LootCondition[0], Ariente.MODID + ":lootboots"));
//                        main.addEntry(new LootEntryItem(ModItems.powerSuitLegs, WorldgenConfiguration.OVERWORLD_LOOT_SUIT.get(), 0, new LootFunction[0],
//                                new LootCondition[0], Ariente.MODID + ":lootlegs"));
//                        main.addEntry(new LootEntryItem(ModItems.powerSuitHelmet, WorldgenConfiguration.OVERWORLD_LOOT_SUIT.get(), 0, new LootFunction[0],
//                                new LootCondition[0], Ariente.MODID + ":loothelmet"));
//                        main.addEntry(new LootEntryItem(ModItems.energySabre, WorldgenConfiguration.OVERWORLD_LOOT_SUIT.get(), 0, new LootFunction[0],
//                                new LootCondition[0], Ariente.MODID + ":lootsabre"));
//                        main.addEntry(new LootEntryItem(ModItems.enhancedEnergySabreItem, WorldgenConfiguration.OVERWORLD_LOOT_SUIT.get(), 0, new LootFunction[0],
//                                new LootCondition[0], Ariente.MODID + ":lootesabre"));
                    }
                }
            }
        }
    }

    // @todo 1.14
//    @SubscribeEvent
//    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
//        if (Config.mainConfig.hasChanged()) {
//            Config.mainConfig.save();
//        }
//    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.world.isClientSide && event.world.dimension() == Level.OVERWORLD) {
            PowerSystem.getPowerSystem(event.world).tick();
        }
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        ItemStack feetStack = event.getEntityLiving().getItemBySlot(EquipmentSlot.FEET);
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
                    if (source.isExplosion()) {
                        event.setAmount(damage / 5);
                    } else if (source.isProjectile()) {
                        event.setCanceled(true);
                    } else if (!source.isBypassArmor()) {
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
        Level world = event.getWorld();
        BlockPos pos = event.getPos();
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ILockable) {
            if (((ILockable) te).isLocked()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent event) {
        Level world = event.getWorld();
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
