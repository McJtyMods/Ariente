package mcjty.ariente.gui;

import com.google.common.base.Optional;
import mcjty.ariente.Ariente;
import mcjty.ariente.entities.FluxLevitatorEntity;
import mcjty.ariente.sounds.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HoloGuiEntity extends Entity {

    private static final DataParameter<Optional<BlockPos>> GUITILE = EntityDataManager.createKey(HoloGuiEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final DataParameter<String> TAG = EntityDataManager.createKey(HoloGuiEntity.class, DataSerializers.STRING);
    private static final DataParameter<String> GUIID = EntityDataManager.createKey(HoloGuiEntity.class, DataSerializers.STRING);
    private static final DataParameter<Boolean> SMALL = EntityDataManager.createKey(HoloGuiEntity.class, DataSerializers.BOOLEAN);

    private AxisAlignedBB playerDetectionBox = null;

    private int timeout;
    private int maxTimeout;
    private int ticks;  // For syncing TE to client
    private IGuiComponent panel;

    // Client side only
    private double cursorX;
    private double cursorY;
    private Vec3d hit;
    private String lastGuiId = null;   // Last guiId that was rendered on the client. If it changes then we have to redo the gui
    public int tooltipTimeout = 10;
    public IGuiComponent tooltipComponent = null;

    public HoloGuiEntity(World worldIn) {
        super(worldIn);
        maxTimeout = 20 * 4;
        timeout = maxTimeout;
        ticks = 5;
        setSize(1f, 1f);
    }

    public double getCursorX() {
        return cursorX;
    }

    public double getCursorY() {
        return cursorY;
    }

    public void setMaxTimeout(int maxTimeout) {
        this.maxTimeout = maxTimeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Vec3d getHit() {
        return hit;
    }

    public void setGuiTile(BlockPos guiTile) {
        this.dataManager.set(GUITILE, Optional.fromNullable(guiTile));
    }

    public BlockPos getGuiTile() {
        return (BlockPos) ((Optional) this.dataManager.get(GUITILE)).orNull();
    }

    public String getGuiId() {
        return this.dataManager.get(GUIID);
    }

    public void setGuiId(String guiId) {
        this.dataManager.set(GUIID, guiId);
    }

    public void setTag(String tag) {
        this.dataManager.set(TAG, tag);
    }

    public String getTag() {
        return this.dataManager.get(TAG);
    }

    public void setSmall(boolean b) {
        this.dataManager.set(SMALL, b);
    }

    public boolean isSmall() {
        return this.dataManager.get(SMALL);
    }

    @Override
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
//        System.out.println("setPositionAndRotationDirect: yaw = " + yaw);
        super.setPositionAndRotationDirect(x, y, z, yaw, pitch, posRotationIncrements, teleport);
    }

    @Override
    protected void setRotation(float yaw, float pitch) {
        super.setRotation(yaw, pitch);
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);
    }

    @Override
    public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
        super.setLocationAndAngles(x, y, z, yaw, pitch);
//        System.out.println("yaw = " + yaw);
    }



    @Override
    public void move(MoverType type, double x, double y, double z) {
    // @todo remove
        super.move(type, x, y, z);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (world.isRemote) {
            String id = getGuiId();
            if (id != null && !id.isEmpty()) {
                if (!id.equals(lastGuiId)) {
                    panel = null;
                    lastGuiId = id;
                }
            }
            onUpdateClient();
        } else {
            onUpdateServer();
        }
    }

    private void onUpdateServer() {
        if (playerDetectionBox == null) {
            playerDetectionBox = new AxisAlignedBB(posX - 10, posY - 10, posZ - 10, posX + 10, posY + 10, posZ + 10);
        }

        ticks--;
        if (ticks < 0) {
            ticks = 5;
            String id = getGuiId();
            if (id != null && !id.isEmpty()) {
                // @todo do we have to do anything here?
            } else {
                BlockPos tile = getGuiTile();
                if (tile != null) {
                    TileEntity te = world.getTileEntity(tile);
                    if (te instanceof IGuiTile) {
                        ((IGuiTile) te).syncToClient();
                    }
                }
            }
        }

        if (timeout < 2000000000) {
            timeout--;
            if (timeout <= 0) {
                world.playSound(null, posX, posY, posZ, ModSounds.guiopen, SoundCategory.PLAYERS, 0.2f, 1.0f);
                this.setDead();
            }
            if (world.getEntitiesWithinAABB(EntityPlayer.class, playerDetectionBox)
                    .stream()
                    .anyMatch(this::playerLooksAtMe)) {
                timeout = maxTimeout;
            }
        }
    }


    private boolean playerLooksAtMe(EntityPlayer player) {
        Vec3d lookVec = getLookVec();
        Vec3d v = getIntersect3D(player, lookVec);
        Vec2f vec2d = get2DProjection(lookVec, v);

        double cx;
        double cy;
        if (isSmall()) {
            cx = vec2d.x * 10 / .6 - 4.1;
            cy = vec2d.y * 10 / .6 - 4.1;
        } else {
            cx = vec2d.x * 10 - .8;
            cy = vec2d.y * 10 - .8;
        }
        return cx >= 0 && cx <= 10 && cy >= 0 && cy <= 10;
    }

    private void onUpdateClient() {
        EntityPlayer player = Ariente.proxy.getClientPlayer();
        Vec3d lookVec = getLookVec();
        Vec3d v = getIntersect3D(player, lookVec);
        Vec2f vec2d = get2DProjection(lookVec, v);

        if (isSmall()) {
            cursorX = vec2d.x * 10 / .6 - 4.1;
            cursorY = vec2d.y * 10 / .6 - 4.1;
        } else {
            cursorX = vec2d.x * 10 - .8;
            cursorY = vec2d.y * 10 - .8;
        }

        hit = v;
    }

    public void switchGui(String guiId) {
        setGuiId(guiId);
        panel = null;
    }

    public IGuiComponent getGui(EntityPlayer player) {
        if (panel == null) {
            String id = getGuiId();
            if (id != null && !id.isEmpty()) {
                panel = Ariente.instance.guiRegistry.createGui(id, player);
            } else {
                BlockPos tile = getGuiTile();
                if (tile != null) {
                    TileEntity te = world.getTileEntity(tile);
                    if (te instanceof IGuiTile) {
                        IGuiTile guiTile = (IGuiTile) te;
                        panel = guiTile.createGui(getTag());
                    }
                }
            }
        }
        return panel;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        world.playSound(posX, posY, posZ, ModSounds.guiopen, SoundCategory.PLAYERS, 0.2f, 1.0f, true);  // @todo config
        setDead();
        return false;
    }


    private Vec2f intersect(EntityPlayer player) {
        Vec3d lookVec = getLookVec();
        Vec3d v = getIntersect3D(player, lookVec);
        return get2DProjection(lookVec, v);
    }

    @Override
    public boolean canRiderInteract() {
        return true;
    }

    private Vec2f get2DProjection(Vec3d lookVec, Vec3d v) {
        double x = v.x;
        double y = v.y;
        double z = v.z;
        // Origin on plane is posX, posY, posZ
        // Point on plane in 2D x direction: cross( (0,1,0), (xn,yn,zn) )
        // Point on plane in 2D y direction: posX, posY-1, posZ
        Vec3d vx = lookVec.crossProduct(new Vec3d(0, 1, 0));    // @todo optimize
        Vec3d vy = new Vec3d(0, -1, 0);
//        Vec3d vy = vx.crossProduct(new Vec3d(1, 0, 0));
        x -= posX;
        y -= posY;
        z -= posZ;
        double x2d = vx.x * x + vx.y * y + vx.z * z + .5;
        double y2d = vy.x * x + vy.y * y + vy.z * z + 1;

        return new Vec2f((float) x2d, (float) y2d);
    }

    private Vec3d getIntersect3D(EntityPlayer player, Vec3d lookVec) {
        // Center point of plane: posX, posY, posZ
        // Perpendicular to the plane: getLookVec()
        double xn = lookVec.x;
        double yn = lookVec.y;
        double zn = lookVec.z;

        // Plane: Ax + By + Cz + D = 0
        double D = -(xn * posX + yn * posY + zn * posZ);
        double A = xn;
        double B = yn;
        double C = zn;

        // Line (from player): (x = x1 + at, y = y1 + bt, z = z1 + ct)
        double x1 = player.posX;
        double y1 = player.posY + player.eyeHeight;
        double z1 = player.posZ;
        Vec3d playerLookVec = player.getLookVec();
        double a = playerLookVec.x;
        double b = playerLookVec.y;
        double c = playerLookVec.z;

        // Intersection:
        double factor = (A * x1 + B * y1 + C * z1 + D) / (A * a + B * b + C * c);
        double x = x1 - a * factor;
        double y = y1 - b * factor;
        double z = z1 - c * factor;
        return new Vec3d(x, y, z);
    }

    @Override
    public boolean hitByEntity(Entity entityIn) {
        if (entityIn instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityIn;
            Vec2f vec2d = intersect(player);
            IGuiComponent gui = getGui(player);
            if (gui != null) {
                double x;
                double y;
                if (isSmall()) {
                    x = vec2d.x * 10 / .6 - 4.1;
                    y = vec2d.y * 10 / .6 - 4.1;
                } else {
                    x = vec2d.x * 10 - .8;
                    y = vec2d.y * 10 - .8;
                }

                if (!world.isRemote) {
                    gui.hit(player, this, x, y);
                } else {
                    gui.hitClient(player, this, x, y);
                }
            }
        }
        return true;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(GUITILE, Optional.absent());
        this.dataManager.register(TAG, "");
        this.dataManager.register(GUIID, "");
        this.dataManager.register(SMALL, false);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.timeout = compound.getInteger("timeout");
        this.maxTimeout = compound.getInteger("maxTimeout");
        if (compound.hasKey("guix")) {
            int x = compound.getInteger("guix");
            int y = compound.getInteger("guiy");
            int z = compound.getInteger("guiz");
            setGuiTile(new BlockPos(x, y, z));
        }
        setTag(compound.getString("tag"));
        setGuiId(compound.getString("guiId"));
        setSmall(compound.getBoolean("small"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("timeout", timeout);
        compound.setInteger("maxTimeout", maxTimeout);
        BlockPos tile = getGuiTile();
        if (tile != null) {
            compound.setInteger("guix", tile.getX());
            compound.setInteger("guiy", tile.getY());
            compound.setInteger("guiz", tile.getZ());
        }
        String tag = getTag();
        if (tag != null) {
            compound.setString("tag", tag);
        }
        String guiid = getGuiId();
        if (guiid != null) {
            compound.setString("guiId", guiid);
        }
        if (isSmall()) {
            compound.setBoolean("small", true);
        }
    }
}
