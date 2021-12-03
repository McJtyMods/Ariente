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
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.BlockPosTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;

public class AICoreTile extends GenericTileEntity implements ITickableTileEntity, IAlarmMode, IAICoreTile {

    private ChunkPos cityCenter;
    private int tickCounter = 10;
    private String cityName = "";

    public AICoreTile() {
        super(Registration.AICORE_TILE.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .properties(Block.Properties.of(Material.METAL)
                        .harvestTool(ToolType.PICKAXE)
                        .harvestLevel(2)
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
    public void tick() {
        if (!level.isClientSide) {
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
    }

    @Override
    public boolean isHighAlert() {
        return true;
    }

    private ChunkPos getCityCenter() {
        if (cityCenter == null) {
            cityCenter = BlockPosTools.getChunkCoordFromPos(worldPosition);
            // @todo check if Ariente World is there!
            cityCenter = ArienteWorldCompat.getArienteWorld().getNearestCityCenter(cityCenter);
        }
        return cityCenter;
    }

    @Override
    public void onReplaced(World world, BlockPos pos, BlockState state, BlockState newstate) {
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
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        CompoundNBT info = tagCompound.getCompound("Info");
        cityName = info.getString("cityName");
    }

    @Override
    public CompoundNBT save(CompoundNBT tagCompound) {
        tagCompound = super.save(tagCompound);
        getOrCreateInfo(tagCompound).putString("cityName", cityName);
        return tagCompound;
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
