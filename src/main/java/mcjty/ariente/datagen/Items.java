package mcjty.ariente.datagen;

import mcjty.ariente.Ariente;
import mcjty.lib.datagen.BaseItemModelProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class Items extends BaseItemModelProvider {

    public Items(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Ariente.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
    }

    @Override
    public String getName() {
        return "Ariente Item Models";
    }
}
