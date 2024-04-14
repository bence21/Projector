package projector.application;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.utils.AppProperties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import static projector.controller.util.FileUtil.getGson;

public class ApplicationVersion {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationVersion.class);
    private static ApplicationVersion instance;
    private final String filePath = AppProperties.getInstance().getWorkDirectory() + "application.version";
    @Expose
    private int version;
    private boolean testing;

    private ApplicationVersion() {
    }

    public static ApplicationVersion getInstance() {
        if (instance == null) {
            instance = new ApplicationVersion();
            instance.load();
        }
        return instance;
    }

    private void load() {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8));
            StringBuilder s = new StringBuilder();
            String readLine = bufferedReader.readLine();
            while (readLine != null) {
                s.append(readLine);
                readLine = bufferedReader.readLine();
            }
            bufferedReader.close();
            Gson gson = getGson();
            ApplicationVersion fromJson = gson.fromJson(s.toString(), ApplicationVersion.class);
            if (fromJson == null) {
                return;
            }
            this.version = fromJson.version;
        } catch (Exception e) {
            LOG.error(e.getMessage() + "\n" + filePath, e);
        }
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void save() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
            Gson gson = getGson();
            String json = gson.toJson(this);
            bufferedWriter.write(json);
            bufferedWriter.close();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public boolean isNotTesting() {
        return !isTesting();
    }

    public boolean isTesting() {
        return testing;
    }

    public void setTesting(boolean testing) {
        this.testing = testing;
    }
}
