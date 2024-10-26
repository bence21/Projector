package projector.controller;

import javafx.scene.image.Image;
import projector.application.ProjectionType;
import projector.controller.util.AutomaticAction;
import projector.controller.util.ProjectionData;

import java.util.Date;

public interface ProjectionTextChangeListener {
    void onSetText(String text, ProjectionType projectionType, ProjectionData projectionData);

    void onImageChanged(Image image, ProjectionType projectionType, ProjectionData projectionData);

    void onSetCountDownTimer(Date finishDate, AutomaticAction selectedAction, boolean showFinishTime);
}
