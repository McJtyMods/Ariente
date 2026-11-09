package mcjty.ariente.recipes;

import net.minecraft.network.codec.ByteBufCodecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class ConstructorRecipeSerializer  {

    private static final Codec<ItemStack> INPUT = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ItemStack::getItem),
            Codec.intRange(1, 99).optionalFieldOf("count", 1).forGetter(ItemStack::getCount)
    ).apply(instance, ItemStack::new));
    public static final MapCodec<ConstructorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(recipe -> recipe.getDestination().getItem()),
            Codec.intRange(1, 99).optionalFieldOf("count", 1).forGetter(recipe -> recipe.getDestination().getCount()),
            Codec.floatRange(0, 1).optionalFieldOf("chance", 1f).forGetter(ConstructorRecipe::getChance),
            INPUT.listOf().fieldOf("input").forGetter(ConstructorRecipe::getIngredientList)
    ).apply(instance, (item, count, chance, input) -> new ConstructorRecipe(new ItemStack(item, count), chance, input.toArray(ItemStack[]::new))));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConstructorRecipe> STREAM_CODEC = StreamCodec.of(
            (buffer, recipe) -> {
                buffer.writeIdentifier(recipe.getId());
                ItemStack.STREAM_CODEC.encode(buffer, recipe.getDestination());
                buffer.writeFloat(recipe.getChance());
                ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.getIngredientList());
            }, buffer -> new ConstructorRecipe(buffer.readIdentifier(), ItemStack.STREAM_CODEC.decode(buffer),
                    buffer.readFloat(), ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer).toArray(ItemStack[]::new)));

    public MapCodec<ConstructorRecipe> codec() {
        return CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, ConstructorRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
