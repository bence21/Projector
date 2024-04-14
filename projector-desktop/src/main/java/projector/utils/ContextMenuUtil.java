package projector.utils;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import projector.application.Settings;

public class ContextMenuUtil {

    public static void initializeContextMenu(ContextMenu contextMenu, Logger LOG) {
        contextMenu.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            try {
                if (event.getButton() == MouseButton.SECONDARY) {
                    event.consume();
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        setContextMenuHideAction(contextMenu, LOG);
    }

    public static void setContextMenuHideAction(ContextMenu contextMenu, Logger LOG) {
        contextMenu.setOnAction(event -> {
            try {
                contextMenu.hide();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
    }

    public static MenuItem getDeleteMenuItem() {
        MenuItem deleteMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Delete"));
        deleteMenuItem.setId("deleteMenuItem");
        return deleteMenuItem;
    }
}
