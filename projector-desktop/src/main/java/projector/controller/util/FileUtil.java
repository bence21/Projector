package projector.controller.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FileUtil {
    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    public static String getLinesFromFile(String file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder s = new StringBuilder();
            String readLine = br.readLine();
            while (readLine != null) {
                s.append(readLine);
                readLine = br.readLine();
            }
            br.close();
            return s.toString();
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .serializeNulls()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }
}
