#version 330

smooth in vec4 interpColor;
in vec2 colorCoord;

uniform sampler2D colorTexture;

out vec4 outputColor;

void main() {
    outputColor = interpColor * texture(colorTexture, colorCoord);
}