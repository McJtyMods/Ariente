package mcjty.ariente.cables;

import net.minecraft.client.resources.model.*;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import mcjty.ariente.Ariente;
import mcjty.lib.client.BaseGeometry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

public class CableModelLoader implements IGeometryLoader<CableModelLoader.CableModelGeometry> {

    @Override
    public CableModelGeometry read(JsonObject modelContents, JsonDeserializationContext deserializationContext) {
        return new CableModelGeometry();
    }

    public static class CableModelGeometry extends BaseGeometry<CableModelGeometry> {

        @Override
        public BakedModel bake() {
            return new GenericCableBakedModel();
        }

        @Override
        public Collection<Material> getMaterials() {
            List<Material> materials = new ArrayList<>();
            materials.add(new Material(InventoryMenu.BLOCK_ATLAS, ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "block/cables/connector_side")));

            for (CableColor color : CableColor.VALUES) {
                String typeName = color.getSerializedName();
                materials.add(new Material(InventoryMenu.BLOCK_ATLAS, ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "block/cables/"+typeName+"/connector")));
                materials.add(new Material(InventoryMenu.BLOCK_ATLAS, ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "block/cables/"+typeName+"/normal_corner_netcable")));
                materials.add(new Material(InventoryMenu.BLOCK_ATLAS, ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "block/cables/"+typeName+"/normal_cross_netcable")));
                materials.add(new Material(InventoryMenu.BLOCK_ATLAS, ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "block/cables/"+typeName+"/normal_end_netcable")));
                materials.add(new Material(InventoryMenu.BLOCK_ATLAS, ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "block/cables/"+typeName+"/normal_netcable")));
                materials.add(new Material(InventoryMenu.BLOCK_ATLAS, ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "block/cables/"+typeName+"/normal_none_netcable")));
                materials.add(new Material(InventoryMenu.BLOCK_ATLAS, ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "block/cables/"+typeName+"/normal_three_netcable")));
            }
            return materials;
        }
    }
}
