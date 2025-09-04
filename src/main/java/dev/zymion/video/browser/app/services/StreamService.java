package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.config.properties.AppPathProperties;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;

@Service
@Slf4j
public class StreamService {

    private final Path videoFolder;

    public StreamService(AppPathProperties appPathProperties) {
        this.videoFolder = appPathProperties.getVideoFolder();
    }

    public void getStream(String relativePath, HttpServletRequest request, HttpServletResponse response) throws IOException {
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
        response.setHeader("Content-Disposition", "inline; filename=\"" + filePath.getFileName().toString() + "\"");

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
        }
    }

    public void getStreamPreview(String relativePath, HttpServletResponse response) {



        Path filePath = videoFolder.resolve(relativePath).normalize();

        if (!filePath.startsWith(videoFolder) || !Files.isRegularFile(filePath, LinkOption.NOFOLLOW_LINKS)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long fileLength;
        try {
            fileLength = Files.size(filePath);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        String contentType;
        try {
            contentType = Files.probeContentType(filePath);
        } catch (IOException e) {
            log.warn("Nie udało się wykryć typu MIME dla pliku: {}", filePath.getFileName(), e);
            contentType = "video/mp4"; // domyślny fallback
        }


        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "inline; filename=\"" + filePath.getFileName().toString() + "\"");
        response.setHeader("Accept-Ranges", "none"); // brak wsparcia dla Range
        response.setStatus(HttpServletResponse.SC_OK);

        // Szacunkowy bitrate – np. 1 MB/s (można dopracować)
        long estimatedBitrate = 1024 * 1024; // 1 MB/s
        long previewLength = 90 * estimatedBitrate;
        long end = Math.min(previewLength, fileLength);

        response.setHeader("Content-Length", String.valueOf(end));

        try (FileChannel in = FileChannel.open(filePath, StandardOpenOption.READ);
             ServletOutputStream out = response.getOutputStream()) {

            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024); // 1 MB
            long remaining = end;

            while (remaining > 0) {
                int toRead = (int) Math.min(buffer.capacity(), remaining);
                buffer.clear().limit(toRead);

                int read = in.read(buffer);
                if (read == -1) break;

                out.write(buffer.array(), 0, read);
                remaining -= read;
            }
            out.flush();
        } catch (IOException e) {
            log.error("Błąd podczas streamowania preview", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }


    }
}
