package mcjty.ariente.gui;

import net.minecraft.world.World;

public class HoloGuiEntitySmall extends HoloGuiEntity {

    public HoloGuiEntitySmall(World worldIn) {
        super(worldIn);
        setSize(.75f, .75f);
    }

    @Override
    public boolean isSmall() {
        return true;
    }
}
