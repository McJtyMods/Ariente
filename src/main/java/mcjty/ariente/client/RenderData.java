package mcjty.ariente.client;

import java.util.IdentityHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.util.context.ContextKey;

public final class RenderData {

    private static final Map<ContextKey<?>, RenderStateDataKey<?>> KEYS = new IdentityHashMap<>();

    private RenderData() {
    }

    @SuppressWarnings("unchecked")
    private static synchronized <T> RenderStateDataKey<T> key(ContextKey<T> key) {
        return (RenderStateDataKey<T>) KEYS.computeIfAbsent(key, ignored -> RenderStateDataKey.create(key::toString));
    }

    public static <T> void set(Object state, ContextKey<T> key, T value) {
        ((FabricRenderState) state).setData(key(key), value);
    }

    public static <T> T get(Object state, ContextKey<T> key) {
        return ((FabricRenderState) state).getData(key(key));
    }
}
