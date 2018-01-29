#version 330

layout(location = 0) in vec3 position;
layout(location = 2) in vec3 normal;
layout(location = 5) in vec2 texCoord;

uniform mat4 modelToCameraMatrix;
uniform mat3 normalModelToCameraMatrix;
uniform vec4 baseDiffuseColor;
uniform mat4 cameraToClipMatrix2;

layout (std140) uniform GlobalMatrices {
    mat4 cameraToClipMatrix;
};

out vec3 vertexNormal;
out vec3 cameraSpacePosition;
out vec4 diffuseColor;
out vec2 colorCoord;

void main() {
    vec4 tempCamPosition = (modelToCameraMatrix * vec4(position, 1.0));
    //gl_Position = cameraToClipMatrix * tempCamPosition;
    gl_Position = cameraToClipMatrix2 * tempCamPosition;

    vertexNormal = normalModelToCameraMatrix * normal;
    diffuseColor = baseDiffuseColor;
    cameraSpacePosition = vec3(tempCamPosition);

    colorCoord = texCoord;
}