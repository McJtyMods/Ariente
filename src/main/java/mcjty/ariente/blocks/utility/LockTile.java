package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityEquipment;
import mcjty.ariente.blocks.BlockProperties;
import mcjty.ariente.blocks.utility.door.DoorMarkerTile;
import mcjty.ariente.items.KeyCardItem;
import mcjty.ariente.security.IKeyCardSlot;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.ModSounds;
import mcjty.hologui.api.*;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.hologui.api.Icons.*;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class LockTile extends GenericTileEntity implements IGuiTile, IKeyCardSlot, ICityEquipment, ILockable {

    private boolean locked = false;
    private String keyId;

    private int horizontalRange = 5;
    private int verticalRange = 3;

    private static final VoxelShape BLOCK_AABB = Shapes.box(1.0D/16.0, 1.0D/16.0, 15.0D/16.0, 15.0D/16.0, 15.0D/16.0, 1.0D);

    public LockTile(BlockPos pos, BlockState state) {
        super(Registration.LOCK_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
//                .flags(REDSTONE_CHECK, RENDER_SOLID, RENDER_CUTOUT)
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
                .topDriver(DRIVER)
                .tileEntitySupplier(LockTile::new)
        ) {
            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                super.createBlockStateDefinition(builder);
                builder.add(BlockProperties.LOCKED);
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
                return BLOCK_AABB;
            }
        };
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
        Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        return InteractionResult.SUCCESS;
    }



    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        boolean locked = isLocked();

        super.onDataPacket(net, packet);

        if (level.isClientSide) {
            // If needed send a render update.
            boolean newLocked = isLocked();
            if (newLocked != locked) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    public int getHorizontalRange(Player player, IHoloGuiEntity holo) {
        return horizontalRange;
    }

    public void setHorizontalRange(int horizontalRange) {
        doLock(false);
        this.horizontalRange = horizontalRange;
        markDirtyQuick();
        doLock(true);
    }

    public int getVerticalRange(Player player, IHoloGuiEntity holo) {
        return verticalRange;
    }

    public void setVerticalRange(int verticalRange) {
        doLock(false);
        this.verticalRange = verticalRange;
        markDirtyQuick();
        doLock(true);
    }

    @Override
    public void onReplaced(Level world, BlockPos pos, BlockState state, BlockState newstate) {
        doLock(false);
        super.onReplaced(world, pos, state, newstate);
    }

    private void doLock(boolean l) {
        if (level != null && !level.isClientSide) {
            for (int dx = -horizontalRange ; dx <= horizontalRange ; dx++) {
                for (int dy = -verticalRange ; dy <= verticalRange ; dy++) {
                    for (int dz = -horizontalRange ; dz <= horizontalRange ; dz++) {
                        BlockPos p = worldPosition.offset(dx, dy, dz);
                        BlockEntity te = level.getBlockEntity(p);
                        if (te instanceof DoorMarkerTile) { // @todo generalize!
                            ((DoorMarkerTile) te).setLocked(l);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        if (locked == this.locked) {
            return;
        }
        this.locked = locked;
        doLock(locked);
        markDirtyClient();
    }

    public void toggleLock() {
        if (!level.isClientSide) {
            setLocked(!locked);
        }
    }

    @Override
    public void acceptKeyCard(ItemStack stack) {
        Set<String> tags = KeyCardItem.getSecurityTags(stack);
        if (tags.contains(keyId)) {
            level.playSound(null, worldPosition, ModSounds.buzzOk, SoundSource.BLOCKS, 1.0f, 1.0f);
            toggleLock();
        } else {
            level.playSound(null, worldPosition, ModSounds.buzzError, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
        setChanged();
    }

    // @todo 1.14
//    @Override
//    public BlockState getActualState(BlockState state) {
//        return state.with(LOCKED, isLocked());
//    }

    @Override
    public Map<String, Object> save() {
        Map<String, Object> data = new HashMap<>();
        data.put("vertical", verticalRange);
        data.put("horizontal", horizontalRange);
        return data;
    }

    @Override
    public void load(Map<String, Object> data) {
        if (data.get("horizontal") instanceof Integer) {
            setHorizontalRange((Integer) data.get("horizontal"));
        }
        if (data.get("vertical") instanceof Integer) {
            setVerticalRange((Integer) data.get("vertical"));
        }
    }

    @Override
    public void setup(ICityAI cityAI, Level world, boolean firstTime) {
        if (firstTime) {
            setKeyId(cityAI.getKeyId());
            setLocked(true);
        }
    }

    private void changeHorizontalRange(int dy) {
        int h = horizontalRange + dy;
        if (h < 1) {
            h = 1;
        } else if (h > 20) {
            h = 20;
        }
        setHorizontalRange(h);
    }


    private void changeVerticalRange(int dy) {
        int h = verticalRange + dy;
        if (h < 1) {
            h = 1;
        } else if (h > 10) {
            h = 10;
        }
        setVerticalRange(h);
    }


    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (isLocked()) {
            return Ariente.guiHandler.createNoAccessPanel();
        }
        return registry.panel(0, 0, 8, 8)
                .add(registry.text(0, 1, 1, 1).text("Horizontal").color(registry.color(StyledColor.LABEL)))
                .add(registry.number(3, 2, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter(this::getHorizontalRange))

                .add(registry.iconButton(1, 2, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_LEFT)).hover(registry.image(WHITE_DOUBLE_ARROW_LEFT))
                        .hitEvent((component, player, entity1, x, y) -> changeHorizontalRange(-8)))
                .add(registry.iconButton(2, 2, 1, 1).icon(registry.image(GRAY_ARROW_LEFT)).hover(registry.image(WHITE_ARROW_LEFT))
                        .hitEvent((component, player, entity1, x, y) -> changeHorizontalRange(-1)))
                .add(registry.iconButton(5, 2, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                        .hitEvent((component, player, entity1, x, y) -> changeHorizontalRange(1)))
                .add(registry.iconButton(6, 2, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                        .hitEvent((component, player, entity1, x, y) -> changeHorizontalRange(8)))


                .add(registry.text(0, 4, 1, 1).text("Vertical").color(registry.color(StyledColor.LABEL)))
                .add(registry.number(3, 5, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter(this::getVerticalRange))

                .add(registry.iconButton(1, 5, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_LEFT)).hover(registry.image(WHITE_DOUBLE_ARROW_LEFT))
                        .hitEvent((component, player, entity1, x, y) -> changeVerticalRange(-8)))
                .add(registry.iconButton(2, 5, 1, 1).icon(registry.image(GRAY_ARROW_LEFT)).hover(registry.image(WHITE_ARROW_LEFT))
                        .hitEvent((component, player, entity1, x, y) -> changeVerticalRange(-1)))
                .add(registry.iconButton(5, 5, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                        .hitEvent((component, player, entity1, x, y) -> changeVerticalRange(1)))
                .add(registry.iconButton(6, 5, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                        .hitEvent((component, player, entity1, x, y) -> changeVerticalRange(8)))
                ;
    }

    @Override
    public void syncToClient() {

    }
}
