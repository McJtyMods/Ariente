package mcjty.ariente.cities;

import mcjty.ariente.ai.CityAI;
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
     * Initialize this equipment for the given city AI. This
     * occurs only once when the city AI is first created
     */
    void initialize(CityAI cityAI, World world);

    /**
     * Setup this equipment immediatelly after finding it
     * whenever a city is created. This happens every time
     * a city AI is created (in contrast with initialize
     * that only happens the first time)
     */
    void setup(CityAI cityAI, World world);
}
