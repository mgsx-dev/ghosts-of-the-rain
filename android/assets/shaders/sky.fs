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
uniform vec4 u_bg_color;
uniform float u_parallax;

void main() {
    vec2 tc = v_texCoords;
    
    vec4 bg = texture2D(u_texture, vec2(tc.x + u_parallax, tc.y));

    vec4 sky = mix(u_color_sky, u_color_horizon, tc.y);
    
    vec3 color = mix(sky.rgb * u_bg_color.rgb, bg.rgb, bg.a * u_bg_color.a);
    
    gl_FragColor = vec4(color, 0.0);
}
