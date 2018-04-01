attribute vec4 a_position;
attribute vec2 a_texCoord0;
attribute vec4 a_color;
uniform mat4 u_projTrans;
varying vec2 v_texCoords;
varying float v_life;
uniform float u_y;
uniform float u_height;
varying vec4 v_color;

void main()
{
	v_color = a_color;
	v_life = (a_position.y - u_y) / u_height;
	v_texCoords = a_texCoord0;
	gl_Position =  u_projTrans * a_position;
}
