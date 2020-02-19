package mcjty.ariente.blocks;

import net.minecraft.state.BooleanProperty;

public class BlockProperties {

    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    public static final BooleanProperty UPPER = BooleanProperty.create("upper");
    public static final BooleanProperty LOWER = BooleanProperty.create("lower");

    public static final BooleanProperty LOCKED = BooleanProperty.create("locked");
    public static final BooleanProperty POWER = BooleanProperty.create("power");
}
