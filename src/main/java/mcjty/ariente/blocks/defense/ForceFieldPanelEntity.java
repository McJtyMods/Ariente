package mcjty.ariente.blocks.defense;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ForceFieldPanelEntity extends Entity {

    private int index;

    public ForceFieldPanelEntity(World worldIn) {
        super(worldIn);
        setSize(1f, 1f);
    }

    public ForceFieldPanelEntity(World worldIn, int index) {
        this(worldIn);
        this.index = index;
    }

    public int getIndex() {
        return index;
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
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        index = compound.getInteger("index");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("index", index);
    }
}
