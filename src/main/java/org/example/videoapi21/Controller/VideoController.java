package org.example.videoapi21.Controller;

import org.example.videoapi21.Exception.InvalidVideoFormatException;
import org.example.videoapi21.Exception.SendVideoTaskException;
import org.example.videoapi21.Kafka.VideoProducer;
import org.example.videoapi21.Response.CustomErrorResponse;
import org.example.videoapi21.Service.VideoService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/videos")
public class VideoController {
    private static final String VIDEO_DIR = "video";
    private static final String VIDEO_HLS = "hls_output/videos";
    private final Path videoStorage = Paths.get(VIDEO_DIR);
    private final VideoService videoService;


    public VideoController(VideoProducer videoProducer, VideoService videoService) throws IOException {
        Files.createDirectories(videoStorage);
        this.videoService = videoService;
    }


    @PostMapping("/upload")
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file) throws IOException, SendVideoTaskException {
        videoService.HandleFlieUpload(file);
        return ResponseEntity.ok().body("Video uploaded");
    }
    @ExceptionHandler(value = SendVideoTaskException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CustomErrorResponse handleSendVideoTaskException(SendVideoTaskException ex){
        return new CustomErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
    }

    @ExceptionHandler(value = InvalidVideoFormatException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public CustomErrorResponse handleInvalidVideoFormatException(InvalidVideoFormatException ex){
        return new CustomErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage());
    }

    @ExceptionHandler(value = IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CustomErrorResponse handleIOException(IOException ex){
        return new CustomErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
    }


    @GetMapping("/{folder}/{filename:.+}")
    public ResponseEntity<FileSystemResource> getHlsFile(
            @PathVariable String folder,
            @PathVariable String filename) {

        File file = new File(VIDEO_HLS + "/" + folder + "/" + filename);

        System.out.println("Searching for file: " + file.getAbsolutePath());

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = filename.endsWith(".m3u8")
                ? MediaType.parseMediaType("application/vnd.apple.mpegurl")
                : MediaType.parseMediaType("video/mp2t");

        System.out.println("File found: " + file.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + filename)
                .contentType(mediaType)
                .body(new FileSystemResource(file));
    }
}
