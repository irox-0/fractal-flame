package academy.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
// @RequiredArgsConstructor
public class AppConfiguration {
    private final Size size;
    private final long seed;

    @JsonProperty("iteration_count")
    private final int iterationCount;

    @JsonProperty("output_path")
    private final Path outputPath;

    @JsonProperty("threads")
    private final int threadQuantity;

    @JsonProperty("affine_params")
    private final List<AffineParams> affineParamsList;

    @JsonProperty("functions")
    private final List<VariationParams> variationsParamsList;

    @JsonIgnore
    private Random random;

    public void setColors() {
        for (AffineParams params : affineParamsList) {
            params.setColor(random.nextDouble(0.0, 1.0));
        }
    }
}
