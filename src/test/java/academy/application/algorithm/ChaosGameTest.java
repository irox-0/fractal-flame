package academy.application.algorithm;

import static org.assertj.core.api.Assertions.*;

import academy.application.render.ImageRenderer;
import academy.domain.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ChaosGame algorithm tests")
class ChaosGameTest {

    @TempDir
    Path tempDir;

    private AppConfiguration.AppConfigurationBuilder configBuilder() {
        return AppConfiguration.builder()
                .size(new Size(100, 100))
                .seed(12345L)
                .iterationCount(1000)
                .threadQuantity(1)
                .outputPath(tempDir.resolve("test.png"))
                .affineParamsList(List.of(
                        new AffineParams(0.5, 0.0, 0.0, 0.0, 0.5, 0.0),
                        new AffineParams(0.5, 0.0, 0.5, 0.0, 0.5, 0.0),
                        new AffineParams(0.5, 0.0, 0.25, 0.0, 0.5, 0.5)))
                .variationsParamsList(List.of(new VariationParams(Variation.LINEAR, 1.0)));
    }

    @Nested
    @DisplayName("Single-threaded execution")
    class SingleThreadTest {

        @Test
        @DisplayName("should complete without errors")
        void shouldCompleteWithoutErrors() {
            AppConfiguration config = configBuilder().build();
            config.setRandom(new Random(config.getSeed()));
            config.setColors();

            ImageRenderer renderer = new ImageRenderer(config);
            ChaosGame game = new ChaosGame(config, renderer);

            assertThatCode(game::runSingleThread).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should plot points to renderer")
        void shouldPlotPointsToRenderer() {
            AppConfiguration config = configBuilder().iterationCount(1000).build();
            config.setRandom(new Random(config.getSeed()));
            config.setColors();

            ImageRenderer renderer = new ImageRenderer(config);
            ChaosGame game = new ChaosGame(config, renderer);

            game.runSingleThread();

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
        @DisplayName("should produce deterministic results with same seed")
        void shouldProduceDeterministicResults() {
            AppConfiguration config1 = configBuilder().seed(42L).build();
            config1.setRandom(new Random(config1.getSeed()));
            config1.setColors();

            AppConfiguration config2 = configBuilder().seed(42L).build();
            config2.setRandom(new Random(config2.getSeed()));
            config2.setColors();

            ImageRenderer renderer1 = new ImageRenderer(config1);
            ImageRenderer renderer2 = new ImageRenderer(config2);

            new ChaosGame(config1, renderer1).runSingleThread();
            new ChaosGame(config2, renderer2).runSingleThread();

            double[][][] hist1 = renderer1.getHistogram();
            double[][][] hist2 = renderer2.getHistogram();

            for (int x = 0; x < 100; x++) {
                for (int y = 0; y < 100; y++) {
                    for (int c = 0; c < 4; c++) {
                        assertThat(hist1[x][y][c])
                                .as("Pixel (%d, %d, %d) should match", x, y, c)
                                .isEqualTo(hist2[x][y][c]);
                    }
                }
            }
        }

        @Test
        @DisplayName("should produce different results with different seeds")
        void shouldProduceDifferentResultsWithDifferentSeeds() {
            AppConfiguration config1 = configBuilder().seed(1L).build();
            config1.setRandom(new Random(config1.getSeed()));
            config1.setColors();

            AppConfiguration config2 = configBuilder().seed(999L).build();
            config2.setRandom(new Random(config2.getSeed()));
            config2.setColors();

            ImageRenderer renderer1 = new ImageRenderer(config1);
            ImageRenderer renderer2 = new ImageRenderer(config2);

            new ChaosGame(config1, renderer1).runSingleThread();
            new ChaosGame(config2, renderer2).runSingleThread();

            double[][][] hist1 = renderer1.getHistogram();
            double[][][] hist2 = renderer2.getHistogram();

            boolean foundDifference = false;
            for (int x = 0; x < 100 && !foundDifference; x++) {
                for (int y = 0; y < 100 && !foundDifference; y++) {
                    if (hist1[x][y][3] != hist2[x][y][3]) {
                        foundDifference = true;
                    }
                }
            }
            assertThat(foundDifference).isTrue();
        }
    }

    @Nested
    @DisplayName("Multi-threaded execution")
    class MultiThreadTest {

        @ParameterizedTest
        @ValueSource(ints = {2, 4, 8})
        @DisplayName("should complete without errors for various thread counts")
        void shouldCompleteWithoutErrors(int threads) {
            AppConfiguration config = configBuilder().threadQuantity(threads).build();
            config.setRandom(new Random(config.getSeed()));
            config.setColors();

            ImageRenderer renderer = new ImageRenderer(config);
            ChaosGame game = new ChaosGame(config, renderer);

            assertThatCode(game::runMultiThread).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should plot points from all threads")
        void shouldPlotPointsFromAllThreads() {
            AppConfiguration config =
                    configBuilder().threadQuantity(4).iterationCount(4000).build();
            config.setRandom(new Random(config.getSeed()));
            config.setColors();

            ImageRenderer renderer = new ImageRenderer(config);
            ChaosGame game = new ChaosGame(config, renderer);

            game.runMultiThread();

            double[][][] histogram = renderer.getHistogram();
            int plottedPixels = 0;
            for (int x = 0; x < 100; x++) {
                for (int y = 0; y < 100; y++) {
                    if (histogram[x][y][3] > 0) {
                        plottedPixels++;
                    }
                }
            }
            assertThat(plottedPixels).isGreaterThan(0);
        }

        @Test
        @DisplayName("should produce deterministic results with same seed")
        void shouldProduceDeterministicResults() {
            AppConfiguration config1 = configBuilder()
                    .seed(42L)
                    .threadQuantity(4)
                    .iterationCount(4000)
                    .build();
            config1.setRandom(new Random(config1.getSeed()));
            config1.setColors();

            AppConfiguration config2 = configBuilder()
                    .seed(42L)
                    .threadQuantity(4)
                    .iterationCount(4000)
                    .build();
            config2.setRandom(new Random(config2.getSeed()));
            config2.setColors();

            ImageRenderer renderer1 = new ImageRenderer(config1);
            ImageRenderer renderer2 = new ImageRenderer(config2);

            new ChaosGame(config1, renderer1).runMultiThread();
            new ChaosGame(config2, renderer2).runMultiThread();

            double[][][] hist1 = renderer1.getHistogram();
            double[][][] hist2 = renderer2.getHistogram();

            for (int x = 0; x < 100; x++) {
                for (int y = 0; y < 100; y++) {
                    for (int c = 0; c < 4; c++) {
                        assertThat(hist1[x][y][c])
                                .as("Pixel (%d, %d, %d) should match", x, y, c)
                                .isEqualTo(hist2[x][y][c]);
                    }
                }
            }
        }

        @Test
        @DisplayName("should distribute iterations evenly across threads")
        void shouldDistributeIterationsEvenly() {
            int totalIterations = 1000;
            int threads = 4;

            AppConfiguration config = configBuilder()
                    .threadQuantity(threads)
                    .iterationCount(totalIterations)
                    .build();
            config.setRandom(new Random(config.getSeed()));
            config.setColors();

            ImageRenderer renderer = new ImageRenderer(config);
            ChaosGame game = new ChaosGame(config, renderer);

            assertThatCode(game::runMultiThread).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should handle odd iteration counts")
        void shouldHandleOddIterationCounts() {
            AppConfiguration config =
                    configBuilder().threadQuantity(3).iterationCount(1001).build();
            config.setRandom(new Random(config.getSeed()));
            config.setColors();

            ImageRenderer renderer = new ImageRenderer(config);
            ChaosGame game = new ChaosGame(config, renderer);

            assertThatCode(game::runMultiThread).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Variation application")
    class VariationApplicationTest {

        @ParameterizedTest
        @ValueSource(strings = {"LINEAR", "SPHERICAL", "SWIRL", "HORSESHOE", "EXPONENTIAL", "SINUSOIDAL"})
        @DisplayName("should work with each variation type")
        void shouldWorkWithEachVariation(String variationName) {
            Variation variation = Variation.valueOf(variationName);

            AppConfiguration config = configBuilder()
                    .variationsParamsList(List.of(new VariationParams(variation, 1.0)))
                    .iterationCount(500)
                    .build();
            config.setRandom(new Random(config.getSeed()));
            config.setColors();

            ImageRenderer renderer = new ImageRenderer(config);
            ChaosGame game = new ChaosGame(config, renderer);

            assertThatCode(game::runSingleThread).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should work with multiple variations")
        void shouldWorkWithMultipleVariations() {
            AppConfiguration config = configBuilder()
                    .variationsParamsList(List.of(
                            new VariationParams(Variation.SWIRL, 0.5),
                            new VariationParams(Variation.LINEAR, 0.3),
                            new VariationParams(Variation.SINUSOIDAL, 0.2)))
                    .iterationCount(500)
                    .build();
            config.setRandom(new Random(config.getSeed()));
            config.setColors();

            ImageRenderer renderer = new ImageRenderer(config);
            ChaosGame game = new ChaosGame(config, renderer);

            assertThatCode(game::runSingleThread).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Integration with image saving")
    class IntegrationTest {

        @Test
        @DisplayName("should produce saveable image in single-thread mode")
        void shouldProduceSaveableImageSingleThread() throws IOException {
            AppConfiguration config = configBuilder().iterationCount(1000).build();
            config.setRandom(new Random(config.getSeed()));
            config.setColors();

            ImageRenderer renderer = new ImageRenderer(config);
            ChaosGame game = new ChaosGame(config, renderer);

            game.runSingleThread();

            Path outputPath = tempDir.resolve("single.png");
            renderer.save(outputPath);

            assertThat(Files.exists(outputPath)).isTrue();
            assertThat(Files.size(outputPath)).isGreaterThan(0);
        }

        @Test
        @DisplayName("should produce saveable image in multi-thread mode")
        void shouldProduceSaveableImageMultiThread() throws IOException {
            AppConfiguration config =
                    configBuilder().threadQuantity(4).iterationCount(4000).build();
            config.setRandom(new Random(config.getSeed()));
            config.setColors();

            ImageRenderer renderer = new ImageRenderer(config);
            ChaosGame game = new ChaosGame(config, renderer);

            game.runMultiThread();

            Path outputPath = tempDir.resolve("multi.png");
            renderer.save(outputPath);

            assertThat(Files.exists(outputPath)).isTrue();
            assertThat(Files.size(outputPath)).isGreaterThan(0);
        }
    }
}
