package mcjty.ariente.api;

/** An entity whose innate regeneration can be suppressed by the Inhibit module. */
public interface IInhibitable {

    void inhibitRegeneration(int ticks);
}
