package mcjty.ariente;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModSounds {

    public static final SoundEvent guiclick = new SoundEvent(new ResourceLocation(Ariente.MODID, "guiclick")).setRegistryName(new ResourceLocation(Ariente.MODID, "guiclick"));
    public static final SoundEvent guiopen = new SoundEvent(new ResourceLocation(Ariente.MODID, "guiopen")).setRegistryName(new ResourceLocation(Ariente.MODID, "guiopen"));

    public static final SoundEvent droneAmbient = new SoundEvent(new ResourceLocation(Ariente.MODID, "drone_ambient")).setRegistryName(new ResourceLocation(Ariente.MODID, "drone_ambient"));
    public static final SoundEvent droneHurt = new SoundEvent(new ResourceLocation(Ariente.MODID, "drone_hurt")).setRegistryName(new ResourceLocation(Ariente.MODID, "drone_hurt"));
    public static final SoundEvent droneDeath = new SoundEvent(new ResourceLocation(Ariente.MODID, "drone_death")).setRegistryName(new ResourceLocation(Ariente.MODID, "drone_death"));
    public static final SoundEvent droneShoot = new SoundEvent(new ResourceLocation(Ariente.MODID, "drone_shoot")).setRegistryName(new ResourceLocation(Ariente.MODID, "drone_shoot"));

    public static void init(IForgeRegistry<SoundEvent> registry) {
        registry.register(guiclick);
        registry.register(guiopen);
        registry.register(droneAmbient);
        registry.register(droneHurt);
        registry.register(droneDeath);
        registry.register(droneShoot);
    }

}
