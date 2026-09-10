package com.bence.projector.server.api.resources;

import com.bence.projector.server.api.resources.util.MediaTypeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

@RestController
public class FileResource {
    static final Pattern ALLOWED_RELEASE_FILE =
            Pattern.compile("^(projector-setup\\.exe|projectorUpdate\\d+\\.zip)$");
    static final String PUBLIC_FOLDER = "aPublic_folder";

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
        return getInputStreamResourceResponseEntity(PUBLIC_FOLDER + "/" + fileName, servletContext);
    }

    @PostMapping(value = "/deployer/api/projectorReleaseFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadReleaseFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("file is required");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            return ResponseEntity.badRequest().body("file name is required");
        }

        String fileName = Paths.get(originalName).getFileName().toString();
        if (!ALLOWED_RELEASE_FILE.matcher(fileName).matches()) {
            return ResponseEntity.badRequest().body("invalid file name: " + fileName);
        }

        Path targetDir = Paths.get(PUBLIC_FOLDER).toAbsolutePath().normalize();
        Path targetFile = targetDir.resolve(fileName).normalize();
        if (!targetFile.startsWith(targetDir)) {
            return ResponseEntity.badRequest().body("invalid file path");
        }

        Files.createDirectories(targetDir);
        Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        return ResponseEntity.ok("uploaded " + fileName);
    }
}
