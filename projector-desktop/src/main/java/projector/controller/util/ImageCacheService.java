package projector.controller.util;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static projector.controller.GalleryController.isMediaFile;
import static projector.controller.util.PdfService.isPdfFile;

public class ImageCacheService {

    private static final Logger LOG = LoggerFactory.getLogger(ImageCacheService.class);
    private static final String CACHE_FOLDER = ".cache";
    private static final String VIDEO_CACHE_EXTENSION = "png";
    private static ImageCacheService instance = null;
    private static final String DIVIDER = "_";
    private static final Object VIDEO_THUMBNAIL_LOCK = new Object();
    private static final long VIDEO_THUMBNAIL_TIMEOUT_SECONDS = 4;
    private final HashMap<String, Boolean> hashMap = new HashMap<>(100);
    private final ExecutorService imageLoadExecutor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "image-cache-load");
        t.setDaemon(true);
        return t;
    });
    private final ConcurrentLinkedQueue<VideoThumbnailRequest> videoThumbnailQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean videoThumbnailScheduled = new AtomicBoolean(false);

    private record VideoThumbnailRequest(String filePath, int width, int height, Consumer<Image> onReady) {
    }

    private ImageCacheService() {
    }

    public static ImageCacheService getInstance() {
        if (instance == null) {
            instance = new ImageCacheService();
        }
        return instance;
    }

    public static String getFileExtension(String filePath) {
        Path path = Path.of(filePath);
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return ""; // No extension or the dot is at the end of the file name
        }
        return fileName.substring(dotIndex + 1);
    }

    public static String replaceLast(String original, String substringToReplace, String replacement) {
        int lastIndex = original.lastIndexOf(substringToReplace);
        if (lastIndex == -1) {
            return original; // Substring not found, return original string
        }
        StringBuilder sb = new StringBuilder(original);
        sb.replace(lastIndex, lastIndex + substringToReplace.length(), replacement);
        return sb.toString();
    }

    private static String getCacheImagePath(File file, int width, int height) {
        String fileName = file.getName();
        String fileExtension = getFileExtension(fileName);
        String sourceSuffix = fileExtension.isEmpty() ? "" : "." + fileExtension;
        String name = sourceSuffix.isEmpty() ? fileName : replaceLast(fileName, sourceSuffix, "");
        String cacheExtension = isMediaFile(file.getAbsolutePath()) ? VIDEO_CACHE_EXTENSION : fileExtension;
        if (cacheExtension.isEmpty()) {
            cacheExtension = VIDEO_CACHE_EXTENSION;
        }
        return CACHE_FOLDER + "/" + name + DIVIDER + width + DIVIDER + height + DIVIDER + file.lastModified() + "." + cacheExtension;
    }

    private static boolean areDimensionsInValid(int width, int height) {
        return width <= 0 || height <= 0;
    }

    private static boolean areDimensionsInValid(double width, double height) {
        return width <= 0 || height <= 0 || Double.isNaN(width) || Double.isNaN(height)
                || Double.isInfinite(width) || Double.isInfinite(height);
    }

    private static boolean logIfInvalid(boolean isInvalid, String context, Object width, Object height) {
        if (isInvalid) {
            LOG.warn("Invalid dimensions for {}: width={}, height={}", context, width, height);
            return true;
        }
        return false;
    }

    private static boolean areDimensionsInValidWithLog(int width, int height, String context) {
        return logIfInvalid(areDimensionsInValid(width, height), context, width, height);
    }

    private static boolean areDimensionsInValidWithLog(double width, double height) {
        return logIfInvalid(areDimensionsInValid(width, height), "original image dimensions", width, height);
    }

    private static boolean areParametersValidWithLog(int width, int height, String filePath) {
        if (areDimensionsInValid(width, height) || filePath == null) {
            LOG.warn("Invalid parameters for {}: width={}, height={}, filePath={}", "checkForImage", width, height, filePath);
            return false;
        }
        return true;
    }

    public Image getImage(String filePath, int width, int height) {
        if (areDimensionsInValid(width, height) || filePath == null) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        String cacheImagePath = getCacheImagePath(file, width, height);
        Image image = getImageFromFile(cacheImagePath);
        if (image == null) {
            hashMap.remove(cacheImagePath);
            if (isMediaFile(filePath)) {
                image = getVideoThumbnailSafely(filePath, width, height);
            } else if (isPdfFile(filePath)) {
                image = getPdfThumbnail(filePath, width, height);
            } else {
                image = getImageFromFile(filePath);
            }
            if (image != null) {
                createCache(image, width, height, cacheImagePath);
            }
        }
        return image;
    }

    public boolean hasCachedImage(String filePath, int width, int height) {
        if (areDimensionsInValid(width, height) || filePath == null) {
            return false;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        return getImageFromFile(getCacheImagePath(file, width, height)) != null;
    }

    /**
     * Loads an image without blocking the JavaFX thread. The callback always runs on the FX thread.
     *
     * @param generateVideoThumbnailIfMissing when false, uncached video files return null without using MediaPlayer
     */
    public void loadImageAsync(String filePath, int width, int height, Consumer<Image> onReady,
                               boolean generateVideoThumbnailIfMissing) {
        if (onReady == null) {
            return;
        }
        if (areDimensionsInValid(width, height) || filePath == null) {
            Platform.runLater(() -> onReady.accept(null));
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            Platform.runLater(() -> onReady.accept(null));
            return;
        }
        String cacheImagePath = getCacheImagePath(file, width, height);
        Image cached = getImageFromFile(cacheImagePath);
        if (cached != null) {
            Platform.runLater(() -> onReady.accept(cached));
            return;
        }
        hashMap.remove(cacheImagePath);
        if (isMediaFile(filePath)) {
            if (!generateVideoThumbnailIfMissing) {
                Platform.runLater(() -> onReady.accept(null));
                return;
            }
            scheduleVideoThumbnailAsync(filePath, width, height, image -> {
                if (image != null) {
                    createCache(image, width, height, cacheImagePath);
                }
                onReady.accept(image);
            });
            return;
        }
        imageLoadExecutor.execute(() -> {
            Image image;
            try {
                if (isPdfFile(filePath)) {
                    image = getPdfThumbnail(filePath, width, height);
                } else {
                    image = getImageFromFile(filePath);
                }
                if (image != null) {
                    createCache(image, width, height, cacheImagePath);
                }
            } catch (Exception e) {
                LOG.error("Error loading image asynchronously", e);
                image = null;
            }
            Image result = image;
            Platform.runLater(() -> onReady.accept(result));
        });
    }

    private Image getVideoThumbnailSafely(String filePath, int width, int height) {
        if (Platform.isFxApplicationThread()) {
            scheduleVideoThumbnailAsync(filePath, width, height, image -> {
                if (image != null) {
                    File file = new File(filePath);
                    if (file.exists()) {
                        createCache(image, width, height, getCacheImagePath(file, width, height));
                    }
                }
            });
            return null;
        }
        return getVideoThumbnail(filePath, width, height);
    }

    private void scheduleVideoThumbnailAsync(String filePath, int width, int height, Consumer<Image> onReady) {
        videoThumbnailQueue.add(new VideoThumbnailRequest(filePath, width, height, onReady));
        if (videoThumbnailScheduled.compareAndSet(false, true)) {
            imageLoadExecutor.execute(this::processVideoThumbnailQueueOnBackground);
        }
    }

    private void processVideoThumbnailQueueOnBackground() {
        VideoThumbnailRequest request = videoThumbnailQueue.poll();
        if (request == null) {
            videoThumbnailScheduled.set(false);
            if (!videoThumbnailQueue.isEmpty() && videoThumbnailScheduled.compareAndSet(false, true)) {
                imageLoadExecutor.execute(this::processVideoThumbnailQueueOnBackground);
            }
            return;
        }
        Image image = null;
        try {
            image = getVideoThumbnail(request.filePath(), request.width(), request.height());
        } catch (Exception e) {
            LOG.error("Error creating video thumbnail for {}", request.filePath(), e);
        }
        Image result = image;
        Platform.runLater(() -> {
            try {
                request.onReady().accept(result);
            } catch (Exception e) {
                LOG.error("Video thumbnail callback failed for {}", request.filePath(), e);
            }
            imageLoadExecutor.execute(this::processVideoThumbnailQueueOnBackground);
        });
    }

    /**
     * Generates a video thumbnail. Must be called from a background thread; blocks that thread until
     * the snapshot is ready or {@link #VIDEO_THUMBNAIL_TIMEOUT_SECONDS} elapses.
     */
    private Image getVideoThumbnail(String filePath, int width, int height) {
        if (!isMediaFile(filePath)) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        if (Platform.isFxApplicationThread()) {
            LOG.warn("getVideoThumbnail must not run on the JavaFX thread: {}", filePath);
            return null;
        }

        synchronized (VIDEO_THUMBNAIL_LOCK) {
            try {
                Media media = new Media(file.toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                MediaView mediaView = new MediaView(mediaPlayer);

                CountDownLatch latch = new CountDownLatch(1);
                AtomicReference<WritableImage> snapshotImage = new AtomicReference<>(new WritableImage(width, height));

                mediaPlayer.setOnError(() -> {
                    if (mediaPlayer.getError() != null) {
                        LOG.error("MediaPlayer error creating thumbnail for {}: {}", filePath, mediaPlayer.getError());
                    }
                    beforeVideoThumbnailResult(latch, mediaPlayer);
                });

                mediaPlayer.setOnReady(() -> {
                    try {
                        int videoWidth = media.getWidth();
                        int videoHeight = media.getHeight();

                        mediaView.setFitWidth(videoWidth);
                        mediaView.setFitHeight(videoHeight);

                        if (videoWidth < 1 || videoHeight < 1) {
                            snapshotImage.set(null);
                            beforeVideoThumbnailResult(latch, mediaPlayer);
                            return;
                        }
                        snapshotImage.set(new WritableImage(videoWidth, videoHeight));

                        double millis = mediaPlayer.getTotalDuration().toMillis();
                        double seekMillis = Math.min(1000, Math.max(millis - 100, 0));
                        mediaPlayer.seek(javafx.util.Duration.millis(seekMillis));

                        Platform.runLater(() -> {
                            try {
                                mediaPlayer.pause();
                                mediaView.snapshot(new SnapshotParameters(), snapshotImage.get());
                            } catch (Exception e) {
                                LOG.error("Error creating video thumbnail snapshot", e);
                            }
                            beforeVideoThumbnailResult(latch, mediaPlayer);
                        });
                    } catch (Exception e) {
                        LOG.error("Error creating video thumbnail", e);
                        beforeVideoThumbnailResult(latch, mediaPlayer);
                    }
                });
                mediaPlayer.setMute(true);
                mediaPlayer.play();

                try {
                    if (!latch.await(VIDEO_THUMBNAIL_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                        LOG.warn("Video thumbnail generation timed out for {}", filePath);
                        stopMediaPlayer(mediaPlayer);
                        return null;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    stopMediaPlayer(mediaPlayer);
                    return null;
                }

                return snapshotImage.get();
            } catch (Exception e) {
                LOG.error("Error creating video thumbnail", e);
                return null;
            }
        }
    }

    private static void beforeVideoThumbnailResult(CountDownLatch latch, MediaPlayer mediaPlayer) {
        latch.countDown();
        stopMediaPlayer(mediaPlayer);
    }

    private static void stopMediaPlayer(MediaPlayer mediaPlayer) {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.stop();
        mediaPlayer.dispose();
    }

    private Image getPdfThumbnail(String filePath, int width, int height) {
        if (!isPdfFile(filePath)) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }

        if (areDimensionsInValidWithLog(width, height, "PDF thumbnail")) {
            return null;
        }

        try {
            // Render first page of PDF at appropriate DPI for thumbnail
            float dpi = Math.max(width, height) * 72.0f / 200.0f; // Scale DPI based on thumbnail size
            BufferedImage bufferedImage = PdfService.getInstance().renderPage(filePath, 0, dpi);
            if (bufferedImage != null) {
                Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                if (fxImage.getWidth() > 0 && fxImage.getHeight() > 0) {
                    // Resize to fit thumbnail dimensions using existing resizeImage method
                    BufferedImage resizedBufferedImage = resizeImage(fxImage, width, height);
                    if (resizedBufferedImage != null) {
                        return SwingFXUtils.toFXImage(resizedBufferedImage, null);
                    }
                } else {
                    LOG.warn("PDF rendered image has invalid dimensions: width={}, height={}",
                            fxImage.getWidth(),
                            fxImage.getHeight());
                }
            }
        } catch (Exception e) {
            LOG.error("Error creating PDF thumbnail", e);
        }
        return null;
    }

    public void checkForImage(String filePath, int width, int height) {
        if (!areParametersValidWithLog(width, height, filePath)) {
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        String cacheImagePath = getCacheImagePath(file, width, height);
        Path path = Paths.get(cacheImagePath);
        if (path.toFile().exists()) {
            return;
        }
        Image image;
        if (isPdfFile(filePath)) {
            image = getPdfThumbnail(filePath, width, height);
        } else {
            image = getImageFromFile(filePath);
        }
        if (image != null) {
            createCache(image, width, height, cacheImagePath);
        }
    }

    private void createCache(Image image, int width, int height, String cacheImagePath) {
        if (hashMap.containsKey(cacheImagePath)) {
            return;
        } else {
            hashMap.put(cacheImagePath, true);
        }
        createCacheFolder();
        BufferedImage bufferedImage = resizeImage(image, width, height);
        saveImage(bufferedImage, cacheImagePath);
    }

    private void createCacheFolder() {
        File folder = new File(CACHE_FOLDER);
        if (!folder.exists()) {
            boolean success = folder.mkdir();
            // Set the folder attribute as hidden (Windows only)
            if (success && System.getProperty("os.name").startsWith("Windows")) {
                try {
                    Runtime.getRuntime().exec("attrib +H " + folder.getAbsolutePath());
                } catch (Exception e) {
                    LOG.warn("Failed to hide folder.", e);
                }
            }
        }
    }

    public static BufferedImage resizeImage(Image originalImage, int newWidth, int newHeight) {
        if (originalImage == null) {
            return null;
        }

        if (areDimensionsInValidWithLog(newWidth, newHeight, "resizeImage")) {
            return null;
        }

        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(originalImage, null);
        if (bufferedImage == null) {
            return null;
        }

        double width = originalImage.getWidth();
        double height = originalImage.getHeight();

        if (areDimensionsInValidWithLog(width, height)) {
            return null;
        }

        double aspectRatio = width / height;

        // Calculate the new dimensions while maintaining the aspect ratio
        if (newWidth / aspectRatio > newHeight) {
            newWidth = (int) (newHeight * aspectRatio);
        } else {
            newHeight = (int) (newWidth / aspectRatio);
        }

        if (areDimensionsInValidWithLog(newWidth, newHeight, "calculated dimensions")) {
            return null;
        }

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, bufferedImage.getType());
        Graphics2D graphics = resizedImage.createGraphics();
        // Use Bicubic interpolation for better quality
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        // Calculate the position to center the image on the canvas
        int x = (newWidth - resizedImage.getWidth()) / 2;
        int y = (newHeight - resizedImage.getHeight()) / 2;

        graphics.drawImage(bufferedImage, x, y, resizedImage.getWidth(), resizedImage.getHeight(), null);
        graphics.dispose();

        return resizedImage;
    }

    public static void saveImage(BufferedImage image, String savePath) {
        if (image == null) {
            return;
        }
        File outputFile = new File(savePath);
        String format = getImageIoWriteFormat(savePath);
        try {
            if (!ImageIO.write(image, format, outputFile)) {
                LOG.warn("ImageIO.write returned false for {} (format={})", savePath, format);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static String getImageIoWriteFormat(String savePath) {
        String extension = getFileExtension(savePath);
        if (!extension.isEmpty() && ImageIO.getImageWritersBySuffix(extension).hasNext()) {
            return extension;
        }
        if (!extension.isEmpty()) {
            LOG.debug("No ImageIO writer for extension '{}', using png for {}", extension, savePath);
        }
        return VIDEO_CACHE_EXTENSION;
    }

    private Image getImageFromFile(String imagePath) {
        try {
            Path path = Paths.get(imagePath);
            if (!path.toFile().exists()) {
                return null;
            }
            byte[] imageData = Files.readAllBytes(path);
            // Create an InputStream from the byte array
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
            return new Image(inputStream);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
}
