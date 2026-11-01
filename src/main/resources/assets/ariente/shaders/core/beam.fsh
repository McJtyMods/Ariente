#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 sampleColor = texture(Sampler0, texCoord0);
    float intensity = max(sampleColor.r, max(sampleColor.g, sampleColor.b));
    // The original glow textures use opaque black for empty space. Do not let
    // those invisible texels occlude glass, shields or other beams in the depth buffer.
    if (intensity < 0.025) {
        discard;
    }
    // Keep the original additive glow: retain the scene beneath each strand,
    // and let overlapping strands accumulate into a bright white core.
    fragColor = sampleColor * vertexColor * ColorModulator * linear_fog_fade(vertexDistance, FogStart, FogEnd);
}
