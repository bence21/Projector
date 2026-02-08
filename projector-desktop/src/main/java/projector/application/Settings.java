package projector.application;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.controller.BibleController;
import projector.controller.SettingsController;
import projector.controller.song.util.OrderMethod;
import projector.controller.util.ImageOrderMethod;
import projector.model.Language;
import projector.service.ServiceManager;
import projector.utils.AppProperties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;

public class Settings {

    private static final Logger LOG = LoggerFactory.getLogger(Settings.class);
    private static Settings instance = null;
    private final BooleanProperty connectedToShared = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty showProgressLine = new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty progressLinePositionIsTop = new SimpleBooleanProperty(true);
    private final SimpleStringProperty networkSharingError = new SimpleStringProperty("");
    private int maxFont = 80;
    private boolean withAccents = false;
    private Color backgroundColor = Color.BLACK;
    private Color color = Color.WHITE;
    private String backgroundImagePath;
    private boolean isBackgroundImage = false;
    private boolean isFastMode = true;
    private boolean isParallel = false;
    private String font = "system";
    private double lineSpace = 3.131991051454138;
    private String fontWeight = "NORMAL";
    private boolean showReferenceOnly = false;
    private boolean referenceItalic = true;
    private boolean logging = false;
    private double previewX;
    private double previewY;
    private double previewWidth;
    private double previewHeight;
    private boolean previewLoadOnStart;
    private double songTabHorizontalSplitPaneDividerPosition = 0.3753943217665615;
    private double songTabVerticalSplitPaneDividerPosition = 0.7344632768361582;
    private double bibleTabHorizontalSplitPaneDividerPosition = 0.7370304114490162;
    private double bibleTabVerticalSplitPaneDividerPosition = 0.2656641604010025;
    private double mainHeight = 600;
    private double mainWidth = 800;
    private boolean referenceChapterSorting = true;
    private boolean referenceVerseSorting = true;
    private Locale preferredLanguage = new Locale("en", "EN");
    private ResourceBundle resourceBundle;
    private double songHeightSliderValue = 250;
    private double verseListViewFontSize = 21;
    private boolean shareOnNetwork = false;
    private Color progressLineColor = new Color(1.0, 1.0, 1.0, 0.7);
    private OrderMethod songOrderMethod = OrderMethod.RELEVANCE;
    private boolean breakLines = false;
    private int breakAfter = 77;
    private Integer progressLineThickness = 0;
    private Language songSelectedLanguage;
    private boolean bibleShortName = false;
    private boolean checkLanguages = false;
    private boolean allowRemote = false;
    private String sceneStyleFile = "application.css";
    private int customCanvasWidth = 400;
    private int customCanvasHeight = 300;
    private boolean shareOnLocalNetworkAutomatically = false;
    private boolean connectToSharedAutomatically = false;
    private BibleController bibleController;
    private boolean showSongSecondText = false;
    private Color songSecondTextColor = new Color(0.46, 1.0, 1.0, 1.0);
    private boolean applicationRunning = true;
    private boolean customCanvasLoadOnStart = false;
    private boolean automaticProjectionScreens = true;
    private boolean forIncomingDisplayOnlySelected = false;

    private boolean strokeFont = false;
    private Color strokeColor = new Color(0, 0, 0, 1.0);
    private double strokeSize = 4.0;
    private StrokeType strokeType = StrokeType.OUTSIDE;
    private ImageOrderMethod imageOrderMethod = ImageOrderMethod.BY_LAST_ACCESSED;
    private Integer maxLine = 0;
    private final double MIDDLE = 0.5;
    private double verticalAlignment = MIDDLE;
    private double horizontalAlignment = MIDDLE;
    private PTextAlignment textAlignment = PTextAlignment.CENTER;
    private double topMargin = 0;
    private double rightMargin = 0;
    private double bottomMargin = 0;
    private double leftMargin = 0;
    private boolean asPadding = true;

    protected Settings() {
        load();
    }

    public synchronized static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public static void shouldBeNull() {
        if (instance != null) {
            LOG.error("Settings should be null");
        }
    }

    public static void emptyInstance() {
        instance = null;
    }

    public static String getSettingFilePath() {
        return AppProperties.getInstance().getWorkDirectory() + "settings.ini";
    }

    public synchronized int getMaxFont() {
        return maxFont;
    }

    public synchronized void setMaxFont(int maxFont) {
        this.maxFont = maxFont;
    }

    public boolean isWithAccents() {
        return withAccents;
    }

    public synchronized void setWithAccents(boolean withAccents) {
        this.withAccents = withAccents;
    }

    public synchronized Color getBackgroundColor() {
        return backgroundColor;
    }

    public synchronized void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public synchronized Color getColor() {
        return color;
    }

    public synchronized void setColor(Color color) {
        this.color = color;
    }

    public synchronized boolean isBackgroundImage() {
        return isBackgroundImage;
    }

    public synchronized void setBackgroundImage(boolean isBackgroundImage) {
        this.isBackgroundImage = isBackgroundImage;
    }

    public synchronized String getBackgroundImagePath() {
        return backgroundImagePath;
    }

    public synchronized void setBackgroundImagePath(String backgroundImagePath) {
        this.backgroundImagePath = backgroundImagePath;
    }

    public synchronized boolean isFastMode() {
        return isFastMode;
    }

    public synchronized void setFastMode(boolean isFastMode) {
        this.isFastMode = isFastMode;
    }

    public synchronized boolean isParallel() {
        return isParallel;
    }

    public synchronized void setParallel(boolean isParallel) {
        this.isParallel = isParallel;
    }

    public synchronized String getFont() {
        return font;
    }

    public synchronized void setFont(String font) {
        this.font = font;
    }

    public synchronized double getLineSpace() {
        return lineSpace;
    }

    public synchronized void setLineSpace(double lineSpace) {
        this.lineSpace = lineSpace;
    }

    public synchronized FontWeight getFontWeight() {
        return SettingsController.getFontWeightByString(fontWeight);
    }

    public synchronized void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
    }

    public synchronized boolean isShowReferenceOnly() {
        return showReferenceOnly;
    }

    public synchronized void setShowReferenceOnly(boolean showReferenceOnly) {
        this.showReferenceOnly = showReferenceOnly;
    }

    public synchronized boolean isReferenceItalic() {
        return referenceItalic;
    }

    public synchronized void setReferenceItalic(boolean referenceItalic) {
        this.referenceItalic = referenceItalic;
    }

    public synchronized void save() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(getSettingFilePath());
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
            bw.write(0 + System.lineSeparator());
            writeIntToFile(bw, "maxFont", maxFont);
            bw.write("withAccents" + System.lineSeparator());
            bw.write(withAccents + System.lineSeparator());
            writeColorToFileWithText(bw, "backgroundColor", backgroundColor);
            writeColorToFileWithText(bw, "color", color);
            bw.write("isImage" + System.lineSeparator());
            bw.write(isBackgroundImage + System.lineSeparator());
            writeAStringToFile(bw, "imagePath", backgroundImagePath);
            bw.write("isFastMode" + System.lineSeparator());
            bw.write(isFastMode + System.lineSeparator());
            bw.write("isParallel" + System.lineSeparator());
            bw.write(isParallel + System.lineSeparator());
            bw.write(System.lineSeparator());
            bw.write(System.lineSeparator());
            bw.write("font" + System.lineSeparator());
            bw.write(font + System.lineSeparator());
            writeDoubleToFile(bw, "lineSpace", lineSpace);
            bw.write("fontWeight" + System.lineSeparator());
            bw.write(fontWeight + System.lineSeparator());
            bw.write("showReferenceOnly" + System.lineSeparator());
            bw.write(showReferenceOnly + System.lineSeparator());
            bw.write("referenceItalic" + System.lineSeparator());
            bw.write(referenceItalic + System.lineSeparator());
            bw.write("logging" + System.lineSeparator());
            bw.write(logging + System.lineSeparator());
            bw.write(System.lineSeparator());
            bw.write(System.lineSeparator());
            writeDoubleToFile(bw, "previewX", previewX);
            writeDoubleToFile(bw, "previewY", previewY);
            writeDoubleToFile(bw, "previewWidth", previewWidth);
            writeDoubleToFile(bw, "previewHeight", previewHeight);
            bw.write("previewLoadOnStart" + System.lineSeparator());
            bw.write(previewLoadOnStart + System.lineSeparator());
            writeDoubleToFile(bw, "songTabHorizontalSplitPaneDividerPosition", songTabHorizontalSplitPaneDividerPosition);
            writeDoubleToFile(bw, "songTabVerticalSplitPaneDividerPosition", songTabVerticalSplitPaneDividerPosition);
            writeDoubleToFile(bw, "bibleTabHorizontalSplitPaneDividerPosition", bibleTabHorizontalSplitPaneDividerPosition);
            writeDoubleToFile(bw, "bibleTabVerticalSplitPaneDividerPosition", bibleTabVerticalSplitPaneDividerPosition);
            writeDoubleToFile(bw, "mainHeight", mainHeight);
            writeDoubleToFile(bw, "mainWidth", mainWidth);
            bw.write("referenceChapterSorting" + System.lineSeparator());
            bw.write(referenceChapterSorting + System.lineSeparator());
            bw.write("referenceVerseSorting" + System.lineSeparator());
            bw.write(referenceVerseSorting + System.lineSeparator());
            bw.write("preferredLanguage" + System.lineSeparator());
            bw.write(preferredLanguage.getLanguage() + System.lineSeparator());
            writeDoubleToFile(bw, "songHeightSliderValue", songHeightSliderValue);
            writeDoubleToFile(bw, "verseListViewFontSize", verseListViewFontSize);
            writeColorToFileWithText(bw, "progressLineColor", progressLineColor);
            bw.write("showProgressLine" + System.lineSeparator());
            bw.write(showProgressLine.get() + System.lineSeparator());
            bw.write("progressLinePositionIsTop" + System.lineSeparator());
            bw.write(progressLinePositionIsTop.get() + System.lineSeparator());
            bw.write(System.lineSeparator());
            bw.write(System.lineSeparator());
            bw.write("songOrderMethod" + System.lineSeparator());
            bw.write(songOrderMethod.name() + System.lineSeparator());
            writeIntToFile(bw, "progressLineThickness", progressLineThickness);
            writeIntToFile(bw, "breakAfter", breakAfter);
            bw.write("breakLines" + System.lineSeparator());
            bw.write(breakLines + System.lineSeparator());
            if (songSelectedLanguage != null) {
                bw.write("songSelectedLanguage" + System.lineSeparator());
                bw.write(songSelectedLanguage.getUuid() + System.lineSeparator());
            } else {
                bw.write(System.lineSeparator());
                bw.write(System.lineSeparator());
            }
            bw.write("bibleShortName" + System.lineSeparator());
            bw.write(bibleShortName + System.lineSeparator());
            bw.write("checkLanguages" + System.lineSeparator());
            bw.write(checkLanguages + System.lineSeparator());
            bw.write("allowRemote" + System.lineSeparator());
            bw.write(allowRemote + System.lineSeparator());
            bw.write("sceneStyleFile" + System.lineSeparator());
            bw.write(sceneStyleFile + System.lineSeparator());
            writeIntToFile(bw, "customCanvasWidth", customCanvasWidth);
            writeIntToFile(bw, "customCanvasHeight", customCanvasHeight);
            bw.write("shareOnLocalNetworkAutomatically" + System.lineSeparator());
            bw.write(shareOnLocalNetworkAutomatically + System.lineSeparator());
            bw.write("connectToSharedAutomatically" + System.lineSeparator());
            bw.write(connectToSharedAutomatically + System.lineSeparator());
            bw.write("showSongSecondText" + System.lineSeparator());
            bw.write(showSongSecondText + System.lineSeparator());
            writeColorToFileWithText(bw, "songSecondTextColor", songSecondTextColor);
            writeBooleanToFile(bw, customCanvasLoadOnStart, "customCanvasLoadOnStart");
            writeBooleanToFile(bw, automaticProjectionScreens, "automaticProjectionScreens");
            writeBooleanToFile(bw, forIncomingDisplayOnlySelected, "forIncomingDisplayOnlySelected");
            writeBooleanToFile(bw, strokeFont, "strokeFont");
            writeColorToFileWithText(bw, "strokeColor", strokeColor);
            writeDoubleToFile(bw, "strokeSize", strokeSize);
            writeIntToFile(bw, "strokeType", strokeType.ordinal());
            writeAStringToFile(bw, "imageOrderMethod", imageOrderMethod.name());
            writeIntToFile(bw, "maxLine", maxLine);
            writeDoubleToFile(bw, "verticalAlignment", verticalAlignment);
            writeDoubleToFile(bw, "horizontalAlignment", horizontalAlignment);
            writeAStringToFile(bw, "textAlignment", textAlignment.name());
            writeDoubleToFile(bw, "topMargin", topMargin);
            writeDoubleToFile(bw, "rightMargin", rightMargin);
            writeDoubleToFile(bw, "bottomMargin", bottomMargin);
            writeDoubleToFile(bw, "leftMargin", leftMargin);
            writeBooleanToFile(bw, asPadding, "asPadding");
            bw.close();
        } catch (IOException e) {
            LOG.warn("There is some error on settings save!", e);
        }
    }

    private void writeAStringToFile(BufferedWriter bw, String s, String name) throws IOException {
        bw.write(s + System.lineSeparator());
        bw.write(name + System.lineSeparator());
    }

    private void writeIntToFile(BufferedWriter bw, String s, int i) throws IOException {
        bw.write(s + System.lineSeparator());
        bw.write(i + System.lineSeparator());
    }

    private void writeDoubleToFile(BufferedWriter bw, String s, double v) throws IOException {
        bw.write(s + System.lineSeparator());
        bw.write(v + System.lineSeparator());
    }

    private void writeColorToFileWithText(BufferedWriter bw, String s, Color color) throws IOException {
        bw.write(s + System.lineSeparator());
        writeColorToFile(bw, color);
    }

    private void writeBooleanToFile(BufferedWriter bw, boolean b, String s) throws IOException {
        bw.write(s + System.lineSeparator());
        bw.write(b + System.lineSeparator());
    }

    private void writeColorToFile(BufferedWriter bw, Color color) throws IOException {
        bw.write(color.getRed() + System.lineSeparator());
        bw.write(color.getGreen() + System.lineSeparator());
        bw.write(color.getBlue() + System.lineSeparator());
        bw.write(color.getOpacity() + System.lineSeparator());
    }

    private synchronized void load() {
        BufferedReader br = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(getSettingFilePath());
            br = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8));
            int bibleNumber = Integer.parseInt(br.readLine());
            for (int i = 0; i < bibleNumber; ++i) {
                br.readLine();
                br.readLine();
            }
            br.readLine();
            maxFont = Integer.parseInt(br.readLine());
            br.readLine();
            String s = br.readLine();
            withAccents = parseBoolean(s);
            br.readLine();
            backgroundColor = new Color(parseDouble(br.readLine()), parseDouble(br.readLine()),
                    parseDouble(br.readLine()), parseDouble(br.readLine()));
            br.readLine();
            color = new Color(parseDouble(br.readLine()), parseDouble(br.readLine()),
                    parseDouble(br.readLine()), parseDouble(br.readLine()));
            br.readLine();
            isBackgroundImage = parseBoolean(br.readLine());
            br.readLine();
            backgroundImagePath = br.readLine();
            br.readLine();
            isFastMode = parseBoolean(br.readLine());
            br.readLine();
            isParallel = parseBoolean(br.readLine());
            br.readLine();
            br.readLine();
            br.readLine();
            font = br.readLine();
            br.readLine();
            lineSpace = parseDouble(br.readLine());
            br.readLine();
            fontWeight = br.readLine();
            br.readLine();
            showReferenceOnly = parseBoolean(br.readLine());
            br.readLine();
            referenceItalic = parseBoolean(br.readLine());
            if (br.ready()) {
                br.readLine();
                logging = parseBoolean(br.readLine());
            }
            br.readLine();
            br.readLine();
            previewX = getDoubleFromFile(br, previewX);
            previewY = getDoubleFromFile(br, previewY);
            br.readLine();
            previewWidth = parseDouble(br.readLine());
            br.readLine();
            previewHeight = parseDouble(br.readLine());
            br.readLine();
            previewLoadOnStart = parseBoolean(br.readLine());
            br.readLine();
            songTabHorizontalSplitPaneDividerPosition = parseDouble(br.readLine());
            br.readLine();
            songTabVerticalSplitPaneDividerPosition = parseDouble(br.readLine());
            br.readLine();
            bibleTabHorizontalSplitPaneDividerPosition = parseDouble(br.readLine());
            br.readLine();
            bibleTabVerticalSplitPaneDividerPosition = parseDouble(br.readLine());
            br.readLine();
            mainHeight = parseDouble(br.readLine());
            br.readLine();
            mainWidth = parseDouble(br.readLine());
            br.readLine();
            referenceChapterSorting = parseBoolean(br.readLine());
            br.readLine();
            referenceVerseSorting = parseBoolean(br.readLine());
            br.readLine();
            setPreferredLanguage(br.readLine());
            br.readLine();
            songHeightSliderValue = parseDouble(br.readLine());
            verseListViewFontSize = getDoubleFromFile(br, verseListViewFontSize);
            br.readLine();
            progressLineColor = getColorFromFile2(br);
            br.readLine();
            showProgressLine.set(parseBoolean(br.readLine()));
            br.readLine();
            progressLinePositionIsTop.set(parseBoolean(br.readLine()));
            br.readLine();
            br.readLine();
            br.readLine();
            songOrderMethod = OrderMethod.valueOf(br.readLine());
            br.readLine();
            progressLineThickness = Integer.parseInt(br.readLine());
            br.readLine();
            breakAfter = Integer.parseInt(br.readLine());
            br.readLine();
            breakLines = parseBoolean(br.readLine());
            br.readLine();
            String uuid = br.readLine();
            if (uuid == null || uuid.isEmpty()) {
                songSelectedLanguage = null;
            } else {
                songSelectedLanguage = ServiceManager.getLanguageService().findByUuid(uuid);
            }
            br.readLine();
            bibleShortName = parseBoolean(br.readLine());
            br.readLine();
            checkLanguages = parseBoolean(br.readLine());
            br.readLine();
            allowRemote = parseBoolean(br.readLine());
            br.readLine();
            String sceneStyleFile = br.readLine();
            if (sceneStyleFile != null) {
                this.sceneStyleFile = sceneStyleFile;
            }
            br.readLine();
            customCanvasWidth = Integer.parseInt(br.readLine());
            br.readLine();
            customCanvasHeight = Integer.parseInt(br.readLine());
            br.readLine();
            shareOnLocalNetworkAutomatically = parseBoolean(br.readLine());
            br.readLine();
            connectToSharedAutomatically = parseBoolean(br.readLine());
            br.readLine();
            showSongSecondText = parseBoolean(br.readLine());
            songSecondTextColor = getColorFromFile(br, songSecondTextColor);
            customCanvasLoadOnStart = getABoolean(br, customCanvasLoadOnStart);
            automaticProjectionScreens = getABoolean(br, automaticProjectionScreens);
            forIncomingDisplayOnlySelected = getABoolean(br, forIncomingDisplayOnlySelected);
            strokeFont = getABoolean(br, strokeFont);
            strokeColor = getColorFromFile(br, strokeColor);
            strokeSize = getDoubleFromFile(br, strokeSize);
            strokeType = getStrokeTypeFromFile(br, strokeType);
            br.readLine();
            imageOrderMethod = ImageOrderMethod.valueOf(br.readLine());
            maxLine = getIntFromFile(br, maxLine);
            verticalAlignment = getDoubleFromFile(br, verticalAlignment);
            horizontalAlignment = getDoubleFromFile(br, horizontalAlignment);
            br.readLine();
            textAlignment = PTextAlignment.valueOf(br.readLine());
            topMargin = getDoubleFromFile(br, topMargin);
            rightMargin = getDoubleFromFile(br, rightMargin);
            bottomMargin = getDoubleFromFile(br, bottomMargin);
            leftMargin = getDoubleFromFile(br, leftMargin);
            asPadding = getABoolean(br, asPadding);
            br.close();
        } catch (IOException | NullPointerException | IllegalArgumentException e) {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e1) {
                LOG.warn("There is some error on settings load!", e1);
            }
        }
    }

    private StrokeType getStrokeTypeFromFile(BufferedReader br, StrokeType defaultValue) {
        try {
            br.readLine();
            String s = br.readLine();
            if (s == null) {
                return defaultValue;
            }
            StrokeType[] strokeTypes = StrokeType.values();
            int strokeTypeIndex = Integer.parseInt(s);
            if (0 <= strokeTypeIndex && strokeTypeIndex < strokeTypes.length) {
                return strokeTypes[strokeTypeIndex];
            }
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
        return defaultValue;
    }

    private double getDoubleFromFile(BufferedReader br, double defaultValue) {
        try {
            br.readLine();
            String s = br.readLine();
            if (s == null) {
                return defaultValue;
            }
            return parseDouble(s);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            return defaultValue;
        }
    }

    private int getIntFromFile(BufferedReader br, int defaultValue) {
        try {
            br.readLine();
            String s = br.readLine();
            if (s == null) {
                return defaultValue;
            }
            return Integer.parseInt(s);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            return defaultValue;
        }
    }

    private Color getColorFromFile(BufferedReader br, Color defaultValue) throws IOException {
        br.readLine();
        Color colorFromFile2 = getColorFromFile2(br);
        if (colorFromFile2 == null) {
            return defaultValue;
        }
        return colorFromFile2;
    }

    private boolean getABoolean(BufferedReader br, boolean defaultValue) throws IOException {
        br.readLine();
        String s = br.readLine();
        if (s == null) {
            return defaultValue;
        }
        return parseBoolean(s);
    }

    private Color getColorFromFile2(BufferedReader br) throws IOException {
        String red = br.readLine();
        if (red == null) {
            return null;
        }
        String green = br.readLine();
        if (green == null) {
            return null;
        }
        String blue = br.readLine();
        if (blue == null) {
            return null;
        }
        String opacity = br.readLine();
        if (opacity == null) {
            return null;
        }
        return new Color(parseDouble(red), parseDouble(green),
                parseDouble(blue), parseDouble(opacity));
    }

    public synchronized String getFontWeightString() {
        return fontWeight;
    }

    public synchronized double getPreviewX() {
        return previewX;
    }

    public synchronized void setPreviewX(double previewX) {
        this.previewX = previewX;
    }

    public synchronized double getPreviewY() {
        return previewY;
    }

    public synchronized void setPreviewY(double previewY) {
        this.previewY = previewY;
    }

    public synchronized double getPreviewWidth() {
        return previewWidth;
    }

    public synchronized void setPreviewWidth(double previewWidth) {
        this.previewWidth = previewWidth;
    }

    public synchronized double getPreviewHeight() {
        return previewHeight;
    }

    public synchronized void setPreviewHeight(double previewHeight) {
        this.previewHeight = previewHeight;
    }

    public synchronized boolean isPreviewLoadOnStart() {
        return previewLoadOnStart;
    }

    public synchronized void setPreviewLoadOnStart(boolean previewLoadOnStart) {
        this.previewLoadOnStart = previewLoadOnStart;
    }

    public synchronized double getSongTabHorizontalSplitPaneDividerPosition() {
        return songTabHorizontalSplitPaneDividerPosition;
    }

    public synchronized void setSongTabHorizontalSplitPaneDividerPosition(double songTabHorizontalSplitPaneDividerPosition) {
        this.songTabHorizontalSplitPaneDividerPosition = songTabHorizontalSplitPaneDividerPosition;
    }

    public synchronized double getSongTabVerticalSplitPaneDividerPosition() {
        return songTabVerticalSplitPaneDividerPosition;
    }

    public synchronized void setSongTabVerticalSplitPaneDividerPosition(double songTabVerticalSplitPaneDividerPosition) {
        this.songTabVerticalSplitPaneDividerPosition = songTabVerticalSplitPaneDividerPosition;
    }

    public synchronized double getBibleTabHorizontalSplitPaneDividerPosition() {
        return bibleTabHorizontalSplitPaneDividerPosition;
    }

    public synchronized void setBibleTabHorizontalSplitPaneDividerPosition(double bibleTabHorizontalSplitPaneDividerPosition) {
        this.bibleTabHorizontalSplitPaneDividerPosition = bibleTabHorizontalSplitPaneDividerPosition;
    }

    public synchronized double getBibleTabVerticalSplitPaneDividerPosition() {
        return bibleTabVerticalSplitPaneDividerPosition;
    }

    public synchronized void setBibleTabVerticalSplitPaneDividerPosition(double bibleTabVerticalSplitPaneDividerPosition) {
        this.bibleTabVerticalSplitPaneDividerPosition = bibleTabVerticalSplitPaneDividerPosition;
    }

    public synchronized double getMainHeight() {
        return mainHeight;
    }

    public synchronized void setMainHeight(double mainHeight) {
        this.mainHeight = mainHeight;
    }

    public synchronized double getMainWidth() {
        return mainWidth;
    }

    public synchronized void setMainWidth(double mainWidth) {
        this.mainWidth = mainWidth;
    }

    public synchronized boolean isReferenceChapterSorting() {
        return referenceChapterSorting;
    }

    public synchronized void setReferenceChapterSorting(boolean referenceChapterSorting) {
        this.referenceChapterSorting = referenceChapterSorting;
    }

    public synchronized boolean isReferenceVerseSorting() {
        return referenceVerseSorting;
    }

    public synchronized void setReferenceVerseSorting(boolean referenceVerseSorting) {
        this.referenceVerseSorting = referenceVerseSorting;
    }

    public synchronized Locale getPreferredLanguage() {
        return preferredLanguage;
    }

    public synchronized void setPreferredLanguage(String language) {
        switch (language) {
            case "hu" -> preferredLanguage = new Locale(language, "HU");
            case "ro" -> preferredLanguage = new Locale(language, "RO");
            default -> preferredLanguage = new Locale("en", "US");
        }
    }

    public synchronized ResourceBundle getResourceBundle() {
        if (resourceBundle == null) {
            resourceBundle = ResourceBundle.getBundle("bundles.language", getPreferredLanguage());
        }
        return resourceBundle;
    }

    public synchronized double getSongHeightSliderValue() {
        return songHeightSliderValue;
    }

    public synchronized void setSongHeightSliderValue(double songHeightSliderValue) {
        this.songHeightSliderValue = songHeightSliderValue;
    }

    public synchronized double getVerseListViewFontSize() {
        return verseListViewFontSize;
    }

    public synchronized void setVerseListViewFontSize(double verseListViewFontSize) {
        this.verseListViewFontSize = verseListViewFontSize;
    }

    public synchronized boolean isShowProgressLine() {
        return showProgressLine.get();
    }

    public synchronized void setShowProgressLine(boolean showProgressLine) {
        this.showProgressLine.set(showProgressLine);
    }

    public synchronized SimpleBooleanProperty showProgressLineProperty() {
        return showProgressLine;
    }

    public synchronized Color getProgressLineColor() {
        return progressLineColor;
    }

    public synchronized void setProgressLineColor(Color progressLineColor) {
        this.progressLineColor = progressLineColor;
    }

    public synchronized boolean isProgressLinePositionIsTop() {
        return progressLinePositionIsTop.get();
    }

    public synchronized void setProgressLinePositionIsTop(boolean progressLinePositionIsTop) {
        this.progressLinePositionIsTop.set(progressLinePositionIsTop);
    }

    public synchronized SimpleBooleanProperty progressLinePositionIsTopProperty() {
        return progressLinePositionIsTop;
    }

    public synchronized boolean isShareOnNetwork() {
        return shareOnNetwork;
    }

    public synchronized void setShareOnNetwork(boolean shareOnNetwork) {
        this.shareOnNetwork = shareOnNetwork;
    }

    public synchronized boolean isConnectedToShared() {
        return connectedToShared.get();
    }

    public synchronized void setConnectedToShared(boolean connectedToShared) {
        this.connectedToShared.set(connectedToShared);
    }

    public synchronized BooleanProperty connectedToSharedProperty() {
        return connectedToShared;
    }

    public synchronized StringProperty networkSharingErrorProperty() {
        return networkSharingError;
    }

    public synchronized void setNetworkSharingError(String error) {
        javafx.application.Platform.runLater(() -> networkSharingError.set(error == null ? "" : error));
    }

    public synchronized OrderMethod getSongOrderMethod() {
        return songOrderMethod;
    }

    public synchronized void setSongOrderMethod(OrderMethod songOrderMethod) {
        this.songOrderMethod = songOrderMethod;
    }

    public synchronized boolean isBreakLines() {
        return breakLines;
    }

    public synchronized void setBreakLines(boolean breakLines) {
        this.breakLines = breakLines;
    }

    public synchronized int getBreakAfter() {
        return breakAfter;
    }

    public synchronized void setBreakAfter(int breakAfter) {
        this.breakAfter = breakAfter;
    }

    public Integer getProgressLineThickness() {
        return progressLineThickness;
    }

    public void setProgressLineThickness(Integer progressLineThickness) {
        this.progressLineThickness = progressLineThickness;
    }

    public Language getSongSelectedLanguage() {
        if (songSelectedLanguage == null || (songSelectedLanguage.getCountedSongsSize() == 0 && songSelectedLanguage.getSongs().isEmpty())) {
            List<Language> languages = ServiceManager.getLanguageService().findAll();
            languages.sort((o1, o2) -> Integer.compare(o2.getSongs().size(), o1.getSongs().size()));
            if (!languages.isEmpty()) {
                songSelectedLanguage = languages.get(0);
            }
        }
        return songSelectedLanguage;
    }

    public void setSongSelectedLanguage(Language songSelectedLanguage) {
        this.songSelectedLanguage = songSelectedLanguage;
    }

    public boolean getBibleShortName() {
        return bibleShortName;
    }

    public void setBibleShortName(boolean bibleShortName) {
        this.bibleShortName = bibleShortName;
    }

    public boolean isCheckLanguages() {
        return checkLanguages;
    }

    public boolean isAllowRemote() {
        return allowRemote;
    }

    public void setAllowRemote(boolean allowRemote) {
        this.allowRemote = allowRemote;
    }

    public String getSceneStyleFile() {
        return sceneStyleFile;
    }

    public void setSceneStyleFile(String sceneStyleFile) {
        this.sceneStyleFile = sceneStyleFile;
    }

    public boolean isDarkTheme() {
        String s = getSceneStyleFile();
        return s != null && s.equals("applicationDark.css");
    }

    public int getCustomCanvasWidth() {
        return customCanvasWidth;
    }

    public int getCustomCanvasHeight() {
        return customCanvasHeight;
    }

    public boolean isShareOnLocalNetworkAutomatically() {
        return shareOnLocalNetworkAutomatically;
    }

    public void setShareOnLocalNetworkAutomatically(boolean shareOnLocalNetworkAutomatically) {
        this.shareOnLocalNetworkAutomatically = shareOnLocalNetworkAutomatically;
    }

    public boolean isConnectToSharedAutomatically() {
        return connectToSharedAutomatically;
    }

    public void setConnectToSharedAutomatically(boolean connectToSharedAutomatically) {
        this.connectToSharedAutomatically = connectToSharedAutomatically;
    }

    public BibleController getBibleController() {
        return bibleController;
    }

    public void setBibleController(BibleController bibleController) {
        this.bibleController = bibleController;
    }

    public boolean isShowSongSecondText() {
        return showSongSecondText;
    }

    public void setShowSongSecondText(boolean showSongSecondText) {
        this.showSongSecondText = showSongSecondText;
    }

    public Color getSongSecondTextColor() {
        return songSecondTextColor;
    }

    public void setSongSecondTextColor(Color songSecondTextColor) {
        this.songSecondTextColor = songSecondTextColor;
    }

    public boolean isApplicationRunning() {
        return applicationRunning;
    }

    public void setApplicationRunning(boolean applicationRunning) {
        this.applicationRunning = applicationRunning;
    }

    public boolean isCustomCanvasLoadOnStart() {
        return customCanvasLoadOnStart;
    }

    public boolean isAutomaticProjectionScreens() {
        return automaticProjectionScreens;
    }

    public void setAutomaticProjectionScreens(boolean automaticProjectionScreens) {
        this.automaticProjectionScreens = automaticProjectionScreens;
    }

    public boolean isForIncomingDisplayOnlySelected() {
        return forIncomingDisplayOnlySelected;
    }

    public void setForIncomingDisplayOnlySelected(boolean forIncomingDisplayOnlySelected) {
        this.forIncomingDisplayOnlySelected = forIncomingDisplayOnlySelected;
    }

    public boolean isStrokeFont() {
        return strokeFont;
    }

    public void setStrokeFont(boolean strokeFont) {
        this.strokeFont = strokeFont;
    }

    public Color getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(Color strokeColor) {
        this.strokeColor = strokeColor;
    }

    public double getStrokeSize() {
        return strokeSize;
    }

    public void setStrokeSize(double strokeSize) {
        this.strokeSize = strokeSize;
    }

    public StrokeType getStrokeType() {
        return strokeType;
    }

    public void setStrokeType(StrokeType strokeType) {
        this.strokeType = strokeType;
    }

    public ImageOrderMethod getImageOrderMethod() {
        return imageOrderMethod;
    }

    public void setImageOrderMethod(ImageOrderMethod imageOrderMethod) {
        this.imageOrderMethod = imageOrderMethod;
    }

    public void setMaxLine(int maxLine) {
        this.maxLine = maxLine;
    }

    public int getMaxLine() {
        return maxLine;
    }

    public void setVerticalAlignment(double verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public double getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setHorizontalAlignment(double horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    public double getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setTextAlignment(PTextAlignment textAlignment) {
        this.textAlignment = textAlignment;
    }

    public PTextAlignment getTextAlignment() {
        return textAlignment;
    }

    public void setTopMargin(double topMargin) {
        this.topMargin = topMargin;
    }

    public double getTopMargin() {
        return topMargin;
    }

    public void setRightMargin(double rightMargin) {
        this.rightMargin = rightMargin;
    }

    public double getRightMargin() {
        return rightMargin;
    }

    public void setBottomMargin(double bottomMargin) {
        this.bottomMargin = bottomMargin;
    }

    public double getBottomMargin() {
        return bottomMargin;
    }

    public void setLeftMargin(double leftMargin) {
        this.leftMargin = leftMargin;
    }

    public double getLeftMargin() {
        return leftMargin;
    }

    public boolean isAsPadding() {
        return asPadding;
    }

    public void setAsPadding(boolean asPadding) {
        this.asPadding = asPadding;
    }
}
