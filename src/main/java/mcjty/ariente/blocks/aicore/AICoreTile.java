package mcjty.ariente.blocks.aicore;

import mcjty.ariente.api.IAICoreTile;
import mcjty.ariente.api.IAlarmMode;
import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityAISystem;
import mcjty.ariente.blocks.ArienteBlockEntity;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;

public class AICoreTile extends ArienteBlockEntity implements IAlarmMode, IAICoreTile {

    private ChunkPos cityCenter;
    private int tickCounter = 10;
    private String cityName = "";

    public AICoreTile(BlockPos pos, BlockState state) {
        super(Registration.AICORE_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock() {
        // With proper pickaxe tags, hardness 60 gives unenchanted netherite a ten-second mining time.
        return new BaseBlock(new BlockBuilder()
                .properties(Block.Properties.of().mapColor(MapColor.METAL)
                        .strength(60.0f, 800).noOcclusion())
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

        if (getCityCenter() != null) {
            ICityAISystem cityAISystem = ArienteWorldCompat.getCityAISystem(level);
            ICityAI cityAI = cityAISystem.getCityAI(cityCenter);
            if (cityAI != null && cityAI.tick(this)) {
                cityAISystem.saveSystem();
            }
        }
    }

    @Override
    public boolean isHighAlert() {
        return true;
    }

    private ChunkPos getCityCenter() {
        if (ArienteWorldCompat.getArienteWorld() == null
                || !level.dimension().equals(ArienteWorldCompat.getArienteWorld().getDimension())) {
            return null;
        }
        if (cityCenter == null) {
            cityCenter = new ChunkPos(worldPosition.getX() >> 4, worldPosition.getZ() >> 4);
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
                if (cityAI != null) {
                    cityAI.breakAICore(world, pos);
                    cityAISystem.saveSystem();
                }
            }
        }
    }

    @Override
    public BlockPos getCorePos() {
        return getBlockPos();
    }

    @Override
    public void loadAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.loadAdditional(tagCompound, provider);
        CompoundTag info = tagCompound.getCompound("Info");
        cityName = info.getString("cityName");
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.saveAdditional(tagCompound, provider);
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
