package projector.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.Settings;
import projector.model.CustomCanvas;
import projector.utils.AppProperties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CustomCanvasService {

    private static final Logger LOG = LoggerFactory.getLogger(CustomCanvasService.class);
    private static CustomCanvasService instance;
    private ArrayList<CustomCanvas> customCanvases;
    private final Settings settings = Settings.getInstance();

    private CustomCanvasService() {
        load();
    }

    public static CustomCanvasService getInstance() {
        if (instance == null) {
            instance = new CustomCanvasService();
        }
        return instance;
    }

    public void save() {
        FileOutputStream ofStream;
        try {
            ofStream = new FileOutputStream(getFileName());
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ofStream, StandardCharsets.UTF_8));
            Gson gson = getGson();
            Type listType = getListType();
            String json = gson.toJson(customCanvases, listType);
            bw.write(json);
            bw.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static Gson getGson() {
        return new GsonBuilder()
                .serializeNulls()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }

    private void load() {
        try {
            String textFromFile = getTextFromFile(getFileName());
            Gson gson = getGson();
            Type listType = getListType();
            customCanvases = gson.fromJson(textFromFile, listType);
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        if (customCanvases == null) {
            customCanvases = new ArrayList<>();
        }
        if (customCanvases.isEmpty()) {
            if (settings.isCustomCanvasLoadOnStart()) {
                customCanvases.add(getCustomCanvasFromOldSettings());
            }
        }
    }

    public CustomCanvas getCustomCanvasFromOldSettings() {
        CustomCanvas customCanvas = new CustomCanvas();
        Settings settings = Settings.getInstance();
        customCanvas.setWidth((double) settings.getCustomCanvasWidth());
        customCanvas.setHeight((double) settings.getCustomCanvasHeight());
        customCanvas.setName("Custom Canvas");
        customCanvas.setPositionX(0.0);
        customCanvas.setPositionY(0.0);
        return customCanvas;
    }

    private String getTextFromFile(String fileName) throws IOException {
        FileInputStream inputStream;
        inputStream = new FileInputStream(fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder stringBuilder = new StringBuilder();
        String readLine = br.readLine();
        while (readLine != null) {
            stringBuilder.append(readLine);
            readLine = br.readLine();
        }
        br.close();
        return stringBuilder.toString();
    }

    private static Type getListType() {
        return new TypeToken<ArrayList<CustomCanvas>>() {
        }.getType();
    }

    private String getFileName() {
        return AppProperties.getInstance().getWorkDirectory() + "/" + "canvases.json";
    }

    public ArrayList<CustomCanvas> getCustomCanvases() {
        return customCanvases;
    }
}
