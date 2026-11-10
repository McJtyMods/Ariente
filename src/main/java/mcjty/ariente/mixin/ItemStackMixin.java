package mcjty.ariente.mixin;

import java.util.function.BiConsumer;
import mcjty.ariente.items.AttributeItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {

    @Inject(
            method =
                    "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void ariente$attributes(
            EquipmentSlot slot,
            BiConsumer<Holder<Attribute>, AttributeModifier> consumer,
            CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof AttributeItem item
                && !stack.getComponentsPatch().entrySet().stream()
                        .anyMatch(entry -> entry.getKey() == DataComponents.ATTRIBUTE_MODIFIERS)) {
            item.getDefaultAttributeModifiers(stack).forEach(slot, consumer);
            ci.cancel();
        }
    }

    @Inject(
            method =
                    "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void ariente$attributeTooltip(
            EquipmentSlotGroup group,
            BiConsumer<Holder<Attribute>, AttributeModifier> consumer,
            CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof AttributeItem item
                && !stack.getComponentsPatch().entrySet().stream()
                        .anyMatch(entry -> entry.getKey() == DataComponents.ATTRIBUTE_MODIFIERS)) {
            item.getDefaultAttributeModifiers(stack).forEach(group, consumer);
            ci.cancel();
        }
    }
}
