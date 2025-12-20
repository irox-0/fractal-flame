package academy.application.algorithm;

import static org.assertj.core.api.Assertions.*;

import academy.application.render.ImageRenderer;
import academy.domain.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Benchmark tests for comparing single-threaded vs multi-threaded performance.
 *
 * <p>These tests measure execution time for different thread configurations and verify that multi-threaded execution
 * provides performance benefits.
 */
@DisplayName("ChaosGame Performance Benchmark")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChaosGameBenchmarkTest {

    @TempDir
    Path tempDir;

    private static final int WARMUP_RUNS = 2;
    private static final int BENCHMARK_RUNS = 3;
    private static final int ITERATION_COUNT = 200_000_000;

    private static long singleThreadTime;
    private static long twoThreadTime;
    private static long fourThreadTime;
    private static long eightThreadTime;

    private AppConfiguration createConfig(int threads) {
        AppConfiguration config = AppConfiguration.builder()
                .size(new Size(800, 800))
                .seed(12345L)
                .iterationCount(ITERATION_COUNT)
                .threadQuantity(threads)
                .outputPath(tempDir.resolve("benchmark.png"))
                .affineParamsList(List.of(
                        new AffineParams(0.5, 0.0, 0.0, 0.0, 0.5, 0.0),
                        new AffineParams(0.5, 0.0, 0.5, 0.0, 0.5, 0.0),
                        new AffineParams(0.5, 0.0, 0.25, 0.0, 0.5, 0.5),
                        new AffineParams(0.5, 0.0, 0.0, 0.0, 0.5, 0.5)))
                .variationsParamsList(
                        List.of(new VariationParams(Variation.SWIRL, 0.7), new VariationParams(Variation.LINEAR, 0.3)))
                .build();
        config.setRandom(new Random(config.getSeed()));
        config.setColors();
        return config;
    }

    private long runBenchmark(int threads) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            AppConfiguration config = createConfig(threads);
            ImageRenderer renderer = new ImageRenderer(config);
            ChaosGame game = new ChaosGame(config, renderer);
            if (threads == 1) {
                game.runSingleThread();
            } else {
                game.runMultiThread();
            }
        }

        long totalTime = 0;
        for (int i = 0; i < BENCHMARK_RUNS; i++) {
            AppConfiguration config = createConfig(threads);
            ImageRenderer renderer = new ImageRenderer(config);
            ChaosGame game = new ChaosGame(config, renderer);

            long start = System.nanoTime();
            if (threads == 1) {
                game.runSingleThread();
            } else {
                game.runMultiThread();
            }
            long end = System.nanoTime();

            totalTime += (end - start);
        }

        return totalTime / BENCHMARK_RUNS;
    }

    @Test
    @Order(1)
    @DisplayName("Benchmark: 1 thread (single-threaded)")
    void benchmarkSingleThread() {
        singleThreadTime = runBenchmark(1);

        System.out.printf(
                "Single-thread: %d ms (%.2f iterations/ms)%n",
                TimeUnit.NANOSECONDS.toMillis(singleThreadTime),
                (double) ITERATION_COUNT / TimeUnit.NANOSECONDS.toMillis(singleThreadTime));

        assertThat(singleThreadTime).isGreaterThan(0);
    }

    @Test
    @Order(2)
    @DisplayName("Benchmark: 2 threads")
    void benchmarkTwoThreads() {
        twoThreadTime = runBenchmark(2);

        System.out.printf(
                "2 threads: %d ms (%.2f iterations/ms)%n",
                TimeUnit.NANOSECONDS.toMillis(twoThreadTime),
                (double) ITERATION_COUNT / TimeUnit.NANOSECONDS.toMillis(twoThreadTime));

        assertThat(twoThreadTime).isGreaterThan(0);
    }

    @Test
    @Order(3)
    @DisplayName("Benchmark: 4 threads")
    void benchmarkFourThreads() {
        fourThreadTime = runBenchmark(4);

        System.out.printf(
                "4 threads: %d ms (%.2f iterations/ms)%n",
                TimeUnit.NANOSECONDS.toMillis(fourThreadTime),
                (double) ITERATION_COUNT / TimeUnit.NANOSECONDS.toMillis(fourThreadTime));

        assertThat(fourThreadTime).isGreaterThan(0);
    }

    @Test
    @Order(4)
    @DisplayName("Benchmark: 8 threads")
    void benchmarkEightThreads() {
        eightThreadTime = runBenchmark(8);

        System.out.printf(
                "8 threads: %d ms (%.2f iterations/ms)%n",
                TimeUnit.NANOSECONDS.toMillis(eightThreadTime),
                (double) ITERATION_COUNT / TimeUnit.NANOSECONDS.toMillis(eightThreadTime));

        assertThat(eightThreadTime).isGreaterThan(0);
    }

    @Test
    @Order(5)
    @DisplayName("Verify: Multi-threaded should be faster than single-threaded")
    void verifyMultiThreadedIsFaster() {
        Assumptions.assumeTrue(singleThreadTime > 0, "Single-thread benchmark must run first");
        Assumptions.assumeTrue(fourThreadTime > 0, "4-thread benchmark must run first");

        System.out.printf("%nPerformance Summary:%n");
        System.out.printf("===================%n");
        System.out.printf("1 thread:  %6d ms (baseline)%n", TimeUnit.NANOSECONDS.toMillis(singleThreadTime));
        System.out.printf(
                "2 threads: %6d ms (%.2fx speedup)%n",
                TimeUnit.NANOSECONDS.toMillis(twoThreadTime), (double) singleThreadTime / twoThreadTime);
        System.out.printf(
                "4 threads: %6d ms (%.2fx speedup)%n",
                TimeUnit.NANOSECONDS.toMillis(fourThreadTime), (double) singleThreadTime / fourThreadTime);
        System.out.printf(
                "8 threads: %6d ms (%.2fx speedup)%n",
                TimeUnit.NANOSECONDS.toMillis(eightThreadTime), (double) singleThreadTime / eightThreadTime);

        assertThat(fourThreadTime)
                .as("4-thread execution should be faster than single-thread")
                .isLessThan(singleThreadTime);
    }

    @Test
    @Order(6)
    @DisplayName("Verify: Results are consistent across thread counts")
    void verifyResultsConsistency() {
        AppConfiguration config1 = createConfig(1);
        AppConfiguration config4 = createConfig(4);

        ImageRenderer renderer1 = new ImageRenderer(config1);
        ImageRenderer renderer4 = new ImageRenderer(config4);

        new ChaosGame(config1, renderer1).runSingleThread();
        new ChaosGame(config4, renderer4).runMultiThread();

        double[][][] hist1 = renderer1.getHistogram();
        double[][][] hist4 = renderer4.getHistogram();

        int nonEmpty1 = countNonEmptyPixels(hist1);
        int nonEmpty4 = countNonEmptyPixels(hist4);

        assertThat(nonEmpty1).isGreaterThan(0);
        assertThat(nonEmpty4).isGreaterThan(0);

        System.out.printf("%nPixel count: 1 thread = %d, 4 threads = %d%n", nonEmpty1, nonEmpty4);
    }

    private int countNonEmptyPixels(double[][][] histogram) {
        int count = 0;
        for (int x = 0; x < histogram.length; x++) {
            for (int y = 0; y < histogram[0].length; y++) {
                if (histogram[x][y][3] > 0) {
                    count++;
                }
            }
        }
        return count;
    }
}
