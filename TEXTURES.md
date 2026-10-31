Ariente native texture traits (Forge 1.20.1)

Ariente implements the small set of rendering effects used by its own assets:
connected quadrants, minimum texture brightness and cutout overlays. This is an
independent implementation using Forge baked models and Minecraft's texture atlas.
It has no CTM dependency and includes no CTM implementation code.

The existing supplemental images retain their original quadrant layout. The
layout reference is https://github.com/Chisel-Team/ConnectedTexturesMod/blob/1.21/dev/src/main/resources/assets/ctm/ctm_logic/ctm.json .
Other CTM render types and configuration features are outside this implementation.

A base texture's .png.mcmeta can contain:

    {
      "animation": { "frametime": 5 },
      "ariente": {
        "connected_texture": "ariente:block/blacktech/blacktech_anim_lines-ctm",
        "glow_texture": "ariente:block/blacktech/blacktech_anim_lines_glow"
      }
    }

The separate glow texture's .png.mcmeta can contain:

    {
      "animation": { "frametime": 5 },
      "ariente": {
        "connected_texture": "ariente:block/blacktech/blacktech_anim_lines-ctm_glow",
        "light": 14,
        "layer": "cutout"
      }
    }

All fields in the ariente section are optional. Sprite names omit textures/ and
.png. Add supplemental and overlay sprites to the block atlas when introducing
new images (assets/minecraft/atlases/blocks.json). Vanilla animation sections on
both base and supplemental images remain intact and use Minecraft's animation
system. Resource reloads refresh the metadata, models and transformed-quad caches.

Connections use matching block identities in the face plane, including diagonal
neighbors for inner corners. Geometry UV directions determine the neighbor axes,
so rotated and mirrored faces retain their texture orientation. Disconnected
quarters use the original texture; connected quarters sample the 4-by-4 grid in
the supplemental image. An image for a 16-pixel base texture is normally 32x32:
each supplemental cell is one 8x8 quarter. Animated sheets stack these frames.

Light is a minimum baked block-light value from 0 through 15. It applies to the
textured pixels, with directional shading and ambient occlusion disabled for
emissive quads. Actual light emitted into the world is defined by block properties.
The cutout overlay retains transparent areas and is rendered once after the base.
The glow_texture binding restores overlays omitted by the intermediate port's
models. The original texture pixels are reused without modification.

Verification uses ArienteWorld's optional gameTest source set. Server checks cover
pane repair across chunk borders and automatic door state/collision synchronization.
The optional client TextureModelChecks check connected and inner-corner tiles,
cutout layering, minimum light, animation frames, item transforms, ramp geometry,
and the same results after a resource reload. Test sources are excluded from jars.

## Tablet manual artwork

`tools/generate_tablet_textures.py` draws the original 16×16 tablet item and the
manual's dark interface textures. Run it with Python 3 and Pillow to regenerate
the PNGs. The 512×256 interface atlas follows Patchouli 1.20's UV layout, including
normal and hovered navigation controls; crafting uses a separate 128×256 atlas.
