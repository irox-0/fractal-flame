package academy.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.file.Path;
import java.util.List;

public record AppConfiguration(
    Size size,
    double seed,
    @JsonProperty("iteration_count") int iterationCount,
    @JsonProperty("output_path") Path outputPath,
    @JsonProperty("threads") int threadQuantity,
    @JsonProperty("affine_params") List<AffineParams> affineParamsList,
    @JsonProperty("functions") List<VariationParams> variationsParamsList

) { record Size(int width, int height) {}
}
