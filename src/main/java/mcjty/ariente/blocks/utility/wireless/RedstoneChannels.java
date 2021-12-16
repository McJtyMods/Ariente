package mcjty.ariente.blocks.utility.wireless;

import mcjty.ariente.api.IRedstoneChannels;
import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class RedstoneChannels extends AbstractWorldData<RedstoneChannels> implements IRedstoneChannels {

    private static final String REDSTONE_CHANNELS_NAME = "ArienteRedstoneChannels";

    private int lastId = 0;

    private final Map<Integer,RedstoneChannel> channels = new HashMap<>();

    public RedstoneChannels(String name) {
        super(name);
    }

    public static RedstoneChannels getChannels(Level world) {
        return getData(world, () -> new RedstoneChannels(REDSTONE_CHANNELS_NAME), REDSTONE_CHANNELS_NAME);
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

    @Override
    public void load(CompoundTag tagCompound) {
        channels.clear();
        ListNBT lst = tagCompound.getList("channels", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.size() ; i++) {
            CompoundTag tc = lst.getCompound(i);
            int channel = tc.getInt("channel");
            int v = tc.getInt("value");

            RedstoneChannel value = new RedstoneChannel();
            value.value = v;
            channels.put(channel, value);
        }
        lastId = tagCompound.getInt("lastId");
    }

    @Override
    public CompoundTag save(CompoundTag tagCompound) {
        ListNBT lst = new ListNBT();
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
