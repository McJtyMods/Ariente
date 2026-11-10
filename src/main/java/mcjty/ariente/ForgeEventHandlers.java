package mcjty.ariente;

import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.blocks.utility.ILockable;
import mcjty.ariente.blocks.utility.autofield.AutoFieldTile;
import mcjty.ariente.commands.ModCommands;
import mcjty.ariente.display.DisplayDataSender;
import mcjty.ariente.facade.FacadeItemBlock;
import mcjty.ariente.facade.IFacadeSupport;
import mcjty.ariente.items.EarlyUseItem;
import mcjty.ariente.items.EnergyLanceItem;
import mcjty.ariente.items.armor.PacketPersonalForcefield;
import mcjty.ariente.items.armor.PersonalForcefield;
import mcjty.ariente.items.armor.PowerSuitEffects;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.multipart.MultipartBlock;
import mcjty.ariente.network.Packets;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.recipes.PurifierRecipes;
import mcjty.ariente.security.AreaLocks;
import mcjty.ariente.setup.Registration;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class ForgeEventHandlers {

    private ForgeEventHandlers() {
    }

    public static void register() {
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            var stack = player.getItemInHand(hand);
            return !player.isSpectator() && stack.getItem() instanceof EnergyLanceItem lance
                    && lance.onLeftClickEntity(stack, player, entity) ? InteractionResult.FAIL : InteractionResult.PASS;
        });
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            var stack = player.getItemInHand(hand);
            return stack.getItem() instanceof EarlyUseItem item
                    ? item.onItemUseFirst(stack, new UseOnContext(player, hand, hit))
                    : InteractionResult.PASS;
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> ModCommands.register(dispatcher));
        ServerTickEvents.START_WORLD_TICK.register(level -> {
            if (level.dimension() == Level.OVERWORLD) PowerSystem.getPowerSystem(level).tick();
            AreaLocks.tick(level);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            DisplayDataSender.tick();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                PowerSuitEffects.tick(player);
                if (player.isAlive() && !player.isPassenger()
                        && player.getItemBySlot(EquipmentSlot.CHEST).is(Registration.POWERSUIT_CHEST.get())
                        && ModuleSupport.hasWorkingUpgrade(player.getItemBySlot(EquipmentSlot.CHEST), ArmorUpgradeType.FLIGHT)) {
                    player.connection.aboveGroundTickCount = 0;
                    player.fallDistance = 0;
                }
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PowerSuitEffects.clear(handler.player);
            DisplayDataSender.logout(handler.player);
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> syncPersonalField(handler.player));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> syncPersonalField(player));
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> PowerSuitEffects.clear(entity));
        EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
            if (entity instanceof LivingEntity living && PersonalForcefield.isCoolingDown(living)) {
                Packets.sendToPlayer(player, new PacketPersonalForcefield(living.getId(), PersonalForcefield.getCooldownUntil(living), PersonalForcefield.getLastHit(living)));
            }
        });
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, tile) -> {
            if (tile instanceof ILockable lock && lock.isLocked()) return false;
            if (state.getBlock() instanceof MultipartBlock multipart) {
                return multipart.onDestroyedByPlayer(state, level, pos, player, player.getMainHandItem(), !player.isCreative(), level.getFluidState(pos));
            }
            if (tile instanceof IFacadeSupport facade && facade.getMimicBlock() != null) {
                var drop = new ItemStack(Registration.FACADE_ITEM.get());
                FacadeItemBlock.setMimicBlock(drop, facade.getMimicBlock());
                facade.setMimicBlock(null);
                if (!player.isCreative()) Block.popResource(level, pos, drop);
                return false;
            }
            return true;
        });

        ServerChunkEvents.CHUNK_LOAD.register((level, chunk) -> {
            AreaLocks.chunkLoaded(level, chunk);
            AutoFieldTile.chunkChanged(level, chunk);
        });
        ServerChunkEvents.CHUNK_UNLOAD.register(AutoFieldTile::chunkChanged);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> PurifierRecipes.tagsUpdated());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> DisplayDataSender.stopped());
    }

    private static void syncPersonalField(LivingEntity entity) {
        if (entity instanceof ServerPlayer player && PersonalForcefield.isCoolingDown(entity)) {
            Packets.sendToPlayer(player, new PacketPersonalForcefield(entity.getId(), PersonalForcefield.getCooldownUntil(entity), PersonalForcefield.getLastHit(entity)));
        }
    }
}
