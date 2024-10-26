package projector.controller.util;

public class ProjectionScreenBunch {
    private ProjectionScreenHolder projectionScreenHolder;
    private String name;

    public ProjectionScreenHolder getProjectionScreenHolder() {
        return projectionScreenHolder;
    }

    public void setProjectionScreenHolder(ProjectionScreenHolder projectionScreenHolder) {
        this.projectionScreenHolder = projectionScreenHolder;
    }

    private String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        if (projectionScreenHolder == null) {
            return getName();
        } else {
            return projectionScreenHolder.getNameWithDefault();
        }
    }
}
