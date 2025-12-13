package academy.domain;

import org.jetbrains.annotations.NotNull;
import java.util.Arrays;


public record AffineParams(
    double a,
    double b,
    double c,
    double d,
    double e,
    double f

) {

    public static AffineParams fromString(String transformString) {
        double[] params = Arrays.stream(transformString.split(","))
            .mapToDouble(Double::parseDouble)
            .toArray();

        return new AffineParams(
            params[0], params[1], params[2],
            params[3], params[4], params[5]
        );
    }

    @Override
    public @NotNull String toString() {
        return "AffineParams{" +
            "a=" + a +
            ", b=" + b +
            ", c=" + c +
            ", d=" + d +
            ", e=" + e +
            ", f=" + f +
            '}';
    }
}
