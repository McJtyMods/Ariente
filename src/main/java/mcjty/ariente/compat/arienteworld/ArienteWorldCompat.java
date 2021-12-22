package mcjty.ariente.compat.arienteworld;

import mcjty.ariente.api.IArienteWorld;
import mcjty.ariente.api.ICityAISystem;
import mcjty.lib.varia.Logging;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.InterModComms;

import javax.annotation.Nullable;
import java.util.function.Function;

public class ArienteWorldCompat {

    public static void register() {
        InterModComms.sendTo("arienteworld", "getArienteWorld", ArienteWorldCompat.GetArienteWorld::new);
    }

    public static ICityAISystem getCityAISystem(Level world) {
        return getArienteWorld().getCityAISystem(world);
    }

    public static IArienteWorld getArienteWorld() {
        return GetArienteWorld.arienteWorld;
    }

    public static class GetArienteWorld implements Function<IArienteWorld, Void> {

        public static IArienteWorld arienteWorld;

        @Nullable
        @Override
        public Void apply(IArienteWorld arienteWorld) {
            GetArienteWorld.arienteWorld = arienteWorld;
            Logging.log("Enabled support for Ariente World");
            return null;
        }
    }
}
