package academy.application.render;

import static org.assertj.core.api.Assertions.*;

import academy.domain.AppConfiguration;
import academy.domain.Point;
import academy.domain.Size;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("ImageRenderer tests")
class ImageRendererTest {

    @TempDir
    Path tempDir;

    private AppConfiguration config;
    private ImageRenderer renderer;

    @BeforeEach
    void setUp() {
        config = AppConfiguration.builder().size(new Size(100, 100)).build();
        renderer = new ImageRenderer(config);
    }

    @Nested
    @DisplayName("Initialization")
    class InitializationTest {

        @Test
        @DisplayName("should initialize with correct dimensions")
        void shouldInitializeWithCorrectDimensions() {
            assertThat(renderer.getWidth()).isEqualTo(100);
            assertThat(renderer.getHeight()).isEqualTo(100);
        }

        @Test
        @DisplayName("should initialize histogram with zeros")
        void shouldInitializeHistogramWithZeros() {
            double[][][] histogram = renderer.getHistogram();

            for (int x = 0; x < 100; x++) {
                for (int y = 0; y < 100; y++) {
                    for (int c = 0; c < 4; c++) {
                        assertThat(histogram[x][y][c]).isZero();
                    }
                }
            }
        }

        @Test
        @DisplayName("should generate valid color palette")
        void shouldGenerateValidPalette() {
            int[][] palette = renderer.getPalette();

            assertThat(palette).hasNumberOfRows(256);

            for (int[] color : palette) {
                assertThat(color).hasSize(3);
                assertThat(color[0]).isBetween(0, 255);
                assertThat(color[1]).isBetween(0, 255);
                assertThat(color[2]).isBetween(0, 255);
            }
        }
    }

    @Nested
    @DisplayName("Plot functionality")
    class PlotTest {

        @Test
        @DisplayName("should plot point within bounds")
        void shouldPlotPointWithinBounds() {
            Point point = Point.builder().x(0.0).y(0.0).color(0.5).build();

            renderer.plot(point);

            double[][][] histogram = renderer.getHistogram();
            boolean foundPlottedPixel = false;
            for (int x = 0; x < 100; x++) {
                for (int y = 0; y < 100; y++) {
                    if (histogram[x][y][3] > 0) {
                        foundPlottedPixel = true;
                        break;
                    }
                }
            }
            assertThat(foundPlottedPixel).isTrue();
        }

        @Test
        @DisplayName("should ignore point outside bounds")
        void shouldIgnorePointOutsideBounds() {
            Point point = Point.builder().x(10.0).y(10.0).color(0.5).build();

            renderer.plot(point);

            double[][][] histogram = renderer.getHistogram();
            for (int x = 0; x < 100; x++) {
                for (int y = 0; y < 100; y++) {
                    assertThat(histogram[x][y][3]).isZero();
                }
            }
        }

        @ParameterizedTest
        @CsvSource({"-3.0, 0.0", "3.0, 0.0", "0.0, -3.0", "0.0, 3.0"})
        @DisplayName("should ignore points outside coordinate bounds")
        void shouldIgnorePointsOutsideCoordinateBounds(double x, double y) {
            Point point = Point.builder().x(x).y(y).color(0.5).build();

            renderer.plot(point);

            double[][][] histogram = renderer.getHistogram();
            boolean anyPlotted = false;
            for (int px = 0; px < 100; px++) {
                for (int py = 0; py < 100; py++) {
                    if (histogram[px][py][3] > 0) {
                        anyPlotted = true;
                    }
                }
            }
            assertThat(anyPlotted).isFalse();
        }

        @Test
        @DisplayName("should accumulate multiple plots on same pixel")
        void shouldAccumulateMultiplePlots() {
            Point point = Point.builder().x(0.0).y(0.0).color(0.5).build();

            renderer.plot(point);
            renderer.plot(point);
            renderer.plot(point);

            double[][][] histogram = renderer.getHistogram();
            double maxAlpha = 0;
            for (int x = 0; x < 100; x++) {
                for (int y = 0; y < 100; y++) {
                    maxAlpha = Math.max(maxAlpha, histogram[x][y][3]);
                }
            }
            assertThat(maxAlpha).isEqualTo(3.0);
        }

        @Test
        @DisplayName("should correctly map color to palette")
        void shouldMapColorToPalette() {
            Point point = Point.builder().x(0.0).y(0.0).color(0.0).build();

            renderer.plot(point);

            double[][][] histogram = renderer.getHistogram();
            int[][] palette = renderer.getPalette();

            for (int x = 0; x < 100; x++) {
                for (int y = 0; y < 100; y++) {
                    if (histogram[x][y][3] > 0) {
                        assertThat(histogram[x][y][0]).isCloseTo(palette[0][0] / 255.0, within(0.01));
                        assertThat(histogram[x][y][1]).isCloseTo(palette[0][1] / 255.0, within(0.01));
                        assertThat(histogram[x][y][2]).isCloseTo(palette[0][2] / 255.0, within(0.01));
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("Save functionality")
    class SaveTest {

        @Test
        @DisplayName("should save PNG file")
        void shouldSavePngFile() throws IOException {
            Point point = Point.builder().x(0.0).y(0.0).color(0.5).build();
            renderer.plot(point);

            Path outputPath = tempDir.resolve("test.png");
            renderer.save(outputPath);

            assertThat(Files.exists(outputPath)).isTrue();
            assertThat(Files.size(outputPath)).isGreaterThan(0);
        }

        @Test
        @DisplayName("should create valid PNG image")
        void shouldCreateValidPngImage() throws IOException {
            Point point = Point.builder().x(0.0).y(0.0).color(0.5).build();
            renderer.plot(point);

            Path outputPath = tempDir.resolve("test.png");
            renderer.save(outputPath);

            BufferedImage image = ImageIO.read(outputPath.toFile());
            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(100);
            assertThat(image.getHeight()).isEqualTo(100);
        }

        @Test
        @DisplayName("should create directories if not exist")
        void shouldCreateDirectoriesIfNotExist() throws IOException {
            Point point = Point.builder().x(0.0).y(0.0).color(0.5).build();
            renderer.plot(point);

            Path outputPath = tempDir.resolve("subdir/nested/test.png");
            renderer.save(outputPath);

            assertThat(Files.exists(outputPath)).isTrue();
        }

        @Test
        @DisplayName("should save empty image without errors")
        void shouldSaveEmptyImageWithoutErrors() throws IOException {
            Path outputPath = tempDir.resolve("empty.png");

            assertThatCode(() -> renderer.save(outputPath)).doesNotThrowAnyException();

            assertThat(Files.exists(outputPath)).isTrue();
        }
    }

    @Nested
    @DisplayName("Merge functionality")
    class MergeTest {

        @Test
        @DisplayName("should merge histograms from multiple renderers")
        void shouldMergeHistograms() {
            ImageRenderer renderer1 = new ImageRenderer(config);
            ImageRenderer renderer2 = new ImageRenderer(config);

            Point point1 = Point.builder().x(-1.0).y(0.0).color(0.5).build();
            Point point2 = Point.builder().x(1.0).y(0.0).color(0.5).build();

            renderer1.plot(point1);
            renderer2.plot(point2);

            ImageRenderer mainRenderer = new ImageRenderer(config);
            mainRenderer.merge(List.of(renderer1, renderer2));

            double[][][] histogram = mainRenderer.getHistogram();
            int plottedPixels = 0;
            for (int x = 0; x < 100; x++) {
                for (int y = 0; y < 100; y++) {
                    if (histogram[x][y][3] > 0) {
                        plottedPixels++;
                    }
                }
            }
            assertThat(plottedPixels).isEqualTo(2);
        }

        @Test
        @DisplayName("should accumulate values when merging same pixels")
        void shouldAccumulateWhenMergingSamePixels() {
            ImageRenderer renderer1 = new ImageRenderer(config);
            ImageRenderer renderer2 = new ImageRenderer(config);

            Point point = Point.builder().x(0.0).y(0.0).color(0.5).build();

            renderer1.plot(point);
            renderer2.plot(point);

            ImageRenderer mainRenderer = new ImageRenderer(config);
            mainRenderer.merge(List.of(renderer1, renderer2));

            double[][][] histogram = mainRenderer.getHistogram();
            double maxAlpha = 0;
            for (int x = 0; x < 100; x++) {
                for (int y = 0; y < 100; y++) {
                    maxAlpha = Math.max(maxAlpha, histogram[x][y][3]);
                }
            }
            assertThat(maxAlpha).isEqualTo(2.0);
        }

        @Test
        @DisplayName("should merge empty list without errors")
        void shouldMergeEmptyListWithoutErrors() {
            assertThatCode(() -> renderer.merge(List.of())).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should preserve original renderer data after merge")
        void shouldPreserveOriginalDataAfterMerge() {
            Point point = Point.builder().x(0.0).y(0.0).color(0.5).build();
            renderer.plot(point);

            ImageRenderer otherRenderer = new ImageRenderer(config);
            Point otherPoint = Point.builder().x(1.0).y(0.0).color(0.3).build();
            otherRenderer.plot(otherPoint);

            renderer.merge(List.of(otherRenderer));

            double[][][] histogram = renderer.getHistogram();
            int plottedPixels = 0;
            for (int x = 0; x < 100; x++) {
                for (int y = 0; y < 100; y++) {
                    if (histogram[x][y][3] > 0) {
                        plottedPixels++;
                    }
                }
            }
            assertThat(plottedPixels).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Image quality verification")
    class ImageQualityTest {

        @Test
        @DisplayName("rendered image should have non-black pixels for plotted points")
        void renderedImageShouldHaveNonBlackPixels() throws IOException {
            for (int i = 0; i < 100; i++) {
                double x = -1.5 + (i / 100.0) * 3.0;
                Point point = Point.builder().x(x).y(0.0).color(0.5).build();
                renderer.plot(point);
            }

            Path outputPath = tempDir.resolve("quality.png");
            renderer.save(outputPath);

            BufferedImage image = ImageIO.read(outputPath.toFile());

            int nonBlackPixels = 0;
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    if (image.getRGB(x, y) != 0) {
                        nonBlackPixels++;
                    }
                }
            }

            assertThat(nonBlackPixels).isGreaterThan(0);
        }

        @Test
        @DisplayName("higher iteration count should produce more visible pixels")
        void higherIterationsShouldProduceMoreVisiblePixels() throws IOException {
            ImageRenderer renderer1 = new ImageRenderer(config);
            ImageRenderer renderer2 = new ImageRenderer(config);

            for (int i = 0; i < 10; i++) {
                Point point =
                        Point.builder().x(-1.0 + i * 0.2).y(0.0).color(0.5).build();
                renderer1.plot(point);
            }

            // Plot more points in renderer2
            for (int i = 0; i < 100; i++) {
                Point point =
                        Point.builder().x(-1.0 + i * 0.02).y(0.0).color(0.5).build();
                renderer2.plot(point);
            }

            Path path1 = tempDir.resolve("less.png");
            Path path2 = tempDir.resolve("more.png");

            renderer1.save(path1);
            renderer2.save(path2);

            BufferedImage image1 = ImageIO.read(path1.toFile());
            BufferedImage image2 = ImageIO.read(path2.toFile());

            int nonBlack1 = countNonBlackPixels(image1);
            int nonBlack2 = countNonBlackPixels(image2);

            assertThat(nonBlack2).isGreaterThanOrEqualTo(nonBlack1);
        }

        private int countNonBlackPixels(BufferedImage image) {
            int count = 0;
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    if (image.getRGB(x, y) != 0) {
                        count++;
                    }
                }
            }
            return count;
        }
    }
}
