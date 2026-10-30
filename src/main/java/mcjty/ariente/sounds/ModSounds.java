package mcjty.ariente.sounds;

import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final RegistryObject<SoundEvent> step = Registration.SOUNDS.register("step", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Ariente.MODID, "step")));
    public static final RegistryObject<SoundEvent> droneAmbient = Registration.SOUNDS.register("drone_ambient", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Ariente.MODID, "drone_ambient")));
    public static final RegistryObject<SoundEvent> droneHurt = Registration.SOUNDS.register("drone_hurt", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Ariente.MODID, "drone_hurt")));
    public static final RegistryObject<SoundEvent> droneDeath = Registration.SOUNDS.register("drone_death", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Ariente.MODID, "drone_death")));
    public static final RegistryObject<SoundEvent> sentinelDeath = Registration.SOUNDS.register("sentinel_death", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Ariente.MODID, "sentinel_death")));
    public static final RegistryObject<SoundEvent> soldierHurt = Registration.SOUNDS.register("soldier_hurt", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Ariente.MODID, "soldier_hurt")));
    public static final RegistryObject<SoundEvent> soldierDeath = Registration.SOUNDS.register("soldier_death", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Ariente.MODID, "soldier_death")));
    public static final RegistryObject<SoundEvent> bossSoldierHurt = Registration.SOUNDS.register("boss_soldier_hurt", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Ariente.MODID, "boss_soldier_hurt")));
    public static final RegistryObject<SoundEvent> bossSoldierDeath = Registration.SOUNDS.register("boss_soldier_death", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Ariente.MODID, "boss_soldier_death")));
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
