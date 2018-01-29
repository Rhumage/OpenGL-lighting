#version 330

layout(location = 0) in vec3 position;

uniform vec3 posOffset;
uniform mat4 modelToCameraMatrix;

layout (std140) uniform GlobalMatrices {
    mat4 cameraToClipMatrix;
};

void main() {
    gl_Position = cameraToClipMatrix * (modelToCameraMatrix * vec4(((position * 0.1) + posOffset), 1.0f));
}