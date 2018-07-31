package mcjty.ariente.blocks.decorative;

import mcjty.ariente.blocks.BaseVariantBlock;
import net.minecraft.block.properties.PropertyEnum;

public class PatternBlock extends BaseVariantBlock<PatternType> {

    public static final PropertyEnum<PatternType> TYPE = PropertyEnum.create("type", PatternType.class);

    public PatternBlock(String name) {
        super(name);
    }

    @Override
    public PropertyEnum<PatternType> getProperty() {
        return TYPE;
    }

    @Override
    public PatternType[] getValues() {
        return PatternType.VALUES;
    }
}
