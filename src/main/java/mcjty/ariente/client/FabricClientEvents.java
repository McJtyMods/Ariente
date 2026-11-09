package mcjty.ariente.client;

import mcjty.ariente.ClientForgeEventHandlers;
import mcjty.ariente.bindings.Binding;
import mcjty.ariente.bindings.KeyInputHandler;
import mcjty.ariente.blocks.ExtremeMachineTile;
import mcjty.ariente.blocks.storagecube.client.StorageCubeClient;
import mcjty.ariente.display.client.DisplayClientEvents;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.sounds.FluxLevitatorSounds;
import mcjty.ariente.sounds.FluxShipSounds;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;

public final class FabricClientEvents {

    private static final ClientForgeEventHandlers HANDLERS = new ClientForgeEventHandlers();
    private static final DisplayClientEvents DISPLAYS = new DisplayClientEvents();
    private static Entity vehicle;

    private FabricClientEvents() {
    }

    public static boolean input(boolean attack) {
        var event = new InputGesture(attack, InteractionHand.MAIN_HAND);
        StorageCubeClient.input(event);
        if (!event.isCanceled()) DISPLAYS.input(event);
        if (!event.isCanceled()) BlockInventoryInput.attack(event);
        if (!event.isCanceled() && attack) HANDLERS.onLeftClickEmpty();
        return event.isCanceled();
    }

    public static void register() {
        var keys = new KeyInputHandler();
        ClientTickEvents.START_CLIENT_TICK.register(client -> HANDLERS.onClientTick());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Binding.checkBindings();
            keys.onClientTick();
            DISPLAYS.tick();
            BlockInventoryInput.tick();
            StorageCubeClient.tick();
            FluxShipSounds.clientTick();
            Entity nextVehicle = client.player == null ? null : client.player.getVehicle();
            if (nextVehicle != vehicle && nextVehicle instanceof FluxLevitatorEntity levitator) {
                FluxLevitatorSounds.playMovingSoundClientInside(client.player, levitator);
            }
            vehicle = nextVehicle;
        });
        ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            FluxShipSounds.entityJoined(entity);
            if (entity instanceof FluxLevitatorEntity levitator) FluxLevitatorSounds.playMovingSoundClient(levitator);
        });
        ItemTooltipCallback.EVENT.register((stack, context, flags, lines) -> {
            if (ExtremeMachineTile.hasExtremeCircuit(stack)) lines.add(Component.translatable("message.ariente.extreme_circuit.upgraded"));
        });
        ClientRecipeSynchronizedEvent.EVENT.register((client, recipes) -> PurifierRecipeSync.received(recipes));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> PurifierRecipeSync.disconnected());
        LevelRenderEvents.END_EXTRACTION.register(context -> {
            HANDLERS.extractForcefields(context);
            DISPLAYS.extract(context);
        });
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            HANDLERS.submitForcefields(context);
            DISPLAYS.capture(context);
        });
        LevelRenderEvents.END_MAIN.register(DISPLAYS::render);
    }
}
