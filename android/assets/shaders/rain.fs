#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying vec2 v_texCoords;
varying vec4 v_color;
uniform sampler2D u_texture;
uniform float u_time;
uniform vec4 u_color;

void main() {
    vec2 tc = v_texCoords;
    
    vec4 perlin = texture2D(u_texture, vec2(tc.x, tc.y - u_time + v_color.r));

    gl_FragColor = vec4(u_color.rgb * perlin.r, v_color.a * perlin.r);
}
