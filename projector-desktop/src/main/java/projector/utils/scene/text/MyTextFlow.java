package projector.utils.scene.text;

import com.sun.javafx.scene.text.TextLayout;
import com.sun.javafx.text.PrismTextLayout;
import com.sun.javafx.text.TextLine;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.ProjectionScreenSettings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static projector.controller.ProjectionScreenController.getBackgroundByPath;

/**
 * Adds additional API to {@link TextFlow}.
 */
@SuppressWarnings("restriction")
public class MyTextFlow extends TextFlow {

    private static final Logger LOG = LoggerFactory.getLogger(MyTextFlow.class);

    private static final Method mGetTextLayout;
    private static final Method mGetLines;

    static {
        Method mGetLineIndex;
        Method mGetCharCount;
        try {
            mGetTextLayout = TextFlow.class.getDeclaredMethod("getTextLayout");
            mGetLines = PrismTextLayout.class.getDeclaredMethod("getLines");
            mGetLineIndex = PrismTextLayout.class.getDeclaredMethod("getLineIndex", float.class);
            mGetCharCount = PrismTextLayout.class.getDeclaredMethod("getCharCount");
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
        mGetTextLayout.setAccessible(true);
        mGetLines.setAccessible(true);
        mGetLineIndex.setAccessible(true);
        mGetCharCount.setAccessible(true);
    }

    private final List<Text> texts = new ArrayList<>();
    private final List<Text> letters = new ArrayList<>();
    private final String colorStartTag = "<color=\"0x";
    private int size = 100;
    private String fontFamily;
    private FontWeight fontWeight = FontWeight.BOLD;
    private String rawText;
    private int height;
    private int width;
    private boolean tmp = false;
    private double total;
    private boolean wrapped;
    private MyTextFlow tmpTextFlow;
    private ReadOnlyObjectProperty<Bounds> boundsReadOnlyObjectProperty;
    private Text prevText = null;
    private boolean prevItalic = false;
    private Color prevColor = null;
    private String secondText;
    private ProjectionScreenSettings projectionScreenSettings;
    private boolean autoHeight = false;
    private boolean disabledStrokeFont = false;
    private Integer maxFontSize;
    private boolean withCustomFontSize = false;
    private int customFontSize = 12;

    public MyTextFlow() {
        projectionScreenSettings = new ProjectionScreenSettings();
    }

    private MyTextFlow(boolean tmp) {
        this();
        this.tmp = tmp;
        boundsReadOnlyObjectProperty = boundsInLocalProperty();
    }

    private static Object invoke(Method m, Object obj, Object... args) {
        try {
            return m.invoke(obj, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return null;
    }

    public static String getStringTextFromRawText(String rawText) {
        if (rawText == null) {
            return "";
        }
        char[] chars = rawText.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            if (chars[i] == '\\') {
                chars[i] = 0;
                ++i;
            } else if (chars[i] == '&') {
                chars[i] = '\n';
            }
        }
        rawText = String.valueOf(chars);
        return rawText;
    }

    private int getLineCount() {
        TextLine[] lines = getLines();
        if (lines == null) {
            return 0;
        }
        return lines.length;
    }

    private TextLine[] getLines() {
        TextLayout textLayout = textLayout();
        if (textLayout == null) {
            return null;
        }
        return (TextLine[]) invoke(mGetLines, textLayout);
    }

    private TextLayout textLayout() {
        return (TextLayout) invoke(mGetTextLayout, this);
    }

    public void setText2(String newText, int width, int height) {
        setText3(newText, width, height);
        setStrokeForTexts();
    }

    private void setText3(String newText, int width, int height) {
        this.width = width;
        this.height = height;
        this.rawText = newText;
        fontFamily = projectionScreenSettings.getFont();
        fontWeight = projectionScreenSettings.getFontWeight();
        newText = ampersandToNewLine(newText);
        String text2 = newText;
        setTextsByCharacters(text2);
        getChildren().clear();
        getChildren().addAll(texts);
        if (!tmp) {
            initializeTmpTextFlow();
            tmpTextFlow.setText3(newText, width, height);
            tmpTextFlow.maximizeSize(width, height);
            size = tmpTextFlow.size;
            setSize(size);
            setSize(size, letters);
            if (size > 10) {
                tmpTextFlow.getChildren().clear();
                tmpTextFlow.getChildren().addAll(letters);
                tmpTextFlow.calculateMaxSizeByLetters(height);
                size = tmpTextFlow.size;
                setSize(size);
                setSize(size, letters);
                maximizeSize(width, height);
                tmpTextFlow.getChildren().clear();
                wrapBetter();
            }
        }
    }

    private void setStrokeForTexts() {
        if (!projectionScreenSettings.isStrokeFont() || this.disabledStrokeFont) {
            return;
        }
        List<Text> texts = getNodeTexts();
        double strokeWidth = getRelativeHeightValue(height, projectionScreenSettings.getStrokeSizeD());
        Color strokeColor = projectionScreenSettings.getStrokeColor();
        StrokeType strokeType = projectionScreenSettings.getStrokeType();
        for (Text text : texts) {
            text.setStrokeWidth(strokeWidth);
            text.setStroke(strokeColor);
            text.setStrokeType(strokeType);
        }
    }

    private void wrapBetter() {
        try {
            List<Phrase> phrases = getPhrases();
            getChildren().clear();
            for (Phrase phrase : phrases) {
                wrapBetter(phrase);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void clearSpacesFromEnd(List<Phrase> lines) {
        List<Phrase> phraseList;
        int size = lines.size();
        if (size > 1) {
            if (!wrapped) {
                phraseList = lines;
            } else {
                phraseList = new ArrayList<>(size - 1);
                phraseList.addAll(lines.subList(0, size - 2));
                phraseList.add(lines.get(size - 1));
            }
        } else {
            phraseList = lines;
        }
        for (Phrase phrase : phraseList) {
            List<Word> words = phrase.getWords();
            size = words.size();
            if (size > 0) {
                Word word = words.get(size - 1);
                List<Text> letters = word.getLetters();
                int lettersSize = letters.size();
                if (lettersSize > 0) {
                    Text text = letters.get(lettersSize - 1);
                    String text1 = text.getText();
                    if (text1.equals(" ")) {
                        text.setText("\n");
                    } else {
                        if (text1.equals("\n")) {
                            if (lettersSize > 1) {
                                text = letters.get(lettersSize - 2);
                                if (text.getText().equals(" ")) {
                                    text.setText("DELETED");
                                }
                            } else {
                                if (size > 1) {
                                    word = words.get(size - 2);
                                    letters = word.getLetters();
                                    lettersSize = letters.size();
                                    if (lettersSize > 0) {
                                        text = letters.get(lettersSize - 1);
                                        text1 = text.getText();
                                        if (text1.equals(" ")) {
                                            text.setText("DELETED");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Phrase phrase : lines) {
            for (Word word : phrase.getWords()) {
                for (Text text : word.getLetters()) {
                    if (!text.getText().equals("DELETED")) {
                        getChildren().add(text);
                    }
                }
            }
        }
    }

    private void wrapBetter(Phrase phrase) {
        int width = (int) (this.getPrefWidth() + this.width) / 2;
        tmpTextFlow.resize(width, height);
        ObservableList<Node> nodes = tmpTextFlow.getChildren();
        nodes.clear();
        Phrase letters = new Phrase();
        List<Phrase> lines = new ArrayList<>();
        for (Word word : phrase.getWords()) {
            nodes.addAll(word.getClonedLetters());
            if (tmpTextFlow.getLineCount() > 1 + word.getNewLineCount()) {
                nodes.clear();
                nodes.addAll(word.getClonedLetters());
                if (!letters.getWords().isEmpty()) {
                    lines.add(letters);
                    letters = new Phrase();
                }
                letters.addWord(word);
            } else {
                letters.addWord(word);
            }
        }
        lines.add(letters);
        int size = lines.size();
        if (size > 1) {
            Phrase penultimateLine = lines.get(size - 2);
            double sum = penultimateLine.getWidth();
            Phrase lastLine = lines.get(size - 1);
            double sum2 = lastLine.getWidth();
            double average = (sum + sum2) / 2;
            total = 0d;
            double max = Math.max(sum, sum2);
            wrapped = false;
            wrap(penultimateLine, average, max);
            if (!wrapped) {
                wrap(lastLine, average, max);
                if (wrapped) {
                    LOG.error(rawText + " " + average + " " + max);
                }
            }
        }
        clearSpacesFromEnd(lines);
    }

    private void wrap(Phrase line, double average, double max) {
        Word prev = null;
        for (Word word : line.getWords()) {
            total += word.getWidth();
            if (total > max) {
                return;
            }
            if (total > average) {
                List<Text> wordLetters;
                if (total - average < Math.abs(total - average - word.getWidth()) || prev == null) {
                    wordLetters = word.getLetters();
                } else {
                    wordLetters = prev.getLetters();
                }
                Text text = wordLetters.get(wordLetters.size() - 1);
                text.setText("\n");
                wrapped = true;
                return;
            }
            prev = word;
        }
    }

    private List<Phrase> getPhrases() {
        List<Phrase> phrases = new ArrayList<>();
        Phrase phrase = new Phrase();
        Word word = new Word();
        for (Text letter : letters) {
            if (letter.getText().equals("\n")) {
                word.addLetter(letter);
                phrase.addWord(word);
                word = new Word();
                phrases.add(phrase);
                phrase = new Phrase();
            } else {
                word.addLetter(letter);
                char ch = letter.getText().charAt(0);
                if (Character.isWhitespace(ch)) {
                    phrase.addWord(word);
                    word = new Word();
                }
            }
        }
        phrase.addWord(word);
        phrases.add(phrase);
        return phrases;
    }

    private void maximizeSize(int trueWidth, int height) {
        double lineSpace = projectionScreenSettings.getLineSpace();
        setLineSpacing(lineSpace);
        double aDouble = 1.0;
        int width = (int) (trueWidth * aDouble);
        setLayoutY(0);
        if (tmp) {
            resize(width, 20);
            calculateMaxSize(trueWidth, height);
        } else {
            setLayoutX(0);
            setPrefWidth(width);
            setPrefHeight(height);
            int w;
            int h;
            Bounds bounds = tmpTextFlow.boundsInLocalProperty().getValue();
            w = (int) bounds.getWidth();
            h = (int) bounds.getHeight();
            resize(width, height);
            setLayoutX((double) (trueWidth - w) / 2);
            alignY(height, h);
            setPrefHeight2(h);
        }
    }

    private void alignY(double textFlowHeight, double contentHeight) {
        double verticalAlignment = projectionScreenSettings.getVerticalAlignmentD();
        if (contentHeight > textFlowHeight) {
            return;
        }
        double shift = textFlowHeight - contentHeight;
        double y = shift * verticalAlignment;
        setLayoutY(y);
    }

    private void setPrefHeight2(int height) {
        setPrefHeight(height);
        if (isAutoHeight()) {
            setBackGroundImage(height);
        }
    }

    private void setTextsByCharacters(String text) {
        texts.clear();
        letters.clear();
        prevColor = null;
        boolean italic = false;
        Stack<Color> colors = new Stack<>();
        colors.push(projectionScreenSettings.getColor());
        int[] codePoints = text.codePoints().toArray();
        int length = codePoints.length;
        int offset = 0;
        for (int i = 0; i < length; ++i) {
            int codePoint = codePoints[i];
            StringBuilder stringBuilder = new StringBuilder().appendCodePoint(codePoint);
            String s = stringBuilder.toString();
            String colorEndTag = "</color>";
            if (s.equals("[")) {
                italic = true;
            } else if (italic && s.equals("]")) {
                italic = false;
            } else if (isaColorStartTag(text, offset, s)) {
                try {
                    int beginIndex = offset + colorStartTag.length();
                    String color = "0x" + text.substring(beginIndex, beginIndex + 8);
                    colors.push(Color.web(color));
                    int shift = colorStartTag.length() + 9;
                    i += shift;
                    offset += shift;
                } catch (IllegalArgumentException ignored) {
                    addCharacter(italic, s, colors.peek());
                }
            } else if (s.equals("<") && text.startsWith(colorEndTag, offset)) {
                if (colors.size() > 1) {
                    colors.pop();
                }
                int shift = colorEndTag.length() - 1;
                i += shift;
                offset += shift;
            } else {
                addCharacter(italic, s, colors.peek());
            }
            offset += s.length();
        }
    }

    private void addCharacter(boolean italic, String s, Color color) {
        letters.add(getText(italic, s, color));
        if (prevItalic == italic && color.equals(prevColor)) {
            prevText.setText(prevText.getText() + s);
            return;
        }
        Text e = getText(italic, s, color);
        texts.add(e);
        prevItalic = italic;
        prevColor = color;
        prevText = e;
    }

    private Text getText(boolean italic, String s, Color color) {
        Text e = new Text(s);
        FontPosture fontPosture;
        if (italic) {
            fontPosture = FontPosture.ITALIC;
        } else {
            fontPosture = FontPosture.REGULAR;
        }
        e.setFont(Font.font(fontFamily, fontWeight, fontPosture, Font.getDefault().getSize()));
        e.setFill(color);
        return e;
    }

    private boolean isaColorStartTag(String text, int i, String s) {
        try {
            return s.equals("<") && text.startsWith(colorStartTag, i);
        } catch (StringIndexOutOfBoundsException e) {
            return false;
        }
    }

    private List<Text> getNodeTexts() {
        ObservableList<Node> nodes = getChildren();
        List<Text> texts = new ArrayList<>(nodes.size());
        for (Node node : nodes) {
            if (node instanceof Text) {
                texts.add((Text) node);
            }
        }
        return texts;
    }

    public void setColor(Color value) {
        List<Text> texts = getNodeTexts();
        for (Text text : texts) {
            text.setFill(value);
        }
    }

    private void calculateMaxSize(int trueWidth, int height) {
        int w, h;
        int maxFont = getMaxFontSize();
        size = (int) getRelativeHeightValue(height, maxFont);
        int size2 = (int) getRelativeWidthValue(trueWidth, maxFont);
        if (size2 > size) {
            size = size2;
        }
        size = Math.min(size, getMaxFont());
        int prefMinSize = (int) (size * (((double) projectionScreenSettings.getBreakAfter()) / 100));
        int a = 2, b = size;
        boolean b2 = !projectionScreenSettings.isBreakLines();
        if (b2) {
            int prefLineCount = getLineCount();
            do {
                setSize(size);
                if (trueWidth == 0) {
                    return;
                }
                Bounds value = boundsReadOnlyObjectProperty.getValue();
                w = (int) value.getWidth();
                h = (int) value.getHeight();
                if (size > 1) {
                    double i = w;
                    i /= trueWidth;
                    int lineCount = getLineCount();
                    boolean b1 = lineCount > prefLineCount;
                    if (i > 1.03 || h > height || b1) {
                        if (a < b || b1) {
                            b = size - 1;
                            size = (a + b) / 2;
                        } else {
                            decreaseSizeWhileNotGood(trueWidth, height);
                            break;
                        }
                    } else {
                        if (a < b) {
                            a = size + 1;
                            size = (a + b) / 2;
                        } else {
                            break;
                        }
                    }
                } else {
                    break;
                }
            } while (true);
        }
        if (size < prefMinSize || !b2) {
            size = (int) getRelativeHeightValue(height, maxFont);
            if (size2 > size) {
                size = size2;
            }
            a = 2;
            b = size;
            do {
                setSize(size);
                Bounds value = boundsReadOnlyObjectProperty.getValue();
                w = (int) value.getWidth();
                h = (int) value.getHeight();
                if (size > 1) {
                    double i = w;
                    i /= trueWidth;
                    if (i > 1.03 || h > height) {
                        if (a < b) {
                            b = size - 1;
                            size = (a + b) / 2;
                        } else {
                            decreaseSizeWhileNotGood(trueWidth, height);
                            break;
                        }
                    } else {
                        if (a < b) {
                            a = size + 1;
                            size = (a + b) / 2;
                        } else {
                            break;
                        }
                    }
                } else {
                    break;
                }
            } while (true);
        }
    }

    private Integer getMaxFontSize() {
        if (!withCustomFontSize) {
            return projectionScreenSettings.getMaxFont();
        } else {
            return customFontSize;
        }
    }

    @SuppressWarnings("unused")
    public void setCustomFontSize(int customFontSize) {
        this.customFontSize = customFontSize;
        this.withCustomFontSize = true;
    }

    private int getMaxFont() {
        if (maxFontSize != null) {
            return maxFontSize;
        }
        return Integer.MAX_VALUE;
    }

    private static double getRelativeWidthValue(double width, double v) {
        double designWidth = 1366;
        return v * (width / designWidth);
    }

    private static double getRelativeHeightValue(double height, double v) {
        double designHeight = 768;
        return v * (height / designHeight);
    }

    private void decreaseSizeWhileNotGood(int trueWidth, int height) {
        Bounds value;
        int w;
        int h;
        double i;
        do {
            setSize(--size);
            value = boundsReadOnlyObjectProperty.getValue();
            w = (int) value.getWidth();
            h = (int) value.getHeight();
            i = w;
            i /= trueWidth;
        } while ((i > 1.01 || h > height) && size > 1);
    }

    private void calculateMaxSizeByLetters(int height) {
        int h;
        Bounds value = boundsReadOnlyObjectProperty.getValue();
        h = (int) value.getHeight();
        while ((h > height) && size > 1) {
            setSize(--size);
            value = boundsReadOnlyObjectProperty.getValue();
            h = (int) value.getHeight();
        }
    }

    private String ampersandToNewLine(String newText) {
        newText = getStringTextFromRawText(newText);
        return newText;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public void setBackGroundColor() {
        if (!projectionScreenSettings.isBackgroundImage()) {
            BackgroundFill myBF = new BackgroundFill(projectionScreenSettings.getBackgroundColor(), new CornerRadii(1), new Insets(0.0, 0.0, 0.0, 0.0));
            // then you set to your node
            super.setBackground(new Background(myBF));
        } else {
            setBackGroundImage(height);
        }
    }

    private void setBackGroundImage(int height) {
        if (projectionScreenSettings.isBackgroundImage()) {
            Background background = getBackgroundByPath(projectionScreenSettings.getBackgroundImagePath(), width, height);
            if (background != null) {
                super.setBackground(background);
            }
        }
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        setText2(rawText, width, height);
    }

    public int getSize() {
        return size;
    }

    private void setSize(int size) {
        this.size = size;
        ObservableList<Node> nodes = getChildren();
        for (Node node : nodes) {
            Text text = (Text) node;
            if (text.getFont().getStyle().contains("Italic")) {
                text.setFont(Font.font(text.getFont().getFamily(), fontWeight, FontPosture.ITALIC, size));
            } else {
                text.setFont(Font.font(fontFamily, fontWeight, FontPosture.REGULAR, size));
            }
        }
    }

    private void setSize(int size, List<Text> texts) {
        this.size = size;
        for (Text text : texts) {
            if (text.getFont().getStyle().contains("Italic")) {
                text.setFont(Font.font(text.getFont().getFamily(), fontWeight, FontPosture.ITALIC, size));
            } else {
                text.setFont(Font.font(fontFamily, fontWeight, FontPosture.REGULAR, size));
            }
        }
    }

    public void setSizeAndAlign(int size) {
        setSize(size);
        align();
    }

    private void align() {
        int w;
        int h;
        resize(width, 20);
        Bounds bounds = boundsInLocalProperty().getValue();
        w = (int) bounds.getWidth();
        h = (int) bounds.getHeight();
        resize(width, height);
        setLayoutX((double) (width - w) / 2);
        alignY(height, h);
    }

    private void initializeTmpTextFlow() {
        if (tmpTextFlow == null) {
            tmpTextFlow = new MyTextFlow(true);
            tmpTextFlow.setProjectionScreenSettings(projectionScreenSettings);
            tmpTextFlow.withCustomFontSize = this.withCustomFontSize;
            tmpTextFlow.customFontSize = this.customFontSize;
        }
    }

    public MyTextFlow getTmpTextFlow() {
        initializeTmpTextFlow();
        return tmpTextFlow;
    }

    public String getSecondText() {
        return secondText;
    }

    public void setSecondText(String secondText) {
        this.secondText = secondText;
    }

    public void setProjectionScreenSettings(ProjectionScreenSettings projectionScreenSettings) {
        this.projectionScreenSettings = projectionScreenSettings;
        if (tmpTextFlow != null) {
            tmpTextFlow.setProjectionScreenSettings(projectionScreenSettings);
        }
    }

    private boolean isAutoHeight() {
        return autoHeight;
    }

    public void setAutoHeight(boolean autoHeight) {
        this.autoHeight = autoHeight;
    }

    public void disableStrokeFont() {
        // We are disabling stroke font because it is slow
        this.disabledStrokeFont = true;
    }

    public double getMaxLineWidth() {
        double maxLineWidth = 0.0;
        for (Phrase phrase : getPhrases()) {
            double lineWidth = phrase.getWidth();
            if (lineWidth > maxLineWidth) {
                maxLineWidth = lineWidth;
            }
        }
        return maxLineWidth;
    }

    public void setMaxFontSize(Integer maxFontSize) {
        this.maxFontSize = maxFontSize;
        if (!tmp) {
            getTmpTextFlow().setMaxFontSize(maxFontSize);
        }
    }
}