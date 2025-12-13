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
        if (isNullOrEmpty(value)) {
            log.error("Affine params are null or empty");
            throw new CommandLine.TypeConversionException("Affine params can't be null or empty");
        }

        return Stream.of(value.trim().split("/"))
            .map(String::trim)
            .filter(transformString -> !transformString.isEmpty())
            .peek(transformString -> validateTransformString(transformString, EXPECTED_QUANTITY))
            .map(AffineParams::fromString)
            .toList();
    }

}
