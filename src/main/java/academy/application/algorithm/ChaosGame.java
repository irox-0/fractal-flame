package academy.application.algorithm;

import academy.application.render.ImageRenderer;
import academy.domain.AffineParams;
import academy.domain.AppConfiguration;
import academy.domain.Point;
import academy.domain.VariationParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@RequiredArgsConstructor
public class ChaosGame {
    private static final int WARMUP_ITERATIONS = 20;
    private static final int PROGRESS_LOG_INTERVAL_PERCENT = 10;
    private final AppConfiguration configuration;
    private final ImageRenderer renderer;

    public void runSingleThread() {
        log.info("Starting chaos game algorithm (single-threaded)");
        log.debug("Total iterations: {}, warmup iterations: {}",
            configuration.getIterationCount(), WARMUP_ITERATIONS);

        long startTime = System.currentTimeMillis();
        generatePoints(configuration.getRandom(), renderer, configuration.getIterationCount(), 0);
        long endTime = System.currentTimeMillis();
        log.info("Single-threaded generation completed in {} ms", endTime - startTime);
    }

    public void runMultiThread() {
        int threads = configuration.getThreadQuantity();
        int totalIterations = configuration.getIterationCount();
        int iterationsPerThread = totalIterations / threads;
        int remainingIterations = totalIterations % threads;

        log.info("Starting chaos game algorithm (multi-threaded)");
        log.info("Thread pool size: {}", threads);
        log.debug("Iterations per thread: {}, remaining: {}", iterationsPerThread, remainingIterations);

        long startTime = System.currentTimeMillis();
        try (var executor = Executors.newFixedThreadPool(threads)) {
            List<Future<ImageRenderer>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                final int threadIndex = i;
                final int threadIterations = iterationsPerThread + (i < remainingIterations ? 1 : 0);
                log.debug("Submitting task for thread {}: {} iterations", threadIndex, threadIterations);
                var future = executor.submit(() -> {
                    log.debug("Thread {} started", threadIndex);
                    long threadStartTime = System.currentTimeMillis();

                    ImageRenderer localRenderer = new ImageRenderer(configuration);
                    Random random = new Random(configuration.getSeed() + threadIndex);
                    generatePoints(random, localRenderer, threadIterations, threadIndex);

                    long threadEndTime = System.currentTimeMillis();
                    log.debug("Thread {} completed in {} ms", threadIndex, threadEndTime - threadStartTime);

                    return localRenderer;
                });
                futures.add(future);
            }
            log.debug("All tasks submitted, waiting for completion");
            List<ImageRenderer> rendererList = new ArrayList<>();
            int completedThreads = 0;
            for (var future : futures) {
                try {
                    rendererList.add(future.get());
                    completedThreads++;
                    log.debug("Thread result received ({}/{})", completedThreads, threads);
                } catch (InterruptedException e) {
                    log.error("Thread interrupted while waiting for result", e);
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Generation interrupted", e);
                } catch (ExecutionException e) {
                    log.error("Thread execution failed: {}", e.getCause().getMessage(), e.getCause());
                    throw new RuntimeException("Generation failed", e.getCause());
                }
            }
            log.info("All threads completed, merging results");
            long mergeStartTime = System.currentTimeMillis();
            renderer.merge(rendererList);
            long mergeEndTime = System.currentTimeMillis();
            log.debug("Histogram merge completed in {} ms", mergeEndTime - mergeStartTime);
        }

        long endTime = System.currentTimeMillis();
        log.info("Multi-threaded generation completed in {} ms", endTime - startTime);
    }

    private void generatePoints(Random random, ImageRenderer targetRenderer, int iterations, int threadId) {
        Point point = Point.builder()
            .x(random.nextDouble(-1.0, 1.0))
            .y(random.nextDouble(-1.0, 1.0))
            .color(random.nextDouble(0.0, 1.0))
            .build();

        int progressStep = Math.max(1, iterations / PROGRESS_LOG_INTERVAL_PERCENT);
        int nextProgressLog = progressStep;
        int effectiveIterations = iterations - WARMUP_ITERATIONS;

        log.trace("Thread {}: Starting point generation at ({}, {})",
            threadId, point.getX(), point.getY());

        for (int j = 0; j < iterations; j++) {
            int k = random.nextInt(0, configuration.getAffineParamsList().size());
            AffineParams affineParams = configuration.getAffineParamsList().get(k);
            point = applyFunction(point, configuration.getVariationsParamsList(), affineParams);
            point.setColor((point.getColor() + affineParams.getColor()) / 2);

            if (j < WARMUP_ITERATIONS) continue;

            targetRenderer.plot(point);
            if (threadId == 0 && j >= nextProgressLog) {
                int progressPercent = (int) ((j - WARMUP_ITERATIONS) * 100.0 / effectiveIterations);
                log.info("Generation progress: {}% ({}/{} iterations)",
                    progressPercent, j - WARMUP_ITERATIONS, effectiveIterations);
                nextProgressLog += progressStep;
            }
        }
        if (threadId == 0) {
            log.info("Generation progress: 100% ({}/{} iterations)",
                effectiveIterations, effectiveIterations);
        }
        log.debug("Thread {}: Completed {} iterations ({} effective points)",
            threadId, iterations, iterations - WARMUP_ITERATIONS);
    }

    private Point applyFunction(Point point, List<VariationParams> variationParamsList, AffineParams affine) {
        double xAffine = affine.getA() * point.getX() + affine.getB() * point.getY() + affine.getC();
        double yAffine = affine.getD() * point.getX() + affine.getE() * point.getY() + affine.getF();
        double xResult = 0.0;
        double yResult = 0.0;
        for (VariationParams variationParams : variationParamsList) {
            Point tempPoint = Point.builder()
                .x(xAffine)
                .y(yAffine)
                .color(point.getColor())
                .build();

            Point variedPoint = variationParams.variation().getOperator().apply(tempPoint);
            xResult += variedPoint.getX() * variationParams.weight();
            yResult += variedPoint.getY() * variationParams.weight();
        }

        return Point.builder()
            .x(xResult)
            .y(yResult)
            .color(point.getColor())
            .build();
    }
}
