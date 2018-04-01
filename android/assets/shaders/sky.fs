#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec4 u_color_sky;
uniform vec4 u_color_horizon;


void main() {
    vec2 tc = v_texCoords;
    
    vec4 perlin = texture2D(u_texture, tc);

    vec4 color = mix(u_color_sky, u_color_horizon, tc.y);
    
    gl_FragColor = vec4(color.rgb * mix(1.0, perlin.r, 0.1), 0.0);
}
