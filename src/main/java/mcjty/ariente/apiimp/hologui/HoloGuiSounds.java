package mcjty.ariente.apiimp.hologui;

import mcjty.ariente.Ariente;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class HoloGuiSounds {

    public static final SoundEvent guiclick = new SoundEvent(new ResourceLocation(Ariente.MODID, "guiclick")).setRegistryName(new ResourceLocation(Ariente.MODID, "guiclick"));
    public static final SoundEvent guiopen = new SoundEvent(new ResourceLocation(Ariente.MODID, "guiopen")).setRegistryName(new ResourceLocation(Ariente.MODID, "guiopen"));

    public static void init(IForgeRegistry<SoundEvent> registry) {
        registry.register(guiclick);
        registry.register(guiopen);
    }
}
