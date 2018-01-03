#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;
layout (location=5) in mat4 modelMatrix;
layout (location=9) in vec2 texOffset;
layout (location=10) in float scale;

out vec2 outTexCoord;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

uniform int numCols;
uniform int numRows;

void main()
{
    mat4 modelViewMatrix = viewMatrix * modelMatrix;
    // Preserve scaling
    modelViewMatrix[0][0] = scale;
    modelViewMatrix[1][1] = scale;
    modelViewMatrix[2][2] = scale;
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
    
    // Support for texture atlas, update texture coordinates
    float x = (texCoord.x / numCols + texOffset.x);
    float y = (texCoord.y / numRows + texOffset.y);

    outTexCoord = vec2(x, y);
}