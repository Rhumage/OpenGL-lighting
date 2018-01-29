#version 330

layout(location = 0) in vec3 position;
layout(location = 2) in vec3 normal;
layout(location = 5) in vec2 texCoord;

uniform mat4 modelToCameraMatrix;
uniform mat3 normalModelToCameraMatrix;
uniform vec4 ambientIntensity;
uniform vec3 dirToLight;
uniform vec4 lightIntensity;
uniform vec4 baseDiffuseColor;
uniform mat4 cameraToClipMatrix2;

layout (std140) uniform GlobalMatrices {
    mat4 cameraToClipMatrix;
};

smooth out vec4 interpColor;
out vec2 colorCoord;

void main() {
    //gl_Position = cameraToClipMatrix * (modelToCameraMatrix * vec4(position, 1.0f));
    gl_Position = cameraToClipMatrix2 * (modelToCameraMatrix * vec4(position, 1.0f));

    vec3 normCamSpace = normalize(normalModelToCameraMatrix * normal);

    float cosAngIncidence = dot(normCamSpace, dirToLight);
    cosAngIncidence = clamp(cosAngIncidence, 0, 1);

    interpColor = ((lightIntensity - ambientIntensity) * cosAngIncidence * baseDiffuseColor) + (baseDiffuseColor * ambientIntensity);
    colorCoord = texCoord;
}