package mcjty.ariente.api;

import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Map;

public interface ICityEquipment {

    /**
     * Save settings for this equipment to json (for the persistance system)
     * Return null if nothing has to be saved
     */
    @Nullable
    Map<String, Object> save();

    void load(Map<String, Object> data);

    /**
     * Setup this equipment immediatelly after finding it
     * whenever a city is created. This happens every time
     * a city AI is created.
     *
     * If 'firstTime' is true then this should also do
     * initialization that is only relevant the first time
     */
    void setup(ICityAI cityAI, World world, boolean firstTime);
}
