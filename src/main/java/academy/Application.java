package academy;

import academy.cli.converter.AffineParamsConverter;
import academy.cli.converter.AppConfigurationConverter;
import academy.cli.converter.PathConverter;
import academy.cli.converter.VariationParamsConverter;
import academy.domain.AffineParams;
import academy.domain.AppConfiguration;
import academy.domain.VariationParams;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.nio.file.Path;
import java.util.List;

@Command(name = "Application Example", version = "Example 1.0", mixinStandardHelpOptions = true)
public class Application implements Runnable {

    @Option(
            names = {"-w", "--width"},
            description = "Image width",
            defaultValue = "1920")
    private int width;

    @Option(
            names = {"-h", "--height"},
            description = "Image height",
            defaultValue = "1080")
    private int height;

    @Option(
            names = {"--seed"},
            description = "Random seed",
            defaultValue = "5")
    private long seed;

    @Option(
            names = {"-i", "--iteration-count"},
            description = "Count of generation iterations",
            defaultValue = "2500"
    )
    private int iterationCount;

    @Option(
            names = {"-o", "--otput-path"},
            description = "Path to image file",
            defaultValue = "result.png",
            converter = PathConverter.class
    )
    private Path outputPath;

    @Option(
            names = {"-t", "--threads"},
            description = "Thread quantity",
            defaultValue = "1"
    )
    private int threadQuantity;

    @Option(
            names = {"-ap", "--affine-params"},
            description = "Configuration of affine transformations",
            converter = AffineParamsConverter.class,
            defaultValue = "0.1,0.1,0.1,0.1,0.1,0.1"
    )
    private List<AffineParams> affineParamsList;

    @Option(
            names = {"-f", "--functions"},
            description = "Transformation function and its weight",
            converter = VariationParamsConverter.class,
            defaultValue = "swirl:1.0"
    )
    private List<VariationParams> variationParamsList;

    @Option(
            names = "--config",
            description = "Application configuration file",
            converter = AppConfigurationConverter.class
    )
    private AppConfiguration appConfiguration;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {

    }
}
