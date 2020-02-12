package mcjty.ariente.datagen;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.TechType;
import mcjty.ariente.setup.Registration;
import mcjty.lib.datagen.BaseItemModelProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.BlockItem;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.fml.RegistryObject;

import java.util.Map;

public class Items extends BaseItemModelProvider {

    public Items(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Ariente.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        parentedItem(Registration.AICORE_ITEM.get(), "block/aicore");
        parentedItem(Registration.ALARM_ITEM.get(), "block/machines/alarm");
        parentedItem(Registration.AUTO_CONSTRUCTOR_ITEM.get(), "block/machines/auto_constructor");
        parentedItem(Registration.AUTOMATION_FIELD_ITEM.get(), "block/machines/automation_field");
        for (Map.Entry<TechType, RegistryObject<BlockItem>> entry : Registration.BLACK_TECH_ITEMS.entrySet()) {
            parentedItem(entry.getValue().get(), "block/decoration/blacktech_" + entry.getKey().getName());
        }
    }

    @Override
    public String getName() {
        return "Ariente Item Models";
    }
}
