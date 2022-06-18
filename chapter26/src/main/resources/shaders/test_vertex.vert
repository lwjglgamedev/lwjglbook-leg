#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;

out vec2 outTexCoord;

void main()
{
    gl_Position = vec4(position/2, 1.0);
    outTexCoord = texCoord;
}