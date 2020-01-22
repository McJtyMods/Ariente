package mcjty.ariente.sounds;

import mcjty.ariente.Ariente;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModSounds {

    public static final SoundEvent step = new SoundEvent(new ResourceLocation(Ariente.MODID, "step")).setRegistryName(new ResourceLocation(Ariente.MODID, "step"));
    public static final SoundEvent droneAmbient = new SoundEvent(new ResourceLocation(Ariente.MODID, "drone_ambient")).setRegistryName(new ResourceLocation(Ariente.MODID, "drone_ambient"));
    public static final SoundEvent droneHurt = new SoundEvent(new ResourceLocation(Ariente.MODID, "drone_hurt")).setRegistryName(new ResourceLocation(Ariente.MODID, "drone_hurt"));
    public static final SoundEvent droneDeath = new SoundEvent(new ResourceLocation(Ariente.MODID, "drone_death")).setRegistryName(new ResourceLocation(Ariente.MODID, "drone_death"));
    public static final SoundEvent sentinelDeath = new SoundEvent(new ResourceLocation(Ariente.MODID, "sentinel_death")).setRegistryName(new ResourceLocation(Ariente.MODID, "sentinel_death"));
    public static final SoundEvent soldierHurt = new SoundEvent(new ResourceLocation(Ariente.MODID, "soldier_hurt")).setRegistryName(new ResourceLocation(Ariente.MODID, "soldier_hurt"));
    public static final SoundEvent soldierDeath = new SoundEvent(new ResourceLocation(Ariente.MODID, "soldier_death")).setRegistryName(new ResourceLocation(Ariente.MODID, "soldier_death"));
    public static final SoundEvent bossSoldierHurt = new SoundEvent(new ResourceLocation(Ariente.MODID, "boss_soldier_hurt")).setRegistryName(new ResourceLocation(Ariente.MODID, "boss_soldier_hurt"));
    public static final SoundEvent bossSoldierDeath = new SoundEvent(new ResourceLocation(Ariente.MODID, "boss_soldier_death")).setRegistryName(new ResourceLocation(Ariente.MODID, "boss_soldier_death"));
    public static final SoundEvent laser = new SoundEvent(new ResourceLocation(Ariente.MODID, "laser")).setRegistryName(new ResourceLocation(Ariente.MODID, "laser"));

    public static final SoundEvent forcefield = new SoundEvent(new ResourceLocation(Ariente.MODID, "forcefield")).setRegistryName(new ResourceLocation(Ariente.MODID, "forcefield"));
    public static final SoundEvent buzzError = new SoundEvent(new ResourceLocation(Ariente.MODID, "buzz_error")).setRegistryName(new ResourceLocation(Ariente.MODID, "buzz_error"));
    public static final SoundEvent buzzOk = new SoundEvent(new ResourceLocation(Ariente.MODID, "buzz_ok")).setRegistryName(new ResourceLocation(Ariente.MODID, "buzz_ok"));
    public static final SoundEvent alarm = new SoundEvent(new ResourceLocation(Ariente.MODID, "alarm")).setRegistryName(new ResourceLocation(Ariente.MODID, "alarm"));
    public static final SoundEvent door = new SoundEvent(new ResourceLocation(Ariente.MODID, "door")).setRegistryName(new ResourceLocation(Ariente.MODID, "door"));

    public static final SoundEvent levitator = new SoundEvent(new ResourceLocation(Ariente.MODID, "levitator")).setRegistryName(new ResourceLocation(Ariente.MODID, "levitator"));

    public static void init(IForgeRegistry<SoundEvent> registry) {
        // @todo 1.14 use DeferredRegistry
        registry.register(step);
        registry.register(droneAmbient);
        registry.register(droneHurt);
        registry.register(droneDeath);
        registry.register(soldierHurt);
        registry.register(soldierDeath);
        registry.register(bossSoldierHurt);
        registry.register(bossSoldierDeath);
        registry.register(sentinelDeath);
        registry.register(laser);
        registry.register(forcefield);
        registry.register(buzzError);
        registry.register(buzzOk);
        registry.register(alarm);
        registry.register(door);
        registry.register(levitator);
    }

}
