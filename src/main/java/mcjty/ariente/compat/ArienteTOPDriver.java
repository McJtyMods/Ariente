package mcjty.ariente.compat;

import mcjty.ariente.api.ISignalChannel;
import mcjty.ariente.blocks.aicore.AICoreTile;
import mcjty.ariente.blocks.generators.NegariteGeneratorTile;
import mcjty.ariente.blocks.generators.PosiriteGeneratorTile;
import mcjty.ariente.blocks.utility.AlarmTile;
import mcjty.ariente.blocks.utility.LockTile;
import mcjty.ariente.blocks.utility.StorageTile;
import mcjty.ariente.blocks.utility.WarperTile;
import mcjty.ariente.blocks.utility.wireless.SignalReceiverTile;
import mcjty.ariente.blocks.utility.wireless.WirelessLockTile;
import mcjty.ariente.power.IPowerBlob;
import mcjty.ariente.power.IPowerUser;
import mcjty.ariente.setup.Registration;
import mcjty.lib.compat.theoneprobe.McJtyLibTOPDriver;
import mcjty.lib.compat.theoneprobe.TOPDriver;
import mcjty.lib.varia.Tools;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

import static mcjty.theoneprobe.api.TextStyleClass.INFO;
import static mcjty.theoneprobe.api.TextStyleClass.WARNING;

public class ArienteTOPDriver implements TOPDriver {

    public static final ArienteTOPDriver DRIVER = new ArienteTOPDriver();

    private final Map<ResourceLocation, TOPDriver> drivers = new HashMap<>();

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        ResourceLocation id = blockState.getBlock().getRegistryName();
        if (!drivers.containsKey(id)) {
            Block block = blockState.getBlock();
            if (block == Registration.AICORE.get()) {
                drivers.put(id, new AICoreDriver());
            } else if (block == Registration.NEGARITE_GENERATOR.get()) {
                drivers.put(id, new NegariteGeneratorDriver());
            } else if (block == Registration.POSIRITE_GENERATOR.get()) {
                drivers.put(id, new PosiriteGeneratorDriver());
            } else if (block == Registration.SIGNAL_RECEIVER.get()) {
                drivers.put(id, new SignalReceiverDriver());
            } else if (block == Registration.WIRELESS_LOCK.get()) {
                drivers.put(id, new WirelessLockDriver());
            } else if (block == Registration.LOCK.get()) {
                drivers.put(id, new LockDriver());
            } else if (block == Registration.ALARM.get()) {
                drivers.put(id, new AlarmDriver());
            } else if (block == Registration.STORAGE.get()) {
                drivers.put(id, new StorageDriver());
            } else if (block == Registration.WARPER.get()) {
                drivers.put(id, new WarperDriver());
            } else {
                drivers.put(id, new DefaultDriver());
            }
        }
        TOPDriver driver = drivers.get(id);
        if (driver != null) {
            driver.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        }
    }

    private static class DefaultDriver implements TOPDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            McJtyLibTOPDriver.DRIVER.addStandardProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getBlockEntity(data.getPos()), (IPowerBlob te) -> {
                probeInfo.text(CompoundText.createLabelInfo("Network: ", te.getCableId()));
            });
            Tools.safeConsume(world.getBlockEntity(data.getPos()), (ISignalChannel te) -> {
                probeInfo.text(CompoundText.createLabelInfo("Channel: ", te.getChannel(false)));
            });
            Tools.safeConsume(world.getBlockEntity(data.getPos()), (IPowerUser te) -> {
                probeInfo.text(CompoundText.createLabelInfo("Using: ", te.getUsingPower() + " flux"));
            });
        }
    }

    private static class AICoreDriver extends DefaultDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getBlockEntity(data.getPos()), (AICoreTile te) -> {
                probeInfo.text(CompoundText.createLabelInfo("City: ", te.getCityName()));
            }, "Bad tile entity!");
        }
    }

    private static class NegariteGeneratorDriver extends DefaultDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getBlockEntity(data.getPos()), (NegariteGeneratorTile te) -> {
                if (te.isWorking()) {
                    probeInfo.text(CompoundText.createLabelInfo("Generating: ",NegariteGeneratorTile.POWERGEN + " flux"));
                }
            }, "Bad tile entity!");
        }
    }

    private static class PosiriteGeneratorDriver extends DefaultDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getBlockEntity(data.getPos()), (PosiriteGeneratorTile te) -> {
                if (te.isWorking()) {
                    probeInfo.text(CompoundText.createLabelInfo("Generating: ", PosiriteGeneratorTile.POWERGEN + " flux"));
                }
            }, "Bad tile entity!");
        }
    }

    private static class SignalReceiverDriver extends DefaultDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getBlockEntity(data.getPos()), (SignalReceiverTile te) -> {
                probeInfo.text(CompoundText.create().style(WARNING).text("Output: ").style(TextStyleClass.HIGHLIGHTED).text(String.valueOf(te.checkOutput())));
            }, "Bad tile entity!");
        }
    }

    private static class WirelessLockDriver extends DefaultDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getBlockEntity(data.getPos()), (WirelessLockTile te) -> {
                if (te.isLocked()) {
                    probeInfo.text(CompoundText.create().style(WARNING).text("Locked!"));
                }
            }, "Bad tile entity!");
        }
    }

    private static class LockDriver extends DefaultDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getBlockEntity(data.getPos()), (LockTile te) -> {
                probeInfo.text(CompoundText.createLabelInfo("Key ", te.getKeyId()));
                if (te.isLocked()) {
                    probeInfo.text(CompoundText.create().style(WARNING).text("Locked!"));
                }
            }, "Bad tile entity!");
        }
    }

    private static class AlarmDriver extends DefaultDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getBlockEntity(data.getPos()), (AlarmTile te) -> {
                switch (te.getAlarmType()) {
                    case DEAD:
                        probeInfo.text(CompoundText.create().style(INFO).text("City eliminated!"));
                        break;
                    case SAFE:
                        probeInfo.text(CompoundText.create().style(INFO).text("City ok"));
                        break;
                    case ALERT:
                        probeInfo.text(CompoundText.create().style(INFO).text("City Alert!"));
                        break;
                }
            }, "Bad tile entity!");
        }
    }

    private static class StorageDriver extends DefaultDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getBlockEntity(data.getPos()), (StorageTile te) -> {
                String keyId = te.getKeyId();
                if (keyId != null && !keyId.isEmpty()) {
                    probeInfo.text(CompoundText.createLabelInfo("Key ", keyId));
                }
                if (te.isLocked()) {
                    probeInfo.text(CompoundText.create().style(WARNING).text("Locked!"));
                }
            }, "Bad tile entity!");
        }
    }

    private static class WarperDriver extends DefaultDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getBlockEntity(data.getPos()), (WarperTile te) -> {
                int pct = te.getChargePercentage();
                probeInfo.text(CompoundText.createLabelInfo( "Charged: ", pct + "%"));
            }, "Bad tile entity!");
        }
    }
}
