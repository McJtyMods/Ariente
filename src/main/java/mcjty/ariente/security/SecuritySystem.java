package mcjty.ariente.security;

import mcjty.ariente.api.ISecuritySystem;
import mcjty.lib.varia.WorldTools;
import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SecuritySystem extends AbstractWorldData<SecuritySystem> implements ISecuritySystem {

    private static final String NAME = "ArienteSecurity";

    private long lastSecurityID = 1;

    public SecuritySystem(String name) {
        super(name);
    }

    public long newSecurityID() {
        long id = lastSecurityID;
        lastSecurityID++;
        save();
        return id;
    }

    @Override
    public String generateKeyId() {
        long id = newSecurityID();
        // @todo 1.14 avoid this!
        World world = WorldTools.getOverworld();
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
    public static SecuritySystem getSecuritySystem(World world) {
        return getData(world, () -> new SecuritySystem(NAME), NAME);
    }

    @Override
    public void read(CompoundNBT compound) {
        lastSecurityID = compound.getLong("lastSecurityID");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putLong("lastSecurityID", lastSecurityID);
        return compound;
    }
}
