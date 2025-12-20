package academy.application.algorithm;

import academy.application.render.ImageRenderer;
import academy.domain.AffineParams;
import academy.domain.AppConfiguration;
import academy.domain.Point;
import academy.domain.VariationParams;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RequiredArgsConstructor
public class ChaosGame {

    private static final int WARMUP_ITERATIONS = 20;

    private final AppConfiguration configuration;
    private final ImageRenderer renderer;

    public void runSingleThread() {
        generatePoints(configuration.getRandom(), renderer, configuration.getIterationCount());
    }

    public void runMultiThread() {
        try (var executor = Executors.newFixedThreadPool(configuration.getThreadQuantity())) {
            int threads = configuration.getThreadQuantity();
            int iterationPerThread = configuration.getIterationCount() / threads;
            List<Future<ImageRenderer>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                var threadIndex = i;
                var future = executor.submit(() -> {
                    ImageRenderer localRenderer = new ImageRenderer(configuration);
                    Random random = new Random(configuration.getSeed() + threadIndex);
                    generatePoints(random, localRenderer, iterationPerThread);
                    return localRenderer;
                });
                futures.add(future);
            }

            List<ImageRenderer> rendererList = new ArrayList<>();
            for (var future : futures) {
                try {
                    rendererList.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("end of multi");
            renderer.merge(rendererList);
        }
    }

    private void generatePoints(Random random, ImageRenderer targetRenderer, int iterations) {
        Point point = Point.builder()
            .x(random.nextDouble(-1.0, 1.0))
            .y(random.nextDouble(-1.0, 1.0))
            .color(random.nextDouble(0.0, 1.0))
            .build();

        for (int j = 0; j < iterations; j++) {
            int k = random.nextInt(0, configuration.getAffineParamsList().size());
            AffineParams affineParams = configuration.getAffineParamsList().get(k);
            point = applyFunction(point, configuration.getVariationsParamsList(), affineParams);
            point.setColor((point.getColor() + affineParams.getColor()) / 2);

            if (j < WARMUP_ITERATIONS) continue;
            targetRenderer.plot(point);
        }
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
