package mcjty.ariente.cables;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import mcjty.ariente.Ariente;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class CableModelLoader implements IModelLoader<CableModelLoader.CableModelGeometry> {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {

    }

    @Override
    public CableModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        return new CableModelGeometry();
    }

    public static class CableModelGeometry implements IModelGeometry<CableModelGeometry> {
        @Override
        public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
            return new GenericCableBakedModel();
        }

        @Override
        public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            List<Material> materials = new ArrayList<>();
            materials.add(new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(Ariente.MODID, "block/cables/connector_side")));

            for (CableColor color : CableColor.VALUES) {
                String typeName = color.getSerializedName();
                materials.add(new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(Ariente.MODID, "block/cables/"+typeName+"/connector")));
                materials.add(new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(Ariente.MODID, "block/cables/"+typeName+"/normal_corner_netcable")));
                materials.add(new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(Ariente.MODID, "block/cables/"+typeName+"/normal_cross_netcable")));
                materials.add(new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(Ariente.MODID, "block/cables/"+typeName+"/normal_end_netcable")));
                materials.add(new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(Ariente.MODID, "block/cables/"+typeName+"/normal_netcable")));
                materials.add(new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(Ariente.MODID, "block/cables/"+typeName+"/normal_none_netcable")));
                materials.add(new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(Ariente.MODID, "block/cables/"+typeName+"/normal_three_netcable")));
            }
            return materials;
        }
    }
}
