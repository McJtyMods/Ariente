package mcjty.ariente.items.armor;

import mcjty.ariente.Ariente;
import mcjty.ariente.items.modules.ArmorUpgradeType;
import mcjty.lib.McJtyRegister;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class PowerSuit extends ItemArmor {

    public PowerSuit(EntityEquipmentSlot slot) {
        super(ArmorMaterial.LEATHER, 0, slot);
        setRegistryName("powersuit_" + slot.getName());
        setUnlocalizedName(Ariente.MODID + ".powersuit_" + slot.getName());
        setCreativeTab(Ariente.creativeTab);
        McJtyRegister.registerLater(this, Ariente.instance);
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return Ariente.MODID + ":textures/armor/suit.png";
    }

    @Nullable
    @Override
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        return PowerSuitModel.getModel(entityLiving, itemStack);
    }

    public static boolean hasFullArmor(EntityPlayer player) {
        if (player == null) {
            return false;
        }
        if (!(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() instanceof PowerSuit)) {
            return false;
        }
        if (!(player.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() instanceof PowerSuit)) {
            return false;
        }
        if (!(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof PowerSuit)) {
            return false;
        }
        if (!(player.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() instanceof PowerSuit)) {
            return false;
        }
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag b) {
        super.addInformation(stack, world, list, b);
        list.add("Power suit part");
        if (stack.hasTagCompound()) {
            NBTTagCompound compound = stack.getTagCompound();
            for (ArmorUpgradeType type : ArmorUpgradeType.values()) {
                String key = "module_" + type.getName();
                if (compound.hasKey(key)) {
                    boolean activated = compound.getBoolean(key);
                    if (activated) {
                        list.add("    " + TextFormatting.GREEN + type.getDescription() + " (on)");
                    } else {
                        list.add("    " + TextFormatting.GRAY + type.getDescription() + " (off)");
                    }
                }
            }
        }
    }

}
