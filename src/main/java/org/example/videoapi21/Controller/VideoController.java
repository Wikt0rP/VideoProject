package org.example.videoapi21.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.example.videoapi21.Entity.Video;
import org.example.videoapi21.Exception.UserNotFoundException;
import org.example.videoapi21.Repository.VideoRepository;
import org.example.videoapi21.Request.CreateVidoeEntityRequest;
import org.example.videoapi21.Response.ThumbnailUpdateResponse;
import org.example.videoapi21.Response.VideoCreateResponse;
import org.example.videoapi21.Service.VideoService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/videos")
public class VideoController {
    private static final String VIDEO_DIR = "video";
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

    @PatchMapping(value = "/{uuid}/update/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ThumbnailUpdateResponse> updateVideoThumbnail(@PathVariable String uuid, @RequestPart("thumbnail") MultipartFile thumbnailFile, HttpServletRequest request)
            throws ResourceNotFoundException, AccessDeniedException, UserNotFoundException {
        UUID videoUUID = UUID.fromString(uuid);
        return videoService.handleThumbnailUpdate(thumbnailFile, videoUUID, request);
    }

    @GetMapping("/test")
    public ResponseEntity<?> getAll(HttpServletRequest request) {
        List<Video> videoList = videoRepository.findAll();
        return ResponseEntity.ok().body(videoList);
    }

    @GetMapping("/{uuid}/playlist.m3u8")
    public ResponseEntity<FileSystemResource> getPlaylist(@PathVariable String uuid) {
        UUID videoUUID = UUID.fromString(uuid);
        return videoService.getVideoFromUUID(videoUUID);
    }

    @GetMapping("/{uuid}/{segment}.ts")
    public ResponseEntity<FileSystemResource> getSegment(
            @PathVariable String uuid,
            @PathVariable String segment) {
        UUID videoUUID = UUID.fromString(uuid);
        return videoService.getSegmentFile(videoUUID, segment);
    }

    @GetMapping("/recent")
    public ResponseEntity<Page<Video>> getRecentVideos(Pageable pageable) {
        return videoService.getRecentVideos(pageable);
    }

    @GetMapping("/{uuid}/thumbnail")
    public ResponseEntity<FileSystemResource> getThumbnail(@PathVariable String uuid){
        UUID videoUUID = UUID.fromString(uuid);
        return videoService.getThumbnailFromUUID(videoUUID);
    }

    @GetMapping("/myVideos")
    public ResponseEntity<Page<Video>> getMyVideos(Pageable pageable, HttpServletRequest request) throws UserNotFoundException{
        return videoService.getMyVideos(pageable, request);
    }

}
