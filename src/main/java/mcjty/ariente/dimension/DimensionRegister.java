package mcjty.ariente.dimension;

import mcjty.ariente.Ariente;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

public class DimensionRegister {

    public static DimensionType dimensionType;

    public static void init() {
        registerDimensionTypes();
        registerDimensions();
    }

    private static void registerDimensionTypes() {
        dimensionType = DimensionType.register(Ariente.MODID, "_earth", 222/* @todo config*/, ArienteWorldProvider.class, false);
    }

    private static void registerDimensions() {
        DimensionManager.registerDimension(222, dimensionType);
    }

}
