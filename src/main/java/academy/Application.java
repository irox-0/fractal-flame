package academy;

import academy.application.algorithm.ChaosGame;
import academy.application.render.ImageRenderer;
import academy.cli.converter.AffineParamsConverter;
import academy.cli.converter.AppConfigurationConverter;
import academy.cli.converter.PathConverter;
import academy.cli.converter.VariationParamsConverter;
import academy.cli.validator.ArgumentValidator;
import academy.domain.AffineParams;
import academy.domain.AppConfiguration;
import academy.domain.Size;
import academy.domain.VariationParams;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Slf4j
@Command(name = "Fractal Flame Generator", version = "1.0", mixinStandardHelpOptions = true)
public class Application implements Runnable {

    @Option(
            names = {"-w", "--width"},
            description = "Image width (default: ${DEFAULT-VALUE})",
            defaultValue = "1920")
    private int width;

    @Option(
            names = {"-h", "--height"},
            description = "Image height (default: ${DEFAULT-VALUE})",
            defaultValue = "1080")
    private int height;

    @Option(
            names = {"--seed"},
            description = "Random seed (default: ${DEFAULT-VALUE})",
            defaultValue = "5")
    private long seed;

    @Option(
            names = {"-i", "--iteration-count"},
            description = "Count of generation iterations (default: ${DEFAULT-VALUE})",
            defaultValue = "2500")
    private int iterationCount;

    @Option(
            names = {"-o", "--output-path"},
            description = "Path to output image file (default: ${DEFAULT-VALUE})",
            defaultValue = "result.png",
            converter = PathConverter.class)
    private Path outputPath;

    @Option(
            names = {"-t", "--threads"},
            description = "Thread quantity (default: ${DEFAULT-VALUE})",
            defaultValue = "1")
    private int threadQuantity;

    @Option(
            names = {"-ap", "--affine-params"},
            description = "Configuration of affine transformations",
            converter = AffineParamsConverter.class,
            defaultValue = "0.1,0.1,0.1,0.1,0.1,0.1")
    private List<AffineParams> affineParamsList;

    @Option(
            names = {"-f", "--functions"},
            description = "Transformation function and its weight",
            converter = VariationParamsConverter.class,
            defaultValue = "swirl:1.0")
    private List<VariationParams> variationParamsList;

    @Option(
            names = "--config",
            description = "Application configuration file (JSON)",
            converter = AppConfigurationConverter.class)
    private AppConfiguration appConfiguration;

    public static void main(String[] args) {
        log.info("Starting Fractal Flame Generator");
        int exitCode = new CommandLine(new Application()).execute(args);
        if (exitCode == 0) {
            log.info("Application completed successfully");
        } else {
            log.error("Application finished with exit code: {}", exitCode);
        }
        System.exit(exitCode);
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();

        try {
            initializeConfiguration();
            validate();
            logConfiguration();
            initializeRandom();
            ImageRenderer renderer = new ImageRenderer(appConfiguration);
            ChaosGame game = new ChaosGame(appConfiguration, renderer);
            executeGeneration(game);
            renderer.save(appConfiguration.getOutputPath());
            long endTime = System.currentTimeMillis();
            log.info("Total execution time: {} ms", endTime - startTime);

        } catch (CommandLine.ParameterException e) {
            log.error("Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Fatal error during fractal generation: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void initializeConfiguration() {
        if (appConfiguration == null) {
            log.info("No config file provided, using CLI arguments and defaults");
            Size size = new Size(width, height);
            appConfiguration = AppConfiguration.builder()
                    .size(size)
                    .seed(seed)
                    .iterationCount(iterationCount)
                    .outputPath(outputPath)
                    .threadQuantity(threadQuantity)
                    .affineParamsList(affineParamsList)
                    .variationsParamsList(variationParamsList)
                    .build();
        } else {
            log.info("Configuration loaded from file");
        }
    }

    private void validate() {
        log.debug("Starting validation of input arguments");
        try {
            ArgumentValidator.validate(appConfiguration, this);
            log.debug("All validations passed successfully");
        } catch (CommandLine.ParameterException e) {
            log.error("Validation failed: {}", e.getMessage());
            throw e;
        }
    }

    private void initializeRandom() {
        Random random = new Random(appConfiguration.getSeed());
        appConfiguration.setRandom(random);
        appConfiguration.setColors();
        log.debug("Random generator initialized with seed: {}", appConfiguration.getSeed());
    }

    private void logConfiguration() {
        log.info("=== Fractal Flame Configuration ===");
        log.info(
                "Image size: {}x{}",
                appConfiguration.getSize().width(),
                appConfiguration.getSize().height());
        log.info("Iterations: {}", appConfiguration.getIterationCount());
        log.info("Threads: {}", appConfiguration.getThreadQuantity());
        log.info("Seed: {}", appConfiguration.getSeed());
        log.info("Output path: {}", appConfiguration.getOutputPath());
        log.info(
                "Affine transformations: {}",
                appConfiguration.getAffineParamsList().size());
        log.info(
                "Variation functions: {}",
                appConfiguration.getVariationsParamsList().size());

        log.debug("Affine params details:");
        for (int i = 0; i < appConfiguration.getAffineParamsList().size(); i++) {
            log.debug(
                    "Detail #{}: {}", i, appConfiguration.getAffineParamsList().get(i));
        }

        log.debug("Variation params details:");
        for (int i = 0; i < appConfiguration.getVariationsParamsList().size(); i++) {
            log.debug(
                    "Detail #{}: {}",
                    i,
                    appConfiguration.getVariationsParamsList().get(i));
        }
    }

    private void executeGeneration(ChaosGame game) {
        if (appConfiguration.getThreadQuantity() == 1) {
            log.info("Starting single-threaded generation");
            game.runSingleThread();
        } else {
            log.info("Starting multi-threaded generation with {} threads", appConfiguration.getThreadQuantity());
            game.runMultiThread();
        }
    }
}
