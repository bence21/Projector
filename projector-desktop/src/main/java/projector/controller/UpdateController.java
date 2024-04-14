package projector.controller;

import com.bence.projector.common.dto.ProjectorVersionDTO;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import projector.application.Updater;

import java.util.List;

public class UpdateController {

    @FXML
    private Button updateButton;
    @FXML
    private TextFlow textFlow;
    private List<ProjectorVersionDTO> projectorVersions;

    public void initialize() {
    }

    public void updateButtonOnAction() {
        Updater.getInstance().updateExe(projectorVersions);
        updateButton.setDisable(true);
    }

    public void setProjectorVersions(List<ProjectorVersionDTO> projectorVersions) {
        this.projectorVersions = projectorVersions;
        ObservableList<Node> children = textFlow.getChildren();
        for (ProjectorVersionDTO projectorVersionDTO : projectorVersions) {
            Text text = new Text(projectorVersionDTO.getVersion().trim() + ":\n");
            text.setFont(Font.font(19));
            Text description = new Text(projectorVersionDTO.getDescription().trim() + "\n\n");
            description.setFont(Font.font(18));
            children.addAll(text, description);
        }
    }
}
