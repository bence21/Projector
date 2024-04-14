package projector.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import projector.application.Settings;
import projector.model.Bible;
import projector.model.Reference;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TreeSet;

import static projector.controller.BibleController.setGeneralTextColor;
import static projector.controller.RecentController.getRecentFilePath;

public class HistoryController {

    @FXML
    private ListView<TextFlow> listView;
    private Bible bible;
    private BibleController bibleController;

    private static Text getGeneralText() {
        Text text = new Text();
        setGeneralTextColor(text);
        return text;
    }

    void setBibleController(BibleController bibleController) {
        this.bibleController = bibleController;
    }

    void loadRecents() {
        if (listView.getItems().size() > 12) {
            return;
        }
        try (FileInputStream inputStream = new FileInputStream(getRecentFilePath());
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            br.mark(4);
            if ('\ufeff' != br.read()) {
                br.reset(); // not the BOM marker
            }
            ArrayList<TextFlow> historyList = new ArrayList<>();
            String strLine;
            boolean bibleInitialized = false;
            while ((strLine = br.readLine()) != null) {
                Text dateText = getGeneralText();
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
                    Date date = simpleDateFormat.parse(strLine);
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime(date);
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH) + 1;
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    String parsedDate = year + "-";
                    if (month < 10) {
                        parsedDate += " ";
                    }
                    parsedDate += month + "-";
                    if (day < 10) {
                        parsedDate += " ";
                    }
                    parsedDate += day + " ";
                    if (hour < 10) {
                        parsedDate += " ";
                    }
                    parsedDate += hour;
                    int minutes = calendar.get(Calendar.MINUTE);
                    parsedDate += ":";
                    if (minutes < 10) {
                        parsedDate += "0";
                    }
                    parsedDate += minutes;
                    dateText.setText(parsedDate);
                    dateText.setStyle("-fx-font-weight: bold;");
                } catch (ParseException ignored) {
                }
                Reference reference = new Reference();
                while ((strLine = br.readLine()) != null && !strLine.isEmpty()) {
                    try {
                        strLine = strLine.replace("</color>", "");
                        reference.addVerse(strLine);
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                    }
                }
                Text verses = getGeneralText();
                try {
                    if (bible == null && !bibleInitialized) {
                        bibleInitialized = true;
                        bibleController.initializeBibles();
                    }
                    String referenceText = reference.getReference(bible);
                    if (!referenceText.isEmpty()) {
                        verses.setText("\n" + referenceText);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println(strLine);
                }
                Set<String> songTitles = new TreeSet<>();
                while ((strLine = br.readLine()) != null && !strLine.isEmpty()) {
                    songTitles.add(strLine);
                }
                if (verses.getText().length() > 0 || songTitles.size() > 0) {
                    final String songs = Settings.getInstance().getResourceBundle().getString("Songs");
                    StringBuilder stringBuilder = new StringBuilder().append("\n").append(songs).append(":\n");
                    for (String title : songTitles) {
                        stringBuilder.append(title).append("\n");
                    }
                    Text songsText = new Text(songTitles.size() > 0 ? stringBuilder.toString() : "");
                    setGeneralTextColor(songsText);
                    TextFlow textFlow = new TextFlow();
                    textFlow.getChildren().addAll(dateText, verses, songsText);
                    historyList.add(textFlow);
                }
            }
            for (int i = historyList.size() - 1; i >= 0; --i) {
                listView.getItems().add(historyList.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBible(Bible bible) {
        this.bible = bible;
    }
}
