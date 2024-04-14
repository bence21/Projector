package projector.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.api.BibleApiBean;
import projector.application.Settings;
import projector.model.Bible;
import projector.model.Book;
import projector.service.BibleService;
import projector.service.BookService;
import projector.service.ServiceManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import static projector.utils.BibleImport.bibleImportFromSQLite;

public class DownloadBiblesController {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadBiblesController.class);
    private final List<Bible> updateNeededBibles = new ArrayList<>();
    @FXML
    private Label label;
    @FXML
    private Button selectButton;
    @FXML
    private Button updateButton;
    @FXML
    private VBox listView;
    private List<Bible> bibles;
    private List<CheckBox> checkBoxes;
    private HashMap<String, CheckBox> checkBoxHashMap;
    private BibleService bibleService;
    private Stage stage;
    private BibleController bibleController;
    private BookService bookService;

    public void initialize() {
        bibleService = ServiceManager.getBibleService();
        bookService = ServiceManager.getBookService();
        bibles = bibleService.findAll();
        bibleService.sort(bibles);
        int initialCapacity = bibles.size();
        checkBoxes = new ArrayList<>(initialCapacity);
        checkBoxHashMap = new HashMap<>(initialCapacity);
        for (Bible bible : bibles) {
            addBibleToVBox(bible);
        }
        BibleApiBean bibleApiBean = new BibleApiBean();
        updateButton.setVisible(false);
        Thread thread = new Thread(() -> {
            List<Bible> onlineBibles = bibleApiBean.getBibleTitles();
            if (onlineBibles == null) {
                noInternetMessage();
                return;
            }
            bibleService.sort(onlineBibles);
            HashMap<String, Bible> hashMap = new HashMap<>();
            for (Bible bible : bibles) {
                String bibleUuid = bible.getUuid();
                if (bibleUuid != null) {
                    hashMap.put(bibleUuid, bible);
                }
            }
            for (Bible onlineBible : onlineBibles) {
                Bible bible = hashMap.get(onlineBible.getUuid());
                if (bible == null) {
                    addBibleToVBox(onlineBible);
                    bibles.add(onlineBible);
                } else {
                    if (bible.getModifiedDate().before(onlineBible.getModifiedDate())) {
                        updateNeededBibles.add(bible);
                        CheckBox checkBox = checkBoxHashMap.get(bible.getUuid());
                        String s = Settings.getInstance().getResourceBundle().getString("Update available");
                        Platform.runLater(() -> checkBox.setText(checkBox.getText() + " (" + s + ")"));
                    }
                }
            }
            if (updateNeededBibles.size() > 0) {
                updateButton.setVisible(true);
            }
            Platform.runLater(() -> label.setText(""));
        });
        thread.start();
        selectButton.setOnAction(event -> {
            try {
                selectButton.setDisable(true);
                Thread thread2 = new Thread(() -> {
                    Thread thread1 = null;
                    for (int i = 0; i < bibles.size(); ++i) {
                        CheckBox checkBox = checkBoxes.get(i);
                        if (checkBox.isSelected() && !checkBox.isDisabled()) {
                            Bible bible = bibleApiBean.getBible(bibles.get(i).getUuid());
                            thread1 = saveBibleInThread(thread1, bible);
                        }
                    }
                    waitAndCloseStage(thread1);
                });
                thread2.start();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        setOnUpdateButton(bibleApiBean);
    }

    private void waitAndCloseStage(Thread thread1) {
        if (thread1 != null) {
            try {
                thread1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        closeDownloadBibleStage();
    }

    private void setOnUpdateButton(BibleApiBean bibleApiBean) {
        updateButton.setOnAction(event -> {
            try {
                updateButton.setDisable(true);
                Thread thread2 = new Thread(() -> {
                    Thread thread1 = null;
                    for (Bible updateNeededBible : updateNeededBibles) {
                        List<Book> oldBooks = updateNeededBible.getBooks();
                        Bible bible = bibleApiBean.updateBible(updateNeededBible);
                        thread1 = updateBibleInThread(thread1, bible, oldBooks);
                    }
                    waitAndCloseStage(thread1);
                });
                thread2.start();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
    }

    private Thread updateBibleInThread(Thread thread, Bible bible, List<Book> oldBooks) {
        if (bible == null) {
            return null;
        }
        try {
            if (thread != null) {
                thread.join();
            }
            thread = new Thread(() -> {
                bookService.delete(oldBooks);
                bibleService.create(bible); // we need to save also the books
            });
            thread.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return thread;
    }

    private void closeDownloadBibleStage() {
        if (stage != null) {
            Platform.runLater(() -> {
                bibleController.initializeBiblesWithoutSameSizeCheck();
                this.stage.close();
            });
        }
    }

    private Thread saveBibleInThread(Thread thread, Bible bible) {
        if (bible == null) {
            return null;
        }
        try {
            if (thread != null) {
                thread.join();
            }
            thread = new Thread(() -> bibleService.create(bible));
            thread.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return thread;
    }

    private void addBibleToVBox(Bible bible) {
        CheckBox checkBox = new CheckBox(bible.getName() + " - " + bible.getShortName());
        boolean b = bible.getId() != null;
        checkBox.setSelected(b);
        checkBox.setDisable(b);
        Platform.runLater(() -> listView.getChildren().add(checkBox));
        checkBoxes.add(checkBox);
        String bibleUuid = bible.getUuid();
        if (bibleUuid != null) {
            checkBoxHashMap.put(bibleUuid, checkBox);
        }
    }

    private void noInternetMessage() {
        final ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
        final String no_internet_connection = resourceBundle.getString("No internet connection");
        final String try_again_later = resourceBundle.getString("Try again later");
        Platform.runLater(() -> label.setText(no_internet_connection + "! " + try_again_later + "!"));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    void setBibleController(BibleController bibleController) {
        this.bibleController = bibleController;
    }

    public void openMyBibleModuleDownloadSite() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI("https://www.ph4.org/b4_index.php"));
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void importMyBibleModule() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose the MyBible module!");
        fileChooser.setInitialDirectory(new File(new File(".").getAbsolutePath()));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MyBible", "*.SQLite3"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            Bible bible = bibleImportFromSQLite(selectedFile.getAbsolutePath());
            ServiceManager.getBibleService().create(bible);
            closeDownloadBibleStage();
        }
    }
}
