package mcjty.ariente.api;

public interface ISignalChannel {
    int getChannel(boolean initialize);

    void setChannel(int channel);

    int getDesiredChannel();

    void setDesiredChannel(int desiredChannel);
}
