package academy.domain;

import static java.lang.Math.*;

import java.util.Arrays;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public enum Variation {
    LINEAR(point -> point),

    SPHERICAL(point -> {
        double r2 = point.getR2();
        if (r2 == 0) {
            log.trace("SPHERICAL: r2=0, returning point unchanged to avoid division by zero");
            return point;
        }
        double x = point.getX();
        double y = point.getY();
        return point.setX(x / r2).setY(y / r2);
    }),

    SWIRL(point -> {
        double x = point.getX();
        double y = point.getY();
        double r2 = point.getR2();
        return point.setX(x * sin(r2) - y * cos(r2)).setY(x * cos(r2) + y * sin(r2));
    }),

    HORSESHOE(point -> {
        double x = point.getX();
        double y = point.getY();
        double r = point.getR();
        if (r == 0) {
            log.trace("HORSESHOE: r=0, returning point unchanged to avoid division by zero");
            return point;
        }
        return point.setX((x - y) * (x + y) / r).setY(2 * x * y / r);
    }),

    EXPONENTIAL(point -> {
        double x = point.getX();
        double y = point.getY();
        double expPart = exp(x - 1);
        return point.setX(expPart * cos(PI * y)).setY(expPart * sin(PI * y));
    }),

    SINUSOIDAL(point -> point.setX(sin(point.getX())).setY(sin(point.getY())));

    private final UnaryOperator<Point> operator;

    Variation(UnaryOperator<Point> operator) {
        this.operator = operator;
    }

    public static String getValuesAsString() {
        return Arrays.stream(values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.joining(", "));
    }

    public static boolean isValidVariation(String variation) {
        boolean valid =
                Arrays.stream(values()).map(String::valueOf).anyMatch(value -> value.equalsIgnoreCase(variation));

        if (!valid) {
            log.debug("Variation '{}' is not valid. Available: {}", variation, getValuesAsString());
        }

        return valid;
    }
}
