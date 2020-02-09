package mcjty.ariente.blocks.decorative;

import mcjty.ariente.api.MarbleType;
import mcjty.ariente.blocks.BaseVariantBlock;
import net.minecraft.state.EnumProperty;

public class MarbleTechBlock extends BaseVariantBlock<MarbleType> {

    @Override
    public EnumProperty<MarbleType> getProperty() {
        return MarbleType.TYPE;
    }

    @Override
    public MarbleType[] getValues() {
        return MarbleType.VALUES;
    }
}
