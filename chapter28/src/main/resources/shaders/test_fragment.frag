#version 330

const int NUM_CASCADES = 3;

in vec2 outTexCoord;
out vec4 fragColor;

uniform sampler2D texture_sampler[NUM_CASCADES];

void main()
{
    fragColor = texture(texture_sampler[0], outTexCoord);
}