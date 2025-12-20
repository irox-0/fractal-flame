package academy.cli.converter;

import academy.domain.AffineParams;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import java.util.List;
import java.util.stream.Stream;

import static academy.cli.utils.CliUtils.*;

@Slf4j
public class AffineParamsConverter implements CommandLine.ITypeConverter<List<AffineParams>> {

    public static final int EXPECTED_QUANTITY = 6;

    @Override
    public List<AffineParams> convert(String value) throws CommandLine.TypeConversionException {
        log.debug("Converting affine params from string: '{}'", value);

        if (isNullOrEmpty(value)) {
            log.error("Affine params are null or empty");
            throw new CommandLine.TypeConversionException("Affine params can't be null or empty");
        }

        try {
            List<AffineParams> result = Stream.of(value.trim().split("/"))
                .map(String::trim)
                .filter(transformString -> !transformString.isEmpty())
                .peek(transformString -> validateTransformString(transformString, EXPECTED_QUANTITY))
                .map(AffineParams::fromString)
                .toList();

            log.info("Successfully parsed {} affine transformation(s)", result.size());
            log.debug("Affine params: {}", result);

            return result;

        } catch (NumberFormatException e) {
            log.error("Invalid number format in affine params: {}", e.getMessage());
            throw new CommandLine.TypeConversionException(
                "Invalid number format in affine params: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to parse affine params: {}", e.getMessage());
            throw new CommandLine.TypeConversionException(
                "Failed to parse affine params: " + e.getMessage());
        }
    }
}
