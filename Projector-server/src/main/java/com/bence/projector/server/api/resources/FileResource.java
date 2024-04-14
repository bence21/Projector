package com.bence.projector.server.api.resources;

import com.bence.projector.server.api.resources.util.MediaTypeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
public class FileResource {
    @Autowired
    private ServletContext servletContext;

    public static String doubleQuote(String encode) {
        return "\"" + encode.replaceAll("\"", "\\\\\"") + "\"";
    }

    public static ResponseEntity<InputStreamResource> getInputStreamResourceResponseEntity(String filePath, ServletContext servletContext) {
        try {
            String fileName = getFileName(filePath);
            MediaType mediaType = MediaTypeUtils.getMediaTypeForFileName(servletContext, fileName);
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(bytes));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "filename=" + doubleQuote(fileName))
                    .contentType(mediaType)
                    .contentLength(bytes.length)
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return onBadFile();
        }
    }

    private static String getFileName(String filePath) {
        String[] split = filePath.split("/");
        if (split.length > 0) {
            return split[split.length - 1];
        }
        return filePath;
    }

    public static ResponseEntity<InputStreamResource> onBadFile() {
        return ResponseEntity.badRequest()
                .body(new InputStreamResource(new ByteArrayInputStream("[]".getBytes())));
    }

    @RequestMapping(value = "/api/files/{pFileName}")
    public ResponseEntity<InputStreamResource> file(@PathVariable String pFileName) {
        String fileName = getFileName(pFileName); // fileName because we don't want other directories to be readable
        return getInputStreamResourceResponseEntity("aPublic_folder/" + fileName, servletContext);
    }
}
