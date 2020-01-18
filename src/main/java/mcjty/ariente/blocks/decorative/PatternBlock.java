package mcjty.ariente.blocks.decorative;

import mcjty.ariente.blocks.BaseVariantBlock;
import net.minecraft.state.EnumProperty;

public class PatternBlock extends BaseVariantBlock<PatternType> {

    public static final EnumProperty<PatternType> TYPE = EnumProperty.create("type", PatternType.class);

    public PatternBlock(String name) {
        super(name);
    }

    @Override
    public EnumProperty<PatternType> getProperty() {
        return TYPE;
    }

    @Override
    public PatternType[] getValues() {
        return PatternType.VALUES;
    }
}
