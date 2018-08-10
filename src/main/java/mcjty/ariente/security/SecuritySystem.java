package mcjty.ariente.security;

import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SecuritySystem extends AbstractWorldData<SecuritySystem> {

    private static final String NAME = "ArienteSecurity";

    private long lastSecurityID = 1;

    public SecuritySystem(String name) {
        super(name);
    }

    @Override
    public void clear() {
        lastSecurityID = 1;
    }

    public long newSecurityID() {
        long id = lastSecurityID;
        lastSecurityID++;
        save();
        return id;
    }

    public String generateKeyId() {
        long id = newSecurityID();
        World world = DimensionManager.getWorld(0);
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
        return getData(world, SecuritySystem.class, NAME);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        lastSecurityID = compound.getLong("lastSecurityID");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setLong("lastSecurityID", lastSecurityID);
        return compound;
    }
}
