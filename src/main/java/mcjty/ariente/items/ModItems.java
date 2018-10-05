package mcjty.ariente.items;

import mcjty.ariente.items.armor.PowerSuit;
import mcjty.ariente.items.modules.ArmorModuleItem;
import mcjty.ariente.items.modules.ArmorUpgradeType;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class ModItems {

    public static GenericItem platinumIngot;
    public static GenericItem lithiumIngot;
    public static GenericItem manganeseIngot;
    public static GenericItem silverIngot;

    public static GenericItem negariteDust;
    public static GenericItem posiriteDust;

    public static EnergySabreItem energySabre;
    public static EnhancedEnergySabreItem enhancedEnergySabreItem;
    public static KeyCardItem keyCardItem;
    public static BlueprintItem blueprintItem;
    public static DirtyDiamondItem dirtyDiamondItem;
    public static FluxLevitatorItem fluxLevitatorItem;

    public static PowerSuit powerSuitHelmet;
    public static PowerSuit powerSuitChest;
    public static PowerSuit powerSuitLegs;
    public static PowerSuit powerSuitBoots;

    public static ArmorModuleItem moduleArmor;
    public static ArmorModuleItem moduleEnergy;
    public static ArmorModuleItem moduleFeatherFalling;
    public static ArmorModuleItem moduleFlight;
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
        platinumIngot = new GenericItem("ingot_platinum");
        lithiumIngot = new GenericItem("ingot_lithium");
        manganeseIngot = new GenericItem("ingot_manganese");
        silverIngot = new GenericItem("ingot_silver");

        negariteDust = new GenericItem("dust_negarite");
        posiriteDust = new GenericItem("dust_posirite");

        energySabre = new EnergySabreItem("energy_sabre");
        enhancedEnergySabreItem = new EnhancedEnergySabreItem("enhanced_energy_sabre");
        keyCardItem = new KeyCardItem();
        blueprintItem = new BlueprintItem();
        fluxLevitatorItem = new FluxLevitatorItem();

        dirtyDiamondItem = new DirtyDiamondItem();

        powerSuitBoots = new PowerSuit(EntityEquipmentSlot.FEET);
        powerSuitChest = new PowerSuit(EntityEquipmentSlot.CHEST);
        powerSuitHelmet = new PowerSuit(EntityEquipmentSlot.HEAD);
        powerSuitLegs = new PowerSuit(EntityEquipmentSlot.LEGS);

        moduleArmor = new ArmorModuleItem("module_armor", ArmorUpgradeType.ARMOR);
        moduleEnergy = new ArmorModuleItem("module_energy", ArmorUpgradeType.ENERGY);
        moduleFeatherFalling = new ArmorModuleItem("module_featherfalling", ArmorUpgradeType.FEATHERFALLING);
        moduleFlight = new ArmorModuleItem("module_flight", ArmorUpgradeType.FLIGHT);
        moduleForcefield = new ArmorModuleItem("module_forcefield", ArmorUpgradeType.FORCEFIELD);
        moduleInvisibility = new ArmorModuleItem("module_invisibility", ArmorUpgradeType.INVISIBILITY);
        moduleNightvision = new ArmorModuleItem("module_nightvision", ArmorUpgradeType.NIGHTVISION);
        moduleRegeneration = new ArmorModuleItem("module_regeneration", ArmorUpgradeType.REGENERATION);
        moduleScramble = new ArmorModuleItem("module_scramble", ArmorUpgradeType.SCRAMBLE);
        moduleAutofeed = new ArmorModuleItem("module_autofeed", ArmorUpgradeType.AUTOFEED);
        moduleSpeed = new ArmorModuleItem("module_speed", ArmorUpgradeType.SPEED);
        moduleStepassist = new ArmorModuleItem("module_stepassist", ArmorUpgradeType.STEPASSIST);

        moduleInhibit = new ArmorModuleItem("module_inhibit", ArmorUpgradeType.INHIBIT);
        modulePower = new ArmorModuleItem("module_power", ArmorUpgradeType.POWER);
        moduleLooting = new ArmorModuleItem("module_looting", ArmorUpgradeType.LOOTING);
        moduleFire = new ArmorModuleItem("module_fire", ArmorUpgradeType.FIRE);
    }

    public static void initOreDict() {
        OreDictionary.registerOre("ingotSilver", silverIngot);
        OreDictionary.registerOre("ingotPlatinum", platinumIngot);
        OreDictionary.registerOre("ingotManganese", manganeseIngot);
        OreDictionary.registerOre("ingotLithium", lithiumIngot);
    }

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        platinumIngot.initModel();
        lithiumIngot.initModel();
        manganeseIngot.initModel();
        silverIngot.initModel();
        negariteDust.initModel();
        posiriteDust.initModel();

        energySabre.initModel();
        enhancedEnergySabreItem.initModel();
        keyCardItem.initModel();
        fluxLevitatorItem.initModel();
        blueprintItem.initModel();

        dirtyDiamondItem.initModel();

        moduleArmor.initModel();
        moduleEnergy.initModel();
        moduleFeatherFalling.initModel();
        moduleFlight.initModel();
        moduleForcefield.initModel();
        moduleInvisibility.initModel();
        moduleNightvision.initModel();
        moduleRegeneration.initModel();
        moduleScramble.initModel();
        moduleAutofeed.initModel();
        moduleSpeed.initModel();
        moduleStepassist.initModel();

        moduleInhibit.initModel();
        modulePower.initModel();
        moduleLooting.initModel();
        moduleFire.initModel();

        ModelLoader.setCustomModelResourceLocation(powerSuitBoots, 0, new ModelResourceLocation(powerSuitBoots.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(powerSuitChest, 0, new ModelResourceLocation(powerSuitChest.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(powerSuitHelmet, 0, new ModelResourceLocation(powerSuitHelmet.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(powerSuitLegs, 0, new ModelResourceLocation(powerSuitLegs.getRegistryName(), "inventory"));
    }

}
