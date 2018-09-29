package mcjty.ariente;

import mcjty.ariente.ai.CityAI;
import mcjty.ariente.ai.CityAISystem;
import mcjty.ariente.ai.IAlarmMode;
import mcjty.ariente.biomes.ModBiomes;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.blocks.utility.ILockable;
import mcjty.ariente.cities.BuildingPart;
import mcjty.ariente.cities.City;
import mcjty.ariente.cities.CityTools;
import mcjty.ariente.config.WorldgenConfiguration;
import mcjty.ariente.dimension.ArienteChunkGenerator;
import mcjty.ariente.dimension.EditMode;
import mcjty.ariente.entities.FluxLevitatorEntity;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.items.modules.ArmorUpgradeType;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.sounds.FluxLevitatorSounds;
import mcjty.ariente.sounds.ModSounds;
import mcjty.lib.McJtyRegister;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        McJtyRegister.registerBlocks(Ariente.instance, event.getRegistry());
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        McJtyRegister.registerItems(Ariente.instance, event.getRegistry());
        ModBlocks.initOreDict();
        ModItems.initOreDict();
        ModCrafting.init();
    }

    @SubscribeEvent
    public void registerBiomes(RegistryEvent.Register<Biome> event) {
        IForgeRegistry<Biome> registry = event.getRegistry();
        ModBiomes.registerBiomes(registry);
    }

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> sounds) {
        ModSounds.init(sounds.getRegistry());
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (Ariente.proxy.getConfig().hasChanged()) {
            Ariente.proxy.getConfig().save();
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.world.isRemote && event.world.provider.getDimension() == 0) {
            PowerSystem.getPowerSystem(event.world).tick();
        }
    }

    @SubscribeEvent
    public void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (event.isSpawner()) {
            return;
        }
        if (event.getWorld().provider.getDimension() == WorldgenConfiguration.DIMENSION_ID) {
            if (event.getEntity() instanceof IAnimals) {
                event.setResult(Event.Result.DENY);
            }
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
        if (world.provider.getDimension() == WorldgenConfiguration.DIMENSION_ID) {
            EntityPlayer player = event.getPlayer();
            if (te instanceof IAlarmMode) {
                boolean highAlert = ((IAlarmMode) te).isHighAlert();
                alertCity(world, pos, player, highAlert);
            } else if (world.getBlockState(pos).getBlock() == ModBlocks.reinforcedMarble) {
                alertCity(world, pos, player, true);
            }
        }
    }

    private void alertCity(World world, BlockPos pos, EntityPlayer player, boolean highAlert) {
        int cx = pos.getX() >> 4;
        int cz = pos.getZ() >> 4;
        ArienteChunkGenerator generator = (ArienteChunkGenerator) (((WorldServer) world).getChunkProvider().chunkGenerator);
        if (CityTools.isCityChunk(cx, cz)) {
            City city = CityTools.getNearestCity(generator, cx, cz);
            CityAISystem cityAISystem = CityAISystem.getCityAISystem(world);
            CityAI cityAI = cityAISystem.getCityAI(city.getCenter());
            if (cityAI != null) {
                if (highAlert) {
                    cityAI.highAlertMode(player);
                } else {
                    cityAI.alertCity(player);
                }
                cityAISystem.save();
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
        if (!EditMode.editMode) {
            onBlockBreakNormal(event);
            return;
        }
        World world = event.getWorld();
        if (!world.isRemote) {
            if (world.provider.getDimension() == WorldgenConfiguration.DIMENSION_ID) {
                ArienteChunkGenerator generator = (ArienteChunkGenerator)(((WorldServer) world).getChunkProvider().chunkGenerator);
                BlockPos pos = event.getPos();
                int cx = pos.getX() >> 4;
                int cz = pos.getZ() >> 4;
                City city = CityTools.getNearestCity(generator, cx, cz);
                if (city != null) {
                    List<BuildingPart> parts = CityTools.getBuildingParts(city, cx, cz);
                    if (!parts.isEmpty()) {
                        BuildingPart found = null;
                        int partY = -1;
                        int lowesty = CityTools.getLowestHeight(city, generator, cx, cz);
                        for (int i = 0 ; i < parts.size() ; i++) {
                            int count = parts.get(i).getSliceCount();
                            if (pos.getY() >= lowesty && pos.getY() < lowesty + count) {
                                found = parts.get(i);
                                partY = lowesty;
                                break;
                            }
                            lowesty += count;

                        }
                        if (found != null) {
                            EditMode.breakBlock(city, event.getWorld(), found, pos.getX() & 0xf, pos.getY() - partY, pos.getZ() & 0xf);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        if (!EditMode.editMode) {
            return;
        }
        World world = event.getWorld();
        if (!world.isRemote) {
            if (world.provider.getDimension() == WorldgenConfiguration.DIMENSION_ID) {
                ArienteChunkGenerator generator = (ArienteChunkGenerator)(((WorldServer) world).getChunkProvider().chunkGenerator);
                BlockPos pos = event.getPos();
                int cx = pos.getX() >> 4;
                int cz = pos.getZ() >> 4;
                City city = CityTools.getNearestCity(generator, cx, cz);
                if (city != null) {
                    List<BuildingPart> parts = CityTools.getBuildingParts(city, cx, cz);
                    if (!parts.isEmpty()) {
                        BuildingPart found = null;
                        int partY = -1;
                        int lowesty = CityTools.getLowestHeight(city, generator, cx, cz);
                        for (int i = 0 ; i < parts.size() ; i++) {
                            int count = parts.get(i).getSliceCount();
                            if (pos.getY() >= lowesty && pos.getY() < lowesty + count) {
                                found = parts.get(i);
                                partY = lowesty;
                                break;
                            }
                            lowesty += count;

                        }
                        if (found != null) {
                            EditMode.copyBlock(city, event.getWorld(), event.getPlacedBlock(), found, pos.getX() & 0xf, pos.getY() - partY, pos.getZ() & 0xf);
                        }
                    }
                }
            }
        }
    }

}
