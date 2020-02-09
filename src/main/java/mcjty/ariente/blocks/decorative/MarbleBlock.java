package mcjty.ariente.blocks.decorative;

import mcjty.ariente.api.MarbleColor;
import mcjty.ariente.blocks.BaseVariantBlock;
import net.minecraft.state.EnumProperty;

public class MarbleBlock extends BaseVariantBlock<MarbleColor> {

    @Override
    public EnumProperty<MarbleColor> getProperty() {
        return MarbleColor.COLOR;
    }

    @Override
    public MarbleColor[] getValues() {
        return MarbleColor.VALUES;
    }
}
