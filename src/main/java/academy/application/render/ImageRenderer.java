package academy.application.render;

import academy.domain.AppConfiguration;
import academy.domain.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class ImageRenderer {
    public static final int BRIGHTNESS = 255;
    public static final double AMPLITUDE = 0.5;
    private static final double X_MIN = -2.0;
    private static final double X_MAX = 2.0;
    private static final double Y_MIN = -2.0;
    private static final double Y_MAX = 2.0;

    private final int width;
    private final int height;
    private final double[][][] histogram;
    private final int[][] palette;
    private long plotCount = 0;
    private long outOfBoundsCount = 0;

    public ImageRenderer(AppConfiguration configuration) {
        this.width = configuration.getSize().width();
        this.height = configuration.getSize().height();
        this.histogram = new double[width][height][4];
        this.palette = generatePalette();

        log.debug("ImageRenderer initialized: {}x{} pixels", width, height);
        log.trace("Coordinate bounds: X[{}, {}], Y[{}, {}]", X_MIN, X_MAX, Y_MIN, Y_MAX);
    }

    public void plot(Point point) {
        int px = (int) Math.floor((point.getX() - X_MIN) / (X_MAX - X_MIN) * width);
        int py = (int) Math.floor((point.getY() - Y_MIN) / (Y_MAX - Y_MIN) * height);

        if (px < 0 || px >= width || py < 0 || py >= height) {
            outOfBoundsCount++;
            return;
        }

        int colorIndex = (int) Math.floor(point.getColor() * BRIGHTNESS);
        colorIndex = Math.max(0, Math.min(BRIGHTNESS, colorIndex));
        int[] color = palette[colorIndex];

        histogram[px][py][0] += color[0] / (double) BRIGHTNESS;
        histogram[px][py][1] += color[1] / (double) BRIGHTNESS;
        histogram[px][py][2] += color[2] / (double) BRIGHTNESS;
        histogram[px][py][3] += 1.0;

        plotCount++;
    }

    public void save(Path outputPath) {
        log.info("Starting image rendering to {}", outputPath);
        long startTime = System.currentTimeMillis();

        // Проверяем и создаем директорию при необходимости
        Path parentDir = outputPath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
                log.debug("Created output directory: {}", parentDir);
            } catch (IOException e) {
                log.error("Failed to create output directory: {}", parentDir, e);
                throw new RuntimeException("Cannot create output directory: " + e.getMessage(), e);
            }
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        double maxAlpha = findMaxAlpha();
        if (maxAlpha == 0) {
            log.warn("No points were plotted! The resulting image will be empty.");
            log.warn("This may indicate incorrect affine parameters or coordinate bounds.");
            maxAlpha = 1;
        } else {
            log.debug("Max alpha value: {}", maxAlpha);
        }

        double logMaxAlpha = Math.log(maxAlpha);
        log.debug("Log max alpha: {}", logMaxAlpha);

        int nonEmptyPixels = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = computePixelColor(x, y, logMaxAlpha);
                image.setRGB(x, y, rgb);
                if (rgb != 0) {
                    nonEmptyPixels++;
                }
            }
        }

        double coveragePercent = nonEmptyPixels * 100.0 / (width * height);
        log.info(
                "Image statistics: {} non-empty pixels ({}% coverage)",
                nonEmptyPixels, String.format("%.2f", coveragePercent));

        if (coveragePercent < 1.0) {
            log.warn(
                    "Very low pixel coverage ({}%). Consider adjusting parameters.",
                    String.format("%.2f", coveragePercent));
        }

        try {
            ImageIO.write(image, "PNG", outputPath.toFile());
            long fileSize = Files.size(outputPath);
            long endTime = System.currentTimeMillis();

            log.info("Image saved successfully: {} ({} bytes)", outputPath, fileSize);
            log.info("Rendering completed in {} ms", endTime - startTime);
            log.debug("Total points plotted: {}, out of bounds: {}", plotCount, outOfBoundsCount);
        } catch (IOException e) {
            log.error("Failed to save image to {}: {}", outputPath, e.getMessage());
            throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
        }
    }

    private int computePixelColor(int x, int y, double logMaxAlpha) {
        double alpha = histogram[x][y][3];

        if (alpha == 0) {
            return 0;
        }

        double logScale = Math.log(alpha) / alpha;
        double r = histogram[x][y][0] * logScale;
        double g = histogram[x][y][1] * logScale;
        double b = histogram[x][y][2] * logScale;

        if (logMaxAlpha > 0) {
            r /= logMaxAlpha;
            g /= logMaxAlpha;
            b /= logMaxAlpha;
        }

        r = Math.min(1.0, Math.max(0.0, r));
        g = Math.min(1.0, Math.max(0.0, g));
        b = Math.min(1.0, Math.max(0.0, b));

        int r8 = (int) (r * BRIGHTNESS);
        int g8 = (int) (g * BRIGHTNESS);
        int b8 = (int) (b * BRIGHTNESS);

        return (r8 << 16) | (g8 << 8) | b8;
    }

    private double findMaxAlpha() {
        double max = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (histogram[x][y][3] > max) {
                    max = histogram[x][y][3];
                }
            }
        }
        return max;
    }

    private int[][] generatePalette() {
        log.trace("Generating color palette with {} colors", BRIGHTNESS + 1);
        int[][] pal = new int[256][3];

        for (int i = 0; i <= BRIGHTNESS; i++) {
            double t = i / (double) BRIGHTNESS;
            pal[i][0] = (int) (BRIGHTNESS * (AMPLITUDE + AMPLITUDE * Math.sin(2 * Math.PI * t + 0)));
            pal[i][1] = (int) (BRIGHTNESS * (AMPLITUDE + AMPLITUDE * Math.sin(2 * Math.PI * t + 2 * Math.PI / 3)));
            pal[i][2] = (int) (BRIGHTNESS * (AMPLITUDE + AMPLITUDE * Math.sin(2 * Math.PI * t + 4 * Math.PI / 3)));
        }

        return pal;
    }

    public void merge(List<ImageRenderer> others) {
        log.debug("Merging {} histograms", others.size());
        long startTime = System.currentTimeMillis();

        int mergedPixelCount = 0;
        for (var other : others) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (other.getHistogram()[i][j][3] > 0) {
                        mergedPixelCount++;
                    }
                    for (int k = 0; k < 4; k++) {
                        this.histogram[i][j][k] += other.getHistogram()[i][j][k];
                    }
                }
            }
            this.plotCount += other.plotCount;
            this.outOfBoundsCount += other.outOfBoundsCount;
        }

        long endTime = System.currentTimeMillis();
        log.debug(
                "Merge completed in {} ms. Merged {} non-empty pixels from {} renderers. Total plot count: {}",
                endTime - startTime,
                mergedPixelCount,
                others.size(),
                this.plotCount);
    }
}
