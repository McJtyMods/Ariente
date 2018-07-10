package mcjty.ariente.proxy;

/**
 * Created by jorrit on 16.12.16.
 */
public class ServerProxy extends CommonProxy {

    @Override
    public boolean isJumpKeyDown() {
        throw new IllegalStateException("Only call this client-side!");
    }
}
