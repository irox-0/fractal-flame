package academy.cli.converter;

import static org.assertj.core.api.Assertions.*;

import academy.domain.AffineParams;
import academy.domain.Variation;
import academy.domain.VariationParams;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

@DisplayName("CLI Converters tests")
class ConvertersTest {

    @Nested
    @DisplayName("AffineParamsConverter")
    class AffineParamsConverterTest {

        private final AffineParamsConverter converter = new AffineParamsConverter();

        @Test
        @DisplayName("should parse single affine transformation")
        void shouldParseSingleTransformation() {
            String input = "1.0,2.0,3.0,4.0,5.0,6.0";
            List<AffineParams> result = converter.convert(input);

            assertThat(result).hasSize(1);
            AffineParams params = result.getFirst();
            assertThat(params.getA()).isEqualTo(1.0);
            assertThat(params.getB()).isEqualTo(2.0);
            assertThat(params.getC()).isEqualTo(3.0);
            assertThat(params.getD()).isEqualTo(4.0);
            assertThat(params.getE()).isEqualTo(5.0);
            assertThat(params.getF()).isEqualTo(6.0);
        }

        @Test
        @DisplayName("should parse multiple affine transformations")
        void shouldParseMultipleTransformations() {
            String input = "1.0,2.0,3.0,4.0,5.0,6.0/0.1,0.2,0.3,0.4,0.5,0.6";
            List<AffineParams> result = converter.convert(input);

            assertThat(result).hasSize(2);

            assertThat(result.get(0).getA()).isEqualTo(1.0);
            assertThat(result.get(1).getA()).isEqualTo(0.1);
        }

        @Test
        @DisplayName("should parse negative values")
        void shouldParseNegativeValues() {
            String input = "-1.0,-2.0,-3.0,-4.0,-5.0,-6.0";
            List<AffineParams> result = converter.convert(input);

            assertThat(result).hasSize(1);
            AffineParams params = result.getFirst();
            assertThat(params.getA()).isEqualTo(-1.0);
            assertThat(params.getB()).isEqualTo(-2.0);
        }

        @Test
        @DisplayName("should handle whitespace")
        void shouldHandleWhitespace() {
            String input = " 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 ";
            List<AffineParams> result = converter.convert(input);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getA()).isEqualTo(1.0);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("should throw exception for null or empty input")
        void shouldThrowForNullOrEmpty(String input) {
            assertThatThrownBy(() -> converter.convert(input)).isInstanceOf(CommandLine.TypeConversionException.class);
        }

        @Test
        @DisplayName("should throw exception for incorrect parameter count")
        void shouldThrowForIncorrectParameterCount() {
            String input = "1.0,2.0,3.0";

            assertThatThrownBy(() -> converter.convert(input))
                    .isInstanceOf(CommandLine.TypeConversionException.class)
                    .hasMessageContaining("Incorrect");
        }

        @Test
        @DisplayName("should throw exception for invalid number format")
        void shouldThrowForInvalidNumberFormat() {
            String input = "1.0,abc,3.0,4.0,5.0,6.0";

            assertThatThrownBy(() -> converter.convert(input)).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("should skip empty segments between separators")
        void shouldSkipEmptySegments() {
            String input = "1.0,2.0,3.0,4.0,5.0,6.0//0.1,0.2,0.3,0.4,0.5,0.6";
            List<AffineParams> result = converter.convert(input);

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("VariationParamsConverter")
    class VariationParamsConverterTest {

        private final VariationParamsConverter converter = new VariationParamsConverter();

        @Test
        @DisplayName("should parse single variation")
        void shouldParseSingleVariation() {
            String input = "swirl:1.0";
            List<VariationParams> result = converter.convert(input);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().variation()).isEqualTo(Variation.SWIRL);
            assertThat(result.getFirst().weight()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("should parse multiple variations")
        void shouldParseMultipleVariations() {
            String input = "swirl:1.0,linear:0.5,spherical:0.8";
            List<VariationParams> result = converter.convert(input);

            assertThat(result).hasSize(3);
            assertThat(result.get(0).variation()).isEqualTo(Variation.SWIRL);
            assertThat(result.get(1).variation()).isEqualTo(Variation.LINEAR);
            assertThat(result.get(2).variation()).isEqualTo(Variation.SPHERICAL);
        }

        @Test
        @DisplayName("should parse variation names case-insensitively")
        void shouldParseCaseInsensitively() {
            String input = "SWIRL:1.0,Swirl:0.5,swirl:0.3";
            List<VariationParams> result = converter.convert(input);

            assertThat(result).hasSize(3);
            result.forEach(vp -> assertThat(vp.variation()).isEqualTo(Variation.SWIRL));
        }

        @Test
        @DisplayName("should parse decimal weights")
        void shouldParseDecimalWeights() {
            String input = "linear:0.123456";
            List<VariationParams> result = converter.convert(input);

            assertThat(result.getFirst().weight()).isEqualTo(0.123456);
        }

        @Test
        @DisplayName("should parse negative weights")
        void shouldParseNegativeWeights() {
            String input = "linear:-0.5";
            List<VariationParams> result = converter.convert(input);

            assertThat(result.getFirst().weight()).isEqualTo(-0.5);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("should throw exception for null or empty input")
        void shouldThrowForNullOrEmpty(String input) {
            assertThatThrownBy(() -> converter.convert(input)).isInstanceOf(CommandLine.TypeConversionException.class);
        }

        @Test
        @DisplayName("should throw exception for unknown variation")
        void shouldThrowForUnknownVariation() {
            String input = "unknown:1.0";

            assertThatThrownBy(() -> converter.convert(input))
                    .isInstanceOf(CommandLine.TypeConversionException.class)
                    .hasMessageContaining("Unknown variation");
        }

        @Test
        @DisplayName("should throw exception for invalid weight format")
        void shouldThrowForInvalidWeightFormat() {
            String input = "swirl:abc";

            assertThatThrownBy(() -> converter.convert(input)).isInstanceOf(CommandLine.TypeConversionException.class);
        }

        @Test
        @DisplayName("should throw exception for missing weight")
        void shouldThrowForMissingWeight() {
            String input = "swirl";

            assertThatThrownBy(() -> converter.convert(input)).isInstanceOf(CommandLine.TypeConversionException.class);
        }

        @Test
        @DisplayName("should parse all available variations")
        void shouldParseAllVariations() {
            String input = "linear:1.0,spherical:1.0,swirl:1.0,horseshoe:1.0,exponential:1.0,sinusoidal:1.0";
            List<VariationParams> result = converter.convert(input);

            assertThat(result).hasSize(6);
            assertThat(result)
                    .extracting(VariationParams::variation)
                    .containsExactly(
                            Variation.LINEAR,
                            Variation.SPHERICAL,
                            Variation.SWIRL,
                            Variation.HORSESHOE,
                            Variation.EXPONENTIAL,
                            Variation.SINUSOIDAL);
        }
    }

    @Nested
    @DisplayName("PathConverter")
    class PathConverterTest {

        private final PathConverter converter = new PathConverter();

        @Test
        @DisplayName("should convert simple filename")
        void shouldConvertSimpleFilename() {
            String input = "result.png";
            Path result = converter.convert(input);

            assertThat(result.getFileName().toString()).isEqualTo("result.png");
        }

        @Test
        @DisplayName("should convert relative path")
        void shouldConvertRelativePath() {
            String input = "output/images/result.png";
            Path result = converter.convert(input);

            assertThat(result.toString()).contains("output");
            assertThat(result.toString()).contains("images");
            assertThat(result.getFileName().toString()).isEqualTo("result.png");
        }

        @Test
        @DisplayName("should convert absolute path")
        void shouldConvertAbsolutePath() {
            String input = "/tmp/result.png";
            Path result = converter.convert(input);

            assertThat(result.isAbsolute()).isTrue();
            assertThat(result.getFileName().toString()).isEqualTo("result.png");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("should throw exception for null or empty input")
        void shouldThrowForNullOrEmpty(String input) {
            assertThatThrownBy(() -> converter.convert(input)).isInstanceOf(CommandLine.TypeConversionException.class);
        }

        @Test
        @DisplayName("should handle path with spaces")
        void shouldHandlePathWithSpaces() {
            String input = "my folder/my file.png";
            Path result = converter.convert(input);

            assertThat(result.getFileName().toString()).isEqualTo("my file.png");
        }
    }
}
