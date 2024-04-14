package projector.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

public class ImageUtil {

    public static ImageView getImageView(double fitWidth, double fitHeight, String imageName, Class<?> aClass) {
        ImageView imageView = getImageView(fitWidth, fitHeight);
        InputStream resourceAsStream = aClass.getResourceAsStream("/icons/" + imageName + ".png");
        if (resourceAsStream != null) {
            Image image = new Image(resourceAsStream);
            imageView.setImage(image);
        }
        return imageView;
    }

    public static ImageView getImageView(double fitWidth, double fitHeight) {
        ImageView imageView = new ImageView();
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(fitWidth);
        imageView.setFitHeight(fitHeight);
        return imageView;
    }
}
