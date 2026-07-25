package projector.utils;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import projector.application.Settings;
import projector.model.Song;

import java.io.InputStream;
import java.util.ResourceBundle;

public final class SongForkBadgeFactory {

    public enum ForkBadgeKind {
        NONE, EDITED, LOCAL_FORK
    }

    private static final double BADGE_SIZE = 16.0;
    private static final String PENCIL_ICON = "/icons/pencil.png";
    private static final String CLOUD_ICON = "/icons/cloud.png";

    private static final Image PENCIL_IMAGE = loadImage(PENCIL_ICON);
    private static final Image CLOUD_IMAGE = loadImage(CLOUD_ICON);

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

    public static Node createSwapButtonGraphic(boolean targetIsLocalEdit) {
        Text swapIcon = new Text("⇄");
        swapIcon.getStyleClass().add("song-version-swap-icon");

        ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
        Image targetImage = targetIsLocalEdit ? PENCIL_IMAGE : CLOUD_IMAGE;
        String targetTooltip = targetIsLocalEdit
                ? resourceBundle.getString("Edited locally")
                : resourceBundle.getString("Online version");
        String targetStyleClass = targetIsLocalEdit ? "song-badge-edited" : "song-badge-online-version";
        ImageView targetIcon = createBadge(targetImage, targetTooltip, targetStyleClass);

        HBox graphic = new HBox(4.0, swapIcon);
        if (targetIcon != null) {
            graphic.getChildren().add(targetIcon);
        }
        graphic.setAlignment(Pos.CENTER_LEFT);
        return graphic;
    }

    public static ImageView createOnlineVersionBadge() {
        ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
        return createBadge(CLOUD_IMAGE, resourceBundle.getString("Online version"), "song-badge-online-version");
    }

    public static ImageView createLocalEditBadge() {
        ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
        return createBadge(PENCIL_IMAGE, resourceBundle.getString("Edited locally"), "song-badge-edited");
    }

    public static void configureCompareColumnLabel(Label label, Song song) {
        if (label == null || song == null) {
            return;
        }
        ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
        boolean localEdit = song.isFork();
        label.setGraphic(localEdit ? createLocalEditBadge() : createOnlineVersionBadge());
        label.setContentDisplay(ContentDisplay.LEFT);
        label.setGraphicTextGap(6.0);
        String role = localEdit
                ? resourceBundle.getString("Edited locally")
                : resourceBundle.getString("Online version");
        label.setText(role + ": " + song.getTitle());
    }

    public static ImageView createFromKind(ForkBadgeKind kind) {
        if (kind == null || kind == ForkBadgeKind.NONE) {
            return null;
        }
        ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
        return switch (kind) {
            case EDITED -> createBadge(PENCIL_IMAGE, resourceBundle.getString("Edited locally"), "song-badge-edited");
            case LOCAL_FORK ->
                    createBadge(CLOUD_IMAGE, resourceBundle.getString("Online version"), "song-badge-online-version");
            default -> null;
        };
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
