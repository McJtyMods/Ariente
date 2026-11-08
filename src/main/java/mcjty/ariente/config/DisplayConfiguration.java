package mcjty.ariente.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class DisplayConfiguration {

    public static ModConfigSpec.IntValue LIVE_CAMERA_TEXTURE_WIDTH;
    public static ModConfigSpec.IntValue LIVE_CAMERA_FPS;

    public static void init(ModConfigSpec.Builder client) {
        client.comment("Holographic display rendering settings").push("display");
        LIVE_CAMERA_TEXTURE_WIDTH = client
                .comment("Live camera image width in pixels; height follows the display module's aspect ratio.",
                        "Higher values improve detail but increase GPU memory use and rendering cost.")
                .defineInRange("liveCameraTextureWidth", 512, 128, 2048);
        LIVE_CAMERA_FPS = client
                .comment("Maximum live camera updates per second for each distinct visible feed.",
                        "Actual refresh rate also depends on game FPS and the shared budget of one feed update per game frame.",
                        "Matching views share a feed. Higher values increase rendering cost.")
                .defineInRange("liveCameraFps", 25, 1, 120);
        client.pop();
    }
}
