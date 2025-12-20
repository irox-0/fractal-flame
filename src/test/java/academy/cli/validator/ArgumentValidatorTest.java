package academy.cli.validator;

import static org.assertj.core.api.Assertions.*;

import academy.Application;
import academy.domain.AffineParams;
import academy.domain.AppConfiguration;
import academy.domain.Size;
import academy.domain.Variation;
import academy.domain.VariationParams;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

@DisplayName("ArgumentValidator tests")
class ArgumentValidatorTest {

    private Application app;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        app = new Application();
    }

    private AppConfiguration.AppConfigurationBuilder validConfigBuilder() {
        return AppConfiguration.builder()
                .size(new Size(1920, 1080))
                .iterationCount(1000)
                .threadQuantity(4)
                .seed(12345)
                .outputPath(tempDir.resolve("result.png"))
                .affineParamsList(List.of(new AffineParams(1, 0, 0, 0, 1, 0)))
                .variationsParamsList(List.of(new VariationParams(Variation.SWIRL, 1.0)));
    }

    @Nested
    @DisplayName("Image dimensions validation")
    class ImageDimensionsTest {

        @ParameterizedTest
        @ValueSource(ints = {1, 100, 1920, 4096, 16384})
        @DisplayName("should accept valid width values")
        void shouldAcceptValidWidth(int width) {
            AppConfiguration config =
                    validConfigBuilder().size(new Size(width, 1080)).build();

            assertThatCode(() -> ArgumentValidator.validateImageDimensions(config, app))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100, 16385, 20000})
        @DisplayName("should reject invalid width values")
        void shouldRejectInvalidWidth(int width) {
            AppConfiguration config =
                    validConfigBuilder().size(new Size(width, 1080)).build();

            assertThatThrownBy(() -> ArgumentValidator.validateImageDimensions(config, app))
                    .isInstanceOf(CommandLine.ParameterException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 100, 1080, 4096, 16384})
        @DisplayName("should accept valid height values")
        void shouldAcceptValidHeight(int height) {
            AppConfiguration config =
                    validConfigBuilder().size(new Size(1920, height)).build();

            assertThatCode(() -> ArgumentValidator.validateImageDimensions(config, app))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100, 16385, 20000})
        @DisplayName("should reject invalid height values")
        void shouldRejectInvalidHeight(int height) {
            AppConfiguration config =
                    validConfigBuilder().size(new Size(1920, height)).build();

            assertThatThrownBy(() -> ArgumentValidator.validateImageDimensions(config, app))
                    .isInstanceOf(CommandLine.ParameterException.class);
        }
    }

    @Nested
    @DisplayName("Iteration count validation")
    class IterationCountTest {

        @ParameterizedTest
        @ValueSource(ints = {1, 100, 1000, 1000000})
        @DisplayName("should accept valid iteration counts")
        void shouldAcceptValidIterationCount(int iterations) {
            AppConfiguration config =
                    validConfigBuilder().iterationCount(iterations).build();

            assertThatCode(() -> ArgumentValidator.validateIterationCount(config, app))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100})
        @DisplayName("should reject invalid iteration counts")
        void shouldRejectInvalidIterationCount(int iterations) {
            AppConfiguration config =
                    validConfigBuilder().iterationCount(iterations).build();

            assertThatThrownBy(() -> ArgumentValidator.validateIterationCount(config, app))
                    .isInstanceOf(CommandLine.ParameterException.class);
        }
    }

    @Nested
    @DisplayName("Thread quantity validation")
    class ThreadQuantityTest {

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 4, 8, 16})
        @DisplayName("should accept valid thread quantities")
        void shouldAcceptValidThreadQuantity(int threads) {
            AppConfiguration config =
                    validConfigBuilder().threadQuantity(threads).build();

            assertThatCode(() -> ArgumentValidator.validateThreadQuantity(config, app))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -4})
        @DisplayName("should reject invalid thread quantities")
        void shouldRejectInvalidThreadQuantity(int threads) {
            AppConfiguration config =
                    validConfigBuilder().threadQuantity(threads).build();

            assertThatThrownBy(() -> ArgumentValidator.validateThreadQuantity(config, app))
                    .isInstanceOf(CommandLine.ParameterException.class);
        }
    }

    @Nested
    @DisplayName("Output path validation")
    class OutputPathTest {

        @Test
        @DisplayName("should accept valid PNG path")
        void shouldAcceptValidPngPath() {
            AppConfiguration config = validConfigBuilder()
                    .outputPath(tempDir.resolve("result.png"))
                    .build();

            assertThatCode(() -> ArgumentValidator.validateOutputPath(config, app))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should accept path with uppercase PNG extension")
        void shouldAcceptUppercasePngExtension() {
            AppConfiguration config = validConfigBuilder()
                    .outputPath(tempDir.resolve("result.PNG"))
                    .build();

            assertThatCode(() -> ArgumentValidator.validateOutputPath(config, app))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should reject non-PNG extension")
        void shouldRejectNonPngExtension() {
            AppConfiguration config = validConfigBuilder()
                    .outputPath(tempDir.resolve("result.jpg"))
                    .build();

            assertThatThrownBy(() -> ArgumentValidator.validateOutputPath(config, app))
                    .isInstanceOf(CommandLine.ParameterException.class)
                    .hasMessageContaining(".png");
        }

        @Test
        @DisplayName("should reject null output path")
        void shouldRejectNullOutputPath() {
            AppConfiguration config = validConfigBuilder().outputPath(null).build();

            assertThatThrownBy(() -> ArgumentValidator.validateOutputPath(config, app))
                    .isInstanceOf(CommandLine.ParameterException.class);
        }

        @Test
        @DisplayName("should warn but accept existing file")
        void shouldWarnButAcceptExistingFile() throws IOException {
            Path existingFile = tempDir.resolve("existing.png");
            Files.createFile(existingFile);

            AppConfiguration config =
                    validConfigBuilder().outputPath(existingFile).build();

            // Should not throw, just warn
            assertThatCode(() -> ArgumentValidator.validateOutputPath(config, app))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Affine params validation")
    class AffineParamsTest {

        @Test
        @DisplayName("should accept valid affine params")
        void shouldAcceptValidAffineParams() {
            AppConfiguration config = validConfigBuilder().build();

            assertThatCode(() -> ArgumentValidator.validateAffineParams(config, app))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should accept multiple affine params")
        void shouldAcceptMultipleAffineParams() {
            AppConfiguration config = validConfigBuilder()
                    .affineParamsList(List.of(
                            new AffineParams(1, 0, 0, 0, 1, 0),
                            new AffineParams(0.5, 0.5, 0.1, -0.5, 0.5, 0.2),
                            new AffineParams(0.3, -0.3, 0, 0.3, 0.3, 0)))
                    .build();

            assertThatCode(() -> ArgumentValidator.validateAffineParams(config, app))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should reject null affine params list")
        void shouldRejectNullAffineParams() {
            AppConfiguration config =
                    validConfigBuilder().affineParamsList(null).build();

            assertThatThrownBy(() -> ArgumentValidator.validateAffineParams(config, app))
                    .isInstanceOf(CommandLine.ParameterException.class);
        }

        @Test
        @DisplayName("should reject empty affine params list")
        void shouldRejectEmptyAffineParams() {
            AppConfiguration config = validConfigBuilder()
                    .affineParamsList(Collections.emptyList())
                    .build();

            assertThatThrownBy(() -> ArgumentValidator.validateAffineParams(config, app))
                    .isInstanceOf(CommandLine.ParameterException.class);
        }

        @Test
        @DisplayName("should reject affine params with NaN values")
        void shouldRejectAffineParamsWithNaN() {
            AppConfiguration config = validConfigBuilder()
                    .affineParamsList(List.of(new AffineParams(Double.NaN, 0, 0, 0, 1, 0)))
                    .build();

            assertThatThrownBy(() -> ArgumentValidator.validateAffineParams(config, app))
                    .isInstanceOf(CommandLine.ParameterException.class)
                    .hasMessageContaining("NaN");
        }

        @Test
        @DisplayName("should reject affine params with Infinite values")
        void shouldRejectAffineParamsWithInfinity() {
            AppConfiguration config = validConfigBuilder()
                    .affineParamsList(List.of(new AffineParams(Double.POSITIVE_INFINITY, 0, 0, 0, 1, 0)))
                    .build();

            assertThatThrownBy(() -> ArgumentValidator.validateAffineParams(config, app))
                    .isInstanceOf(CommandLine.ParameterException.class)
                    .hasMessageContaining("Infinite");
        }
    }

    @Nested
    @DisplayName("Variation params validation")
    class VariationParamsTest {

        @Test
        @DisplayName("should accept valid variation params")
        void shouldAcceptValidVariationParams() {
            AppConfiguration config = validConfigBuilder().build();

            assertThatCode(() -> ArgumentValidator.validateVariationParams(config, app))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should accept multiple variation params")
        void shouldAcceptMultipleVariationParams() {
            AppConfiguration config = validConfigBuilder()
                    .variationsParamsList(List.of(
                            new VariationParams(Variation.SWIRL, 1.0),
                            new VariationParams(Variation.LINEAR, 0.5),
                            new VariationParams(Variation.SPHERICAL, 0.8)))
                    .build();

            assertThatCode(() -> ArgumentValidator.validateVariationParams(config, app))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should reject null variation params list")
        void shouldRejectNullVariationParams() {
            AppConfiguration config =
                    validConfigBuilder().variationsParamsList(null).build();

            assertThatThrownBy(() -> ArgumentValidator.validateVariationParams(config, app))
                    .isInstanceOf(CommandLine.ParameterException.class);
        }

        @Test
        @DisplayName("should reject empty variation params list")
        void shouldRejectEmptyVariationParams() {
            AppConfiguration config = validConfigBuilder()
                    .variationsParamsList(Collections.emptyList())
                    .build();

            assertThatThrownBy(() -> ArgumentValidator.validateVariationParams(config, app))
                    .isInstanceOf(CommandLine.ParameterException.class);
        }

        @Test
        @DisplayName("should reject zero total weight")
        void shouldRejectZeroTotalWeight() {
            AppConfiguration config = validConfigBuilder()
                    .variationsParamsList(List.of(
                            new VariationParams(Variation.SWIRL, 0.0), new VariationParams(Variation.LINEAR, 0.0)))
                    .build();

            assertThatThrownBy(() -> ArgumentValidator.validateVariationParams(config, app))
                    .isInstanceOf(CommandLine.ParameterException.class)
                    .hasMessageContaining("zero");
        }

        @Test
        @DisplayName("should accept negative weights with warning")
        void shouldAcceptNegativeWeightsWithWarning() {
            AppConfiguration config = validConfigBuilder()
                    .variationsParamsList(List.of(new VariationParams(Variation.SWIRL, -1.0)))
                    .build();

            assertThatCode(() -> ArgumentValidator.validateVariationParams(config, app))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Full validation")
    class FullValidationTest {

        @Test
        @DisplayName("should pass full validation for valid config")
        void shouldPassFullValidation() {
            AppConfiguration config = validConfigBuilder().build();

            assertThatCode(() -> ArgumentValidator.validate(config, app)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should fail fast on first invalid parameter")
        void shouldFailFastOnFirstInvalidParameter() {
            AppConfiguration config = validConfigBuilder()
                    .size(new Size(-1, -1))
                    .iterationCount(-1)
                    .build();

            assertThatThrownBy(() -> ArgumentValidator.validate(config, app))
                    .isInstanceOf(CommandLine.ParameterException.class)
                    .hasMessageContaining("width");
        }
    }
}
