package academy.cli.converter;

import static org.assertj.core.api.Assertions.*;

import academy.domain.AppConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import picocli.CommandLine;

@DisplayName("AppConfigurationConverter (JSON parsing) tests")
class AppConfigurationConverterTest {

    private final AppConfigurationConverter converter = new AppConfigurationConverter();

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("should parse valid JSON configuration")
    void shouldParseValidJsonConfiguration() throws IOException {
        String json =
                """
            {
              "size": {
                "width": 1920,
                "height": 1080
              },
              "iteration_count": 5000,
              "output_path": "result.png",
              "threads": 4,
              "seed": 12345,
              "functions": [
                {
                  "name": "swirl",
                  "weight": 1.0
                }
              ],
              "affine_params": [
                {
                  "a": 1.0,
                  "b": 0.0,
                  "c": 0.0,
                  "d": 0.0,
                  "e": 1.0,
                  "f": 0.0
                }
              ]
            }
            """;

        Path configFile = tempDir.resolve("config.json");
        Files.writeString(configFile, json);

        AppConfiguration result = converter.convert(configFile.toString());

        assertThat(result).isNotNull();
        assertThat(result.getSize().width()).isEqualTo(1920);
        assertThat(result.getSize().height()).isEqualTo(1080);
        assertThat(result.getIterationCount()).isEqualTo(5000);
        assertThat(result.getThreadQuantity()).isEqualTo(4);
        assertThat(result.getSeed()).isEqualTo(12345);
        assertThat(result.getVariationsParamsList()).hasSize(1);
        assertThat(result.getAffineParamsList()).hasSize(1);
    }

    @Test
    @DisplayName("should parse configuration with multiple functions")
    void shouldParseMultipleFunctions() throws IOException {
        String json =
                """
            {
              "size": {"width": 800, "height": 600},
              "iteration_count": 1000,
              "output_path": "test.png",
              "threads": 2,
              "seed": 1,
              "functions": [
                {"name": "swirl", "weight": 1.0},
                {"name": "horseshoe", "weight": 0.7},
                {"name": "linear", "weight": 0.3}
              ],
              "affine_params": [
                {"a": 1.0, "b": 0.0, "c": 0.0, "d": 0.0, "e": 1.0, "f": 0.0}
              ]
            }
            """;

        Path configFile = tempDir.resolve("config.json");
        Files.writeString(configFile, json);

        AppConfiguration result = converter.convert(configFile.toString());

        assertThat(result.getVariationsParamsList()).hasSize(3);
    }

    @Test
    @DisplayName("should parse configuration with multiple affine params")
    void shouldParseMultipleAffineParams() throws IOException {
        String json =
                """
            {
              "size": {"width": 800, "height": 600},
              "iteration_count": 1000,
              "output_path": "test.png",
              "threads": 1,
              "seed": 1,
              "functions": [{"name": "linear", "weight": 1.0}],
              "affine_params": [
                {"a": 1.0, "b": 0.0, "c": 0.0, "d": 0.0, "e": 1.0, "f": 0.0},
                {"a": 0.5, "b": 0.5, "c": 0.1, "d": -0.5, "e": 0.5, "f": 0.2},
                {"a": 0.3, "b": -0.3, "c": 0.0, "d": 0.3, "e": 0.3, "f": 0.0}
              ]
            }
            """;

        Path configFile = tempDir.resolve("config.json");
        Files.writeString(configFile, json);

        AppConfiguration result = converter.convert(configFile.toString());

        assertThat(result.getAffineParamsList()).hasSize(3);
        assertThat(result.getAffineParamsList().get(0).getA()).isEqualTo(1.0);
        assertThat(result.getAffineParamsList().get(1).getA()).isEqualTo(0.5);
        assertThat(result.getAffineParamsList().get(2).getA()).isEqualTo(0.3);
    }

    @Test
    @DisplayName("should parse negative affine parameters")
    void shouldParseNegativeAffineParams() throws IOException {
        String json =
                """
            {
              "size": {"width": 100, "height": 100},
              "iteration_count": 100,
              "output_path": "test.png",
              "threads": 1,
              "seed": 1,
              "functions": [{"name": "linear", "weight": 1.0}],
              "affine_params": [
                {"a": -1.0, "b": -0.5, "c": -0.2, "d": -0.3, "e": -0.8, "f": -0.1}
              ]
            }
            """;

        Path configFile = tempDir.resolve("config.json");
        Files.writeString(configFile, json);

        AppConfiguration result = converter.convert(configFile.toString());

        assertThat(result.getAffineParamsList().get(0).getA()).isEqualTo(-1.0);
        assertThat(result.getAffineParamsList().get(0).getB()).isEqualTo(-0.5);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("should return null for null or empty path")
    void shouldReturnNullForNullOrEmpty(String input) {
        AppConfiguration result = converter.convert(input);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("should throw exception for non-existent file")
    void shouldThrowForNonExistentFile() {
        String nonExistentPath = "/path/to/non/existent/config.json";

        assertThatThrownBy(() -> converter.convert(nonExistentPath))
                .isInstanceOf(CommandLine.TypeConversionException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("should throw exception for invalid JSON")
    void shouldThrowForInvalidJson() throws IOException {
        String invalidJson = "{ this is not valid json }";

        Path configFile = tempDir.resolve("invalid.json");
        Files.writeString(configFile, invalidJson);

        assertThatThrownBy(() -> converter.convert(configFile.toString()))
                .isInstanceOf(CommandLine.TypeConversionException.class);
    }

    @Test
    @DisplayName("should throw exception for directory path")
    void shouldThrowForDirectoryPath() {
        assertThatThrownBy(() -> converter.convert(tempDir.toString()))
                .isInstanceOf(CommandLine.TypeConversionException.class)
                .hasMessageContaining("not a file");
    }

    @Test
    @DisplayName("should parse floating point seed")
    void shouldParseFloatingPointSeed() throws IOException {
        String json =
                """
            {
              "size": {"width": 100, "height": 100},
              "iteration_count": 100,
              "output_path": "test.png",
              "threads": 1,
              "seed": 123456789,
              "functions": [{"name": "linear", "weight": 1.0}],
              "affine_params": [{"a": 1.0, "b": 0.0, "c": 0.0, "d": 0.0, "e": 1.0, "f": 0.0}]
            }
            """;

        Path configFile = tempDir.resolve("config.json");
        Files.writeString(configFile, json);

        AppConfiguration result = converter.convert(configFile.toString());

        assertThat(result.getSeed()).isEqualTo(123456789L);
    }

    @Test
    @DisplayName("should parse all variation types from JSON")
    void shouldParseAllVariationTypes() throws IOException {
        String json =
                """
            {
              "size": {"width": 100, "height": 100},
              "iteration_count": 100,
              "output_path": "test.png",
              "threads": 1,
              "seed": 1,
              "functions": [
                {"name": "linear", "weight": 1.0},
                {"name": "spherical", "weight": 0.5},
                {"name": "swirl", "weight": 0.8},
                {"name": "horseshoe", "weight": 0.6},
                {"name": "exponential", "weight": 0.4},
                {"name": "sinusoidal", "weight": 0.3}
              ],
              "affine_params": [{"a": 1.0, "b": 0.0, "c": 0.0, "d": 0.0, "e": 1.0, "f": 0.0}]
            }
            """;

        Path configFile = tempDir.resolve("config.json");
        Files.writeString(configFile, json);

        AppConfiguration result = converter.convert(configFile.toString());

        assertThat(result.getVariationsParamsList()).hasSize(6);
    }
}
