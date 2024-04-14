package projector;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.Before;
import org.testfx.framework.junit.ApplicationTest;
import projector.application.ApplicationVersion;

public class BaseTest extends ApplicationTest {

    @Before
    public void setUpClass() throws Exception {
        ApplicationVersion.getInstance().setTesting(true);
        ApplicationTest.launch(MainDesktop.class);
    }

    @Override
    public void start(Stage stage) {
        stage.show();
    }

    public <T extends Node> T find(final String query) {
        Pane root = MainDesktop.getRoot();
        return from(root).lookup(query).query();
    }

    public <T extends Node> T find(final String query, Pane root) {
        return from(root).lookup(query).query();
    }

    public void write_(final String s) {
        for (char c : s.toCharArray()) {
            KeyCode keyCode = KeyCode.valueOf((c + "").toUpperCase());
            type(keyCode);
        }
    }
}