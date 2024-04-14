package projector.controller.listener;

import projector.controller.util.ProjectionScreenHolder;

public interface ProjectionScreenListener {

    void onNew(ProjectionScreenHolder projectionScreenHolder);

    void onRemoved(ProjectionScreenHolder projectionScreenHolder);
}
