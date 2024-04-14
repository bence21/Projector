package projector.controller.song;

import com.bence.projector.common.model.SectionType;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.controller.song.util.OnChangeListener;
import projector.model.SongVerse;
import projector.utils.CloneUtil;

import java.util.List;

import static projector.utils.scene.text.MyTextFlow.getStringTextFromRawText;

public class VerseController {

    private static final Logger LOG = LoggerFactory.getLogger(VerseController.class);
    @FXML
    private Button addToVerseOrderButton;
    @FXML
    private ComboBox<SectionType> sectionTypeComboBox;
    @FXML
    private BorderPane rightBorderPane;
    @FXML
    private TextArea secondTextArea;
    @FXML
    private TextArea textArea;
    private SongVerse songVerse;
    private OnChangeListener onChangeListener;
    private boolean onChangeListenerPause = false;

    public static String getRawTextFromVerseString(String textAreaText) {
        if (textAreaText != null) {
            String text = textAreaText.trim();
            char[] chars = text.toCharArray();
            StringBuilder newText = new StringBuilder();
            for (int i = 0; i < chars.length; ++i) {
                if (chars[i] == '\\') {
                    newText.append("\\\\");
                } else if (chars[i] == '&') {
                    newText.append("\\&");
                } else if (chars[i] == '\n' && i + 1 < chars.length && chars[i + 1] == '\n') {
                    newText.append("&\n");
                    ++i;
                } else {
                    newText.append(chars[i]);
                }
            }
            return newText.toString();
        }
        return "";
    }

    public void initialize() {
        showSecondText(false);
        fillSectionTypeComboBox();
        Callback<ListView<SectionType>, ListCell<SectionType>> cellFactory = new Callback<ListView<SectionType>, ListCell<SectionType>>() {
            @Override
            public ListCell<SectionType> call(ListView<SectionType> param) {
                return new ListCell<SectionType>() {
                    @Override
                    protected void updateItem(SectionType sectionType, boolean empty) {
                        try {
                            super.updateItem(sectionType, empty);
                            if (sectionType != null && !empty) {
                                if (songVerse.getSectionType().equals(sectionType)) {
                                    setText(songVerse.getLongSectionTypeStringWithCount());
                                } else {
                                    setText(songVerse.getSectionTypeString(sectionType));
                                }
                            } else {
                                setText(null);
                            }
                        } catch (Exception e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                };
            }
        };
        sectionTypeComboBox.setButtonCell(cellFactory.call(null));
        sectionTypeComboBox.setCellFactory(cellFactory);
        sectionTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            if (onChangeListener != null && !onChangeListenerPause) {
                songVerse.setSectionType(newValue);
                onChangeListener.onChange();
            }
        });
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            songVerse.setText(newValue);
            if (onChangeListener != null && !onChangeListenerPause) {
                onChangeListener.onChange();
            }
        });
        addToVerseOrderButton.setOnAction(event -> {
            if (onChangeListener != null) {
                onChangeListener.onAddToVerseButton();
            }
        });
    }

    private void fillSectionTypeComboBox() {
        ObservableList<SectionType> sectionTypeComboBoxItems = sectionTypeComboBox.getItems();
        sectionTypeComboBoxItems.clear();
        sectionTypeComboBoxItems.add(SectionType.INTRO);
        sectionTypeComboBoxItems.add(SectionType.VERSE);
        sectionTypeComboBoxItems.add(SectionType.PRE_CHORUS);
        sectionTypeComboBoxItems.add(SectionType.CHORUS);
        sectionTypeComboBoxItems.add(SectionType.BRIDGE);
        sectionTypeComboBoxItems.add(SectionType.CODA);
    }

    String getRawText() {
        final String textAreaText = textArea.getText();
        return getRawTextFromVerseString(textAreaText);
    }

    TextArea getTextArea() {
        return textArea;
    }

    TextArea getSecondTextArea() {
        return secondTextArea;
    }

    SongVerse getSongVerse() {
        songVerse.setSectionType(sectionTypeComboBox.getValue());
        songVerse.setText(textArea.getText().trim());
        songVerse.setSecondText(secondTextArea.getText());
        return songVerse;
    }

    void setSongVerse(SongVerse songVerse) {
        this.songVerse = songVerse;
        sectionTypeComboBox.getSelectionModel().select(songVerse.getSectionType());
        textArea.setText(getStringTextFromRawText(songVerse.getText()));
        try {
            String secondText = songVerse.getSecondText();
            if (secondText == null) {
                secondText = "";
            }
            secondTextArea.setText(secondText);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    void showSecondText(boolean showSecondText) {
        try {
            if (showSecondText) {
                rightBorderPane.setCenter(secondTextArea);
            } else {
                rightBorderPane.setCenter(null);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    void setOnChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    void reFillSectionType() {
        onChangeListenerPause = true;
        SectionType value = sectionTypeComboBox.getValue();
        ObservableList<SectionType> items = sectionTypeComboBox.getItems();
        if (items != null) {
            List<SectionType> sectionTypes = CloneUtil.cloneList(items);
            items.clear();
            //noinspection ConstantConditions
            items.addAll(sectionTypes);
            sectionTypeComboBox.setItems(items);
            sectionTypeComboBox.setValue(value);
        }
        onChangeListenerPause = false;
    }
}
