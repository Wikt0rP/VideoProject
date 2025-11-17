package org.example.videoapi21.Controller;

import org.example.videoapi21.Exception.InvalidVideoFormatException;
import org.example.videoapi21.Exception.SendVideoTaskException;
import org.example.videoapi21.Request.CreateVidoeEntityRequest;
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

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/videos")
public class VideoController {
    private static final String VIDEO_DIR = "video";
    private static final String VIDEO_HLS = "hls_output/videos";
    private final Path videoStorage = Paths.get(VIDEO_DIR);
    private final VideoService videoService;


    public VideoController(VideoService videoService) throws IOException {
        Files.createDirectories(videoStorage);
        this.videoService = videoService;
    }


    @PostMapping("/upload")
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file, @RequestBody CreateVidoeEntityRequest request) throws IOException, SendVideoTaskException, InvalidVideoFormatException {
        videoService.handleFileUpload(file, request);
        return ResponseEntity.ok().body("Video uploaded");
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
