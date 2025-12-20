package academy.domain;

import static java.lang.Math.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Variation transformations tests")
class VariationTest {

    private static final double DELTA = 1e-10;

    private Point createPoint(double x, double y) {
        return Point.builder().x(x).y(y).color(0.5).build();
    }

    @Nested
    @DisplayName("LINEAR variation")
    class LinearTest {

        @Test
        @DisplayName("should return the same point unchanged")
        void shouldReturnSamePoint() {
            Point input = createPoint(1.5, -2.3);
            Point result = Variation.LINEAR.getOperator().apply(input);

            assertThat(result.getX()).isEqualTo(1.5);
            assertThat(result.getY()).isEqualTo(-2.3);
        }

        @Test
        @DisplayName("should handle zero coordinates")
        void shouldHandleZeroCoordinates() {
            Point input = createPoint(0.0, 0.0);
            Point result = Variation.LINEAR.getOperator().apply(input);

            assertThat(result.getX()).isEqualTo(0.0);
            assertThat(result.getY()).isEqualTo(0.0);
        }

        @ParameterizedTest
        @CsvSource({"1.0, 1.0", "-1.0, -1.0", "0.5, -0.5", "100.0, 200.0"})
        @DisplayName("should preserve coordinates for various inputs")
        void shouldPreserveCoordinates(double x, double y) {
            Point input = createPoint(x, y);
            Point result = Variation.LINEAR.getOperator().apply(input);

            assertThat(result.getX()).isEqualTo(x);
            assertThat(result.getY()).isEqualTo(y);
        }
    }

    @Nested
    @DisplayName("SPHERICAL variation")
    class SphericalTest {

        @Test
        @DisplayName("should correctly transform point on unit circle")
        void shouldTransformPointOnUnitCircle() {
            Point input = createPoint(1.0, 0.0);
            Point result = Variation.SPHERICAL.getOperator().apply(input);

            assertThat(result.getX()).isCloseTo(1.0, within(DELTA));
            assertThat(result.getY()).isCloseTo(0.0, within(DELTA));
        }

        @Test
        @DisplayName("should invert distance from origin")
        void shouldInvertDistanceFromOrigin() {
            Point input = createPoint(2.0, 0.0);
            Point result = Variation.SPHERICAL.getOperator().apply(input);

            assertThat(result.getX()).isCloseTo(0.5, within(DELTA));
            assertThat(result.getY()).isCloseTo(0.0, within(DELTA));
        }

        @Test
        @DisplayName("should correctly transform arbitrary point")
        void shouldTransformArbitraryPoint() {
            Point input = createPoint(3.0, 4.0);
            Point result = Variation.SPHERICAL.getOperator().apply(input);

            assertThat(result.getX()).isCloseTo(0.12, within(DELTA));
            assertThat(result.getY()).isCloseTo(0.16, within(DELTA));
        }

        @Test
        @DisplayName("should handle point close to origin (large result)")
        void shouldHandlePointCloseToOrigin() {
            Point input = createPoint(0.1, 0.0);
            Point result = Variation.SPHERICAL.getOperator().apply(input);

            assertThat(result.getX()).isCloseTo(10.0, within(DELTA));
        }
    }

    @Nested
    @DisplayName("SWIRL variation")
    class SwirlTest {

        @Test
        @DisplayName("should correctly apply swirl formula")
        void shouldApplySwirlFormula() {
            double x = 1.0;
            double y = 0.0;
            double r2 = x * x + y * y;

            Point input = createPoint(x, y);
            Point result = Variation.SWIRL.getOperator().apply(input);

            assertThat(result.getX()).isCloseTo(sin(1.0), within(DELTA));
            assertThat(result.getY()).isCloseTo(cos(1.0), within(DELTA));
        }

        @Test
        @DisplayName("should handle origin point")
        void shouldHandleOriginPoint() {
            Point input = createPoint(0.0, 0.0);
            Point result = Variation.SWIRL.getOperator().apply(input);

            assertThat(result.getX()).isCloseTo(0.0, within(DELTA));
            assertThat(result.getY()).isCloseTo(0.0, within(DELTA));
        }

        @Test
        @DisplayName("should correctly transform point with both coordinates")
        void shouldTransformPointWithBothCoordinates() {
            double x = 1.0;
            double y = 1.0;
            double r2 = 2.0;

            Point input = createPoint(x, y);
            Point result = Variation.SWIRL.getOperator().apply(input);

            double expectedX = x * sin(r2) - y * cos(r2);
            double expectedY = x * cos(r2) + y * sin(r2);

            assertThat(result.getX()).isCloseTo(expectedX, within(DELTA));
            assertThat(result.getY()).isCloseTo(expectedY, within(DELTA));
        }
    }

    @Nested
    @DisplayName("HORSESHOE variation")
    class HorseshoeTest {

        @Test
        @DisplayName("should correctly apply horseshoe formula")
        void shouldApplyHorseshoeFormula() {
            double x = 3.0;
            double y = 4.0;
            double r = 5.0;

            Point input = createPoint(x, y);
            Point result = Variation.HORSESHOE.getOperator().apply(input);

            assertThat(result.getX()).isCloseTo(-1.4, within(DELTA));
            assertThat(result.getY()).isCloseTo(4.8, within(DELTA));
        }

        @Test
        @DisplayName("should handle point on x-axis")
        void shouldHandlePointOnXAxis() {
            double x = 2.0;
            double y = 0.0;
            double r = 2.0;

            Point input = createPoint(x, y);
            Point result = Variation.HORSESHOE.getOperator().apply(input);

            assertThat(result.getX()).isCloseTo(2.0, within(DELTA));
            assertThat(result.getY()).isCloseTo(0.0, within(DELTA));
        }

        @Test
        @DisplayName("should handle point on y-axis")
        void shouldHandlePointOnYAxis() {
            double x = 0.0;
            double y = 2.0;
            double r = 2.0;

            Point input = createPoint(x, y);
            Point result = Variation.HORSESHOE.getOperator().apply(input);

            assertThat(result.getX()).isCloseTo(-2.0, within(DELTA));
            assertThat(result.getY()).isCloseTo(0.0, within(DELTA));
        }
    }

    @Nested
    @DisplayName("EXPONENTIAL variation")
    class ExponentialTest {

        @Test
        @DisplayName("should correctly apply exponential formula")
        void shouldApplyExponentialFormula() {
            double x = 1.0;
            double y = 0.0;

            Point input = createPoint(x, y);
            Point result = Variation.EXPONENTIAL.getOperator().apply(input);

            assertThat(result.getX()).isCloseTo(1.0, within(DELTA));
            assertThat(result.getY()).isCloseTo(0.0, within(DELTA));
        }

        @Test
        @DisplayName("should handle y = 0.5 (half period)")
        void shouldHandleHalfPeriod() {
            double x = 1.0;
            double y = 0.5;

            Point input = createPoint(x, y);
            Point result = Variation.EXPONENTIAL.getOperator().apply(input);

            assertThat(result.getX()).isCloseTo(0.0, within(DELTA));
            assertThat(result.getY()).isCloseTo(1.0, within(DELTA));
        }

        @Test
        @DisplayName("should handle y = 1 (full period)")
        void shouldHandleFullPeriod() {
            double x = 1.0;
            double y = 1.0;

            Point input = createPoint(x, y);
            Point result = Variation.EXPONENTIAL.getOperator().apply(input);

            assertThat(result.getX()).isCloseTo(-1.0, within(DELTA));
            assertThat(result.getY()).isCloseTo(0.0, within(DELTA));
        }

        @Test
        @DisplayName("should scale with x coordinate")
        void shouldScaleWithXCoordinate() {
            double x = 2.0;
            double y = 0.0;

            Point input = createPoint(x, y);
            Point result = Variation.EXPONENTIAL.getOperator().apply(input);

            assertThat(result.getX()).isCloseTo(E, within(DELTA));
            assertThat(result.getY()).isCloseTo(0.0, within(DELTA));
        }
    }

    @Nested
    @DisplayName("SINUSOIDAL variation")
    class SinusoidalTest {

        @Test
        @DisplayName("should apply sine to both coordinates")
        void shouldApplySineToBothCoordinates() {
            double x = PI / 2;
            double y = PI / 6;

            Point input = createPoint(x, y);
            Point result = Variation.SINUSOIDAL.getOperator().apply(input);

            assertThat(result.getX()).isCloseTo(1.0, within(DELTA));
            assertThat(result.getY()).isCloseTo(0.5, within(DELTA));
        }

        @Test
        @DisplayName("should handle zero coordinates")
        void shouldHandleZeroCoordinates() {
            Point input = createPoint(0.0, 0.0);
            Point result = Variation.SINUSOIDAL.getOperator().apply(input);

            assertThat(result.getX()).isCloseTo(0.0, within(DELTA));
            assertThat(result.getY()).isCloseTo(0.0, within(DELTA));
        }

        @Test
        @DisplayName("should handle negative coordinates")
        void shouldHandleNegativeCoordinates() {
            double x = -PI / 2;
            double y = -PI / 2;

            Point input = createPoint(x, y);
            Point result = Variation.SINUSOIDAL.getOperator().apply(input);

            assertThat(result.getX()).isCloseTo(-1.0, within(DELTA));
            assertThat(result.getY()).isCloseTo(-1.0, within(DELTA));
        }

        @Test
        @DisplayName("should bound output between -1 and 1")
        void shouldBoundOutput() {
            Point input = createPoint(100.0, -100.0);
            Point result = Variation.SINUSOIDAL.getOperator().apply(input);

            assertThat(result.getX()).isBetween(-1.0, 1.0);
            assertThat(result.getY()).isBetween(-1.0, 1.0);
        }
    }

    @Nested
    @DisplayName("Variation utility methods")
    class UtilityMethodsTest {

        @Test
        @DisplayName("getValuesAsString should return all variations")
        void shouldReturnAllVariationsAsString() {
            String result = Variation.getValuesAsString();

            assertThat(result).contains("linear");
            assertThat(result).contains("spherical");
            assertThat(result).contains("swirl");
            assertThat(result).contains("horseshoe");
            assertThat(result).contains("exponential");
            assertThat(result).contains("sinusoidal");
        }

        @ParameterizedTest
        @ValueSource(strings = {"linear", "LINEAR", "Linear", "SPHERICAL", "swirl", "HORSESHOE"})
        @DisplayName("isValidVariation should return true for valid variations")
        void shouldReturnTrueForValidVariations(String variation) {
            assertThat(Variation.isValidVariation(variation)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid", "unknown", "twist", "", "null"})
        @DisplayName("isValidVariation should return false for invalid variations")
        void shouldReturnFalseForInvalidVariations(String variation) {
            assertThat(Variation.isValidVariation(variation)).isFalse();
        }
    }

    @Nested
    @DisplayName("Color preservation")
    class ColorPreservationTest {

        @ParameterizedTest
        @ValueSource(doubles = {0.0, 0.25, 0.5, 0.75, 1.0})
        @DisplayName("all variations should preserve color value")
        void allVariationsShouldPreserveColor(double colorValue) {
            Point input = Point.builder().x(1.0).y(1.0).color(colorValue).build();

            for (Variation variation : Variation.values()) {
                Point result = variation.getOperator().apply(input);
                assertThat(result.getColor())
                        .as("Color should be preserved for %s", variation.name())
                        .isEqualTo(colorValue);
            }
        }
    }
}
