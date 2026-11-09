package mcjty.ariente.blocks.utility.wireless;

import mcjty.ariente.varia.SavedDataMigration;
import java.util.HashMap;
import java.util.Map;
import mcjty.ariente.api.IRedstoneChannels;
import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;

public class RedstoneChannels extends AbstractWorldData<RedstoneChannels> implements IRedstoneChannels {

    private static final String REDSTONE_CHANNELS_NAME = "ariente:redstone_channels";

    private int lastId = 0;

    private final Map<Integer,RedstoneChannel> channels = new HashMap<>();

    public RedstoneChannels() {
        super();
    }

    public static RedstoneChannels getChannels(Level world) {
        SavedDataMigration.migrateGlobal(world, REDSTONE_CHANNELS_NAME, "ArienteRedstoneChannels");
        return getData(world, RedstoneChannels::createChannels, () -> new RedstoneChannels(), REDSTONE_CHANNELS_NAME);
    }

    private static RedstoneChannels createChannels(CompoundTag tag) {
       RedstoneChannels channels = new RedstoneChannels();
       channels.load(tag);
       return channels;
    }

    public RedstoneChannel getOrCreateChannel(int id) {
        RedstoneChannel channel = channels.get(id);
        if (channel == null) {
            channel = new RedstoneChannel();
            channels.put(id, channel);
        }
        return channel;
    }

    public RedstoneChannel getChannel(int id) {
        return channels.get(id);
    }

    public void deleteChannel(int id) {
        channels.remove(id);
    }

    @Override
    public int newChannel() {
        lastId++;
        save();
        return lastId;
    }

    public void load(CompoundTag tagCompound) {
        channels.clear();
        ListTag lst = tagCompound.getListOrEmpty("channels");
        for (int i = 0 ; i < lst.size() ; i++) {
            CompoundTag tc = lst.getCompoundOrEmpty(i);
            int channel = tc.getIntOr("channel", 0);
            int v = tc.getIntOr("value", 0);

            RedstoneChannel value = new RedstoneChannel();
            value.value = v;
            channels.put(channel, value);
        }
        lastId = tagCompound.getIntOr("lastId", 0);
    }

    @Override
    public CompoundTag save(CompoundTag tagCompound, HolderLookup.Provider provider) {
        ListTag lst = new ListTag();
        for (Map.Entry<Integer, RedstoneChannel> entry : channels.entrySet()) {
            CompoundTag tc = new CompoundTag();
            tc.putInt("channel", entry.getKey());
            tc.putInt("value", entry.getValue().getValue());
            lst.add(tc);
        }
        tagCompound.put("channels", lst);
        tagCompound.putInt("lastId", lastId);
        return tagCompound;
    }

    public static class RedstoneChannel {
        private int value = 0;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
