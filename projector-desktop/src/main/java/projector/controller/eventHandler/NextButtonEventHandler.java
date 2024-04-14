package projector.controller.eventHandler;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;

public class NextButtonEventHandler implements EventHandler<KeyEvent> {
    private Button nextButton;
    private Logger LOG;

    protected NextButtonEventHandler(Button nextButton, Logger log) {
        this.nextButton = nextButton;
        LOG = log;
    }

    @Override
    public void handle(KeyEvent event) {
        try {
            if (event.getCode().equals(KeyCode.ENTER)) {
                nextButton.fire();
                event.consume();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}