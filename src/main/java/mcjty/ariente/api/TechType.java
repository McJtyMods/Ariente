package mcjty.ariente.api;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public enum TechType implements StringRepresentable {
    DOTS("dots", "blacktech_dots", "blacktech_dots_glow"),
    LINES("lines", "blacktech_lines", "blacktech_lines_glow"),
    VAR1("var1", "blacktech_var1", "blacktech_var1_glow"),
    RED_VAR1("red_var1", "blacktech_red_var1", "blacktech_red_var1_glow"),
    LINES_GLOW("lines_glow", "blacktech_anim_lines", "blacktech_anim_lines_glow"),
    RED_LINES_GLOW("red_lines_glow", "blacktech_red_anim_lines", "blacktech_red_anim_lines_glow"),
    PANEL("panel", "blacktech_panel", "blacktech_panel_glow"),
    RED_LINES("red_lines", "blacktech_red_lines", "blacktech_red_lines_glow"),
    DARKBLUE_BLUE("darkblue_blue", "blacktech_blue_blue", "blacktech_lines_glow"),
    DARKBLUE_RED("darkblue_red", "blacktech_blue_red", "blacktech_red_lines_glow"),
    DOTS2("dots2", "blacktech_dots2", "blacktech_dots2_glow"),
    VAR1_ANIM("var1_anim", "blacktech_blue_var1_anim", "blacktech_blue_var1_anim_glow"),
    RED_VAR1_ANIM("red_var1_anim", "blacktech_red_var1_anim", "blacktech_red_var1_anim_glow"),
    ;

    public static final TechType[] VALUES = new TechType[TechType.values().length];

    public static final EnumProperty<TechType> TYPE = EnumProperty.create("type", TechType.class);

    static {
        int i = 0;
        for (TechType type : TechType.values()) {
            VALUES[i++] = type;
        }
    }

    private final String name;
    private final String texture;
    private final String textureGlow;

    TechType(String name, String texture, String textureGlow) {
        this.name = name;
        this.texture = texture;
        this.textureGlow = textureGlow;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String getTexture() {
        return texture;
    }

    public String getTextureGlow() {
        return textureGlow;
    }

    @Override
    public String toString() {
        return name;
    }
}
