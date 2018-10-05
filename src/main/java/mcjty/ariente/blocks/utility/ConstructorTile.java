package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.ai.CityAI;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.cities.ICityEquipment;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.RedstoneMode;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ConstructorTile extends GenericTileEntity implements DefaultSidedInventory, IGuiTile, ITickable, IPowerReceiver, ICityEquipment {

    public static final PropertyBool WORKING = PropertyBool.create("working");
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(new ResourceLocation(Ariente.MODID, "gui/constructor.gui"));
    public static final int SLOT_BLUEPRINT = 0;
    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, 1);

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }


    @Override
    public void update() {
    }

    @Override
    public IBlockState getActualState(IBlockState state) {
        return state.withProperty(WORKING, isWorking());
    }

    public boolean isWorking() {
        return isMachineEnabled();
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        boolean working = isWorking();

        super.onDataPacket(net, packet);

        if (world.isRemote) {
            // If needed send a render update.
            boolean newWorking = isWorking();
            if (newWorking != working) {
                world.markBlockRangeForRenderUpdate(getPos(), getPos());
                BlockPos p = pos.up();
                IBlockState state = world.getBlockState(p);
                while (state.getBlock() == ModBlocks.negariteTankBlock) {
                    world.markBlockRangeForRenderUpdate(p, p);
                    p = p.up();
                    state = world.getBlockState(p);
                }
            }
        }
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return new int[] { SLOT_BLUEPRINT };
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        return isItemValidForSlot(index, stack);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == SLOT_BLUEPRINT) {
            return stack.getItem() == ModItems.negariteDust;    // @todo blueprint
        }
        return true;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }



    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        probeInfo.text(TextStyleClass.LABEL + "Using: " + TextStyleClass.INFO + POWER_USAGE + " flux");
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
    }


    @Nullable
    @Override
    public Map<String, Object> save() {
        return null;
    }

    @Override
    public void load(Map<String, Object> data) {

    }

    @Override
    public void setup(CityAI cityAI, World world, boolean firstTime) {

    }

    @Override
    public IGuiComponent createGui(String tag, IGuiComponentRegistry registry) {
        return registry.panel(0, 0, 8, 8)
                .add(registry.text(0, 0, 8, 1).text("Constructor").color(0xaaccff))
//                .add(registry.stackIcon(0, 3, 1, 1).itemStack(new ItemStack(ModItems.negariteDust)))

                .add(registry.icon(1, 3, 1, 1).icon(128+64, 128))
                .add(registry.number(2, 3, 1, 1).color(0xffffff).getter((p,h) -> HoloGuiTools.countItem(p, ModItems.negariteDust)))

//                .add(registry.iconButton(2, 4, 1, 1).icon(128+32, 128+16).hover(128+32+16, 128+16)
//                        .hitEvent((component, player, entity1, x, y) -> toPlayer(player, 64)))
//                .add(registry.iconButton(3, 4, 1, 1).icon(128+32, 128).hover(128+32+16, 128)
//                        .hitEvent((component, player, entity1, x, y) -> toPlayer(player, 1)))
//                .add(registry.iconButton(5, 4, 1, 1).icon(128, 128).hover(128+16, 128)
//                        .hitEvent((component, player, entity1, x, y) -> toMachine(player, 1)))
//                .add(registry.iconButton(6, 4, 1, 1).icon(128, 128+16).hover(128+16, 128+16)
//                        .hitEvent((component, player, entity1, x, y) -> toMachine(player, 64)))

//                .add(registry.stackIcon(5, 3, 1, 1).itemStack(new ItemStack(ModBlocks.negariteGeneratorBlock)))
//                .add(registry.number(6, 3, 1, 1).color(0xffffff).getter(this::countNegariteGenerator))

                .add(registry.iconChoice(7, 6, 1, 1)
                        .getter((player) -> getRSModeInt())
                        .icon(128, 128+32)
                        .icon(128+16, 128+32)
                        .icon(128+32, 128+32)
                        .hitEvent((component, player, entity1, x, y) -> changeMode()))
                ;
    }

    private void changeMode() {
        int current = rsMode.ordinal() + 1;
        if (current >= RedstoneMode.values().length) {
            current = 0;
        }
        setRSMode(RedstoneMode.values()[current]);
        markDirtyClient();
    }

    @Override
    public void syncToClient() {

    }
}
