#version 330

in vec2 outTexCoord;
in vec3 mvPos;
out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform vec3 colour;
uniform int hasTexture;

void main()
{
    if ( hasTexture == 1 )
    {
        fragColor = vec4(colour, 1) * texture(texture_sampler, outTexCoord);
    }
    else
    {
        fragColor = vec4(colour, 1);
    }
}