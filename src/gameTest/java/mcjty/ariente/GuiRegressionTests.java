package mcjty.ariente;

import com.google.gson.JsonObject;
import mcjty.ariente.gui.NumberFormatting;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class GuiRegressionTests {

    @GameTest(template = "empty")
    public static void registeredObjectsHaveEnglishNames(GameTestHelper helper) throws Exception {
        try (var stream = GuiRegressionTests.class.getResourceAsStream("/assets/ariente/lang/en_us.json")) {
            JsonObject names = GsonHelper.parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
            ForgeRegistries.BLOCKS.getEntries().stream().filter(entry -> entry.getKey().location().getNamespace().equals(Ariente.MODID))
                    .forEach(entry -> helper.assertTrue(names.has(entry.getValue().getDescriptionId()), "Missing name for " + entry.getKey()));
            ForgeRegistries.ITEMS.getEntries().stream().filter(entry -> entry.getKey().location().getNamespace().equals(Ariente.MODID))
                    .forEach(entry -> helper.assertTrue(names.has(entry.getValue().getDescriptionId()), "Missing name for " + entry.getKey()));
            ForgeRegistries.ENTITY_TYPES.getEntries().stream().filter(entry -> entry.getKey().location().getNamespace().equals(Ariente.MODID))
                    .forEach(entry -> helper.assertTrue(names.has(entry.getValue().getDescriptionId()), "Missing name for " + entry.getKey()));
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void largeAmountsStayCompact(GameTestHelper helper) {
        long[] values = {0, 999, 1000, 10_000, 999_999, 1_000_000, Integer.MAX_VALUE, 2_147_483_647_000L, Long.MAX_VALUE, Long.MIN_VALUE};
        String[] expected = {"0", "999", "1k", "10k", "999k", "1M", "2.1G", "2.1T", "9.2E", "-9.2E"};
        for (int i = 0; i < values.length; i++) {
            helper.assertTrue(NumberFormatting.compact(values[i]).equals(expected[i]), "Incorrect compact value for " + values[i]);
        }
        helper.succeed();
    }
}
