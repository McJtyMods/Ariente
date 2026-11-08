package mcjty.ariente.bindings;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public class Binding {

    private final String name;
    private final KeyMapping binding;
    private final Runnable callable;

    private static final List<Binding> BINDINGS = new ArrayList<>();

    private Binding(String name, int keyCode, Runnable callable) {
        this.name = name;
        this.callable = callable;
        binding = new KeyMapping(name, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, keyCode, "key.categories.ariente");
    }

    public static Binding create(String name, int keyCode, Runnable callable) {
        Binding binding = new Binding(name, keyCode, callable);
        BINDINGS.add(binding);
        return binding;
    }

    public static Binding createHeld(String name, int keyCode) {
        return create(name, keyCode, null);
    }

    public boolean isDown() {
        return binding.isDown();
    }

    public void release() {
        binding.setDown(false);
    }

    public static void register(RegisterKeyMappingsEvent event) {
        KeyBindings.init();
        BINDINGS.forEach(binding -> event.register(binding.binding));
    }

    public static void checkBindings() {
        for (Binding binding : BINDINGS) {
            if (binding.binding.consumeClick() && binding.callable != null) {
                binding.callable.run();
            }
        }
    }

    public String getDisplayName() {
        return binding.saveString();
    }

    public String getKeyName() {
        return binding.getTranslatedKeyMessage().getString();
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
