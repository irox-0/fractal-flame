package academy.domain;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Variation {
    LINEAR,
    SPHERICAL,
    SWIRL,
    HORSESHOE,
    EXPONENTIAL;

    public static String getValuesAsString() {
        return Arrays.stream(values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.joining(", "));
    }

    public static boolean isValidVariation(String variation) {
        return Arrays.stream(values()).map(String::valueOf).anyMatch(value -> value.equals(variation.toUpperCase()));
    }

}
