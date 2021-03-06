package mcjty.ariente.blocks.decorative;

import mcjty.ariente.api.MarbleColor;
import mcjty.ariente.blocks.BaseVariantBlock;
import net.minecraft.block.properties.PropertyEnum;

public class MarbleBlock extends BaseVariantBlock<MarbleColor> {

    public MarbleBlock(String name) {
        super(name);
    }

    @Override
    public PropertyEnum<MarbleColor> getProperty() {
        return MarbleColor.COLOR;
    }

    @Override
    public MarbleColor[] getValues() {
        return MarbleColor.VALUES;
    }
}
