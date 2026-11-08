package mcjty.ariente.display;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import mcjty.ariente.Ariente;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Server-side click/drag transaction. No module is moved until a valid release. */
public class DisplayGesture {

    private static final Map<ServerPlayer, Session> SESSIONS = new WeakHashMap<>();
    private static final int TIMEOUT = 1200;

    public static boolean begin(ServerPlayer player, BlockPos pos, UUID id, UUID ticket, Vec3 look) {
        SESSIONS.remove(player);
        if (player.isSpectator() || !player.level().hasChunkAt(pos)
                || !(player.level().getBlockEntity(pos) instanceof DisplayBaseTile source)
                || !player.level().mayInteract(player, pos)) {
            return false;
        }
        InstalledModule module = source.findModule(id);
        DisplayGeometry.Hit hit = DisplayGeometry.project(source, player, Math.min(8, player.blockInteractionRange()), look);
        if (module == null || hit == null || !module.contains(hit.x(), hit.y())
                || !DisplayGeometry.fits(source, module.x(), module.y(), module.size())) {
            return false;
        }
        SESSIONS.put(player, new Session(source, module, hit, ticket));
        return true;
    }

    public static void cancel(ServerPlayer player, UUID ticket) {
        Session session = SESSIONS.get(player);
        if (session != null && session.ticket.equals(ticket)) {
            SESSIONS.remove(player);
        }
    }

    public static void keepAlive(ServerPlayer player, BlockPos pos, UUID id, UUID ticket) {
        Session session = SESSIONS.get(player);
        if (session != null && session.ticket.equals(ticket) && valid(player, session, pos, id)) {
            session.deadline = player.level().getGameTime() + TIMEOUT;
        } else if (session != null && session.ticket.equals(ticket)) {
            SESSIONS.remove(player);
        }
    }

    private static boolean valid(ServerPlayer player, Session session, BlockPos pos, UUID id) {
        if (session == null || player.isSpectator() || player.level().getGameTime() > session.deadline) {
            return false;
        }
        DisplayBaseTile source = session.source.get();
        InstalledModule module = session.module.get();
        return source != null && module != null && !source.isRemoved() && source.getLevel() == player.level()
                && player.level().hasChunkAt(pos) && player.level().getBlockEntity(pos) == source
                && source.getBlockPos().equals(pos) && module.id().equals(id) && source.findModule(id) == module
                && source.getHeight() == session.height && DisplayGeometry.facing(source) == session.facing
                && player.level().mayInteract(player, pos);
    }

    public static DisplayGeometry.DragPosition finish(ServerPlayer player, BlockPos pos, UUID id, UUID ticket,
                                                       Vec3 look, boolean dragged, boolean allowDrop) {
        Session session = SESSIONS.get(player);
        if (session == null || !session.ticket.equals(ticket)) {
            return null;
        }
        SESSIONS.remove(player);
        if (!valid(player, session, pos, id)) {
            return null;
        }
        DisplayBaseTile source = session.source.get();
        InstalledModule module = session.module.get();
        DisplayGeometry.Hit release = DisplayGeometry.project(source, player, Math.min(8, player.blockInteractionRange()), look);
        if (release == null) {
            return null;
        }
        if (!dragged) {
            if (module.contains(release.x(), release.y())) {
                Ariente.guiHandler.openHoloGuiEntity(player.level(), pos, player, ModuleSettings.installedTag(id, false), 1);
            }
            return null;
        }
        if (!allowDrop) {
            return null;
        }
        DisplayGeometry.DragPosition proposed = DisplayGeometry.dragPosition(source, module, session.start, release);
        return proposed.valid() && source.moveModule(player, module, proposed.target(), proposed.x(), proposed.y()) ? proposed : null;
    }

    private static class Session {

        private final WeakReference<DisplayBaseTile> source;
        private final WeakReference<InstalledModule> module;
        private final DisplayGeometry.Hit start;
        private final int height;
        private final Direction facing;
        private final UUID ticket;
        private long deadline;

        private Session(DisplayBaseTile source, InstalledModule module, DisplayGeometry.Hit start, UUID ticket) {
            this.source = new WeakReference<>(source);
            this.module = new WeakReference<>(module);
            // Do not retain the tile through Hit: sessions must not keep unloaded worlds alive.
            this.start = new DisplayGeometry.Hit(null, start.x(), start.y(), start.distance());
            this.ticket = ticket;
            height = source.getHeight();
            facing = DisplayGeometry.facing(source);
            deadline = source.getLevel().getGameTime() + TIMEOUT;
        }
    }
}
