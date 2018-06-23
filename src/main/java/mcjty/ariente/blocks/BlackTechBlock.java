package mcjty.ariente.blocks;

import net.minecraft.block.properties.PropertyEnum;

public class BlackTechBlock extends BaseVariantBlock<TechType> {

    public static final PropertyEnum<TechType> TYPE = PropertyEnum.create("type", TechType.class);

    public BlackTechBlock(String name) {
        super(name);
    }

    @Override
    public PropertyEnum<TechType> getProperty() {
        return TYPE;
    }

    @Override
    public TechType[] getValues() {
        return TechType.VALUES;
    }
}
