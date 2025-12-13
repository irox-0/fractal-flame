package academy.cli.converter;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import static academy.cli.utils.CliUtils.*;

@Slf4j
public class PathConverter implements CommandLine.ITypeConverter<Path> {

    @Override
    public Path convert(String value) throws CommandLine.TypeConversionException {
        if (isNullOrEmpty(value)) {
            log.error("Path value is null or empty");
            throw new CommandLine.TypeConversionException("Path value can't be null or empty");
        }
        try {
            Path path = Path.of(value);
            log.debug("Successfully converted path: {}", value);
            return path;
        } catch (InvalidPathException e) {
            log.error("Invalid path format: {}. Error: {}", value, e.getMessage());
            throw new CommandLine.TypeConversionException("Invalid path format: " + value);
        }
    }
}
