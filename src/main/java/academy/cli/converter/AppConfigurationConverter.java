package academy.cli.converter;

import static academy.cli.utils.CliUtils.*;
import academy.domain.AppConfiguration;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public class AppConfigurationConverter implements CommandLine.ITypeConverter<AppConfiguration> {

    @Override
    public AppConfiguration convert(String value) throws CommandLine.TypeConversionException {
        if (isNullOrEmpty(value)) {
            return null;
        }
        Path path = Path.of(value);

        ObjectReader reader = new ObjectMapper(new JsonFactory()).findAndRegisterModules().reader();

        AppConfiguration configuration;

        try {
            configuration = reader.readValue(path.toFile(), AppConfiguration.class);
            log.debug("Successfully read json config file from {}", path);
        } catch (IOException e) {
            log.error("Failed to read file {}. Cause: {}", path, e.getMessage());
            throw new CommandLine.TypeConversionException(String.format("Unexpected IO exception. Message: %s", e.getMessage()));
        }

        return configuration;
    }
}
