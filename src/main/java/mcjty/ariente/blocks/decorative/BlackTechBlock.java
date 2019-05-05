package mcjty.ariente.blocks.decorative;

import mcjty.ariente.api.TechType;
import mcjty.ariente.blocks.BaseVariantBlock;
import net.minecraft.block.properties.PropertyEnum;

public class BlackTechBlock extends BaseVariantBlock<TechType> {

    public BlackTechBlock(String name) {
        super(name);
    }

    @Override
    public PropertyEnum<TechType> getProperty() {
        return TechType.TYPE;
    }

    @Override
    public TechType[] getValues() {
        return TechType.VALUES;
    }
}
