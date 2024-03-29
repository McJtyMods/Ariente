package mcjty.ariente.blocks.aicore;

import mcjty.ariente.api.IAICoreTile;
import mcjty.ariente.api.IAlarmMode;
import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityAISystem;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.TickingTileEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.core.BlockPos;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;

public class AICoreTile extends TickingTileEntity implements IAlarmMode, IAICoreTile {

    private ChunkPos cityCenter;
    private int tickCounter = 10;
    private String cityName = "";

    public AICoreTile(BlockPos pos, BlockState state) {
        super(Registration.AICORE_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .properties(Block.Properties.of(Material.METAL)
                        .strength(20.0f, 800))
//                .flags(REDSTONE_CHECK, RENDER_SOLID, RENDER_CUTOUT)
                .topDriver(DRIVER)
                .tileEntitySupplier(AICoreTile::new)
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }
        };
    }

    @Override
    public void tickServer() {
        if (tickCounter > 0) {
            tickCounter--;
            return;
        }
        tickCounter = 10;

        // @todo check if Ariente World is there!
        if (getCityCenter() != null) {
            ICityAISystem cityAISystem = ArienteWorldCompat.getCityAISystem(level);
            ICityAI cityAI = cityAISystem.getCityAI(cityCenter);
            if (cityAI.tick(this)) {
                cityAISystem.saveSystem();
            }
        }
    }

    @Override
    public boolean isHighAlert() {
        return true;
    }

    private ChunkPos getCityCenter() {
        if (cityCenter == null) {
            cityCenter = new ChunkPos(worldPosition.getX() >> 4, worldPosition.getZ() >> 4);
            // @todo check if Ariente World is there!
            cityCenter = ArienteWorldCompat.getArienteWorld().getNearestCityCenter(cityCenter);
        }
        return cityCenter;
    }

    @Override
    public void onReplaced(Level world, BlockPos pos, BlockState state, BlockState newstate) {
        super.onReplaced(world, pos, state, newstate);
        if (!this.level.isClientSide) {
            if (getCityCenter() != null) {
                ICityAISystem cityAISystem = ArienteWorldCompat.getCityAISystem(world);
                ICityAI cityAI = cityAISystem.getCityAI(cityCenter);
                cityAI.breakAICore(world, pos);
            }
        }
    }

    @Override
    public BlockPos getCorePos() {
        return getBlockPos();
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        CompoundTag info = tagCompound.getCompound("Info");
        cityName = info.getString("cityName");
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);
        getOrCreateInfo(tagCompound).putString("cityName", cityName);
    }

    @Override
    public void setCityName(String name) {
        cityName = name;
        markDirtyQuick();
    }

    @Override
    public String getCityName() {
        return cityName;
    }
}
