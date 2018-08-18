package mcjty.ariente.bindings;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class KeyBindings {

    public static KeyBinding fullHealth;

    public static void init() {
        fullHealth = new KeyBinding("key.fullhealth", KeyConflictContext.IN_GAME, Keyboard.KEY_O, "key.categories.ariente");
        ClientRegistry.registerKeyBinding(fullHealth);
    }
}
