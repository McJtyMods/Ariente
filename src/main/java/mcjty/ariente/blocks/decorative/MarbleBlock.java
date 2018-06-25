package mcjty.ariente.blocks.decorative;

import mcjty.ariente.blocks.BaseVariantBlock;
import net.minecraft.block.properties.PropertyEnum;

public class MarbleBlock extends BaseVariantBlock<MarbleColor> {

    public static final PropertyEnum<MarbleColor> COLOR = PropertyEnum.create("type", MarbleColor.class);

    public MarbleBlock(String name) {
        super(name);
    }

    @Override
    public PropertyEnum<MarbleColor> getProperty() {
        return COLOR;
    }

    @Override
    public MarbleColor[] getValues() {
        return MarbleColor.VALUES;
    }
}
