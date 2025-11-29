package org.example.videoapi21.Service;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.example.videoapi21.Component.UserComponent;
import org.example.videoapi21.Entity.AppUser;
import org.example.videoapi21.Entity.Video;
import org.example.videoapi21.Exception.*;
import org.example.videoapi21.Exception.UserNotFoundException;
import org.example.videoapi21.Kafka.VideoProducer;
import org.example.videoapi21.Repository.VideoRepository;
import org.example.videoapi21.Request.CreateVidoeEntityRequest;
import org.example.videoapi21.Request.VideoUpdateRequest;
import org.example.videoapi21.Response.DeleteVideoResponse;
import org.example.videoapi21.Response.ThumbnailUpdateResponse;
import org.example.videoapi21.Response.VideoCreateResponse;
import org.example.videoapi21.Response.VideoUpdateDataResponse;
import org.hibernate.sql.Delete;
import org.springframework.core.io.FileSystemResource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

@Service
public class VideoService {
    private static final String VIDEO_DIR = "video";
    private static final String thumbnailPath = "thumbnails/";
    private final String ffmpegPath = "C:\\ffmpeg-2025-11-06-git-222127418b-full_build\\bin\\ffmpeg.exe";

    private final Path videoStorage = Paths.get(VIDEO_DIR);
    private final VideoProducer videoProducer;
    private final VideoRepository videoRepository;
    private final UserComponent userComponent;

    public VideoService(VideoProducer videoProducer, VideoRepository videoRepository, UserComponent userComponent) {
        this.videoProducer = videoProducer;
        this.videoRepository = videoRepository;
        this.userComponent = userComponent;
    }

    public ResponseEntity<VideoCreateResponse> createVideoWithFilesUpload(MultipartFile fileVideo,
            MultipartFile fileThumbnail, CreateVidoeEntityRequest createVidoeEntityRequest, HttpServletRequest request)
            throws IOException, SendVideoTaskException, InvalidVideoFormatException, UserNotFoundException {

        AppUser appUser = userComponent.getUserFromRequest(request);
        Video video = new Video(createVidoeEntityRequest.title(), createVidoeEntityRequest.description(), appUser);
        videoRepository.save(video);

        handleVideoUpload(fileVideo, video.getId());
        handleThumbnailUpload(fileThumbnail, video.getId());

        return ResponseEntity.ok(
                new VideoCreateResponse(video.getTitle(), video.getDescription()));
    }

    public ResponseEntity<ThumbnailUpdateResponse> handleThumbnailUpdate(MultipartFile fileImg, UUID videoUUID, HttpServletRequest request)
            throws ResourceNotFoundException, AccessDeniedException, UserNotFoundException {
        Video video = getVideoFromUUIDIfUserIsAuthor(request, videoUUID);

        video.setThumbnailPath("");
        try{
            Files.deleteIfExists(Paths.get(video.getThumbnailPath()));
        } catch (IOException e) {
            //Continue TODO: log?
        }

        handleThumbnailUpload(fileImg, video.getId());
        return ResponseEntity.ok(new ThumbnailUpdateResponse(video.getTitle(), videoUUID));
    }

    public void handleThumbnailUpload(MultipartFile file, Long videoId)
            throws InvalidImageFormatException, CouldNotSaveFileException {
        BufferedImage image = GetBufferedImage(file);
        String thumbnailPath = saveAsJpg(image);
        setThumbnailPath(videoId, thumbnailPath);
    }

    public String convertToHls(String inputPath) {
        File source = new File(inputPath);
        if (!source.exists()) {
            System.err.println("Source file does not exist: " + inputPath);
            return "";
        }

        String filename = source.getName();
        String baseName = filename.substring(0, filename.lastIndexOf('.'));

        File outputDir = new File("hls_output/videos/" + baseName);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String playlistPath = outputDir.getAbsolutePath() + File.separator + "playlist.m3u8";
        String[] command = getFfmpegString(outputDir, source, playlistPath);

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return playlistPath;
            } else {
                System.err.println("ffmpeg finished with error code: " + exitCode);
                return "";
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void setVideoPath(Long videoID, String vidoPath) throws UnableToSetResourcePath {
        Optional<Video> video = videoRepository.findVideoById(videoID);
        if (video.isPresent()) {
            Video videoEntity = video.get();
            videoEntity.setVideoPath(vidoPath);
            videoRepository.save(videoEntity);
        } else {
            throw new UnableToSetResourcePath("Video could not be found by id, path to resources not set");
        }
    }

    public ResponseEntity<FileSystemResource> getVideoFromUUID(UUID uuid)
            throws VideoNotFoundException {
        Video video = videoRepository.findVideoByUuid(uuid)
                .orElseThrow(() -> new VideoNotFoundException("Video could not be found by uuid"));

        FileSystemResource videoResource = new FileSystemResource(video.getVideoPath());
        if (!videoResource.exists() || !videoResource.isReadable()) {
            throw new ResourceNotFoundException("Video path might not exist");
        }
        return ResponseEntity.ok(videoResource);
    }

    public ResponseEntity<FileSystemResource> getThumbnailFromUUID(UUID uuid){
        Video video = videoRepository.findVideoByUuid(uuid)
                .orElseThrow(() -> new VideoNotFoundException("Video could not be found by uuid"));
        FileSystemResource thumbnailFile = new FileSystemResource(video.getThumbnailPath());
        if(!thumbnailFile.exists() || !thumbnailFile.isReadable()){
            throw new ResourceNotFoundException("Thumbnail path might not exist");
        }
        return ResponseEntity.ok(thumbnailFile);
    }

    public ResponseEntity<FileSystemResource> getSegmentFile(UUID uuid, String segmentName) {
        Video video = videoRepository.findVideoByUuid(uuid)
                .orElseThrow(() -> new VideoNotFoundException("Video not found"));

        Path playlistPath = Paths.get(video.getVideoPath());
        Path videoDir = playlistPath.getParent();
        Path segmentPath = videoDir.resolve(segmentName + ".ts");

        FileSystemResource resource = new FileSystemResource(segmentPath);
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/MP2T"))
                .body(resource);
    }

    public ResponseEntity<Page<Video>> getRecentVideos(Pageable pageable) {
        return ResponseEntity.ok(videoRepository.findAllWithValidPath(pageable));
    }

    public ResponseEntity<Page<Video>> getMyVideos(Pageable pageable, HttpServletRequest request) throws UserNotFoundException{
        AppUser appUser = userComponent.getUserFromRequest(request);
        return ResponseEntity.ok(videoRepository.findVideosByAuthor(appUser, pageable));
    }

    public ResponseEntity<?> updateVideoData(UUID uuid, VideoUpdateRequest videoUpdateRequest, HttpServletRequest request)
            throws ResourceNotFoundException, AccessDeniedException{
        Video video = getVideoFromUUIDIfUserIsAuthor(request, uuid);

        videoUpdateRequest.description().ifPresent(video::setDescription);
        videoUpdateRequest.title().ifPresent(video::setTitle);
        videoRepository.save(video);

        return ResponseEntity.ok().body(new VideoUpdateDataResponse(uuid.toString(), video.getTitle(), video.getDescription()));
    }

    public ResponseEntity<DeleteVideoResponse> deleteVideoByUUID(UUID videoUUID, HttpServletRequest request) throws ResourceNotFoundException,
            AccessDeniedException, IOException {
        Video video = getVideoFromUUIDIfUserIsAuthor(request, videoUUID);

        try {
            Files.deleteIfExists(Paths.get(video.getThumbnailPath()));
            Path videoDirectory = Paths.get(video.getVideoPath()).getParent();
            Files.walk(videoDirectory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }catch (IOException ex){
            //Continue TODO: log?
        }
        String title = video.getTitle();
        videoRepository.delete(video);
        return  ResponseEntity.ok().body(new DeleteVideoResponse(videoUUID.toString(), title));
    }


    private Video getVideoFromUUIDIfUserIsAuthor(HttpServletRequest request, UUID videoUpdateRequest) throws ResourceNotFoundException, AccessDeniedException{
        AppUser appUser = userComponent.getUserFromRequest(request);
        Video video = videoRepository.findVideoByUuid(videoUpdateRequest)
                .orElseThrow(() -> new ResourceNotFoundException("Video Not Found"));

        if (!appUser.getId().equals(video.getAuthor().getId())) {
            throw new AccessDeniedException("Access Denied");
        }
        return video;
    }

    private String[] getFfmpegString(File outputDir, File source, String playlistPath) {
        String segmentPattern = outputDir.getAbsolutePath() + File.separator + "segment%d.ts";

        return new String[] {
                ffmpegPath,
                "-i", source.getAbsolutePath(),

                "-c:v", "h264_nvenc", // kodek wideo
                "-preset", "p1", // preset kompresji
                "-b:v", "9M", // bitrate
                // "-s", "3440x1440", // rozdzielczość: 3440x1440
                // "-r", "60", // liczba klatek na sekundę: 60 fps

                "-c:a", "aac", // kodek audio: AAC
                "-b:a", "128k",
                "-ar", "44100",
                "-ac", "2", // liczba kanałów: stereo

                "-f", "hls", // format wyjściowy: HLS
                "-hls_time", "6", // długość segmentu w sekundach
                "-hls_list_size", "0", // pełna lista segmentów
                "-hls_segment_filename", segmentPattern, // wzorzec nazw segmentów
                playlistPath // ścieżka do playlisty .m3u8
        };
    }

    private void handleVideoUpload(MultipartFile fileVideo, Long videoId) throws IOException {
        String filename = String.valueOf(UUID.randomUUID());
        Path filePath = videoStorage.resolve(filename);
        Files.copy(fileVideo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        videoProducer.sendVideoTask(filePath.toAbsolutePath().toString(), videoId);
    }

    private void setThumbnailPath(Long videoID, String thumbnailPath) throws UnableToSetResourcePath {
        Optional<Video> video = videoRepository.findVideoById(videoID);
        if (video.isPresent()) {
            Video videoEntity = video.get();
            videoEntity.setThumbnailPath(thumbnailPath);
            videoRepository.save(videoEntity);
        } else {
            throw new UnableToSetResourcePath("Video could not be found by id, path to resources not set");
        }
    }

    private static BufferedImage GetBufferedImage(MultipartFile file) throws InvalidImageFormatException {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new InvalidImageFormatException("Can not process file");
            }
            return removeTransparency(image);
        } catch (IOException e) {
            throw new InvalidImageFormatException("Can not process file, recommended format: JPG");
        }
    }

    private static BufferedImage removeTransparency(BufferedImage image) {
        BufferedImage rgbImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgbImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        g.drawImage(image, 0, 0, null);
        g.dispose();
        return rgbImage;
    }

    private static String saveAsJpg(BufferedImage image) throws CouldNotSaveFileException {
        try {
            File dir = new File(thumbnailPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filename = UUID.randomUUID() + ".jpg";
            File outputFile = new File(thumbnailPath + filename);
            ImageIO.write(image, "jpg", outputFile);
            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            throw new CouldNotSaveFileException(e.getMessage());
        }
    }
}
