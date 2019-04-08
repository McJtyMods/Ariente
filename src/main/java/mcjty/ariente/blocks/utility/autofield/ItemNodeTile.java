package mcjty.ariente.blocks.utility.autofield;

import elec332.core.client.model.loading.handler.ItemModelHandler;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.gui.HelpBuilder;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.hologui.api.*;
import mcjty.hologui.api.components.IPlayerInventory;
import mcjty.hologui.api.components.ISlots;
import mcjty.lib.multipart.MultipartHelper;
import mcjty.lib.multipart.PartPos;
import mcjty.lib.multipart.PartSlot;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.ItemStackList;
import mcjty.lib.varia.RedstoneMode;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;

import static mcjty.ariente.blocks.utility.autofield.NodeOrientation.*;
import static mcjty.hologui.api.Icons.*;

public class ItemNodeTile extends GenericTileEntity implements IGuiTile {

    public static final PropertyEnum<NodeOrientation> ORIENTATION = PropertyEnum.create("orientation", NodeOrientation.class, NodeOrientation.values());
    public static final int FILTER_AMOUNT = 12;

    public static String TAG_INPUT = "input";
    public static String TAG_OUTPUT = "output";

    private ItemStackList inputFilter = ItemStackList.create(FILTER_AMOUNT);
    private ItemStackList outputFilter = ItemStackList.create(FILTER_AMOUNT);

    private boolean inputOredict = false;
    private boolean inputDamage = false;
    private boolean inputNbt = false;
    private boolean outputOredict = false;
    private boolean outputDamage = false;
    private boolean outputNbt = false;

    public static IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        NodeOrientation orientation = getOrientationFromPlacement(facing, hitX, hitY, hitZ);
        // Since this is a multipart we can use state that isn't convertable to metadata
        return ModBlocks.itemNode.getDefaultState().withProperty(ORIENTATION, orientation);
    }

    private static final float T = 0.2f;
    private static final float A = 0.1F;

    private static final AxisAlignedBB AABB_DOWN_NW = new AxisAlignedBB(0.0+A, 0, 0.0+A,      0.5-A, T, 0.5-A);
    private static final AxisAlignedBB AABB_DOWN_NE = new AxisAlignedBB(0.5+A, 0, 0.0+A,      1.0-A, T, 0.5-A);
    private static final AxisAlignedBB AABB_DOWN_SW = new AxisAlignedBB(0.0+A, 0, 0.5+A,      0.5-A, T, 1.0-A);
    private static final AxisAlignedBB AABB_DOWN_SE = new AxisAlignedBB(0.5+A, 0, 0.5+A,      1.0-A, T, 1.0-A);
    private static final AxisAlignedBB AABB_UP_NW = new AxisAlignedBB(0.0+A, 1-T, 0.0+A,      0.5-A, 1, 0.5-A);
    private static final AxisAlignedBB AABB_UP_NE = new AxisAlignedBB(0.5+A, 1-T, 0.0+A,      1.0-A, 1, 0.5-A);
    private static final AxisAlignedBB AABB_UP_SW = new AxisAlignedBB(0.0+A, 1-T, 0.5+A,      0.5-A, 1, 1.0-A);
    private static final AxisAlignedBB AABB_UP_SE = new AxisAlignedBB(0.5+A, 1-T, 0.5+A,      1.0-A, 1, 1.0-A);
    private static final AxisAlignedBB AABB_NORTH_DE = new AxisAlignedBB(0.5+A, 0.0+A, 0,     1.0-A, 0.5-A, T);
    private static final AxisAlignedBB AABB_NORTH_DW = new AxisAlignedBB(0.0+A, 0.0+A, 0,     0.5-A, 0.5-A, T);
    private static final AxisAlignedBB AABB_NORTH_UE = new AxisAlignedBB(0.5+A, 0.5+A, 0,     1.0-A, 1.0-A, T);
    private static final AxisAlignedBB AABB_NORTH_UW = new AxisAlignedBB(0.0+A, 0.5+A, 0,     0.5-A, 1.0-A, T);
    private static final AxisAlignedBB AABB_SOUTH_DE = new AxisAlignedBB(0.5+A, 0.0+A, 1-T,   1.0-A, 0.5-A, 1);
    private static final AxisAlignedBB AABB_SOUTH_DW = new AxisAlignedBB(0.0+A, 0.0+A, 1-T,   0.5-A, 0.5-A, 1);
    private static final AxisAlignedBB AABB_SOUTH_UE = new AxisAlignedBB(0.5+A, 0.5+A, 1-T,   1.0-A, 1.0-A, 1);
    private static final AxisAlignedBB AABB_SOUTH_UW = new AxisAlignedBB(0.0+A, 0.5+A, 1-T,   0.5-A, 1.0-A, 1);
    private static final AxisAlignedBB AABB_WEST_DN = new AxisAlignedBB(0, 0.0+A, 0.0+A,      T, 0.5-A, 0.5-A);
    private static final AxisAlignedBB AABB_WEST_DS = new AxisAlignedBB(0, 0.0+A, 0.5+A,      T, 0.5-A, 1.0-A);
    private static final AxisAlignedBB AABB_WEST_UN = new AxisAlignedBB(0, 0.5+A, 0.0+A,      T, 1.0-A, 0.5-A);
    private static final AxisAlignedBB AABB_WEST_US = new AxisAlignedBB(0, 0.5+A, 0.5+A,      T, 1.0-A, 1.0-A);
    private static final AxisAlignedBB AABB_EAST_DN = new AxisAlignedBB(1-T, 0.0+A, 0.0+A,    1, 0.5-A, 0.5-A);
    private static final AxisAlignedBB AABB_EAST_DS = new AxisAlignedBB(1-T, 0.0+A, 0.5+A,    1, 0.5-A, 1.0-A);
    private static final AxisAlignedBB AABB_EAST_UN = new AxisAlignedBB(1-T, 0.5+A, 0.0+A,    1, 1.0-A, 0.5-A);
    private static final AxisAlignedBB AABB_EAST_US = new AxisAlignedBB(1-T, 0.5+A, 0.5+A,    1, 1.0-A, 1.0-A);

    @Override
    public void markDirty() {
        // Make sure to mark the MultipartTE as dirty
        world.getTileEntity(pos).markDirty();
    }

    @Override
    public void markDirtyQuick() {
        // Make sure to mark the MultipartTE as dirty
        ((GenericTileEntity)world.getTileEntity(pos)).markDirtyQuick();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        AutoFieldTile.notifyField(world, pos);
    }

    @Override
    public void onBlockBreak(World world, BlockPos pos, IBlockState state) {
        AutoFieldTile.notifyField(world, pos);
    }

    public static AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        NodeOrientation orientation = state.getValue(ORIENTATION);
        switch (orientation) {
            case DOWN_NE: return AABB_DOWN_NE;
            case DOWN_NW: return AABB_DOWN_NW;
            case DOWN_SE: return AABB_DOWN_SE;
            case DOWN_SW: return AABB_DOWN_SW;
            case UP_NE: return AABB_UP_NE;
            case UP_NW: return AABB_UP_NW;
            case UP_SE: return AABB_UP_SE;
            case UP_SW: return AABB_UP_SW;
            case NORTH_DE: return AABB_NORTH_DE;
            case NORTH_DW: return AABB_NORTH_DW;
            case NORTH_UE: return AABB_NORTH_UE;
            case NORTH_UW: return AABB_NORTH_UW;
            case SOUTH_DE: return AABB_SOUTH_DE;
            case SOUTH_DW: return AABB_SOUTH_DW;
            case SOUTH_UE: return AABB_SOUTH_UE;
            case SOUTH_UW: return AABB_SOUTH_UW;
            case WEST_DN: return AABB_WEST_DN;
            case WEST_DS: return AABB_WEST_DS;
            case WEST_UN: return AABB_WEST_UN;
            case WEST_US: return AABB_WEST_US;
            case EAST_DN: return AABB_EAST_DN;
            case EAST_DS: return AABB_EAST_DS;
            case EAST_UN: return AABB_EAST_UN;
            case EAST_US: return AABB_EAST_US;
        }
        return Block.NULL_AABB;
    }

    @Override
    public boolean onBlockActivated(IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        Ariente.guiHandler.openHoloGuiEntity(world, pos, player, state.getValue(ORIENTATION).getSlot().name(), 1.0);
        return true;
    }

    @Override
    public void onPartAdded(PartSlot slot, IBlockState state, TileEntity multipartTile) {
        this.world = multipartTile.getWorld();
        this.pos = multipartTile.getPos();
    }

    public static NodeOrientation getOrientationFromPlacement(EnumFacing side, float hitX, float hitY, float hitZ) {
        side = side.getOpposite();
        NodeOrientation facing;
        switch (side) {
            case DOWN:
                if (hitX < .5) {    // W
                    facing = hitZ < 0.5 ? DOWN_NW : DOWN_SW;
                } else {            // E
                    facing = hitZ < 0.5 ? DOWN_NE : DOWN_SE;
                }
                break;
            case UP:
                if (hitX < .5) {    // W
                    facing = hitZ < 0.5 ? UP_NW : UP_SW;
                } else {            // E
                    facing = hitZ < 0.5 ? UP_NE : UP_SE;
                }
                break;
            case NORTH:
                if (hitX < .5) {    // W
                    facing = hitY < 0.5 ? NORTH_DW : NORTH_UW;
                } else {            // E
                    facing = hitY < 0.5 ? NORTH_DE : NORTH_UE;
                }
                break;
            case SOUTH:
                if (hitX < .5) {    // W
                    facing = hitY < 0.5 ? SOUTH_DW : SOUTH_UW;
                } else {            // E
                    facing = hitY < 0.5 ? SOUTH_DE : SOUTH_UE;
                }
                break;
            case WEST:
                if (hitZ < .5) {    // N
                    facing = hitY < 0.5 ? WEST_DN : WEST_UN;
                } else {            // S
                    facing = hitY < 0.5 ? WEST_DS : WEST_US;
                }
                break;
            case EAST:
                if (hitZ < .5) {    // N
                    facing = hitY < 0.5 ? EAST_DN : EAST_UN;
                } else {            // S
                    facing = hitY < 0.5 ? EAST_DS : EAST_US;
                }
                break;
            default:
                facing = DOWN_NW;
                break;
        }
        return facing;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, "input", inputFilter);
        readBufferFromNBT(tagCompound, "output", outputFilter);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, "input", inputFilter);
        writeBufferToNBT(tagCompound, "output", outputFilter);
    }

    private void changeMode() {
        int current = rsMode.ordinal() + 1;
        if (current >= RedstoneMode.values().length) {
            current = 0;
        }
        setRSMode(RedstoneMode.values()[current]);
        markDirtyClient();
    }

    public ItemStackList getInputFilter() {
        return inputFilter;
    }

    public ItemStackList getOutputFilter() {
        return outputFilter;
    }

    public boolean isInputOredict() {
        return inputOredict;
    }

    public boolean isInputDamage() {
        return inputDamage;
    }

    public boolean isInputNbt() {
        return inputNbt;
    }

    public boolean isOutputOredict() {
        return outputOredict;
    }

    public boolean isOutputDamage() {
        return outputDamage;
    }

    public boolean isOutputNbt() {
        return outputNbt;
    }

    private SimpleItemHandler inputHandler = null;
    private SimpleItemHandler outputHandler = null;

    private SimpleItemHandler getInputHandler() {
        if (inputHandler == null) {
            inputHandler = new SimpleItemHandler(inputFilter);
        }
        return inputHandler;
    }

    private SimpleItemHandler getOutputHandler() {
        if (outputHandler == null) {
            outputHandler = new SimpleItemHandler(outputFilter);
        }
        return outputHandler;
    }

    @Nullable
    public IItemHandler getConnectedItemHandler(PartPos partPos) {
        IBlockState state = MultipartHelper.getBlockState(world, pos, partPos.getSlot());
        if (state != null && state.getBlock() == ModBlocks.itemNode) {
            NodeOrientation orientation = state.getValue(ORIENTATION);
            EnumFacing mainDirection = orientation.getMainDirection();
            TileEntity otherTe = world.getTileEntity(pos.offset(mainDirection));
            if (otherTe != null && otherTe.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, mainDirection.getOpposite())) {
                return otherTe.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, mainDirection.getOpposite());
            }
        }
        return null;
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        final Pair<String, String> pair = getSlotTag(tag);

        if (TAG_HELP.equals(pair.getRight())) {
            return HoloGuiTools.createHelpGui(registry,
                    HelpBuilder.create()
                            .line("This node can be used in")
                            .line("an automation field to transfer")
                            .line("items"),
                    pair.getLeft() + ":" + TAG_MAIN
            );
        } else if (TAG_INPUT.equals(pair.getRight())) {
            return createInputGui(pair, registry);
        } else if (TAG_OUTPUT.equals(pair.getRight())) {
            return createOutputGui(pair, registry);
        } else {
            return createMainGui(pair, registry);
        }
    }

    private IGuiComponent<?> createMainGui(final Pair<String, String> pair, IGuiComponentRegistry registry) {
        return HoloGuiTools.createPanelWithHelp(registry, entity -> entity.switchTag(pair.getLeft() + ":" + TAG_HELP))
                .add(registry.text(0, 0.5, 1, 1).text("Main menu").color(0xaaccff))
                .add(registry.button(2, 2, 5, 1)
                        .text("Input")
                        .hitEvent((component, p, entity, x1, y1) -> entity.switchTag(pair.getLeft() + ":" + TAG_INPUT)))
                .add(registry.button(2, 4, 5, 1)
                        .text("Output")
                        .hitEvent((component, p, entity, x1, y1) -> entity.switchTag(pair.getLeft() + ":" + TAG_OUTPUT)))

                .add(registry.iconChoice(7, 0, 1, 1)
                        .getter((player) -> getRSModeInt())
                        .addImage(registry.image(REDSTONE_DUST))
                        .addImage(registry.image(REDSTONE_OFF))
                        .addImage(registry.image(REDSTONE_ON))
                        .hitEvent((component, player, entity1, x, y) -> changeMode()))
                ;
    }

    private IGuiComponent<?> createInputGui(final Pair<String, String> pair, IGuiComponentRegistry registry) {
        return HoloGuiTools.createPanelWithHelp(registry, entity -> entity.switchTag(pair.getLeft() + ":" + TAG_HELP))
                .add(registry.text(2.3, -.2, 1, 1).text("Input Config").color(0xaaccff))

                .add(registry.iconToggle(0.5, 0.5, 1, 1)
                        .getter(player -> inputNbt)
                        .icon(registry.image(Icons.NBT_OFF))
                        .selected(registry.image(Icons.NBT_ON))
                        .hitEvent((component, player, entity, x, y) -> toggleInputNBT()))
                .add(registry.iconToggle(0.5, 1.5, 1, 1)
                        .getter(player -> inputDamage)
                        .icon(registry.image(Icons.DAM_OFF))
                        .selected(registry.image(Icons.DAM_ON))
                        .hitEvent((component, player, entity, x, y) -> toggleInputDamage()))
                .add(registry.iconToggle(0.5, 2.5, 1, 1)
                        .getter(player -> inputOredict)
                        .icon(registry.image(Icons.ORE_OFF))
                        .selected(registry.image(Icons.ORE_ON))
                        .hitEvent((component, player, entity, x, y) -> toggleInputOre()))

                .add(registry.slots(2.5, 1.5, 6, 2)
                        .name("slots")
                        .fullBright()
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> removeFromFilter(player, entity, getInputHandler()))
                        .itemHandler(getInputHandler()))

                .add(registry.playerInventory(4)
                        .name("playerSlots")
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> addToFilter(player, entity, getInputHandler())))
                ;
    }

    private IGuiComponent<?> createOutputGui(final Pair<String, String> pair, IGuiComponentRegistry registry) {
        return HoloGuiTools.createPanelWithHelp(registry, entity -> entity.switchTag(pair.getLeft() + ":" + TAG_HELP))
                .add(registry.text(2.3, -.2, 1, 1).text("Output Config").color(0xaaccff))

                .add(registry.iconToggle(0.5, 0.5, 1, 1)
                        .getter(player -> outputNbt)
                        .icon(registry.image(Icons.NBT_OFF))
                        .selected(registry.image(Icons.NBT_ON)))
                .add(registry.iconToggle(0.5, 1.5, 1, 1)
                        .getter(player -> outputDamage)
                        .icon(registry.image(Icons.DAM_OFF))
                        .selected(registry.image(Icons.DAM_ON)))
                .add(registry.iconToggle(0.5, 2.5, 1, 1)
                        .getter(player -> outputOredict)
                        .icon(registry.image(Icons.ORE_OFF))
                        .selected(registry.image(Icons.ORE_ON)))

                .add(registry.slots(2.5, 1.5, 6, 2)
                        .name("slots")
                        .fullBright()
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> removeFromFilter(player, entity, getOutputHandler()))
                        .itemHandler(getOutputHandler()))

                .add(registry.playerInventory(4)
                        .name("playerSlots")
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> addToFilter(player, entity, getOutputHandler())))
                ;
    }

    private void toggleInputNBT() {
        inputNbt = !inputNbt;
    }

    private void toggleInputDamage() {
        inputDamage = !inputDamage;
    }

    private void toggleInputOre() {
        inputOredict = !inputOredict;
    }

    private void addToFilter(EntityPlayer player, IHoloGuiEntity entity, SimpleItemHandler filter) {
        entity.findComponent("playerSlots").ifPresent(component -> {
            if (component instanceof IPlayerInventory) {
                int selected = ((IPlayerInventory) component).getSelected();
                if (selected != -1) {
                    ItemStack extracted = player.inventory.getStackInSlot(selected);
                    if (!extracted.isEmpty()) {
                        ItemHandlerHelper.insertItem(filter, extracted, false);
                        AutoFieldTile.notifyField(world, pos);
                        markDirtyClient();
                    }
                }
            }
        });
    }

    private void removeFromFilter(EntityPlayer player, IHoloGuiEntity entity, SimpleItemHandler filter) {
        entity.findComponent("slots").ifPresent(component -> {
            if (component instanceof ISlots) {
                int selected = ((ISlots) component).getSelected();
                if (selected != -1) {
                    filter.setItemStack(selected, ItemStack.EMPTY);
                    AutoFieldTile.notifyField(world, pos);
                    markDirtyClient();
                }
            }
        });
    }

    private Pair<String, String> getSlotTag(String tag) {
        String[] split = StringUtils.split(tag, ":");
        final String slot = split[0];
        final String t = split.length > 1 ? split[1] : TAG_MAIN;
        return Pair.of(slot, t);
    }

    @Override
    public void syncToClient() {
//        markDirtyClient();
    }
}
