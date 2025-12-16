package academy.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;

@Data
@RequiredArgsConstructor
public class AffineParams{

    private final double a;
    private final double b;
    private final double c;
    private final double d;
    private final double e;
    private final double f;
    private double color;

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
