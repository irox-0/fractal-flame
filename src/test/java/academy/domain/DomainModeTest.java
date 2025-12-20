package academy.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Domain model tests")
class DomainModelTest {

    @Nested
    @DisplayName("Point")
    class PointTest {

        @Test
        @DisplayName("should create point with builder")
        void shouldCreatePointWithBuilder() {
            Point point = Point.builder().x(1.5).y(-2.3).color(0.7).build();

            assertThat(point.getX()).isEqualTo(1.5);
            assertThat(point.getY()).isEqualTo(-2.3);
            assertThat(point.getColor()).isEqualTo(0.7);
        }

        @Test
        @DisplayName("should calculate R2 (squared distance from origin)")
        void shouldCalculateR2() {
            Point point = Point.builder().x(3.0).y(4.0).color(0.5).build();

            assertThat(point.getR2()).isEqualTo(25.0);
        }

        @Test
        @DisplayName("should calculate R (distance from origin)")
        void shouldCalculateR() {
            Point point = Point.builder().x(3.0).y(4.0).color(0.5).build();

            assertThat(point.getR()).isEqualTo(5.0);
        }

        @ParameterizedTest
        @CsvSource({"0.0, 0.0, 0.0", "1.0, 0.0, 1.0", "0.0, 1.0, 1.0", "1.0, 1.0, 1.4142135623730951", "3.0, 4.0, 5.0"})
        @DisplayName("should calculate correct R for various inputs")
        void shouldCalculateCorrectR(double x, double y, double expectedR) {
            Point point = Point.builder().x(x).y(y).color(0.5).build();

            assertThat(point.getR()).isCloseTo(expectedR, within(1e-10));
        }

        @Test
        @DisplayName("should support chained setters")
        void shouldSupportChainedSetters() {
            Point point = Point.builder().x(0.0).y(0.0).color(0.0).build();

            Point result = point.setX(1.0).setY(2.0).setColor(0.5);

            assertThat(result).isSameAs(point);
            assertThat(point.getX()).isEqualTo(1.0);
            assertThat(point.getY()).isEqualTo(2.0);
            assertThat(point.getColor()).isEqualTo(0.5);
        }

        @Test
        @DisplayName("should handle negative coordinates")
        void shouldHandleNegativeCoordinates() {
            Point point = Point.builder().x(-3.0).y(-4.0).color(0.5).build();

            assertThat(point.getR2()).isEqualTo(25.0);
            assertThat(point.getR()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("should handle origin point")
        void shouldHandleOriginPoint() {
            Point point = Point.builder().x(0.0).y(0.0).color(0.5).build();

            assertThat(point.getR2()).isEqualTo(0.0);
            assertThat(point.getR()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("AffineParams")
    class AffineParamsTest {

        @Test
        @DisplayName("should create with constructor")
        void shouldCreateWithConstructor() {
            AffineParams params = new AffineParams(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);

            assertThat(params.getA()).isEqualTo(1.0);
            assertThat(params.getB()).isEqualTo(2.0);
            assertThat(params.getC()).isEqualTo(3.0);
            assertThat(params.getD()).isEqualTo(4.0);
            assertThat(params.getE()).isEqualTo(5.0);
            assertThat(params.getF()).isEqualTo(6.0);
        }

        @Test
        @DisplayName("should parse from string")
        void shouldParseFromString() {
            AffineParams params = AffineParams.fromString("1.5,2.5,3.5,4.5,5.5,6.5");

            assertThat(params.getA()).isEqualTo(1.5);
            assertThat(params.getB()).isEqualTo(2.5);
            assertThat(params.getC()).isEqualTo(3.5);
            assertThat(params.getD()).isEqualTo(4.5);
            assertThat(params.getE()).isEqualTo(5.5);
            assertThat(params.getF()).isEqualTo(6.5);
        }

        @Test
        @DisplayName("should parse negative values from string")
        void shouldParseNegativeValuesFromString() {
            AffineParams params = AffineParams.fromString("-1.0,-2.0,-3.0,-4.0,-5.0,-6.0");

            assertThat(params.getA()).isEqualTo(-1.0);
            assertThat(params.getB()).isEqualTo(-2.0);
        }

        @Test
        @DisplayName("should set and get color")
        void shouldSetAndGetColor() {
            AffineParams params = new AffineParams(1.0, 0.0, 0.0, 0.0, 1.0, 0.0);

            params.setColor(0.75);

            assertThat(params.getColor()).isEqualTo(0.75);
        }

        @Test
        @DisplayName("should have correct toString representation")
        void shouldHaveCorrectToString() {
            AffineParams params = new AffineParams(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);

            String result = params.toString();

            assertThat(result).contains("a=1.0");
            assertThat(result).contains("b=2.0");
            assertThat(result).contains("c=3.0");
            assertThat(result).contains("d=4.0");
            assertThat(result).contains("e=5.0");
            assertThat(result).contains("f=6.0");
        }

        @Test
        @DisplayName("should throw for invalid string format")
        void shouldThrowForInvalidStringFormat() {
            assertThatThrownBy(() -> AffineParams.fromString("1.0,2.0,abc,4.0,5.0,6.0"))
                    .isInstanceOf(NumberFormatException.class);
        }
    }

    @Nested
    @DisplayName("Size")
    class SizeTest {

        @Test
        @DisplayName("should create with width and height")
        void shouldCreateWithWidthAndHeight() {
            Size size = new Size(1920, 1080);

            assertThat(size.width()).isEqualTo(1920);
            assertThat(size.height()).isEqualTo(1080);
        }

        @Test
        @DisplayName("should support equals and hashCode")
        void shouldSupportEqualsAndHashCode() {
            Size size1 = new Size(100, 200);
            Size size2 = new Size(100, 200);
            Size size3 = new Size(200, 100);

            assertThat(size1).isEqualTo(size2);
            assertThat(size1).isNotEqualTo(size3);
            assertThat(size1.hashCode()).isEqualTo(size2.hashCode());
        }
    }

    @Nested
    @DisplayName("VariationParams")
    class VariationParamsTest {

        @Test
        @DisplayName("should create with variation and weight")
        void shouldCreateWithVariationAndWeight() {
            VariationParams params = new VariationParams(Variation.SWIRL, 1.5);

            assertThat(params.variation()).isEqualTo(Variation.SWIRL);
            assertThat(params.weight()).isEqualTo(1.5);
        }

        @Test
        @DisplayName("should parse from string")
        void shouldParseFromString() {
            VariationParams params = VariationParams.fromString("horseshoe:0.8");

            assertThat(params.variation()).isEqualTo(Variation.HORSESHOE);
            assertThat(params.weight()).isEqualTo(0.8);
        }

        @Test
        @DisplayName("should parse case-insensitively")
        void shouldParseCaseInsensitively() {
            VariationParams params1 = VariationParams.fromString("SWIRL:1.0");
            VariationParams params2 = VariationParams.fromString("swirl:1.0");
            VariationParams params3 = VariationParams.fromString("Swirl:1.0");

            assertThat(params1.variation()).isEqualTo(Variation.SWIRL);
            assertThat(params2.variation()).isEqualTo(Variation.SWIRL);
            assertThat(params3.variation()).isEqualTo(Variation.SWIRL);
        }

        @Test
        @DisplayName("should have correct toString representation")
        void shouldHaveCorrectToString() {
            VariationParams params = new VariationParams(Variation.LINEAR, 0.5);

            String result = params.toString();

            assertThat(result).contains("LINEAR");
            assertThat(result).contains("0.5");
        }

        @Test
        @DisplayName("should throw for unknown variation")
        void shouldThrowForUnknownVariation() {
            assertThatThrownBy(() -> VariationParams.fromString("unknown:1.0")).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("should throw for invalid weight format")
        void shouldThrowForInvalidWeightFormat() {
            assertThatThrownBy(() -> VariationParams.fromString("swirl:abc")).isInstanceOf(Exception.class);
        }
    }
}
