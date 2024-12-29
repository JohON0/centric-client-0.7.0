package centric.pl.johon0.utils.shader;

public interface IShader {

    String glsl();

    default String getName() {
        return "SHADERNONAME";
    }

}
