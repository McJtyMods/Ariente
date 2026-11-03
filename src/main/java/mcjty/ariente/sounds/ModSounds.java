package mcjty.ariente.sounds;

import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final RegistryObject<SoundEvent> laser = Registration.SOUNDS.register("laser", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Ariente.MODID, "laser")));
    public static final RegistryObject<SoundEvent> forcefield = Registration.SOUNDS.register("forcefield", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Ariente.MODID, "forcefield")));
    public static final RegistryObject<SoundEvent> buzzError = Registration.SOUNDS.register("buzz_error", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Ariente.MODID, "buzz_error")));
    public static final RegistryObject<SoundEvent> buzzOk = Registration.SOUNDS.register("buzz_ok", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Ariente.MODID, "buzz_ok")));
    public static final RegistryObject<SoundEvent> alarm = Registration.SOUNDS.register("alarm", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Ariente.MODID, "alarm")));
    public static final RegistryObject<SoundEvent> door = Registration.SOUNDS.register("door", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Ariente.MODID, "door")));
    public static final RegistryObject<SoundEvent> levitator = Registration.SOUNDS.register("levitator", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Ariente.MODID, "levitator")));

    public static void register() {
        // Initialize the sound entries before the deferred register is attached.
    }
}
