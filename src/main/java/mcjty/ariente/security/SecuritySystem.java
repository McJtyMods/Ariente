package mcjty.ariente.security;

import mcjty.ariente.varia.SavedDataMigration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import mcjty.ariente.api.ISecuritySystem;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class SecuritySystem extends AbstractWorldData<SecuritySystem> implements ISecuritySystem {

    private static final String NAME = "ariente:security";

    private long lastSecurityID = 1;

    public SecuritySystem() {
        super();
    }

    public long newSecurityID() {
        long id = lastSecurityID;
        lastSecurityID++;
        save();
        return id;
    }

    @Override
    public String generateKeyId(Level w) {
        long id = newSecurityID();
        ServerLevel world = LevelTools.getOverworld(w);
        Random rnd = new Random(world.getSeed() + 234516783139L);       // A fixed seed for this work
        rnd.nextFloat();
        rnd.nextFloat();
        String keyChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        List<Character> keys = new ArrayList<>();
        for (int i = 0 ; i < keyChars.length() ; i++) {
            keys.add(keyChars.charAt(i));
        }

        // The shuffles will always be the same way for this world. The code we get from it depends
        // on the number of this city
        StringBuilder builder = new StringBuilder();
        Collections.shuffle(keys, rnd);
        builder.append(keys.get((int) (id % keyChars.length()))); id /= keyChars.length();
        Collections.shuffle(keys, rnd);
        builder.append(keys.get((int) (id % keyChars.length()))); id /= keyChars.length();
        Collections.shuffle(keys, rnd);
        builder.append(keys.get((int) (id % keyChars.length()))); id /= keyChars.length();
        Collections.shuffle(keys, rnd);
        builder.append(keys.get((int) (id % keyChars.length()))); id /= keyChars.length();
        Collections.shuffle(keys, rnd);
        builder.append(keys.get((int) (id % keyChars.length()))); id /= keyChars.length();
        Collections.shuffle(keys, rnd);
        builder.append(keys.get((int) (id % keyChars.length()))); id /= keyChars.length();
        return builder.toString();
    }

    @Nonnull
    public static SecuritySystem getSecuritySystem(Level world) {
        SavedDataMigration.migrateGlobal(world, NAME, "ArienteSecurity");
        return getData(world, SecuritySystem::createSecuritySystem, () -> new SecuritySystem(), NAME);
    }

    private static SecuritySystem createSecuritySystem(CompoundTag tag) {
        SecuritySystem system = new SecuritySystem();
        system.load(tag);
        return system;
    }

    public void load(CompoundTag compound) {
        lastSecurityID = compound.getLongOr("lastSecurityID", 0);
    }

    @Override
    public CompoundTag save(CompoundTag compound, HolderLookup.Provider provider) {
        compound.putLong("lastSecurityID", lastSecurityID);
        return compound;
    }
}
