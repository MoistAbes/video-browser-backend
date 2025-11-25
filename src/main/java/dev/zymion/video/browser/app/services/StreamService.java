package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.config.properties.AppPathProperties;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.Normalizer;

@Service
@Slf4j
public class StreamService {

    private final Path videoFolder;

    public StreamService(AppPathProperties appPathProperties) {
        this.videoFolder = appPathProperties.getVideoFolder();
    }

    public void getStream(String relativePath, HttpServletRequest request, HttpServletResponse response, Long maxBytes) throws IOException {
        Path filePath = videoFolder.resolve(relativePath).normalize();

        if (!filePath.startsWith(videoFolder) || !Files.isRegularFile(filePath, LinkOption.NOFOLLOW_LINKS)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long fileLength = Files.size(filePath);
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "video/mp4";
        }
        String range = request.getHeader("Range");

        // ETag / Last-Modified
        long lastModified = Files.getLastModifiedTime(filePath, LinkOption.NOFOLLOW_LINKS).toMillis();
        String etag = "\"" + fileLength + "-" + lastModified + "\"";
        response.setHeader("ETag", etag);
        response.setDateHeader("Last-Modified", lastModified);

        // Conditional GET
        if (etag.equals(request.getHeader("If-None-Match")) ||
                (request.getDateHeader("If-Modified-Since") >= 0 &&
                        lastModified / 1000 <= request.getDateHeader("If-Modified-Since") / 1000)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        long start = 0, end = fileLength - 1;
        boolean isPartial = false;

        if (range != null && range.startsWith("bytes=")) {
            isPartial = true;
            String spec = range.substring(6).trim(); // e.g. "0-","100-200","-500"
            if (spec.contains(",")) {
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                response.setHeader("Content-Range", "bytes */" + fileLength);
                return;
            }
            String[] parts = spec.split("-", 2);
            try {
                if (parts[0].isEmpty()) {
                    long suffix = Long.parseLong(parts[1]);
                    if (suffix <= 0) throw new NumberFormatException();
                    start = Math.max(fileLength - suffix, 0);
                } else {
                    start = Long.parseLong(parts[0]);
                    if (parts.length > 1 && !parts[1].isEmpty()) {
                        end = Long.parseLong(parts[1]);
                    }
                }
            } catch (NumberFormatException ex) {
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                response.setHeader("Content-Range", "bytes */" + fileLength);
                return;
            }

            if (start > end || start >= fileLength) {
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                response.setHeader("Content-Range", "bytes */" + fileLength);
                return;
            }
            end = Math.min(end, fileLength - 1);
        }

        // jeśli przekazano limit (np. preview) → ograniczamy end
        if (maxBytes != null) {
            end = Math.min(end, maxBytes - 1);
        }

        long contentLength = end - start + 1;

        // If-Range
        String ifRange = request.getHeader("If-Range");
        if (isPartial && ifRange != null && !ifRange.equals(etag)) {
            isPartial = false;
            start = 0;
            end = fileLength - 1;
            contentLength = fileLength;
        }

        response.setContentType(contentType);
        response.setHeader("Accept-Ranges", "bytes");


        //ta czesc jest zeby tomcat nie mial problemow z polskimi znakami przy response (nie jest to wymagane ale warn lecial)
        String filename = filePath.getFileName().toString();

        // ASCII fallback — Tomcat/Nginx go wymagają
        String asciiName = Normalizer.normalize(filename, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")               // usuwa diakrytyki (ł → l)
                .replaceAll("[^\\x20-\\x7E]", "_");     // inne Unicode → _

        // UTF-8 zgodnie z RFC 5987
        String utf8Name = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");

        response.setHeader("Content-Disposition",
                "inline; filename=\"" + asciiName + "\"; filename*=UTF-8''" + utf8Name);



        if (isPartial) {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
            response.setHeader("Content-Length", String.valueOf(contentLength));
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setHeader("Content-Length", String.valueOf(fileLength));
        }

        if ("HEAD".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        try (FileChannel in = FileChannel.open(filePath, StandardOpenOption.READ);
             ServletOutputStream out = response.getOutputStream()) {

            in.position(start);
            long remaining = contentLength;

            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024); // 1 MB
            while (remaining > 0) {
                int toRead = (int) Math.min(buffer.capacity(), remaining);
                buffer.clear().limit(toRead);

                int read = in.read(buffer);
                if (read == -1) break;

                out.write(buffer.array(), 0, read);
                remaining -= read;
            }
            out.flush();
        } catch (IOException ex) {
            if (ex instanceof org.apache.catalina.connector.ClientAbortException) {
                // klient przerwał połączenie - ignorujemy albo logujemy jako WARN/DEBUG
                log.warn("Klient przerwał połączenie przy streamowaniu: {}", relativePath);
            } else {
                log.error("Błąd podczas streamowania wideo: {}", ex.getMessage());
            }
        }

    }
}
