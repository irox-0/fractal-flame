package academy.domain;

import lombok.Getter;
import java.util.Arrays;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.lang.Math.*;

@Getter
public enum Variation {
    LINEAR(point -> point),

    SPHERICAL(point -> {
        double r2 = point.getR2();
        double x = point.getX();
        double y = point.getY();
        return point.setX(x / r2).setY(y / r2);
    }),

    SWIRL(point -> {
        double x = point.getX();
        double y = point.getY();
        double r2 = point.getR2();
        return point
            .setX(x * sin(r2) - y * cos(r2))
            .setY(x * cos(r2) + y * sin(r2));
    }),

    HORSESHOE(point -> {
        double x = point.getX();
        double y = point.getY();
        double r = point.getR();
        return point
            .setX((x - y) * (x + y) / r)
            .setY(2 * x * y / r);
    }),

    EXPONENTIAL(point -> {
        double x = point.getX();
        double y = point.getY();
        double expPart = exp(x - 1);
        return point
            .setX(expPart * cos(PI * y))
            .setY(expPart * sin(PI * y));
    }),
    SINUSOIDAL(point -> point
        .setX(sin(point.getX()))
        .setY(sin(point.getY()))
    );

    private final UnaryOperator<Point> operator;

    Variation(UnaryOperator<Point> operator) {
        this.operator = operator;
    }

    public static String getValuesAsString() {
        return Arrays.stream(values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.joining(", "));
    }

    public static boolean isValidVariation(String variation) {
        return Arrays.stream(values()).map(String::valueOf).anyMatch(value -> value.equals(variation.toUpperCase()));
    }

}
