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
            log.error("Incorrect affine params quantity. Expected {}, received {}: '{}'", expectedQuantity, parts.length, transformString);
            throw new CommandLine.TypeConversionException(
                String.format("Incorrect affine params quantity. Expected %d, received %d: '%s'",
                    expectedQuantity, parts.length, transformString)
            );
        }
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

}
