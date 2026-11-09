package mcjty.ariente.varia;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/** Imports pre-26 saved-data files without replacing modern data or removing the original. */
public final class SavedDataMigration {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ServerLevel, Set<Identifier>> CHECKED = new WeakHashMap<>();

    private SavedDataMigration() {
    }

    public static void migrateGlobal(Level level, String id, String legacyName) {
        if (level instanceof ServerLevel serverLevel) {
            migrate(serverLevel.getServer().overworld(), Identifier.parse(id), legacyName);
        }
    }

    public static synchronized void migrate(ServerLevel level, Identifier id, String legacyName) {
        Set<Identifier> checked = CHECKED.computeIfAbsent(level, ignored -> new HashSet<>());
        if (checked.contains(id)) {
            return;
        }
        Path directory = DimensionType.getStorageFolder(level.dimension(), level.getServer().getWorldPath(LevelResource.ROOT)).resolve("data");
        Path legacy = directory.resolve(legacyName + ".dat");
        Path destination = id.withSuffix(".dat").resolveAgainst(directory);
        if (!Files.exists(destination) && Files.isRegularFile(legacy)) {
            try {
                Files.createDirectories(destination.getParent());
                Path temporary = Files.createTempFile(destination.getParent(), "ariente-migration-", ".tmp");
                try {
                    Files.copy(legacy, temporary, StandardCopyOption.REPLACE_EXISTING);
                    try {
                        Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE);
                    } catch (AtomicMoveNotSupportedException ignored) {
                        Files.move(temporary, destination);
                    }
                } finally {
                    Files.deleteIfExists(temporary);
                }
                LOGGER.info("Imported legacy Ariente saved data {} as {}", legacyName, id);
            } catch (IOException exception) {
                throw new IllegalStateException("Cannot migrate Ariente saved data " + legacy + " to " + destination, exception);
            }
        }
        checked.add(id);
    }
}
