package mcjty.ariente.items;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.items.armor.PowerSuit;
import mcjty.ariente.items.modules.ArmorModuleItem;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;

import static mcjty.ariente.setup.Registration.ITEMS;

public class ModItems {

//    public static final RegistryObject<Item> MARBLE_TECH_BLOCK_ITEM = ITEMS.register("marbletech", () -> new BlockItem(MARBLE_TECH_BLOCK.get(), createStandardProperties()));

    public static final RegistryObject<Item> platinumIngot = ITEMS.register("ingot_platinum", () -> new Item(ModBlocks.createStandardProperties()));
    public static final RegistryObject<Item> lithiumIngot = ITEMS.register("ingot_lithium", () -> new Item(ModBlocks.createStandardProperties()));
    public static final RegistryObject<Item> manganeseIngot = ITEMS.register("ingot_manganese", () -> new Item(ModBlocks.createStandardProperties()));
    public static final RegistryObject<Item> silverIngot = ITEMS.register("ingot_silver", () -> new Item(ModBlocks.createStandardProperties()));
    public static final RegistryObject<Item> silicon = ITEMS.register("silicon", () -> new Item(ModBlocks.createStandardProperties()));
    public static final RegistryObject<Item> negariteDust = ITEMS.register("dust_negarite", () -> new Item(ModBlocks.createStandardProperties()));
    public static final RegistryObject<Item> posiriteDust = ITEMS.register("dust_posirite", () -> new Item(ModBlocks.createStandardProperties()));


    public static EnergySabreItem energySabre;
    public static EnhancedEnergySabreItem enhancedEnergySabreItem;
    public static KeyCardItem keyCardItem;
    public static BlueprintItem blueprintItem;
    public static ArientePearlItem arientePearlItem;
    public static FluxLevitatorItem fluxLevitatorItem;
    public static FluxShipItem fluxShipItem;
    public static FluxCapacitorItem fluxCapacitorItem;
    public static CircuitItem circuitItem;
    public static CircuitItem advancedCircuitItem;
    public static EnergyHolderItem energyHolderItem;

    public static PowerSuit powerSuitHelmet;
    public static PowerSuit powerSuitChest;
    public static PowerSuit powerSuitLegs;
    public static PowerSuit powerSuitBoots;

    public static ArmorModuleItem moduleArmor;
    public static ArmorModuleItem moduleEnergy;
    public static ArmorModuleItem moduleFeatherFalling;
    public static ArmorModuleItem moduleFlight;
    public static ArmorModuleItem moduleHover;
    public static ArmorModuleItem moduleForcefield;
    public static ArmorModuleItem moduleInvisibility;
    public static ArmorModuleItem moduleNightvision;
    public static ArmorModuleItem moduleRegeneration;
    public static ArmorModuleItem moduleScramble;
    public static ArmorModuleItem moduleAutofeed;
    public static ArmorModuleItem moduleSpeed;
    public static ArmorModuleItem moduleStepassist;
    public static ArmorModuleItem moduleInhibit;
    public static ArmorModuleItem modulePower;
    public static ArmorModuleItem moduleLooting;
    public static ArmorModuleItem moduleFire;

    public static void init() {
        energySabre = new EnergySabreItem();    // @todo 1.14 "energy_sabre"
        enhancedEnergySabreItem = new EnhancedEnergySabreItem();    // @todo 1.14 "enhanced_energy_sabre"
        keyCardItem = new KeyCardItem();
        blueprintItem = new BlueprintItem();
        fluxLevitatorItem = new FluxLevitatorItem();
        fluxShipItem = new FluxShipItem();
        fluxCapacitorItem = new FluxCapacitorItem();
        circuitItem = new CircuitItem(false);
        advancedCircuitItem = new CircuitItem(true);
        energyHolderItem = new EnergyHolderItem();

        arientePearlItem = new ArientePearlItem();

        powerSuitBoots = new PowerSuit(EquipmentSlotType.FEET);
        powerSuitChest = new PowerSuit(EquipmentSlotType.CHEST);
        powerSuitHelmet = new PowerSuit(EquipmentSlotType.HEAD);
        powerSuitLegs = new PowerSuit(EquipmentSlotType.LEGS);

        moduleArmor = new ArmorModuleItem(ArmorUpgradeType.ARMOR);  // @todo 1.14 "module_armor"
        moduleEnergy = new ArmorModuleItem(ArmorUpgradeType.ENERGY);    // @todo 1.14 "module_energy"
        moduleFeatherFalling = new ArmorModuleItem(ArmorUpgradeType.FEATHERFALLING);    // @todo 1.14 "module_featherfalling"
        moduleFlight = new ArmorModuleItem(ArmorUpgradeType.FLIGHT);    // @todo 1.14 "module_flight"
        moduleHover = new ArmorModuleItem(ArmorUpgradeType.HOVER);  // @todo 1.14 "module_hover"
        moduleForcefield = new ArmorModuleItem(ArmorUpgradeType.FORCEFIELD);    // @todo 1.14 "module_forcefield"
        moduleInvisibility = new ArmorModuleItem(ArmorUpgradeType.INVISIBILITY);    // @todo 1.14 "module_invisibility"
        moduleNightvision = new ArmorModuleItem(ArmorUpgradeType.NIGHTVISION);  // @todo 1.14 "module_nightvision"
        moduleRegeneration = new ArmorModuleItem(ArmorUpgradeType.REGENERATION);    // @todo 1.14 "module_regeneration"
        moduleScramble = new ArmorModuleItem(ArmorUpgradeType.SCRAMBLE);    // @todo 1.14 "module_scramble"
        moduleAutofeed = new ArmorModuleItem(ArmorUpgradeType.AUTOFEED);    // @todo 1.14 "module_autofeed"
        moduleSpeed = new ArmorModuleItem(ArmorUpgradeType.SPEED);  // @todo 1.14 "module_speed"
        moduleStepassist = new ArmorModuleItem(ArmorUpgradeType.STEPASSIST);    // @todo 1.14 "module_stepassist"

        moduleInhibit = new ArmorModuleItem(ArmorUpgradeType.INHIBIT);  // @todo 1.14 "module_inhibit"
        modulePower = new ArmorModuleItem(ArmorUpgradeType.POWER);  // @todo 1.14 "module_power"
        moduleLooting = new ArmorModuleItem(ArmorUpgradeType.LOOTING);  // @todo 1.14 "module_looting"
        moduleFire = new ArmorModuleItem(ArmorUpgradeType.FIRE);    // @todo 1.14 "module_fire"
    }

    public static void initOreDict() {
        // @todo 1.14 oredict
//        OreDictionary.registerOre("ingotSilver", silverIngot);
//        OreDictionary.registerOre("ingotPlatinum", platinumIngot);
//        OreDictionary.registerOre("ingotManganese", manganeseIngot);
//        OreDictionary.registerOre("ingotLithium", lithiumIngot);
//        OreDictionary.registerOre("silicon", silicon);
    }

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    public static void initModels() {
//        platinumIngot.initModel();
//        lithiumIngot.initModel();
//        manganeseIngot.initModel();
//        silverIngot.initModel();
//        negariteDust.initModel();
//        posiriteDust.initModel();
//        silicon.initModel();
//
//        energySabre.initModel();
//        enhancedEnergySabreItem.initModel();
//        keyCardItem.initModel();
//        fluxLevitatorItem.initModel();
//        fluxShipItem.initModel();
//        blueprintItem.initModel();
//        fluxCapacitorItem.initModel();
//        circuitItem.initModel();
//        advancedCircuitItem.initModel();
//        energyHolderItem.initModel();
//
//        arientePearlItem.initModel();
//
//        moduleArmor.initModel();
//        moduleEnergy.initModel();
//        moduleFeatherFalling.initModel();
//        moduleFlight.initModel();
//        moduleHover.initModel();
//        moduleForcefield.initModel();
//        moduleInvisibility.initModel();
//        moduleNightvision.initModel();
//        moduleRegeneration.initModel();
//        moduleScramble.initModel();
//        moduleAutofeed.initModel();
//        moduleSpeed.initModel();
//        moduleStepassist.initModel();
//
//        moduleInhibit.initModel();
//        modulePower.initModel();
//        moduleLooting.initModel();
//        moduleFire.initModel();
//
//        ModelLoader.setCustomModelResourceLocation(powerSuitBoots, 0, new ModelResourceLocation(powerSuitBoots.getRegistryName(), "inventory"));
//        ModelLoader.setCustomModelResourceLocation(powerSuitChest, 0, new ModelResourceLocation(powerSuitChest.getRegistryName(), "inventory"));
//        ModelLoader.setCustomModelResourceLocation(powerSuitHelmet, 0, new ModelResourceLocation(powerSuitHelmet.getRegistryName(), "inventory"));
//        ModelLoader.setCustomModelResourceLocation(powerSuitLegs, 0, new ModelResourceLocation(powerSuitLegs.getRegistryName(), "inventory"));
//    }

}
