#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec4 u_color;


void main() {
    vec2 tc = v_texCoords;
    
    vec4 sprite = texture2D(u_texture, tc);
    float lum = sprite.r; //(sprite.r + sprite.g + sprite.b) / 3.0;
    vec4 color = u_color * lum;
    
    gl_FragColor = vec4(color.rgb, sprite.a);
}
