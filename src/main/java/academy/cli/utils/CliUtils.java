package academy.cli.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Slf4j
@UtilityClass
public class CliUtils {

    public static void validateTransformString(String transformString, int expectedQuantity) {
        String[] parts = transformString.split(",");
        if (parts.length != expectedQuantity) {
            log.error(
                    "Incorrect parameter quantity. Expected {}, received {}: '{}'",
                    expectedQuantity,
                    parts.length,
                    transformString);
            throw new CommandLine.TypeConversionException(String.format(
                    "Incorrect parameter quantity. Expected %d, received %d: '%s'",
                    expectedQuantity, parts.length, transformString));
        }
        log.trace("Validated transform string: '{}' ({} parameters)", transformString, parts.length);
    }

    public static boolean isNullOrEmpty(String value) {
        boolean result = value == null || value.trim().isEmpty();
        if (result) {
            log.trace("Value is null or empty");
        }
        return result;
    }
}
