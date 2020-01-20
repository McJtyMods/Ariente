package mcjty.ariente.compat.arienteworld;

import mcjty.ariente.api.IArienteWorld;
import mcjty.ariente.api.ICityAISystem;
import mcjty.lib.varia.Logging;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ArienteWorldCompat {

    public static void register() {
// @todo 1.14
        //        InterModComms.sendTo("arienteworld", "getArienteWorld", () -> {
//
//        })
//        FMLInterModComms.sendFunctionMessage("arienteworld", "getArienteWorld", "mcjty.ariente.compat.arienteworld.ArienteWorldCompat$GetArienteWorld");
    }

    public static ICityAISystem getCityAISystem(World world) {
        return getArienteWorld().getCityAISystem(world);
    }

    public static IArienteWorld getArienteWorld() {
        return GetArienteWorld.arienteWorld;
    }

    public static class GetArienteWorld implements com.google.common.base.Function<IArienteWorld, Void> {

        public static IArienteWorld arienteWorld;

        @Nullable
        @Override
        public Void apply(IArienteWorld theOneProbe) {
            arienteWorld = theOneProbe;
            Logging.log("Enabled support for Ariente World");
            return null;
        }
    }
}
