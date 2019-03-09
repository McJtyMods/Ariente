package mcjty.ariente.dimension;

import mcjty.ariente.Ariente;
import mcjty.ariente.config.WorldgenConfiguration;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

public class DimensionRegister {

    public static DimensionType dimensionType;

    public static void init() {
        registerDimensionTypes();
        registerDimensions();
    }

    private static void registerDimensionTypes() {
        dimensionType = DimensionType.register(Ariente.MODID, "_earth", WorldgenConfiguration.DIMENSION_ID.get(), ArienteWorldProvider.class, false);
    }

    private static void registerDimensions() {
        DimensionManager.registerDimension(WorldgenConfiguration.DIMENSION_ID.get(), dimensionType);
    }

}
