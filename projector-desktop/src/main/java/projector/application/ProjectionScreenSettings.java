package projector.application;

import com.bence.projector.common.serializer.ColorDeserializer;
import com.bence.projector.common.serializer.ColorSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.controller.SettingsController;
import projector.controller.util.ProjectionScreenHolder;
import projector.utils.AppProperties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static projector.application.ScreenProjectionType.copyList;

public class ProjectionScreenSettings {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectionScreenSettings.class);
    private final Settings settings;

    private ProjectionScreenHolder projectionScreenHolder;
    @Expose
    private Integer maxFont;
    @Expose
    private Color backgroundColor;
    @Expose
    private Color color;
    private BackgroundImage backgroundImage;
    @Expose
    private String backgroundImagePath;
    @Expose
    private Boolean isBackgroundImage;
    @Expose
    private String font;
    @Expose
    private Double lineSpace;
    @Expose
    private String fontWeight;
    @Expose
    private Color progressLineColor;
    @Expose
    private Boolean breakLines;
    @Expose
    private Integer breakAfter;
    @Expose
    private Integer progressLineThickness;
    @Expose
    private Boolean showSongSecondText;
    @Expose
    private Color songSecondTextColor;
    @Expose
    private Boolean progressLinePositionIsTop;
    @Expose
    private Boolean strokeFont;
    @Expose
    private List<ScreenProjectionType> screenProjectionTypes;
    private boolean useGlobalSettings = true;
    @Expose
    private Color strokeColor;
    @Expose
    private Double strokeSize;
    @Expose
    private StrokeType strokeType;
    @Expose
    private Double verticalAlignment;
    @Expose
    private Double horizontalAlignment;
    @Expose
    private PTextAlignment textAlignment;
    @Expose
    private Double topMargin;
    @Expose
    private Double rightMargin;
    @Expose
    private Double bottomMargin;
    @Expose
    private Double leftMargin;
    @Expose
    private Boolean asPadding;
    @Expose
    private Boolean focusOnSongPart;
    @Expose
    private Boolean progressBar;
    @Expose
    private Double progressBarHeight;
    // check for copy constructor!
    private Listener onChangedListener = null;

    public ProjectionScreenSettings() {
        settings = Settings.getInstance();
    }

    public ProjectionScreenSettings(ProjectionScreenHolder projectionScreenHolder) {
        this();
        this.projectionScreenHolder = projectionScreenHolder;
        load();
    }

    public ProjectionScreenSettings(ProjectionScreenSettings projectionScreenSettings) {
        settings = copyFromOther(projectionScreenSettings);
        this.projectionScreenHolder = projectionScreenSettings.projectionScreenHolder;
        this.onChangedListener = projectionScreenSettings.onChangedListener;
        this.useGlobalSettings = projectionScreenSettings.useGlobalSettings;
    }

    private Settings copyFromOther(ProjectionScreenSettings projectionScreenSettings) {
        final Settings settings = projectionScreenSettings.settings;
        this.maxFont = projectionScreenSettings.maxFont;
        this.backgroundColor = projectionScreenSettings.backgroundColor;
        this.color = projectionScreenSettings.color;
        this.backgroundImage = projectionScreenSettings.backgroundImage;
        this.backgroundImagePath = projectionScreenSettings.backgroundImagePath;
        this.isBackgroundImage = projectionScreenSettings.isBackgroundImage;
        this.font = projectionScreenSettings.font;
        this.lineSpace = projectionScreenSettings.lineSpace;
        this.fontWeight = projectionScreenSettings.fontWeight;
        this.progressLineColor = projectionScreenSettings.progressLineColor;
        this.breakLines = projectionScreenSettings.breakLines;
        this.breakAfter = projectionScreenSettings.breakAfter;
        this.progressLineThickness = projectionScreenSettings.progressLineThickness;
        this.showSongSecondText = projectionScreenSettings.showSongSecondText;
        this.songSecondTextColor = projectionScreenSettings.songSecondTextColor;
        this.progressLinePositionIsTop = projectionScreenSettings.progressLinePositionIsTop;
        this.strokeFont = projectionScreenSettings.strokeFont;
        this.screenProjectionTypes = copyList(projectionScreenSettings.screenProjectionTypes);
        this.strokeColor = projectionScreenSettings.strokeColor;
        this.strokeSize = projectionScreenSettings.strokeSize;
        this.strokeType = projectionScreenSettings.strokeType;
        this.verticalAlignment = projectionScreenSettings.verticalAlignment;
        this.horizontalAlignment = projectionScreenSettings.horizontalAlignment;
        this.textAlignment = projectionScreenSettings.textAlignment;
        this.topMargin = projectionScreenSettings.topMargin;
        this.rightMargin = projectionScreenSettings.rightMargin;
        this.bottomMargin = projectionScreenSettings.bottomMargin;
        this.leftMargin = projectionScreenSettings.leftMargin;
        this.asPadding = projectionScreenSettings.asPadding;
        this.focusOnSongPart = projectionScreenSettings.focusOnSongPart;
        this.progressBar = projectionScreenSettings.progressBar;
        this.progressBarHeight = projectionScreenSettings.progressBarHeight;
        // Also copy fromJson in load method!!!
        return settings;
    }

    private static boolean isaBoolean(Boolean aBoolean) {
        return aBoolean != null && aBoolean;
    }

    public ProjectionScreenHolder getProjectionScreenHolder() {
        return projectionScreenHolder;
    }

    public Integer getMaxFont() {
        if (maxFont == null && useGlobalSettings) {
            return settings.getMaxFont();
        }
        return maxFont;
    }

    public void setMaxFont(Integer maxFont) {
        this.maxFont = maxFont;
        onChanged();
    }

    private void onChanged() {
        if (onChangedListener != null) {
            onChangedListener.onChanged();
        }
    }

    public Color getBackgroundColor() {
        if (backgroundColor == null && useGlobalSettings) {
            return settings.getBackgroundColor();
        }
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        onChanged();
    }

    public Color getColor() {
        if (color == null && useGlobalSettings) {
            return settings.getColor();
        }
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        onChanged();
    }

    public boolean isBackgroundImage() {
        return isaBoolean(getIsBackgroundImage());
    }

    public Boolean getIsBackgroundImage() {
        if (isBackgroundImage == null && useGlobalSettings) {
            return settings.isBackgroundImage();
        }
        return isBackgroundImage;
    }

    public void setIsBackgroundImage(Boolean isBackgroundImage) {
        this.isBackgroundImage = isBackgroundImage;
        onChanged();
    }

    public String getBackgroundImagePath() {
        if (backgroundImagePath == null && useGlobalSettings) {
            return settings.getBackgroundImagePath();
        }
        return backgroundImagePath;
    }

    public void setBackgroundImagePath(String backgroundImagePath) {
        this.backgroundImagePath = backgroundImagePath;
        onChanged();
    }

    public String getFont() {
        if (font == null && useGlobalSettings) {
            return settings.getFont();
        }
        return font;
    }

    public void setFont(String font) {
        this.font = font;
        onChanged();
    }

    public Double getLineSpace() {
        if (lineSpace == null && useGlobalSettings) {
            return settings.getLineSpace();
        }
        return lineSpace;
    }

    public void setLineSpace(Double lineSpace) {
        this.lineSpace = lineSpace;
        onChanged();
    }

    public FontWeight getFontWeight() {
        return SettingsController.getFontWeightByString(getFontWeightString());
    }

    public void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
        onChanged();
    }

    public void save() {
        FileOutputStream ofStream;
        try {
            ofStream = new FileOutputStream(getFileName());
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ofStream, StandardCharsets.UTF_8));
            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .excludeFieldsWithoutExposeAnnotation()
                    .registerTypeAdapter(Color.class, new ColorSerializer())
                    .create();
            String json = gson.toJson(this);
            bw.write(json);
            bw.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void reload() {
        try {
            File file = new File(getFileName());
            if (!file.exists()) {
                copyFromOther(new ProjectionScreenSettings());
            } else {
                load();
            }
            onChanged();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void load() {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(getFileName());
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder s = new StringBuilder();
            String readLine = br.readLine();
            while (readLine != null) {
                s.append(readLine);
                readLine = br.readLine();
            }
            br.close();
            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .excludeFieldsWithoutExposeAnnotation()
                    .registerTypeAdapter(Color.class, new ColorDeserializer())
                    .create();
            ProjectionScreenSettings fromJson = gson.fromJson(s.toString(), ProjectionScreenSettings.class);
            if (fromJson == null) {
                return;
            }
            this.maxFont = fromJson.maxFont;
            this.backgroundColor = fromJson.backgroundColor;
            this.color = fromJson.color;
            this.backgroundImage = fromJson.backgroundImage;
            this.backgroundImagePath = fromJson.backgroundImagePath;
            this.isBackgroundImage = fromJson.isBackgroundImage;
            this.font = fromJson.font;
            this.lineSpace = fromJson.lineSpace;
            this.fontWeight = fromJson.fontWeight;
            this.progressLineColor = fromJson.progressLineColor;
            this.breakLines = fromJson.breakLines;
            this.breakAfter = fromJson.breakAfter;
            this.progressLineThickness = fromJson.progressLineThickness;
            this.showSongSecondText = fromJson.showSongSecondText;
            this.songSecondTextColor = fromJson.songSecondTextColor;
            this.progressLinePositionIsTop = fromJson.progressLinePositionIsTop;
            this.strokeFont = fromJson.strokeFont;
            this.screenProjectionTypes = fromJson.screenProjectionTypes;
            this.strokeColor = fromJson.strokeColor;
            this.strokeSize = fromJson.strokeSize;
            this.strokeType = fromJson.strokeType;
            this.verticalAlignment = fromJson.verticalAlignment;
            this.horizontalAlignment = fromJson.horizontalAlignment;
            this.textAlignment = fromJson.textAlignment;
            this.topMargin = fromJson.topMargin;
            this.rightMargin = fromJson.rightMargin;
            this.bottomMargin = fromJson.bottomMargin;
            this.leftMargin = fromJson.leftMargin;
            this.focusOnSongPart = fromJson.focusOnSongPart;
            this.progressBar = fromJson.progressBar;
            this.progressBarHeight = fromJson.progressBarHeight;
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private String getFileName() {
        return getFileName(projectionScreenHolder.getName());
    }

    private String getFileName(String name) {
        String screensDirectory = AppProperties.getInstance().getWorkDirectory() + "screens";
        try {
            Files.createDirectories(Paths.get(screensDirectory));
        } catch (IOException ignored) {
        }
        return screensDirectory + "/" + name + ".json";
    }

    public String getFontWeightString() {
        if (fontWeight == null && useGlobalSettings) {
            return settings.getFontWeightString();
        }
        return fontWeight;
    }

    public Color getProgressLineColor() {
        if (progressLineColor == null && useGlobalSettings) {
            return settings.getProgressLineColor();
        }
        return progressLineColor;
    }

    public void setProgressLineColor(Color progressLineColor) {
        this.progressLineColor = progressLineColor;
        onChanged();
    }

    public boolean isBreakLines() {
        return isaBoolean(getBreakLines());
    }

    public Boolean getBreakLines() {
        if (breakLines == null && useGlobalSettings) {
            return settings.isBreakLines();
        }
        return breakLines;
    }

    public void setBreakLines(Boolean breakLines) {
        this.breakLines = breakLines;
        onChanged();
    }

    public Integer getBreakAfter() {
        if (breakAfter == null && useGlobalSettings) {
            return settings.getBreakAfter();
        }
        return breakAfter;
    }

    public void setBreakAfter(Integer breakAfter) {
        this.breakAfter = breakAfter;
        onChanged();
    }

    public Integer getProgressLineThickness() {
        if (progressLineThickness == null && useGlobalSettings) {
            return settings.getProgressLineThickness();
        }
        return progressLineThickness;
    }

    public void setProgressLineThickness(Integer progressLineThickness) {
        this.progressLineThickness = progressLineThickness;
        onChanged();
    }

    public boolean isShowSongSecondText() {
        return isaBoolean(getShowSongSecondText());
    }

    public Boolean getShowSongSecondText() {
        if (showSongSecondText == null && useGlobalSettings) {
            return settings.isShowSongSecondText();
        }
        return showSongSecondText;
    }

    public void setShowSongSecondText(Boolean showSongSecondText) {
        this.showSongSecondText = showSongSecondText;
        onChanged();
    }

    public Color getSongSecondTextColor() {
        if (songSecondTextColor == null && useGlobalSettings) {
            return settings.getSongSecondTextColor();
        }
        return songSecondTextColor;
    }

    public void setSongSecondTextColor(Color songSecondTextColor) {
        this.songSecondTextColor = songSecondTextColor;
        onChanged();
    }

    public boolean isProgressLinePositionIsTop() {
        return isaBoolean(getProgressLinePosition());
    }

    public void setProgressLinePositionIsTop(Boolean progressLinePositionIsTop) {
        this.progressLinePositionIsTop = progressLinePositionIsTop;
        onChanged();
    }

    public Boolean getProgressLinePosition() {
        if (progressLinePositionIsTop == null && useGlobalSettings) {
            return settings.isProgressLinePositionIsTop();
        }
        return progressLinePositionIsTop;
    }

    public void setUseGlobalSettings(boolean useGlobalSettings) {
        this.useGlobalSettings = useGlobalSettings;
    }

    public Boolean getStrokeFont() {
        return strokeFont;
    }

    public void setStrokeFont(Boolean strokeFont) {
        this.strokeFont = strokeFont;
        onChanged();
    }

    public boolean isStrokeFont() {
        if (strokeFont == null && useGlobalSettings) {
            return settings.isStrokeFont();
        }
        return strokeFont != null && strokeFont;
    }

    public Color getStrokeColor() {
        if (strokeColor == null && useGlobalSettings) {
            return settings.getStrokeColor();
        }
        return strokeColor;
    }

    public void setStrokeColor(Color strokeColor) {
        this.strokeColor = strokeColor;
        onChanged();
    }

    public Double getStrokeSize() {
        if (strokeSize == null && useGlobalSettings) {
            return settings.getStrokeSize();
        }
        return strokeSize;
    }

    public double getStrokeSizeD() {
        return get_double(getStrokeSize());
    }

    private static boolean get_boolean(Boolean aBoolean) {
        return aBoolean != null ? aBoolean : false;
    }

    private static double get_double(Double aDouble) {
        if (aDouble == null) {
            return 0;
        }
        return aDouble;
    }

    public void setStrokeSize(Double strokeSize) {
        this.strokeSize = strokeSize;
        onChanged();
    }

    public StrokeType getStrokeType() {
        if (strokeType == null && useGlobalSettings) {
            return settings.getStrokeType();
        }
        return strokeType;
    }

    public void setStrokeType(StrokeType strokeType) {
        this.strokeType = strokeType;
        onChanged();
    }

    public void setOnChangedListener(Listener listener) {
        this.onChangedListener = listener;
    }

    public void renameSettingsFile(String newValue) {
        renameSettingsFile2(newValue, false);
    }

    public void renameSettingsFile2(String newValue, boolean ignoreFileNotExists) {
        renameSettingsFile3(projectionScreenHolder.getName(), newValue, ignoreFileNotExists);
    }

    public void renameSettingsFile3(String oldFileName, String newValue, boolean ignoreFileNotExists) {
        try {
            File oldFile = new File(getFileName(oldFileName));
            if (!oldFile.exists()) {
                if (ignoreFileNotExists) {
                    return;
                }
                LOG.warn("File not exists: {}", oldFileName);
            }
            File newFile = new File(getFileName(newValue));
            boolean success = oldFile.renameTo(newFile);
            if (!success) {
                LOG.warn("Could not rename: {} to {}", oldFileName, newValue);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void copyTo(String s) {
        try {
            Path sourcePath = Paths.get(getFileName());
            Path destinationPath = Paths.get(getFileName(s));
            if (!Files.exists(destinationPath)) {
                Files.copy(sourcePath, destinationPath);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public ScreenProjectionAction getScreenProjectionAction(ProjectionType projectionType) {
        for (ScreenProjectionType screenProjectionType : getScreenProjectionTypes()) {
            if (screenProjectionType.getProjectionType().equals(projectionType)) {
                return screenProjectionType.getScreenProjectionAction();
            }
        }
        return ScreenProjectionAction.DISPLAY;
    }

    public TextAlignment getTextAlignmentT() {
        return getTextAlignmentN().asTextAlignment();
    }

    public Boolean getAsPadding() {
        if (asPadding == null && useGlobalSettings) {
            return settings.isAsPadding();
        }
        return asPadding;
    }

    public void setAsPadding(Boolean asPadding) {
        this.asPadding = asPadding;
        onChanged();
    }

    public boolean isAsPadding() {
        Boolean padding = getAsPadding();
        return padding == null || padding;
    }

    public void setProgressBarHeight(double progressBarHeightSlider) {
        this.progressBarHeight = progressBarHeightSlider;
        onChanged();
    }

    public Double getProgressBarHeight() {
        return progressBarHeight;
    }

    public double getProgressBarHeightD() {
        Double progressBarHeight = getProgressBarHeight();
        if (progressBarHeight == null) {
            return 0.08; // default value
        }
        return get_double(progressBarHeight);
    }

    public boolean isProgressBar() {
        return get_boolean(progressBar);
    }

    public void setProgressBar(Boolean progressBar) {
        this.progressBar = progressBar;
        onChanged();
    }

    public Boolean getProgressBar() {
        return progressBar;
    }

    public interface Listener {
        void onChanged();
    }

    public List<ScreenProjectionType> getScreenProjectionTypes() {
        if (screenProjectionTypes == null) {
            screenProjectionTypes = new ArrayList<>();
        }
        return screenProjectionTypes;
    }

    public Double getVerticalAlignment() {
        if (verticalAlignment == null && useGlobalSettings) {
            return settings.getVerticalAlignment();
        }
        return verticalAlignment;
    }

    public double getVerticalAlignmentD() {
        return get_double(getVerticalAlignment());
    }

    public void setVerticalAlignment(Double verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
        onChanged();
    }

    public Double getHorizontalAlignment() {
        if (horizontalAlignment == null && useGlobalSettings) {
            return settings.getHorizontalAlignment();
        }
        return horizontalAlignment;
    }

    public double getHorizontalAlignmentD() {
        return get_double(getHorizontalAlignment());
    }

    public void setHorizontalAlignment(Double horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        onChanged();
    }

    public PTextAlignment getTextAlignment() {
        if (textAlignment == null && useGlobalSettings) {
            return settings.getTextAlignment();
        }
        return textAlignment;
    }

    public PTextAlignment getTextAlignmentN() {
        PTextAlignment textAlignment = getTextAlignment();
        if (textAlignment == null) {
            return PTextAlignment.CENTER;
        }
        return textAlignment;
    }

    public void setTextAlignment(PTextAlignment textAlignment) {
        this.textAlignment = textAlignment;
        onChanged();
    }

    public Double getTopMargin() {
        if (topMargin == null && useGlobalSettings) {
            return settings.getTopMargin();
        }
        return topMargin;
    }

    public double getTopMarginD() {
        return get_double(getTopMargin());
    }

    public void setTopMargin(Double topMargin) {
        this.topMargin = topMargin;
        onChanged();
    }

    public Double getRightMargin() {
        if (rightMargin == null && useGlobalSettings) {
            return settings.getRightMargin();
        }
        return rightMargin;
    }

    public double getRightMarginD() {
        return get_double(getRightMargin());
    }

    public void setRightMargin(Double rightMargin) {
        this.rightMargin = rightMargin;
        onChanged();
    }

    public Double getBottomMargin() {
        if (bottomMargin == null && useGlobalSettings) {
            return settings.getBottomMargin();
        }
        return bottomMargin;
    }

    public double getBottomMarginD() {
        return get_double(getBottomMargin());
    }

    public void setBottomMargin(Double bottomMargin) {
        this.bottomMargin = bottomMargin;
        onChanged();
    }

    public Double getLeftMargin() {
        if (leftMargin == null && useGlobalSettings) {
            return settings.getLeftMargin();
        }
        return leftMargin;
    }

    public double getLeftMarginD() {
        return get_double(getLeftMargin());
    }

    public void setLeftMargin(Double leftMargin) {
        this.leftMargin = leftMargin;
        onChanged();
    }

    public void setFocusOnSongPart(Boolean focusOnSongPart) {
        this.focusOnSongPart = focusOnSongPart;
    }

    public boolean isFocusOnSongPart() {
        return focusOnSongPart != null && focusOnSongPart;
    }
}
