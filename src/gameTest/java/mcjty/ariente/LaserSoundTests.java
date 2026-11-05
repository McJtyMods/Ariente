package mcjty.ariente;

import mcjty.ariente.entities.LaserEntity;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.ModSounds;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class LaserSoundTests {

    @GameTest(template = "empty")
    public static void eachProjectilePlaysItsFiringSoundOnce(GameTestHelper helper) {
        AtomicInteger sounds = new AtomicInteger();
        Consumer<PlayLevelSoundEvent.AtPosition> listener = event -> {
            if (event.getLevel() == helper.getLevel() && event.getSound() != null
                    && event.getSound().value() == ModSounds.laser.get()) {
                sounds.incrementAndGet();
            }
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayLevelSoundEvent.AtPosition.class, listener);
        try {
            for (int shot = 1; shot <= 2; shot++) {
                LaserEntity laser = Registration.ENTITY_LASER.get().create(helper.getLevel());
                laser.setPos(helper.absoluteVec(new Vec3(1.5, 2, 1.5)));
                try {
                    laser.baseTick();
                    helper.assertTrue(sounds.get() == shot, "Each new laser must play its firing sound immediately");
                    for (int tick = 1; tick < 100; tick++) {
                        laser.baseTick();
                    }
                    helper.assertTrue(sounds.get() == shot,
                            "A laser must not replay its firing sound after the old 40- and 80-tick repeat points");
                } finally {
                    laser.discard();
                }
            }
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        helper.succeed();
    }
}
