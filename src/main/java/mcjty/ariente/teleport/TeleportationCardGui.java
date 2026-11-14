package mcjty.ariente.teleport;

import mcjty.ariente.items.ItemData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.ariente.Ariente;
import mcjty.ariente.display.client.CameraDisplayRenderer;
import mcjty.ariente.items.TeleportationCardItem;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.teleport.client.WarperPreviewCache;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.api.components.IPanel;
import mcjty.hologui.gui.HoloGuiRenderTools;
import mcjty.hologui.gui.components.AbstractHoloComponent;
import mcjty.lib.client.RenderBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.UUID;

public class TeleportationCardGui {

    private static final int ROWS = 5;

    public static String guiId(InteractionHand hand) {
        return "ariente.teleportation_card." + hand.name();
    }

    public static void register() {
        for (InteractionHand hand : InteractionHand.values()) {
            Ariente.guiHandler.getGuiRegistry().registerGui(guiId(hand), player -> create(player, hand));
        }
    }

    public static ItemStack boundCard(Player player, InteractionHand hand, UUID id) {
        ItemStack stack = player.getItemInHand(hand);
        return id != null && stack.is(Registration.TELEPORTATION_CARD.get()) && Objects.equals(id, TeleportationCardItem.id(stack)) ? stack : ItemStack.EMPTY;
    }

    private static int page(ItemStack stack) {
        int last = Math.max(0, (Math.min(TeleportationCardItem.MAX_DESTINATIONS, TeleportationCardItem.destinations(stack).size()) - 1) / ROWS);
        return ItemData.has(stack) ? Math.max(0, Math.min(last, ItemData.get(stack).getIntOr("Page", 0))) : 0;
    }

    private static IGuiComponent<?> create(Player player, InteractionHand hand) {
        UUID id = TeleportationCardItem.id(player.getItemInHand(hand));
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        IPanel panel = registry.panel(0, 0, 8, 8)
                .add(registry.text(.2, 0, 7.6, .8).text("Teleportation Card").scale(.65f).color(0x76eaff))
                .add(registry.text(.2, .85, 7.6, .5).text("Select a destination").scale(.35f));
        for (int row = 0; row < ROWS; row++) {
            panel.add(new DestinationRow(hand, id, row).name("destination_" + row));
        }
        panel.add(new Preview(hand, id).name("destination_preview"));
        panel.add(registry.button(.2, 6.8, 1.4, .7).text("Prev").scale(.4f).hitEvent((c, p, e, x, y) -> turnPage(p, hand, id, -1)))
                .add(registry.button(2.8, 6.8, 1.4, .7).text("Next").scale(.4f).hitEvent((c, p, e, x, y) -> turnPage(p, hand, id, 1)))
                .add(registry.text(1.7, 6.8, 1, .7).text(() -> Integer.toString(page(boundCard(player, hand, id)) + 1)).scale(.45f))
                .add(registry.text(.2, 7.65, 7.6, .5).text("Right-click a Warper to redirect it.").scale(.32f));
        return panel;
    }

    private static void turnPage(Player player, InteractionHand hand, UUID id, int delta) {
        ItemStack stack = boundCard(player, hand, id);
        if (!stack.isEmpty()) {
            ItemData.update(stack, data -> data.putInt("Page", page(stack) + delta));
            ItemData.update(stack, data -> data.putInt("Page", page(stack)));
            TeleportationCardItem.sync(player);
        }
    }

    private static class DestinationRow extends AbstractHoloComponent<DestinationRow> {

        private final InteractionHand hand;
        private final UUID card;
        private final int row;

        private DestinationRow(InteractionHand hand, UUID card, int row) {
            super(.2, 1.5 + row, 4, .9);
            this.hand = hand;
            this.card = card;
            this.row = row;
        }

        @Override
        public void render(PoseStack poses, RenderBufferSource buffer, Player player, IHoloGuiEntity holo, double cursorX, double cursorY) {
            ItemStack stack = boundCard(player, hand, card);
            int index = page(stack) * ROWS + row;
            WarperDestination destination = TeleportationCardItem.destination(stack, index);
            if (destination == null) {
                if (row == 0 && TeleportationCardItem.destinations(stack).isEmpty()) {
                    HoloGuiRenderTools.renderFittedText(poses, buffer, x, y, "Sneak-click Warpers to add them", 0x83eaff, .32f, w);
                }
                return;
            }
            boolean selected = TeleportationCardItem.selectedIndex(stack) == index;
            HoloGuiRenderTools.renderControl(poses, buffer, x, y, w, h, isInside(cursorX, cursorY));
            if (selected) {
                HoloGuiRenderTools.renderBorder(poses, buffer, x, y, w, h, 118, 234, 255, 255);
            }
            HoloGuiRenderTools.renderControlText(poses, buffer, x + .15, y + .05, w - .3, .42,
                    TeleportationCardItem.destinationName(stack, index), selected ? 0x76eaff : 0xffffff, .42f);
            HoloGuiRenderTools.renderControlText(poses, buffer, x + .15, y + .49, w - .3, .32,
                    destination.dimension().identifier() + " / " + destination.coordinates(), 0xa0c5d0, .26f);
        }

        @Override
        public void hit(Player player, IHoloGuiEntity entity, double cursorX, double cursorY) {
            ItemStack stack = boundCard(player, hand, card);
            if (player instanceof ServerPlayer serverPlayer && !stack.isEmpty()) {
                WarperPreviews.select(serverPlayer, stack, page(stack) * ROWS + row);
            }
        }
    }

    private static class Preview extends AbstractHoloComponent<Preview> {

        private final InteractionHand hand;
        private final UUID card;

        private Preview(InteractionHand hand, UUID card) {
            super(4.5, 1.5, 3.3, 5);
            this.hand = hand;
            this.card = card;
        }

        @Override
        public void render(PoseStack poses, RenderBufferSource buffer, Player player, IHoloGuiEntity holo, double cursorX, double cursorY) {
            ItemStack stack = boundCard(player, hand, card);
            CompoundTag scene = WarperPreviewCache.get(card, TeleportationCardItem.selected(stack));
            HoloGuiRenderTools.renderFittedText(poses, buffer, x, y, "Destination preview", 0x76eaff, .38f, w);
            if (scene == null || scene.contains("Status")) {
                String text = scene == null ? "Select a destination" : scene.getStringOr("Status", "");
                HoloGuiRenderTools.renderFittedText(poses, buffer, x, y + 1.5, text, 0xffbc7a, .35f, w);
                return;
            }
            poses.pushPose();
            poses.translate((x + w / 2) * .1 - .42, .42 - (y + 2.7) * .1, .18);
            poses.rotate(Axis.XP.rotationDegrees(30));
            poses.rotate(Axis.YP.rotationDegrees((player.level().getGameTime() % 720) * .5f));
            CameraDisplayRenderer.renderMiniature(poses, buffer, this, scene, .30f, 255);
            poses.popPose();
        }
    }
}
