package academy.cli.converter;

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
        if (isNullOrEmpty(value)) {
            log.error("Transform params are null or empty");
            throw new CommandLine.TypeConversionException("Affine params can't be null or empty");
        }

        return Stream.of(value.trim().split(","))
            .map(String::trim)
            .filter(transformString -> !transformString.isEmpty())
            .map(VariationParams::fromString)
            .toList();
    }
}
