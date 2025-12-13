package academy.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import picocli.CommandLine;

public record VariationParams(
    @JsonProperty("name")
    Variation variation,
    Double weight
    ) {

    @JsonCreator
    public VariationParams(
        @JsonProperty("name") String name,
        @JsonProperty("weight") Double weight
    ) {
        this(Variation.valueOf(name.toUpperCase()), weight);
    }


    public static VariationParams fromString(String value) {
        String[] values = value.split(":");
        VariationParams params;
        try {
            params = new VariationParams(Variation.valueOf(values[0].toUpperCase()), Double.valueOf(values[1]));
        } catch (Exception e) {
            throw new CommandLine.TypeConversionException("Incorrect values");
        }

        return params;
    }

    @Override
    public String toString() {
        return "VariationParams{" +
            "variation=" + variation +
            ", weight=" + weight +
            '}';
    }
}
