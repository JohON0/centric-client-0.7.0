package centric.pl.johon0.utils.shader.shaders;

import centric.pl.johon0.utils.shader.IShader;

public class AlphaGlsl implements IShader {

    @Override
    public String glsl() {
        return """
                #version 120

                uniform sampler2D texture;
                uniform float state;  

                void main() {
                    vec3 sum = texture2D(texture, gl_TexCoord[0].st).rgb;
                    

                    gl_FragColor = vec4(sum, state);
                }
                    """;
    }

}
