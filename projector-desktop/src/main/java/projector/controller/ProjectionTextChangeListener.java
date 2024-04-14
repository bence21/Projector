package projector.controller;

import com.bence.projector.common.dto.ProjectionDTO;
import javafx.scene.image.Image;
import projector.application.ProjectionType;

public interface ProjectionTextChangeListener {
    void onSetText(String text, ProjectionType projectionType, ProjectionDTO projectionDTO);

    void onImageChanged(Image image, ProjectionType projectionType, ProjectionDTO projectionDTO);
}
