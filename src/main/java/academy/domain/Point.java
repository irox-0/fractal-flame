package academy.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(chain = true)
public class Point {
    private double x;
    private double y;
    private double color;

    public double getR() {
        return Math.sqrt(getR2());
    }

    public double getR2() {
        return x * x + y * y;
    }
}
