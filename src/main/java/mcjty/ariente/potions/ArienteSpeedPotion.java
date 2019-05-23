package mcjty.ariente.potions;

import mcjty.ariente.Ariente;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class ArienteSpeedPotion extends Potion {

    public ArienteSpeedPotion() {
        super(false, 0xffff0000);
        setPotionName("arienteSpeed");
        setRegistryName(new ResourceLocation(Ariente.MODID, "arienteSpeed"));
        registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, UUID.randomUUID().toString(), 0.2, 2);
        registerPotionAttributeModifier(SharedMonsterAttributes.FLYING_SPEED, UUID.randomUUID().toString(), 0.2, 2);
    }
}
