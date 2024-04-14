package projector.utils;

import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ColorRectCell extends ListCell<String> {
    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        Rectangle rect = new Rectangle(100, 20);
        if (item != null) {
            rect.setFill(Color.AQUA);
            setGraphic(rect);
        }
////		Text text = new Text();
//		text.wrappingWidthProperty().bind(list.widthProperty().subtract(50));
//		text.textProperty().bind(itemProperty());
//
//		// setPrefWidth(0);
//		setGraphic(text);
    }
}
