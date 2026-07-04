package projector.utils;

import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import projector.application.Settings;
import projector.model.Song;
import projector.service.SongService;

import java.io.InputStream;
import java.util.ResourceBundle;

public final class SongForkBadgeFactory {

    public enum ForkBadgeKind {
        NONE, EDITED, LOCAL_FORK
    }

    private static final double BADGE_SIZE = 16.0;
    private static final String PENCIL_ICON = "/icons/pencil.png";
    private static final String COPY_ICON = "/icons/copy.png";

    private static final Image PENCIL_IMAGE = loadImage(PENCIL_ICON);
    private static final Image COPY_IMAGE = loadImage(COPY_ICON);

    private SongForkBadgeFactory() {
    }

    public static ForkBadgeKind resolve(Song song) {
        if (song == null) {
            return ForkBadgeKind.NONE;
        }
        if (song.isFork()) {
            return Boolean.TRUE.equals(song.getLocalChangesCached()) ? ForkBadgeKind.EDITED : ForkBadgeKind.NONE;
        }
        if (song.hasLocalFork()) {
            return ForkBadgeKind.LOCAL_FORK;
        }
        return ForkBadgeKind.NONE;
    }

    public static ForkBadgeKind resolve(Song song, SongService songService) {
        if (song == null || songService == null) {
            return ForkBadgeKind.NONE;
        }
        if (song.isFork()) {
            Boolean changed = song.getLocalChangesCached();
            if (changed != null) {
                return Boolean.TRUE.equals(changed) ? ForkBadgeKind.EDITED : ForkBadgeKind.NONE;
            }
            if (songService.hasLocalChanges(song)) {
                return ForkBadgeKind.EDITED;
            }
            return ForkBadgeKind.NONE;
        }
        if (song.hasLocalFork()) {
            return ForkBadgeKind.LOCAL_FORK;
        }
        if (songService.hasLocalFork(song)) {
            return ForkBadgeKind.LOCAL_FORK;
        }
        return ForkBadgeKind.NONE;
    }

    public static ImageView createFromKind(ForkBadgeKind kind) {
        if (kind == null || kind == ForkBadgeKind.NONE) {
            return null;
        }
        ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
        return switch (kind) {
            case EDITED -> createBadge(PENCIL_IMAGE, resourceBundle.getString("Edited locally"), "song-badge-edited");
            case LOCAL_FORK ->
                    createBadge(COPY_IMAGE, resourceBundle.getString("Local version available"), "song-badge-local-fork");
            default -> null;
        };
    }

    public static ImageView createBadgeIfNeeded(Song song, SongService songService) {
        return createFromKind(resolve(song, songService));
    }

    public static ImageView createBadgeIfNeeded(Song song) {
        return createFromKind(resolve(song));
    }

    private static Image loadImage(String iconPath) {
        InputStream stream = SongForkBadgeFactory.class.getResourceAsStream(iconPath);
        if (stream == null) {
            return null;
        }
        return new Image(stream);
    }

    private static ImageView createBadge(Image image, String tooltipText, String styleClass) {
        if (image == null) {
            return null;
        }
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(BADGE_SIZE);
        imageView.setFitWidth(BADGE_SIZE);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("song-badge-icon");
        imageView.getStyleClass().add(styleClass);
        Tooltip.install(imageView, new Tooltip(tooltipText));
        return imageView;
    }
}
