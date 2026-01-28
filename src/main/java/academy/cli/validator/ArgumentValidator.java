package academy.cli.validator;

import academy.Application;
import academy.domain.AffineParams;
import academy.domain.AppConfiguration;
import academy.domain.Variation;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Slf4j
@UtilityClass
public class ArgumentValidator {

    private static final String PNG_EXTENSION = ".png";
    private static final int MIN_DIMENSION = 1;
    private static final int MAX_DIMENSION = 16384;
    private static final int MIN_ITERATIONS = 1;
    private static final int MIN_THREADS = 1;

    public static void validate(AppConfiguration config, Application app) {
        log.debug("Starting validation of application configuration");

        validateImageDimensions(config, app);
        validateIterationCount(config, app);
        validateThreadQuantity(config, app);
        validateOutputPath(config, app);
        validateAffineParams(config, app);
        validateVariationParams(config, app);

        log.debug("All validations passed successfully");
    }

    public static void validateImageDimensions(AppConfiguration config, Application app) {
        log.debug(
                "Validating image dimensions: {}x{}",
                config.getSize().width(),
                config.getSize().height());

        int width = config.getSize().width();
        int height = config.getSize().height();

        if (width < MIN_DIMENSION || width > MAX_DIMENSION) {
            log.error("Invalid image width: {}. Must be between {} and {}", width, MIN_DIMENSION, MAX_DIMENSION);
            throw new CommandLine.ParameterException(
                    new CommandLine(app),
                    String.format(
                            "Image width must be between %d and %d, got: %d", MIN_DIMENSION, MAX_DIMENSION, width));
        }

        if (height < MIN_DIMENSION || height > MAX_DIMENSION) {
            log.error("Invalid image height: {}. Must be between {} and {}", height, MIN_DIMENSION, MAX_DIMENSION);
            throw new CommandLine.ParameterException(
                    new CommandLine(app),
                    String.format(
                            "Image height must be between %d and %d, got: %d", MIN_DIMENSION, MAX_DIMENSION, height));
        }

        log.debug("Image dimensions validation passed");
    }

    public static void validateIterationCount(AppConfiguration config, Application app) {
        log.debug("Validating iteration count: {}", config.getIterationCount());

        int iterations = config.getIterationCount();

        if (iterations < MIN_ITERATIONS) {
            log.error("Invalid iteration count: {}. Must be at least {}", iterations, MIN_ITERATIONS);
            throw new CommandLine.ParameterException(
                    new CommandLine(app),
                    String.format("Iteration count must be at least %d, got: %d", MIN_ITERATIONS, iterations));
        }

        if (iterations < 100) {
            log.warn("Very low iteration count ({}). Image quality may be poor.", iterations);
        }

        log.debug("Iteration count validation passed");
    }

    public static void validateThreadQuantity(AppConfiguration config, Application app) {
        log.debug("Validating thread quantity: {}", config.getThreadQuantity());

        int threads = config.getThreadQuantity();
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        if (threads < MIN_THREADS) {
            log.error("Invalid thread quantity: {}. Must be at least {}", threads, MIN_THREADS);
            throw new CommandLine.ParameterException(
                    new CommandLine(app),
                    String.format("Thread quantity must be at least %d, got: %d", MIN_THREADS, threads));
        }

        if (threads > availableProcessors * 2) {
            log.warn(
                    "Thread quantity ({}) exceeds recommended maximum ({}). " + "This may degrade performance.",
                    threads,
                    availableProcessors * 2);
        }

        log.debug("Thread quantity validation passed");
    }

    public static void validateOutputPath(AppConfiguration config, Application app) {
        Path outputPath = config.getOutputPath();
        log.debug("Validating output path: {}", outputPath);

        if (outputPath == null) {
            log.error("Output path is null");
            throw new CommandLine.ParameterException(new CommandLine(app), "Output path cannot be null");
        }

        Path fileNamePath = outputPath.getFileName();
        if (fileNamePath == null) {
            log.error("Output path is a root directory: {}", outputPath);
            throw new CommandLine.ParameterException(
                    new CommandLine(app),
                    String.format("Output path must include a file name, got root directory: %s", outputPath));
        }

        String fileName = fileNamePath.toString().toLowerCase();
        if (!fileName.endsWith(PNG_EXTENSION)) {
            log.error("Invalid output file extension: {}. Expected: {}", fileName, PNG_EXTENSION);
            throw new CommandLine.ParameterException(
                    new CommandLine(app),
                    String.format("Output file must have %s extension, got: %s", PNG_EXTENSION, fileName));
        }

        Path parentDir = outputPath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            log.debug("Parent directory does not exist, will be created: {}", parentDir);
        }

        if (Files.exists(outputPath)) {
            log.warn("Output file already exists and will be overwritten: {}", outputPath);
        }

        log.debug("Output path validation passed");
    }

    public static void validateAffineParams(AppConfiguration config, Application app) {
        log.debug("Validating affine parameters");

        var affineParams = config.getAffineParamsList();

        if (affineParams == null || affineParams.isEmpty()) {
            log.error("Affine parameters list is null or empty");
            throw new CommandLine.ParameterException(
                    new CommandLine(app), "At least one affine transformation is required");
        }

        log.debug("Found {} affine transformation(s)", affineParams.size());

        for (int i = 0; i < affineParams.size(); i++) {
            var params = affineParams.get(i);
            if (params == null) {
                log.error("Affine parameter at index {} is null", i);
                throw new CommandLine.ParameterException(
                        new CommandLine(app), String.format("Affine parameter at index %d is null", i));
            }

            if (isValidAffineParams(params)) {
                log.error("Affine parameter at index {} contains NaN or Infinite values", i);
                throw new CommandLine.ParameterException(
                        new CommandLine(app),
                        String.format("Affine parameter at index %d contains invalid values (NaN or Infinite)", i));
            }

            log.trace("Affine params [{}]: {}", i, params);
        }

        log.debug("Affine parameters validation passed");
    }

    private static boolean isValidAffineParams(AffineParams params) {
        return Double.isNaN(params.getA())
                || Double.isInfinite(params.getA())
                || Double.isNaN(params.getB())
                || Double.isInfinite(params.getB())
                || Double.isNaN(params.getC())
                || Double.isInfinite(params.getC())
                || Double.isNaN(params.getD())
                || Double.isInfinite(params.getD())
                || Double.isNaN(params.getE())
                || Double.isInfinite(params.getE())
                || Double.isNaN(params.getF())
                || Double.isInfinite(params.getF());
    }

    public static void validateVariationParams(AppConfiguration config, Application app) {
        log.debug("Validating variation parameters");

        var variationParams = config.getVariationsParamsList();

        if (variationParams == null || variationParams.isEmpty()) {
            log.error("Variation parameters list is null or empty");
            throw new CommandLine.ParameterException(
                    new CommandLine(app), "At least one variation function is required");
        }

        log.debug("Found {} variation function(s)", variationParams.size());

        double totalWeight = 0.0;

        for (int i = 0; i < variationParams.size(); i++) {
            var params = variationParams.get(i);

            if (params == null) {
                log.error("Variation parameter at index {} is null", i);
                throw new CommandLine.ParameterException(
                        new CommandLine(app), String.format("Variation parameter at index %d is null", i));
            }

            if (params.variation() == null) {
                log.error("Variation type at index {} is null", i);
                throw new CommandLine.ParameterException(
                        new CommandLine(app),
                        String.format(
                                "Variation type at index %d is null. Available: %s", i, Variation.getValuesAsString()));
            }

            if (params.weight() == null || Double.isNaN(params.weight()) || Double.isInfinite(params.weight())) {
                log.error("Invalid weight at index {}: {}", i, params.weight());
                throw new CommandLine.ParameterException(
                        new CommandLine(app), String.format("Invalid weight at index %d: %s", i, params.weight()));
            }

            if (params.weight() < 0) {
                log.warn(
                        "Negative weight ({}) for variation {} at index {}. This may produce unexpected results.",
                        params.weight(),
                        params.variation(),
                        i);
            }

            totalWeight += Math.abs(params.weight());
            log.trace("Variation params [{}]: {} (weight: {})", i, params.variation(), params.weight());
        }

        if (totalWeight == 0.0) {
            log.error("Total weight of all variations is zero");
            throw new CommandLine.ParameterException(new CommandLine(app), "Total weight of variations cannot be zero");
        }

        log.debug("Variation parameters validation passed (total weight: {})", totalWeight);
    }
}
