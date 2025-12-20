package academy.cli.converter;

import academy.domain.Variation;
import academy.domain.VariationParams;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import java.util.List;
import java.util.stream.Stream;
import static academy.cli.utils.CliUtils.*;

@Slf4j
public class VariationParamsConverter implements CommandLine.ITypeConverter<List<VariationParams>> {

    @Override
    public List<VariationParams> convert(String value) throws CommandLine.TypeConversionException {
        log.debug("Converting variation params from string: '{}'", value);

        if (isNullOrEmpty(value)) {
            log.error("Variation params are null or empty");
            throw new CommandLine.TypeConversionException("Variation params can't be null or empty");
        }

        try {
            List<VariationParams> result = Stream.of(value.trim().split(","))
                .map(String::trim)
                .filter(transformString -> !transformString.isEmpty())
                .map(this::parseVariationParam)
                .toList();

            log.info("Successfully parsed {} variation function(s)", result.size());
            for (VariationParams vp : result) {
                log.debug("  Variation: {} (weight: {})", vp.variation(), vp.weight());
            }
            return result;

        } catch (CommandLine.TypeConversionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse variation params: {}", e.getMessage());
            throw new CommandLine.TypeConversionException(
                "Failed to parse variation params: " + e.getMessage());
        }
    }

    private VariationParams parseVariationParam(String value) {
        String[] parts = value.split(":");

        if (parts.length != 2) {
            log.error("Invalid variation format: '{}'. Expected format: 'name:weight'", value);
            throw new CommandLine.TypeConversionException(
                "Invalid variation format: '" + value + "'. Expected format: 'name:weight'");
        }

        String variationName = parts[0].trim().toUpperCase();
        String weightStr = parts[1].trim();

        if (!Variation.isValidVariation(variationName)) {
            log.error("Unknown variation: '{}'. Available variations: {}",
                variationName, Variation.getValuesAsString());
            throw new CommandLine.TypeConversionException(
                "Unknown variation: '" + variationName + "'. Available: " + Variation.getValuesAsString());
        }

        double weight;
        try {
            weight = Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            log.error("Invalid weight value: '{}' for variation '{}'", weightStr, variationName);
            throw new CommandLine.TypeConversionException(
                "Invalid weight value: '" + weightStr + "'. Must be a number.");
        }

        if (weight < 0) {
            log.warn("Negative weight {} for variation {}. This may produce unexpected results.",
                weight, variationName);
        }

        return new VariationParams(Variation.valueOf(variationName), weight);
    }
}
