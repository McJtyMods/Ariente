package mcjty.ariente;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/** Synthetic third-party content, loaded only by opt-in game-test runs. */
@Mod.EventBusSubscriber(modid = Ariente.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PurifierTestContent {

    public static final ResourceLocation ORE = new ResourceLocation("purifier_test", "ore");
    public static final ResourceLocation BLOCK_TAG_ORE = new ResourceLocation("purifier_test", "block_tag_ore");
    public static final ResourceLocation OVERRIDE_ORE = new ResourceLocation("purifier_test", "override_ore");
    public static final ResourceLocation AMBIGUOUS_ORE = new ResourceLocation("purifier_test", "ambiguous_ore");
    public static final ResourceLocation RAW = new ResourceLocation("purifier_test", "raw_metal");
    public static final ResourceLocation ALTERNATIVE_RAW = new ResourceLocation("purifier_alternative_test", "raw_metal");
    public static final ResourceLocation OTHER_RAW = new ResourceLocation("purifier_test", "raw_other_metal");

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
            for (ResourceLocation id : new ResourceLocation[]{ORE, BLOCK_TAG_ORE, OVERRIDE_ORE, AMBIGUOUS_ORE}) {
                helper.register(id, new Block(Block.Properties.of().strength(2)));
            }
        });
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            for (ResourceLocation id : new ResourceLocation[]{ORE, BLOCK_TAG_ORE, OVERRIDE_ORE, AMBIGUOUS_ORE}) {
                helper.register(id, new BlockItem(ForgeRegistries.BLOCKS.getValue(id), new Item.Properties()));
            }
            for (ResourceLocation id : new ResourceLocation[]{RAW, ALTERNATIVE_RAW, OTHER_RAW}) {
                helper.register(id, new Item(new Item.Properties()));
            }
        });
    }
}
