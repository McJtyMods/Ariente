package mcjty.ariente.blocks.defense;

import mcjty.ariente.varia.Intersections;
import mcjty.ariente.varia.Triangle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ForceFieldPanelEntity extends Entity {

    private static final DataParameter<Integer> INDEX = EntityDataManager.<Integer>createKey(ForceFieldPanelEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Float> DX = EntityDataManager.<Float>createKey(ForceFieldPanelEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> DY = EntityDataManager.<Float>createKey(ForceFieldPanelEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> DZ = EntityDataManager.<Float>createKey(ForceFieldPanelEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> SCALE = EntityDataManager.<Float>createKey(ForceFieldPanelEntity.class, DataSerializers.FLOAT);

    private AxisAlignedBB aabb = null;

    public ForceFieldPanelEntity(World worldIn) {
        super(worldIn);
    }

    public ForceFieldPanelEntity(World worldIn, int index, float scale, Vec3d offset) {
        this(worldIn);
        setIndex(index);
        setOffset(offset);
        setScale(scale);

        // @todo not efficient
//        Triangle triangle = PentakisDodecahedron.getTriangle(getIndex());
//        Vec3d offs = triangle.getMid().scale(scale);
//        Vec3d entityPos = this.getPositionVector();
//        Vec3d a = triangle.getA().scale(scale).subtract(offs).add(entityPos);
//        Vec3d b = triangle.getB().scale(scale).subtract(offs).add(entityPos);
//        Vec3d c = triangle.getC().scale(scale).subtract(offs).add(entityPos);
//
//        double minx = Intersections.min3(a.x, b.x, c.x);
//        double miny = Intersections.min3(a.y, b.y, c.y);
//        double minz = Intersections.min3(a.z, b.z, c.z);
//        double maxx = Intersections.max3(a.x, b.x, c.x);
//        double maxy = Intersections.max3(a.y, b.y, c.y);
//        double maxz = Intersections.max3(a.z, b.z, c.z);
//        float width = (float) Math.max(maxx - minx, maxz - minz);
//        float height = (float) (maxy - miny);
//        setSize(width * 2.0f, height * 2.0f);
//        setSize(width, height);
    }

    @Override
    protected void setSize(float width, float height) {
        super.setSize(width, height);
//        System.out.println("width = " + width);
//        System.out.println("height = " + height);
//        AxisAlignedBB aabb = getEntityBoundingBox();
//        System.out.println("aabb = " + aabb);
    }

    @Override
    public AxisAlignedBB getEntityBoundingBox() {
        if (aabb == null) {
            // @todo don't do this!
            Triangle triangle = PentakisDodecahedron.getTriangle(getIndex());
            float scale = getScale();
            Vec3d offs = triangle.getMid().scale(scale);
            Vec3d entityPos = this.getPositionVector();
            Vec3d a = triangle.getA().scale(scale).subtract(offs).add(entityPos);
            Vec3d b = triangle.getB().scale(scale).subtract(offs).add(entityPos);
            Vec3d c = triangle.getC().scale(scale).subtract(offs).add(entityPos);

            double minx = Intersections.min3(a.x, b.x, c.x);
            double miny = Intersections.min3(a.y, b.y, c.y);
            double minz = Intersections.min3(a.z, b.z, c.z);
            double maxx = Intersections.max3(a.x, b.x, c.x);
            double maxy = Intersections.max3(a.y, b.y, c.y);
            double maxz = Intersections.max3(a.z, b.z, c.z);
            float width = (float) Math.max(maxx - minx, maxz - minz);
            float height = (float) (maxy - miny);

            double d0 = (double) width / 2.0D;
            aabb = new AxisAlignedBB(this.posX - d0, this.posY, this.posZ - d0, this.posX + d0, this.posY + (double) height, this.posZ + d0);
        }
        return aabb;
//        return super.getEntityBoundingBox();
    }

    public float getScale() {
        return this.dataManager.get(SCALE);
    }

    public void setScale(float scale) {
        this.dataManager.set(SCALE, scale);
    }

    public int getIndex() {
        return this.dataManager.get(INDEX);
    }

    public void setIndex(int index) {
        this.dataManager.set(INDEX, index);
    }

    public void setOffset(Vec3d offset) {
        this.dataManager.set(DX, (float)offset.x);
        this.dataManager.set(DY, (float)offset.y);
        this.dataManager.set(DZ, (float)offset.z);
    }

    public double getOffsetX() {
        return this.dataManager.get(DX);
    }
    public double getOffsetY() {
        return this.dataManager.get(DY);
    }
    public double getOffsetZ() {
        return this.dataManager.get(DZ);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean hitByEntity(Entity entityIn) {
        return true;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(INDEX, 0);
        this.dataManager.register(DX, 0.0f);
        this.dataManager.register(DY, 0.0f);
        this.dataManager.register(DZ, 0.0f);
        this.dataManager.register(SCALE, 1.0f);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        setIndex(compound.getInteger("index"));
        setOffset(new Vec3d(compound.getDouble("dx"), compound.getDouble("dy"), compound.getDouble("dz")));
        setScale(compound.getFloat("scale"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("index", getIndex());
        compound.setDouble("dx", getOffsetX());
        compound.setDouble("dy", getOffsetY());
        compound.setDouble("dz", getOffsetZ());
        compound.setFloat("scale", getScale());
    }

    @Override
    public void onEntityUpdate() {
//        super.onEntityUpdate();
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer player) {
        AxisAlignedBB box = player.getEntityBoundingBox();
        Triangle triangle = PentakisDodecahedron.getTriangle(getIndex());
        // @todo not very efficient
        float scale = getScale();
        Vec3d offs = triangle.getMid().scale(scale);
        Vec3d entityPos = this.getPositionVector();
        Vec3d a = triangle.getA().scale(scale).subtract(offs).add(entityPos);
        Vec3d b = triangle.getB().scale(scale).subtract(offs).add(entityPos);
        Vec3d c = triangle.getC().scale(scale).subtract(offs).add(entityPos);
        if (Intersections.boxTriangleTest(box, new Triangle(a, b, c))) {
            System.out.println("ForceFieldPanelEntity.onCollideWithPlayer");
        }
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        super.applyEntityCollision(entityIn);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return super.attackEntityFrom(source, amount);
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    @Override
    public RayTraceResult rayTrace(double blockReachDistance, float partialTicks) {
        return super.rayTrace(blockReachDistance, partialTicks);
    }
}
