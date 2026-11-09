package mcjty.ariente.blocks.utility.camera;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import java.util.HashMap;
import java.util.Map;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.ArienteBlockEntity;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CameraTile extends ArienteBlockEntity implements IGuiTile {

    private boolean live;
    private BlockState capturedState;
    private final Map<Integer, Capture> captures = new HashMap<>();

    public CameraTile(BlockPos pos, BlockState state) {
        super(mcjty.ariente.setup.Registration.CAMERA_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock(BlockBehaviour.Properties properties) {
        return new BaseBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
                .properties(properties.mapColor(MapColor.METAL).strength(2).noOcclusion())
                .tileEntitySupplier(CameraTile::new)) {

            @Override
            public BlockState getStateForPlacement(BlockPlaceContext context) {
                return defaultBlockState().setValue(BlockStateProperties.FACING, context.getClickedFace());
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                return shape(state.getValue(BlockStateProperties.FACING));
            }
        };
    }

    public static VoxelShape shape(Direction facing) {
        return switch (facing) {
            case UP -> Shapes.or(Block.box(0, 0, 0, 16, 1, 16), Block.box(4, 1, 4, 12, 9, 12));
            case DOWN -> Shapes.or(Block.box(0, 15, 0, 16, 16, 16), Block.box(4, 7, 4, 12, 15, 12));
            case NORTH -> Shapes.or(Block.box(0, 0, 15, 16, 16, 16), Block.box(4, 4, 7, 12, 12, 15));
            case SOUTH -> Shapes.or(Block.box(0, 0, 0, 16, 16, 1), Block.box(4, 4, 1, 12, 12, 9));
            case WEST -> Shapes.or(Block.box(15, 0, 0, 16, 16, 16), Block.box(7, 4, 4, 15, 12, 12));
            case EAST -> Shapes.or(Block.box(0, 0, 0, 1, 16, 16), Block.box(1, 4, 4, 9, 12, 12));
        };
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        if (this.live != live) {
            this.live = live;
            captures.clear();
            if (level != null && !level.isClientSide()) {
                markDirtyClient();
            }
        }
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult hit) {
        Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        return HoloGuiTools.createPanelWithHelp(registry, Identifier.fromNamespaceAndPath(Ariente.MODID, "construction/displays"))
                .add(registry.text(.3, 0, 7.2, 1).text("CAMERA").scale(.65f).color(0x76eaff))
                .add(registry.textChoice(.3, 1.2, 7.2, .9).name("camera_mode").scale(.45f)
                        .addText("Mode: Holographic").addText("Mode: Live")
                        .getter(p -> live ? 1 : 0).hitEvent((c, p, e, x, y) -> setLive(!live)))
                .add(registry.text(.3, 2.7, 7.2, .6).text("Holographic: a captured 3D scene.").scale(.37f))
                .add(registry.text(.3, 3.5, 7.2, .6).text("Live: sky, terrain and actual entities.").scale(.35f))
                .add(registry.text(.3, 4.5, 7.2, .6).text("Live starts looking away from the base.").scale(.35f))
                .add(registry.text(.3, 5.3, 7.2, .6).text("Use Pan and Tilt on a linked module.").scale(.35f))
                .add(registry.text(.3, 6.5, 7.2, .6).text("Live works nearby in your current dimension.").scale(.31f));
    }

    @Override
    public void syncToClient() {
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putBoolean("LiveCamera", live);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        live = tag.getBooleanOr("LiveCamera", false);
        captures.clear();
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putBoolean("LiveCamera", live);
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        live = tag.getBooleanOr("LiveCamera", false);
        captures.clear();
    }

    /** Demand driven: linked displays share one capture per second; the camera never ticks or loads chunks. */

    public CompoundTag capture() {
        return capture(CameraCapture.DEFAULT_RANGE);
    }

    public CompoundTag capture(int range) {
        if (range < CameraCapture.MIN_RANGE || range > CameraCapture.MAX_RANGE) {
            throw new IllegalArgumentException("Invalid camera range");
        }
        long now = level.getGameTime();
        if (capturedState != getBlockState()) {
            captures.clear();
            capturedState = getBlockState();
        }
        captures.entrySet().removeIf(entry -> now < entry.getValue().tick() || now - entry.getValue().tick() >= 20);
        Capture saved = captures.get(range);
        if (saved == null) {
            CompoundTag data;
            if (live) {
                data = new CompoundTag();
                data.putBoolean("Live", true);
                data.putLong("Camera", worldPosition.asLong());
                data.putString("Dimension", level.dimension().identifier().toString());
                data.putInt("Facing", getBlockState().getValue(BlockStateProperties.FACING).get3DDataValue());
                data.putInt("Range", range);
            } else {
                data = CameraCapture.scan(level, worldPosition, getBlockState().getValue(BlockStateProperties.FACING), range);
            }
            saved = new Capture(now, data);
            captures.put(range, saved);
        }
        return saved.data();
    }

    private record Capture(long tick, CompoundTag data) {
    }
}
