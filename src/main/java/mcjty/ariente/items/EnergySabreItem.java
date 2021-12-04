package mcjty.ariente.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.Attributes;
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
        super(new Properties().tab(Ariente.setup.getTab()).stacksTo(1).durability(ItemTier.DIAMOND.getUses()));
        this.tier = ItemTier.DIAMOND;
        this.attackDamage = 12.0F;
    }

    public float getAttackDamage() {
        return this.tier.getAttackDamageBonus();
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        Block block = state.getBlock();
        Material material = state.getMaterial();
        return material != Material.PLANT && material != Material.PLANT && material != Material.CORAL && material != Material.LEAVES && material != Material.VEGETABLE ? 1.0F : 1.5F;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, livingEntity -> {});
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (state.getDestroySpeed(worldIn, pos) != 0.0D) {
            stack.hurtAndBreak(2, entityLiving, livingEntity -> {});
        }
        return true;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState blockIn) {
        return false;
    }

//   @todo 1.14
//    @Override
//    public boolean isFull3D() {
//        return true;
//    }

    @Override
    public int getEnchantmentValue() {
        return this.tier.getEnchantmentValue();
    }

    public String getToolMaterialName() {
        return this.tier.toString();
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        ItemStack mat = new ItemStack(Registration.INGOT_LITHIUM.get());
        // @todo 1.14 oredict
//        if (!mat.isEmpty() && net.minecraftforge.oredict.OreDictionary.itemMatches(mat, repair, false)) {
//            return true;
//        }
        return super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = getOriginalAttributeModifiers(slot, stack);

        if (slot != EquipmentSlotType.MAINHAND) {
            return multimap;
        }

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = new ImmutableMultimap.Builder<Attribute, AttributeModifier>();
        builder.putAll(multimap)
            .put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", this.attackDamage, AttributeModifier.Operation.ADDITION))
            .put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4000000953674316D, AttributeModifier.Operation.ADDITION));

        return builder.build();
    }

    protected Multimap<Attribute, AttributeModifier> getOriginalAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        return super.getAttributeModifiers(slot, stack);
    }
}