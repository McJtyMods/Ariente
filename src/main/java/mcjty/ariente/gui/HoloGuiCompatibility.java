package mcjty.ariente.gui;

import java.util.function.Function;
import javax.annotation.Nullable;
import mcjty.ariente.Ariente;
import mcjty.ariente.multipart.MultipartTE;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.IHoloGuiHandler;
import mcjty.lib.varia.Logging;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.InterModComms;

public class HoloGuiCompatibility {

    private static boolean registered = false;

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        InterModComms.sendTo("hologui", "getHoloHandler", GetHoloHandler::new);
    }

    public static class GetHoloHandler implements Function<IHoloGuiHandler, Void> {

        @Nullable
        @Override
        public Void apply(IHoloGuiHandler holoHandler) {
            Ariente.guiHandler = holoHandler;
            Logging.log("Enabled support for Holo Gui");
            ModGuis.init();
            Ariente.guiHandler.registerProvider((world, pos) -> {
                BlockEntity tileEntity = world.getBlockEntity(pos);
                if (tileEntity instanceof IGuiTile) {
                    return (IGuiTile) tileEntity;
                } else if (tileEntity instanceof MultipartTE) {
                    return new MultipartGuiTile(world, pos);
                }
                return null;
            });
            return null;
        }
    }
}
