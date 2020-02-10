package mcjty.ariente.datagen;

import mcjty.ariente.Ariente;
import mcjty.lib.datagen.BaseBlockStateProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class BlockStates extends BaseBlockStateProvider {

    public BlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, Ariente.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
    }
}
