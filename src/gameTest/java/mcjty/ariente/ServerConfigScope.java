package mcjty.ariente;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import mcjty.ariente.config.Config;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;

/** Keep temporary test settings away from autosave and asynchronous file reloads. */
final class ServerConfigScope implements AutoCloseable {

    private final CommentedConfig original;

    ServerConfigScope() {
        original = ConfigTracker.INSTANCE.configSets().get(ModConfig.Type.SERVER).stream()
                .filter(config -> config.getSpec() == Config.SERVER_CONFIG)
                .findFirst().orElseThrow().getConfigData();
        Config.SERVER_CONFIG.setConfig(copy(original));
    }

    private static CommentedConfig copy(UnmodifiableConfig source) {
        CommentedConfig result = CommentedConfig.copy(source);
        for (UnmodifiableConfig.Entry entry : source.entrySet()) {
            if (entry.getValue() instanceof UnmodifiableConfig child) {
                result.set(List.of(entry.getKey()), copy(child));
            }
        }
        return result;
    }

    @Override
    public void close() {
        Config.SERVER_CONFIG.setConfig(original);
    }
}
