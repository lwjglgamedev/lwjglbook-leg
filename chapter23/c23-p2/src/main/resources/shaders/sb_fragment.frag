#version 330

in vec2 outTexCoord;
in vec3 mvPos;
out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform vec4 colour;
uniform vec3 ambientLight;
uniform int hasTexture;

void main()
{
    if ( hasTexture == 1 )
    {
        fragColor = vec4(ambientLight, 1) * texture(texture_sampler, outTexCoord);
    }
    else
    {
        fragColor = colour;
    }
}