package mcjty.ariente.entities;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class RenderArientePearl {
    public static EntityRendererProvider<EntityArientePearl> FACTORY = context -> new ThrownItemRenderer(context);
}
