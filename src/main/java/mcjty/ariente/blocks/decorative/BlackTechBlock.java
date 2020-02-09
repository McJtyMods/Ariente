package mcjty.ariente.blocks.decorative;

import mcjty.ariente.api.TechType;
import mcjty.ariente.blocks.BaseVariantBlock;
import net.minecraft.state.EnumProperty;

public class BlackTechBlock extends BaseVariantBlock<TechType> {

    @Override
    public EnumProperty<TechType> getProperty() {
        return TechType.TYPE;
    }

    @Override
    public TechType[] getValues() {
        return TechType.VALUES;
    }
}
