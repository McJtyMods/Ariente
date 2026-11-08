package mcjty.ariente.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mcjty.ariente.Ariente;
import mcjty.ariente.config.WorldgenConfiguration;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.setup.Registration;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class DungeonLootModifier extends LootModifier {

    public static final MapCodec<DungeonLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance)
            .and(Codec.BOOL.optionalFieldOf("standalone_only", false).forGetter(modifier -> modifier.standaloneOnly))
            .apply(instance, DungeonLootModifier::new));

    private final boolean standaloneOnly;

    public DungeonLootModifier(LootItemCondition[] conditions) {
        this(conditions, false);
    }

    public DungeonLootModifier(LootItemCondition[] conditions, boolean standaloneOnly) {
        super(conditions);
        this.standaloneOnly = standaloneOnly;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        // Server config is read when loot is rolled, never while tables are parsed.
        if (!WorldgenConfiguration.doDungeonLoot() || standaloneOnly && Ariente.setup.arienteWorld) {
            return loot;
        }
        boolean standalone = !Ariente.setup.arienteWorld;
        double blueprints = WorldgenConfiguration.OVERWORLD_LOOT_BLUEPRINTS.get()
                * (standalone ? (double) WorldgenConfiguration.STANDALONE_BLUEPRINT_MULTIPLIER.get() : 1.0);
        int results = WorldgenConfiguration.OVERWORLD_LOOT_ITEMS.get();
        int suit = WorldgenConfiguration.OVERWORLD_LOOT_SUIT.get();
        RandomSource random = context.getRandom();
        double roll = random.nextDouble() * (100.0 + blueprints + results + 6.0 * suit);
        if (roll < blueprints + (double) results) {
            if (standalone && roll < blueprints) {
                StandaloneBlueprintLoot.addBlueprints(loot, random, WorldgenConfiguration.STANDALONE_BLUEPRINT_COUNT.get());
            } else {
                ConstructorRecipe recipe = BlueprintRecipeRegistry.getRandomRecipe(random, roll < blueprints);
                loot.add(roll < blueprints ? BlueprintItem.makeBluePrint(recipe.getDestination()) : recipe.getDestination().copy());
            }
        } else if (roll < blueprints + (double) results + 6.0 * suit) {
            Item[] items = {Registration.POWERSUIT_CHEST.get(), Registration.POWERSUIT_FEET.get(), Registration.POWERSUIT_LEGS.get(),
                    Registration.POWERSUIT_HEAD.get(), Registration.ENERGY_SABRE.get(), Registration.ENHANCED_ENERGY_SABRE.get()};
            loot.add(new ItemStack(items[random.nextInt(items.length)]));
        }
        return loot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
