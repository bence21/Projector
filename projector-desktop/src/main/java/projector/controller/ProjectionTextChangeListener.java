package projector.controller;

import javafx.scene.image.Image;
import projector.application.ProjectionType;
import projector.controller.util.ProjectionData;

public interface ProjectionTextChangeListener {
    void onSetText(String text, ProjectionType projectionType, ProjectionData projectionData);

    void onImageChanged(Image image, ProjectionType projectionType, ProjectionData projectionData);
}
