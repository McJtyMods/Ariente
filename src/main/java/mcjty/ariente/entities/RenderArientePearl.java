package mcjty.ariente.entities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderArientePearl {
    static IRenderFactory<EntityArientePearl> FACTORY = manager -> new SpriteRenderer(manager, Minecraft.getInstance().getItemRenderer());
}
