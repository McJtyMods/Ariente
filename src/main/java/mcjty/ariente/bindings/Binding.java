package mcjty.ariente.bindings;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Binding {

    private final String name;
    private final KeyBinding binding;
    private final Runnable callable;

    private static final List<Binding> BINDINGS = new ArrayList<>();

    private Binding(String name, int keyCode, Runnable callable) {
        this.name = name;
        this.callable = callable;
        binding = null; // @todo 1.14 new KeyBinding(name, KeyConflictContext.IN_GAME, keyCode, "key.categories.ariente");
        ClientRegistry.registerKeyBinding(binding);
    }

    public static Binding create(String name, int keyCode, Runnable callable) {
        Binding binding = new Binding(name, keyCode, callable);
        BINDINGS.add(binding);
        return binding;
    }

    public static void checkBindings() {
        for (Binding binding : BINDINGS) {
            if (binding.binding.isPressed()) {
                binding.callable.run();
            }
        }
    }

    public String getDisplayName() {
        return binding.getLocalizedName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Binding binding = (Binding) o;
        return Objects.equals(name, binding.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
