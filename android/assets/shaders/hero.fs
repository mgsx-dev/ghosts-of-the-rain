#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_texture2;
uniform vec4 u_color;
uniform float u_life;
varying float v_life;
varying vec4 v_color;

void main() {
    vec2 tc = v_texCoords;
    
    vec4 sprite1 = texture2D(u_texture, tc);
    vec4 sprite2 = texture2D(u_texture2, tc);
    vec4 sprite;
    vec3 fillColor;
    if(v_life < u_life){
    	sprite = sprite1;
    	fillColor = vec3(1.0, 0.0, 0.0);
    }else{
    	sprite = sprite2;
    	fillColor = vec3(1.0, 1.0, 1.0);
    }
    
    float lum = sprite.r; //(sprite.r + sprite.g + sprite.b) / 3.0;
    vec3 color = fillColor * lum;
    
    gl_FragColor = vec4(color.rgb, sprite.a);
}
