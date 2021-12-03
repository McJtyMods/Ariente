package mcjty.ariente.cables;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import mcjty.ariente.Ariente;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
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
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public CableModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        return new CableModelGeometry();
    }

    public static class CableModelGeometry implements IModelGeometry<CableModelGeometry> {
        @Override
        public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
            return new GenericCableBakedModel();
        }

        @Override
        public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            List<RenderMaterial> materials = new ArrayList<>();
            materials.add(new RenderMaterial(PlayerContainer.BLOCK_ATLAS, new ResourceLocation(Ariente.MODID, "block/cables/connector_side")));

            for (CableColor color : CableColor.VALUES) {
                String typeName = color.getSerializedName();
                materials.add(new RenderMaterial(PlayerContainer.BLOCK_ATLAS, new ResourceLocation(Ariente.MODID, "block/cables/"+typeName+"/connector")));
                materials.add(new RenderMaterial(PlayerContainer.BLOCK_ATLAS, new ResourceLocation(Ariente.MODID, "block/cables/"+typeName+"/normal_corner_netcable")));
                materials.add(new RenderMaterial(PlayerContainer.BLOCK_ATLAS, new ResourceLocation(Ariente.MODID, "block/cables/"+typeName+"/normal_cross_netcable")));
                materials.add(new RenderMaterial(PlayerContainer.BLOCK_ATLAS, new ResourceLocation(Ariente.MODID, "block/cables/"+typeName+"/normal_end_netcable")));
                materials.add(new RenderMaterial(PlayerContainer.BLOCK_ATLAS, new ResourceLocation(Ariente.MODID, "block/cables/"+typeName+"/normal_netcable")));
                materials.add(new RenderMaterial(PlayerContainer.BLOCK_ATLAS, new ResourceLocation(Ariente.MODID, "block/cables/"+typeName+"/normal_none_netcable")));
                materials.add(new RenderMaterial(PlayerContainer.BLOCK_ATLAS, new ResourceLocation(Ariente.MODID, "block/cables/"+typeName+"/normal_three_netcable")));
            }
            return materials;
        }
    }
}
