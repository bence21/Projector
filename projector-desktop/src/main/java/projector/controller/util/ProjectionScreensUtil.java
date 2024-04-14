package projector.controller.util;

import projector.controller.ProjectionScreenController;
import projector.controller.listener.ProjectionScreenListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectionScreensUtil {
    private static ProjectionScreensUtil instance = null;
    private final List<ProjectionScreenHolder> projectionScreenHolders;
    private final Map<Integer, ProjectionScreenHolder> doubleScreenHolders;
    private final Map<Integer, ProjectionScreenHolder> automaticScreenHolders;
    private final List<ProjectionScreenListener> projectionScreenListeners;

    private ProjectionScreensUtil() {
        projectionScreenHolders = new ArrayList<>();
        projectionScreenListeners = new ArrayList<>();
        doubleScreenHolders = new HashMap<>();
        automaticScreenHolders = new HashMap<>();
    }

    public static ProjectionScreensUtil getInstance() {
        if (instance == null) {
            instance = new ProjectionScreensUtil();
        }
        return instance;
    }

    public List<ProjectionScreenHolder> getProjectionScreenHolders() {
        return projectionScreenHolders;
    }

    public ProjectionScreenHolder addProjectionScreenController(ProjectionScreenController projectionScreenController, String name) {
        ProjectionScreenHolder projectionScreenHolder = new ProjectionScreenHolder(projectionScreenController, name);
        addProjectionScreenHolder(projectionScreenHolder);
        projectionScreenController.setProjectionScreenSettings(projectionScreenHolder.getProjectionScreenSettings());
        return projectionScreenHolder;
    }

    private void addProjectionScreenHolder(ProjectionScreenHolder projectionScreenHolder) {
        projectionScreenHolders.add(projectionScreenHolder);
        for (ProjectionScreenListener projectionScreenListener : projectionScreenListeners) {
            projectionScreenListener.onNew(projectionScreenHolder);
        }
    }

    public void addDoubleProjectionScreenController(ProjectionScreenController doubleProjectionScreenController) {
        getADoubleProjectionScreenHolder(doubleProjectionScreenController, " - double screen", doubleScreenHolders);
    }

    private int getNextIndex(Map<Integer, ProjectionScreenHolder> screenHolders) {
        int n = screenHolders.size();
        for (int i = 0; i < n; ++i) {
            if (!screenHolders.containsKey(i)) {
                return i;
            }
        }
        return n;
    }

    public void addAutomaticDoubleProjectionScreenController(ProjectionScreenController doubleProjectionScreenController) {
        ProjectionScreenHolder projectionScreenHolder = getADoubleProjectionScreenHolder(doubleProjectionScreenController, " - screen", automaticScreenHolders);
        projectionScreenHolder.setOpenedAutomatically(true);
    }

    private ProjectionScreenHolder getADoubleProjectionScreenHolder(ProjectionScreenController doubleProjectionScreenController, String caption, Map<Integer, ProjectionScreenHolder> screenHolders) {
        int index = getNextIndex(screenHolders);
        int number = index + 2;
        String name = number + caption;
        ProjectionScreenHolder projectionScreenHolder = new ProjectionScreenHolder(doubleProjectionScreenController, name);
        projectionScreenHolder.setDoubleIndex(index);
        addProjectionScreenHolder(projectionScreenHolder);
        doubleProjectionScreenController.setProjectionScreenSettings(projectionScreenHolder.getProjectionScreenSettings());
        screenHolders.put(index, projectionScreenHolder);
        return projectionScreenHolder;
    }

    public void addProjectionScreenListener(ProjectionScreenListener projectionScreenListener) {
        projectionScreenListeners.add(projectionScreenListener);
    }

    public void removeProjectionScreenController(ProjectionScreenController projectionScreenController) {
        ProjectionScreenHolder projectionScreenHolder = projectionScreenController.getProjectionScreenSettings().getProjectionScreenHolder();
        doubleScreenHolders.remove(projectionScreenHolder.getDoubleIndex());
        removeProjectionScreenHolder(projectionScreenHolder);
    }

    public ProjectionScreenHolder getScreenHolderByIndex(Integer index) {
        if (index < 0 || index >= automaticScreenHolders.size()) {
            return null;
        }
        return automaticScreenHolders.get(index);
    }

    public void closeFromIndex(int index) {
        while (index < automaticScreenHolders.size()) {
            ProjectionScreenHolder projectionScreenHolder = getScreenHolderByIndex(index);
            if (projectionScreenHolder == null) {
                return;
            }
            if (projectionScreenHolder.isOpenedAutomatically()) {
                projectionScreenHolder.close();
                automaticScreenHolders.remove(projectionScreenHolder.getDoubleIndex());
                removeProjectionScreenHolder(projectionScreenHolder);
            }
            ++index;
        }
    }

    private void removeProjectionScreenHolder(ProjectionScreenHolder projectionScreenHolder) {
        projectionScreenHolders.remove(projectionScreenHolder);
        onRemoveListenersCall(projectionScreenHolder);
    }

    private void onRemoveListenersCall(ProjectionScreenHolder projectionScreenHolder) {
        for (ProjectionScreenListener projectionScreenListener : projectionScreenListeners) {
            projectionScreenListener.onRemoved(projectionScreenHolder);
        }
    }
}
