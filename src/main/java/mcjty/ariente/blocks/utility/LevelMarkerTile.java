package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.ArienteBlockEntity;
import mcjty.ariente.gui.GuiHelp;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.StyledColor;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class LevelMarkerTile extends ArienteBlockEntity implements IGuiTile {

    private static final VoxelShape BLOCK_AABB = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 1.0D/16.0, 1.0D);

    private static final int MAX_NAME_LENGTH = 32;

    private String floorName = "";

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (TAG_HELP.equals(tag)) {
            return GuiHelp.LEVEL_MARKER.createHelpGui(registry);
        }
        return GuiHelp.LEVEL_MARKER.createPanel(registry)
                .add(registry.text(.3, 1, 7.2, 1).text("Floor name").color(registry.color(StyledColor.LABEL)))
                .add(registry.textEntry(.3, 2, 7.2, 1.2).name("floor_name").maxLength(MAX_NAME_LENGTH)
                        .text(this::getFloorName).changed((player, entity, text) -> setFloorName(text)))
                .add(registry.text(.3, 3.6, 7.2, .6).text("Esc / click outside saves the name.").scale(.35f))
                .add(registry.text(.3, 4.3, 7.2, .6).text("Leave empty to show only the floor number.").scale(.3f));
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String name) {
        StringBuilder clean = new StringBuilder();
        name.codePoints().map(c -> c == '\n' || c == '\r' || c == '\t' ? ' ' : c)
                .filter(c -> !Character.isISOControl(c) && c != 167)
                .limit(MAX_NAME_LENGTH).forEach(clean::appendCodePoint);
        String value = clean.toString().strip();
        if (!floorName.equals(value)) {
            floorName = value;
            markDirtyClient();
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        floorName = tag.getString("FloorName");
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putString("FloorName", floorName);
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putString("FloorName", floorName);
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        floorName = tag.getString("FloorName");
    }

    public LevelMarkerTile(BlockPos pos, BlockState state) {
        super(Registration.LEVEL_MARKER_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
                .topDriver(DRIVER)
                .tileEntitySupplier(LevelMarkerTile::new)
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
                return BLOCK_AABB;
            }

            @Override
            public VoxelShape getCollisionShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
                return Shapes.empty();
            }
        };
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
        Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void syncToClient() {

    }
}
