package academy.cli.converter;

import static academy.cli.utils.CliUtils.*;

import academy.domain.AppConfiguration;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Slf4j
public class AppConfigurationConverter implements CommandLine.ITypeConverter<AppConfiguration> {

    @Override
    public AppConfiguration convert(String value) throws CommandLine.TypeConversionException {
        if (isNullOrEmpty(value)) {
            log.debug("No configuration file specified, will use CLI arguments or defaults");
            return null;
        }

        Path path = Path.of(value);
        log.info("Loading configuration from file: {}", path.toAbsolutePath());

        if (!Files.exists(path)) {
            log.error("Configuration file not found: {}", path.toAbsolutePath());
            throw new CommandLine.TypeConversionException("Configuration file not found: " + path.toAbsolutePath());
        }

        if (!Files.isRegularFile(path)) {
            log.error("Configuration path is not a file: {}", path.toAbsolutePath());
            throw new CommandLine.TypeConversionException("Configuration path is not a file: " + path.toAbsolutePath());
        }

        if (!Files.isReadable(path)) {
            log.error("Configuration file is not readable: {}", path.toAbsolutePath());
            throw new CommandLine.TypeConversionException(
                    "Configuration file is not readable: " + path.toAbsolutePath());
        }

        ObjectReader reader =
                new ObjectMapper(new JsonFactory()).findAndRegisterModules().reader();

        AppConfiguration configuration;

        try {
            configuration = reader.readValue(path.toFile(), AppConfiguration.class);
            log.info("Successfully loaded configuration from {}", path);
            logConfigurationDetails(configuration);

        } catch (IOException e) {
            log.error("Failed to parse configuration file {}: {}", path, e.getMessage());
            log.debug("Parse error details:", e);
            throw new CommandLine.TypeConversionException(
                    String.format("Failed to parse configuration file: %s", e.getMessage()));
        }

        return configuration;
    }

    private void logConfigurationDetails(AppConfiguration config) {
        log.debug("Configuration details:");
        log.debug(
                "  Image size: {}x{}",
                config.getSize() != null ? config.getSize().width() : "null",
                config.getSize() != null ? config.getSize().height() : "null");
        log.debug("  Iterations: {}", config.getIterationCount());
        log.debug("  Threads: {}", config.getThreadQuantity());
        log.debug("  Seed: {}", config.getSeed());
        log.debug("  Output: {}", config.getOutputPath());
        log.debug(
                "  Affine params count: {}",
                config.getAffineParamsList() != null
                        ? config.getAffineParamsList().size()
                        : 0);
        log.debug(
                "  Variations count: {}",
                config.getVariationsParamsList() != null
                        ? config.getVariationsParamsList().size()
                        : 0);
    }
}
