package mcjty.ariente.blocks.storagecube.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import mcjty.ariente.blocks.storagecube.StorageCubeTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

/** Eight cached icons per visible cube. No entities, packets, inventory scans or per-frame random sampling. */
public class StorageCubeRenderer implements BlockEntityRenderer<StorageCubeTile> {

    private final Map<StorageCubeTile, Animation> animations = new WeakHashMap<>();

    public StorageCubeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return 24;
    }

    @Override
    public void render(StorageCubeTile tile, float partialTicks, PoseStack poses, MultiBufferSource buffer, int light, int overlay) {
        List<StorageCubeTile.Preview> preview = tile.getPreview();
        if (preview.isEmpty()) {
            animations.remove(tile);
            return;
        }
        double now = tile.getLevel().getGameTime() + partialTicks;
        Animation animation = animations.computeIfAbsent(tile, key -> new Animation(key.getBlockPos().asLong()));
        for (int i = 0; i < animation.icons.length; i++) {
            Icon icon = animation.icons[i];
            if (icon == null && i >= preview.size()) {
                continue;
            }
            if (icon == null || now - icon.born >= 100 || now < icon.born) {
                icon = animation.replace(i, preview, now);
            }
            if (icon == null) {
                continue;
            }
            double age = now - icon.born;
            float alpha = (float) Math.min(1, Math.min(age / 10, (100 - age) / 10));
            if (alpha <= .01f) {
                continue;
            }
            poses.pushPose();
            poses.translate(icon.x + .035 * Math.sin(now * .013 + icon.phase),
                    icon.y + .05 * Math.sin(now * .018 + icon.phase * 2),
                    icon.z + .035 * Math.cos(now * .015 + icon.phase));
            poses.mulPose(Axis.YP.rotationDegrees((float) (now * .3 + icon.phase * 60)));
            poses.mulPose(Axis.ZP.rotationDegrees((float) (5 * Math.sin(now * .02 + icon.phase))));
            poses.scale(.17f, .17f, .17f);
            MultiBufferSource fading = type -> {
                // Atlas item passes must blend for alpha to fade, including ordinary solid blocks.
                var translucent = type == Sheets.solidBlockSheet() || type == Sheets.cutoutBlockSheet()
                        || type == Sheets.translucentItemSheet() ? Sheets.translucentCullBlockSheet() : type;
                return new VertexConsumerWrapper(buffer.getBuffer(translucent)) {

                    @Override
                    public VertexConsumer setColor(int r, int g, int b, int a) {
                        return super.setColor(r, g, b, Mth.clamp((int) (a * alpha), 0, 255));
                    }
                };
            };
            Minecraft.getInstance().getItemRenderer().renderStatic(icon.stack, ItemDisplayContext.FIXED,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poses, fading, tile.getLevel(), 0);
            poses.popPose();
        }
    }

    private static class Animation {

        private final RandomSource random;
        private final Icon[] icons = new Icon[8];

        private Animation(long seed) {
            random = RandomSource.create(seed);
        }

        private Icon replace(int index, List<StorageCubeTile.Preview> preview, double now) {
            Icon previous = icons[index];
            icons[index] = null;
            int total = 0;
            for (StorageCubeTile.Preview entry : preview) {
                if (!visible(entry.stack())) {
                    total += entry.weight();
                }
            }
            if (total == 0) {
                return null;
            }
            int roll = random.nextInt(total);
            for (StorageCubeTile.Preview entry : preview) {
                if (!visible(entry.stack())) {
                    roll -= entry.weight();
                    if (roll < 0) {
                        // Stagger initial lifetimes so the complete cube never blinks at once.
                        double born = previous == null ? now - index * 11 : now;
                        icons[index] = new Icon(entry.stack(), born, .28 + random.nextDouble() * .44,
                                .26 + random.nextDouble() * .48, .28 + random.nextDouble() * .44, random.nextDouble() * Math.PI * 2);
                        return icons[index];
                    }
                }
            }
            return null;
        }

        private boolean visible(ItemStack stack) {
            for (Icon icon : icons) {
                if (icon != null && ItemStack.isSameItemSameComponents(icon.stack, stack)) {
                    return true;
                }
            }
            return false;
        }
    }

    private record Icon(ItemStack stack, double born, double x, double y, double z, double phase) {
    }
}
