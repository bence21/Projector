package projector.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import static projector.application.ProjectionScreenSettings.getLinesFromFile;

public class AppState {
    private static final Logger LOG = LoggerFactory.getLogger(AppState.class);

    @SuppressWarnings("unused")
    private AppState() {
    }

    private AppState(boolean withLoad) {
        if (withLoad) {
            load();
        }
    }

    private static class Holder {
        private static final AppState INSTANCE = new AppState(true);
    }

    public static AppState getInstance() {
        return AppState.Holder.INSTANCE;
    }

    public void save() {
        FileOutputStream ofStream;
        try {
            ofStream = new FileOutputStream(AppProperties.getInstance().getAppStatePath());
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ofStream, StandardCharsets.UTF_8));
            Gson gson = getGson();
            String json = gson.toJson(this);
            bw.write(json);
            bw.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static Gson getGson() {
        return new GsonBuilder()
                .serializeNulls()
                .create();
    }

    private void load() {
        try {
            String s = getLinesFromFile(AppProperties.getInstance().getAppStatePath());
            Gson gson = getGson();
            AppState fromJson = gson.fromJson(s, AppState.class);
            if (fromJson == null) {
                return;
            }
            this.closed = fromJson.closed;
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private Boolean closed;

    public boolean isClosed() {
        return closed != null && closed;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }
}
