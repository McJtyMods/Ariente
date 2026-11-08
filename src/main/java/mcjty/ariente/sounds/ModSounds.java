package mcjty.ariente.sounds;

import java.util.function.Supplier;
import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {

    public static final Supplier<SoundEvent> laser = Registration.SOUNDS.register("laser", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "laser")));
    public static final Supplier<SoundEvent> forcefield = Registration.SOUNDS.register("forcefield", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "forcefield")));
    public static final Supplier<SoundEvent> buzzError = Registration.SOUNDS.register("buzz_error", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "buzz_error")));
    public static final Supplier<SoundEvent> buzzOk = Registration.SOUNDS.register("buzz_ok", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "buzz_ok")));
    public static final Supplier<SoundEvent> alarm = Registration.SOUNDS.register("alarm", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "alarm")));
    public static final Supplier<SoundEvent> door = Registration.SOUNDS.register("door", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "door")));
    public static final Supplier<SoundEvent> levitator = Registration.SOUNDS.register("levitator", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "levitator")));

    public static void register() {
        // Initialize the sound entries before the deferred register is attached.
    }
}
