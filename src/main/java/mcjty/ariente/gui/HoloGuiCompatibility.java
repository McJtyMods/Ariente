package mcjty.ariente.gui;

import mcjty.ariente.Ariente;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.IHoloGuiHandler;
import mcjty.lib.multipart.MultipartTE;
import mcjty.lib.varia.Logging;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.InterModComms;

import javax.annotation.Nullable;
import java.util.function.Function;

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
                TileEntity tileEntity = world.getTileEntity(pos);
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
