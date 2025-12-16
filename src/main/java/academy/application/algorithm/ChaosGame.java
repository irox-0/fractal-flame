package academy.application.algorithm;

import academy.application.render.ImageRenderer;
import academy.domain.AffineParams;
import academy.domain.AppConfiguration;
import academy.domain.Point;
import academy.domain.VariationParams;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
public class ChaosGame {

    private static final int WARMUP_ITERATIONS = 20;

    private final AppConfiguration configuration;
    private final ImageRenderer renderer;

    public void runSingleThread() {
        Random random = configuration.getRandom();
        Point point = Point.builder()
            .x(random.nextDouble(-1.0, 1.0))
            .y(random.nextDouble(-1.0, 1.0))
            .color(random.nextDouble(0.0, 1.0))
            .build();

        for (int i = 0; i < configuration.getIterationCount(); i++) {
            int k = random.nextInt(0, configuration.getAffineParamsList().size());
            AffineParams affineParams = configuration.getAffineParamsList().get(k);
            point = applyFunction(point, configuration.getVariationsParamsList(), affineParams);
            point.setColor((point.getColor() + affineParams.getColor()) / 2);
            if (i < WARMUP_ITERATIONS) continue;

            renderer.plot(point);
        }

        renderer.save(configuration.getOutputPath());
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
