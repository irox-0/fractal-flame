package academy.application.render;

import academy.domain.AppConfiguration;
import academy.domain.Point;
import lombok.extern.slf4j.Slf4j;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public class ImageRenderer {
    private final int width;
    private final int height;
    private final double[][][] histogram;
    private final double xMin;
    private final double xMax;
    private final double yMin;
    private final double yMax;
    private final int[][] palette;

    public ImageRenderer(AppConfiguration configuration) {
        this.width = configuration.getSize().width();
        this.height = configuration.getSize().height();
        this.histogram = new double[width][height][4];
        this.xMin = -2.0;
        this.xMax = 2.0;
        this.yMin = -2.0;
        this.yMax = 2.0;
        this.palette = generatePalette();
    }

    public void plot(Point point) {
        int px = (int) Math.floor((point.getX() - xMin) / (xMax - xMin) * width);
        int py = (int) Math.floor((point.getY() - yMin) / (yMax - yMin) * height);

        if (px < 0 || px >= width || py < 0 || py >= height) {
            return;
        }

        int colorIndex = (int) Math.floor(point.getColor() * 255);
        colorIndex = Math.max(0, Math.min(255, colorIndex));
        int[] color = palette[colorIndex];

        histogram[px][py][0] += color[0] / 255.0;
        histogram[px][py][1] += color[1] / 255.0;
        histogram[px][py][2] += color[2] / 255.0;
        histogram[px][py][3] += 1.0;
    }


    public void save(Path outputPath) {
        log.info("Rendering image to {}", outputPath);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        double maxAlpha = findMaxAlpha();
        if (maxAlpha == 0) {
            log.warn("No points were plotted!");
            maxAlpha = 1;
        }
        double logMaxAlpha = Math.log(maxAlpha);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = computePixelColor(x, y, logMaxAlpha);
                image.setRGB(x, y, rgb);
            }
        }

        try {
            ImageIO.write(image, "PNG", outputPath.toFile());
            log.info("Image saved successfully");
        } catch (IOException e) {
            log.error("Failed to save image", e);
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


        int r8 = (int) (r * 255);
        int g8 = (int) (g * 255);
        int b8 = (int) (b * 255);

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
        int[][] pal = new int[256][3];

        for (int i = 0; i < 256; i++) {
            double t = i / 255.0;
            pal[i][0] = (int) (255 * (0.5 + 0.5 * Math.sin(2 * Math.PI * t + 0)));
            pal[i][1] = (int) (255 * (0.5 + 0.5 * Math.sin(2 * Math.PI * t + 2 * Math.PI / 3)));
            pal[i][2] = (int) (255 * (0.5 + 0.5 * Math.sin(2 * Math.PI * t + 4 * Math.PI / 3)));
        }

        return pal;
    }
}
