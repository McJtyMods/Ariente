package mcjty.ariente.entities;

import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.IGuiComponent;
import mcjty.ariente.gui.components.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class FluxLevitatorGui {

    public static IGuiComponent createGui(EntityPlayer player) {
        return new HoloPanel(0, 0, 8, 8)

                .add(new HoloButton(2, 2, 1, 1).image(128+32, 128+16).hover(128+32+16, 128+16)
                        .hitEvent((component, p, holo, x, y) -> changeSpeed(holo, -10)))
                .add(new HoloButton(3, 2, 1, 1).image(128+32, 128).hover(128+32+16, 128)
                        .hitEvent((component, p, holo, x, y) -> changeSpeed(holo, -5)))
                .add(new HoloButton(4, 2, 1, 1).image(128+80, 128).hover(128+80+16, 128)
                        .hitEvent((component, p, holo, x, y) -> changeSpeed(holo, 0)))
                .add(new HoloButton(5, 2, 1, 1).image(128, 128).hover(128+16, 128)
                        .hitEvent((component, p, holo, x, y) -> changeSpeed(holo, 5)))
                .add(new HoloButton(6, 2, 1, 1).image(128, 128+16).hover(128+16, 128+16)
                        .hitEvent((component, p, holo, x, y) -> changeSpeed(holo, 10)))

                .add(new HoloText(1, 4, 4, 1, "Speed:", 0xffffff))
                .add(new HoloNumber(5, 4, 1, 1, 0x00ff00, (p,holo) -> getSpeed(holo)))
                ;
    }

    private static int getSpeed(HoloGuiEntity holo) {
        Entity ridingEntity = holo.getRidingEntity();
        if (ridingEntity instanceof FluxLevitatorEntity) {
            FluxLevitatorEntity levitator = (FluxLevitatorEntity) ridingEntity;
            boolean front = holo.getUniqueID().equals(levitator.getHoloFrontUUID());
            return front ? levitator.getSpeed() : -levitator.getSpeed();
        } else {
            return 0;
        }
    }

    private static void changeSpeed(HoloGuiEntity holo, int amount) {
        Entity ridingEntity = holo.getRidingEntity();
        if (ridingEntity instanceof FluxLevitatorEntity) {
            FluxLevitatorEntity levitator = (FluxLevitatorEntity) ridingEntity;
            if (amount != 0) {
                boolean front = holo.getUniqueID().equals(levitator.getHoloFrontUUID());
                if (!front) {
                    amount = -amount;
                }
                levitator.changeSpeed(levitator.getSpeed() + amount);
            } else {
                levitator.changeSpeed(0);
            }
        }
    }
}
