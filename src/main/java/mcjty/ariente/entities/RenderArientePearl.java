package mcjty.ariente.entities;

import mcjty.ariente.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderArientePearl {
    static IRenderFactory<EntityArientePearl> FACTORY = manager -> new RenderSnowball(manager, ModItems.arientePearlItem, Minecraft.getMinecraft().getRenderItem());
}
