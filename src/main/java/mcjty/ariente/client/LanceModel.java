package mcjty.ariente.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.Ariente;
import mcjty.ariente.entities.soldier.SoldierEntity;
import mcjty.ariente.items.EnergyLanceItem;
import mcjty.ariente.items.LanceAim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class LanceModel extends BakedModelWrapper<BakedModel> {

    private final LivingEntity holder;

    private LanceModel(BakedModel original, @Nullable LivingEntity holder) {
        super(original);
        this.holder = holder;
    }

    @SubscribeEvent
    public static void bake(ModelEvent.ModifyBakingResult event) {
        event.getModels().computeIfPresent(new ModelResourceLocation(Ariente.MODID, "energy_lance", "inventory"),
                (location, model) -> new LanceModel(model, null));
    }

    @Override
    public ItemOverrides getOverrides() {
        return new ItemOverrides() {

            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                                      @Nullable LivingEntity entity, int seed) {
                BakedModel resolved = originalModel.getOverrides().resolve(originalModel, stack, level, entity, seed);
                return new LanceModel(resolved == null ? originalModel : resolved, entity);
            }
        };
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext context, PoseStack poses, boolean leftHand) {
        boolean firstPerson = context.firstPerson() && holder == Minecraft.getInstance().player;
        boolean thirdPerson = context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        if (holder == null || !EnergyLanceItem.canFire(holder) || WorldFrame.worldToClip == null
                || !(firstPerson || thirdPerson && WorldFrame.renderingEntities)) {
            return originalModel.applyTransform(context, poses, leftHand);
        }

        float partialTick = WorldFrame.partialTick;
        Vec3 eye = holder.getEyePosition(partialTick);
        Vec3 forward = holder.getViewVector(partialTick);
        if (holder instanceof SoldierEntity soldier && soldier.getTarget() != null) {
            forward = soldier.getTarget().getEyePosition(partialTick).subtract(eye);
        }
        float yaw = Mth.rotLerp(partialTick, holder.yRotO, holder.getYRot());
        LanceAim aim = LanceAim.calculate(holder, eye, forward, yaw);
        Vec3 direction = aim.direction();
        Vec3 right = new Vec3(-Mth.cos(yaw * Mth.DEG_TO_RAD), 0, -Mth.sin(yaw * Mth.DEG_TO_RAD));
        right = right.subtract(direction.scale(right.dot(direction)));
        if (right.lengthSqr() < 1.0E-7D) {
            right = new Vec3(0, 1, 0).cross(direction);
        }
        right = right.normalize();
        Vec3 normal = right.cross(direction);
        Matrix4f orientation = new Matrix4f()
                .m00((float) right.x).m01((float) right.y).m02((float) right.z)
                .m10((float) direction.x).m11((float) direction.y).m12((float) direction.z)
                .m20((float) normal.x).m21((float) normal.y).m22((float) normal.z);
        float scale = firstPerson ? .75F : 1;
        // ItemRenderer subsequently subtracts (0.5, 0.5, 0.5); the model's tip is at (0.5, 2, 0.5).
        Vec3 origin = aim.muzzle().subtract(direction.scale(1.5D * scale)).subtract(WorldFrame.camera);

        // Retain the world's projection even in the hand pass, which can use a different FOV and model-view matrix.
        Matrix4f transform = new Matrix4f(RenderSystem.getProjectionMatrix()).mul(RenderSystem.getModelViewMatrix()).invert()
                .mul(WorldFrame.worldToClip).translate((float) origin.x, (float) origin.y, (float) origin.z)
                .mul(orientation).scale(scale);
        poses.last().pose().set(transform);
        poses.last().normal().set(new Matrix3f(transform).invert().transpose());
        return originalModel;
    }

    @Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
    public static class WorldFrame {

        private static Matrix4f worldToClip;
        private static Vec3 camera;
        private static float partialTick;
        private static boolean renderingEntities;

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void renderStage(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
                // Sky renderers can replace RenderSystem's projection; the event retains the world projection.
                worldToClip = new Matrix4f(event.getProjectionMatrix()).mul(RenderSystem.getModelViewMatrix())
                        .mul(event.getPoseStack().last().pose());
                if (!worldToClip.isFinite()) {
                    worldToClip = null;
                    renderingEntities = false;
                    return;
                }
                camera = event.getCamera().getPosition();
                partialTick = event.getPartialTick();
                renderingEntities = true;
            } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
                renderingEntities = false;
            }
        }
    }
}
