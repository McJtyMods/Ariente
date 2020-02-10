package mcjty.ariente.datagen;

import mcjty.ariente.Ariente;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class Languages extends LanguageProvider {

    public Languages(DataGenerator gen, String locale) {
        super(gen, Ariente.MODID, locale);
    }

    @Override
    protected void addTranslations() {
    }
}
