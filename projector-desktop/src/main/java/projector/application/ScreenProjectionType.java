package projector.application;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class ScreenProjectionType {

    @Expose
    private ProjectionType projectionType;
    @Expose
    private ScreenProjectionAction screenProjectionAction = ScreenProjectionAction.DISPLAY;

    public ScreenProjectionType() {

    }

    public ScreenProjectionType(ScreenProjectionType screenProjectionType) {
        this.projectionType = screenProjectionType.projectionType;
        this.screenProjectionAction = screenProjectionType.screenProjectionAction;
    }

    public ProjectionType getProjectionType() {
        return projectionType;
    }

    public void setProjectionType(ProjectionType projectionType) {
        this.projectionType = projectionType;
    }

    public ScreenProjectionAction getScreenProjectionAction() {
        if (screenProjectionAction == null) {
            screenProjectionAction = ScreenProjectionAction.DISPLAY;
        }
        return screenProjectionAction;
    }

    public void setScreenProjectionAction(ScreenProjectionAction screenProjectionAction) {
        this.screenProjectionAction = screenProjectionAction;
    }

    public static List<ScreenProjectionType> copyList(List<ScreenProjectionType> screenProjectionTypes) {
        if (screenProjectionTypes == null) {
            return null;
        }
        ArrayList<ScreenProjectionType> copiedList = new ArrayList<>(screenProjectionTypes.size());
        for (ScreenProjectionType screenProjectionType : screenProjectionTypes) {
            copiedList.add(new ScreenProjectionType(screenProjectionType));
        }
        return copiedList;
    }

    @Override
    public String toString() {
        return projectionType + " " + screenProjectionAction;
    }
}
