package mcjty.ariente.cities;

import com.google.gson.JsonObject;

public interface IAsset {

    // Called after the asset is fetched from the registry
    default void init() {}

    String getName();

    void readFromJSon(JsonObject object);
}
