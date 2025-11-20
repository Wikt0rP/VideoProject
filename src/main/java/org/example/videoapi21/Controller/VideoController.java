package org.example.videoapi21.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.videoapi21.Entity.Video;
import org.example.videoapi21.Repository.VideoRepository;
import org.example.videoapi21.Request.CreateVidoeEntityRequest;
import org.example.videoapi21.Response.VideoCreateResponse;
import org.example.videoapi21.Service.VideoService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/videos")
public class VideoController {
    private static final String VIDEO_DIR = "video";
    private static final String VIDEO_HLS = "hls_output/videos";
    private final Path videoStorage = Paths.get(VIDEO_DIR);

    private final VideoService videoService;
    private final VideoRepository videoRepository;

    public VideoController(VideoService videoService, VideoRepository videoRepository) throws IOException {
        Files.createDirectories(videoStorage);
        this.videoService = videoService;
        this.videoRepository = videoRepository;
    }


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoCreateResponse> uploadVideo(
            @RequestPart("video") MultipartFile videoFile,
            @RequestPart("thumbnail") MultipartFile thumbnailFile,
            @RequestPart("title") String title,
            @RequestPart("description") String description,
            HttpServletRequest request) throws Exception {

        CreateVidoeEntityRequest createVidoeEntityRequest = new CreateVidoeEntityRequest(title, description);
        return videoService.createVideoWithFilesUpload(videoFile, thumbnailFile, createVidoeEntityRequest, request);
    }


    @GetMapping("/test")
    public ResponseEntity<?> getAll(HttpServletRequest request){
        List<Video> videoList = videoRepository.findAll();
        return ResponseEntity.ok().body(videoList);
    }


    @GetMapping("/{uuid}")
    public ResponseEntity<FileSystemResource> getHlsFile(@PathVariable String uuid) {
        return videoService.getVideoFromUUID(uuid);
    }
}
