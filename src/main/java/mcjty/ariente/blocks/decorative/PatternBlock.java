package mcjty.ariente.blocks.decorative;

import mcjty.ariente.blocks.BaseVariantBlock;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class PatternBlock extends BaseVariantBlock<PatternType> {

    public static final EnumProperty<PatternType> TYPE = EnumProperty.create("type", PatternType.class);

    @Override
    public EnumProperty<PatternType> getProperty() {
        return TYPE;
    }

    @Override
    public PatternType[] getValues() {
        return PatternType.VALUES;
    }
}
