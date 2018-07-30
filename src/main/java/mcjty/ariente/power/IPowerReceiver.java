package mcjty.ariente.power;

public interface IPowerReceiver {

    boolean accepts(PowerType powerType);

    void send(PowerType type, long amount);
}
