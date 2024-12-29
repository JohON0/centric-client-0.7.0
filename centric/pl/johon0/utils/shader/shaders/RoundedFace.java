package centric.pl.johon0.utils.shader.shaders;

import centric.pl.johon0.utils.shader.IShader;

public class RoundedFace implements IShader {

    @Override
    public String glsl() {
        return  "#version 120\n\nuniform vec2 location, size;\nuniform sampler2D texture;\nuniform float radius, alpha;\nuniform float u, v, w, h;\n\nfloat calcLength(vec2 p, vec2 b, float r) {\n    return length(max(abs(p) - b, 0)) - r;\n}\n\nvoid main() {\n    vec2 halfSize = size * 0.5;\n    vec2 st = gl_TexCoord[0].st;\n    st.x = u + st.x * w;\n    st.y = v + st.y * h;\n    float distance = calcLength(halfSize - (gl_TexCoord[0].st * size), halfSize - radius - 1, radius);\n    float smoothedAlpha = (1 - smoothstep(0, 2, distance)) * alpha;\n    vec4 color = texture2D(texture, st);\n    gl_FragColor = vec4(color.rgb, smoothedAlpha);\n}\n";
    }

}
