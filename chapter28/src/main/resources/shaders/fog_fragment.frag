#version 330

out vec4 fragColor;

struct Fog
{
    int activeFog;
    vec3 colour;
    float density;
};

uniform sampler2D positionsText;
uniform sampler2D depthText;
uniform sampler2D sceneText;

uniform vec2 screenSize;

uniform mat4 viewMatrix;
uniform Fog fog;
uniform vec3 ambientLight;
uniform vec3 lightColour;
uniform float lightIntensity;

vec2 getTextCoord()
{
    return gl_FragCoord.xy / screenSize;
}

vec4 calcFog(vec3 pos, vec4 colour, Fog fog, vec3 ambientLight, vec3 lightColour, float lightIntensity)
{
    vec3 fogColor = fog.colour * (ambientLight + lightColour * lightIntensity);
    float distance = length(pos);
    float fogFactor = 1.0 / exp( (distance * fog.density)* (distance * fog.density));
    fogFactor = clamp( fogFactor, 0.0, 1.0 );

    vec3 resultColour = mix(fogColor, colour.xyz, fogFactor);
    return vec4(resultColour.xyz, colour.w);
}

void main()
{
    vec2 textCoord = getTextCoord();
    vec3 worldPos = texture(positionsText, textCoord).xyz;
    vec4 colour = vec4(texture(sceneText, textCoord).xyz, 1);
    vec4 mvVertexPos = viewMatrix * vec4(worldPos, 1);
    float depth = texture(depthText, textCoord).r;
    if ( depth == 1 ) {
        discard;
    }
    if ( fog.activeFog == 1 )
    {
    	fragColor = calcFog(mvVertexPos.xyz, colour, fog, ambientLight, lightColour, lightIntensity);
    }
    else {
	    fragColor = colour;
    }
}