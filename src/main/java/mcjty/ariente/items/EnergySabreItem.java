package mcjty.ariente.items;

import com.google.common.collect.Multimap;
import mcjty.ariente.Ariente;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;



public class EnergySabreItem extends Item {

    protected final float attackDamage;
    private final ItemTier tier;

    public EnergySabreItem() {
        super(new Properties().group(Ariente.setup.getTab()).maxStackSize(1).maxDamage(ItemTier.DIAMOND.getMaxUses()));
        this.tier = ItemTier.DIAMOND;
        this.attackDamage = 12.0F;
    }

    public float getAttackDamage() {
        return this.tier.getAttackDamage();
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        Block block = state.getBlock();
        Material material = state.getMaterial();
        return material != Material.PLANTS && material != Material.PLANTS && material != Material.CORAL && material != Material.LEAVES && material != Material.GOURD ? 1.0F : 1.5F;
    }

    @Override
    public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damageItem(1, attacker, livingEntity -> {});
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (state.getBlockHardness(worldIn, pos) != 0.0D) {
            stack.damageItem(2, entityLiving, livingEntity -> {});
        }
        return true;
    }

    @Override
    public boolean canHarvestBlock(BlockState blockIn) {
        return false;
    }

//   @todo 1.14
//    @Override
//    public boolean isFull3D() {
//        return true;
//    }

    @Override
    public int getItemEnchantability() {
        return this.tier.getEnchantability();
    }

    public String getToolMaterialName() {
        return this.tier.toString();
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        ItemStack mat = new ItemStack(ModItems.lithiumIngot);
        // @todo 1.14 oredict
//        if (!mat.isEmpty() && net.minecraftforge.oredict.OreDictionary.itemMatches(mat, repair, false)) {
//            return true;
//        }
        return super.getIsRepairable(toRepair, repair);
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = getOriginalAttributeModifiers(slot, stack);

        if (slot == EquipmentSlotType.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", this.attackDamage, AttributeModifier.Operation.ADDITION));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, AttributeModifier.Operation.ADDITION));
        }

        return multimap;
    }

    protected Multimap<String, AttributeModifier> getOriginalAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        return super.getAttributeModifiers(slot, stack);
    }
}