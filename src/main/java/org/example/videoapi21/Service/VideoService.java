package org.example.videoapi21.Service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.videoapi21.Component.UserComponent;
import org.example.videoapi21.Entity.AppUser;
import org.example.videoapi21.Entity.Video;
import org.example.videoapi21.Exception.*;
import org.example.videoapi21.Kafka.VideoProducer;
import org.example.videoapi21.Repository.VideoRepository;
import org.example.videoapi21.Request.CreateVidoeEntityRequest;
import org.example.videoapi21.Response.UserValidationResponse;
import org.example.videoapi21.Response.VideoCreateResponse;
import org.springframework.core.io.FileSystemResource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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

    public ResponseEntity<VideoCreateResponse> createVideoWithFilesUpload(MultipartFile fileVideo, MultipartFile fileThumbnail, CreateVidoeEntityRequest createVidoeEntityRequest, HttpServletRequest request)
            throws IOException, SendVideoTaskException, InvalidVideoFormatException, BadCredentialsException {

        UserValidationResponse userValidationResponse = userComponent.getUserFromRequest(request);
        if(!userValidationResponse.status().equals("OK")){
            throw new BadCredentialsException("Unsuccessful token validation");
        }

        AppUser appUser = userValidationResponse.user();
        Video video = new Video(createVidoeEntityRequest.title(), createVidoeEntityRequest.description(), appUser, "", "");
        videoRepository.save(video);

        handleVideoUpload(fileVideo, video.getId());
        handleThumbnailUpload(fileThumbnail, video.getId());

        return ResponseEntity.ok(
                new VideoCreateResponse(video.getTitle(),video.getDescription())
        );
    }


    public void handleThumbnailUpload(MultipartFile file, Long videoId) throws InvalidImageFormatException, CouldNotSaveFileException {
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

            if (exitCode == 0){
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

    public void setVideoPath(Long videoID, String vidoPath) throws UnableToSetResourcePath{
        Optional<Video> video = videoRepository.getVideoById(videoID);
        if(video.isPresent()){
            Video videoEntity = video.get();
            videoEntity.setVideoPath(vidoPath);
            videoRepository.save(videoEntity);
        }else{
            throw new UnableToSetResourcePath("Video could not be found by id, path to resources not set");
        }
    }

    public ResponseEntity<FileSystemResource> getVideoFromUUID(String uuid)
            throws VideoNotFoundException {
        UUID uuidObj = UUID.fromString(uuid);
        Video video = videoRepository.getVideoByUuid(uuidObj)
                .orElseThrow(() -> new VideoNotFoundException("Video could not be found by uuid"));

        FileSystemResource videoResource = new FileSystemResource(video.getVideoPath());
        if(!videoResource.exists() || !videoResource.isReadable()){
            throw new VideoNotFoundException("Video path might not exist");
        }
        return ResponseEntity.ok(videoResource);
    }

    public Page<Video> getRecentVideos(Pageable pageable) {
        return  videoRepository.findAllWithValidPath(pageable);
    }


    private String[] getFfmpegString(File outputDir, File source, String playlistPath) {
        String segmentPattern = outputDir.getAbsolutePath() + File.separator + "segment%d.ts";


        return new String[]{
                ffmpegPath,
                "-i", source.getAbsolutePath(),

                "-c:v", "h264_nvenc",              // kodek wideo
                "-preset", "p1",                 // preset kompresji
                "-b:v", "9M",                     // bitrate
                //"-s", "3440x1440",                 // rozdzielczość: 3440x1440
                //"-r", "60",                        // liczba klatek na sekundę: 60 fps

                "-c:a", "aac",                     // kodek audio: AAC
                "-b:a", "128k",
                "-ar", "44100",
                "-ac", "2",                        // liczba kanałów: stereo

                "-f", "hls",                       // format wyjściowy: HLS
                "-hls_time", "6",                  // długość segmentu w sekundach
                "-hls_list_size", "0",             // pełna lista segmentów
                "-hls_segment_filename", segmentPattern, // wzorzec nazw segmentów
                playlistPath                       // ścieżka do playlisty .m3u8
        };
    }

    private void handleVideoUpload(MultipartFile fileVideo, Long videoId) throws IOException {
        String filename = UUID.randomUUID() + "_" + fileVideo.getOriginalFilename();
        Path filePath = videoStorage.resolve(filename);
        Files.copy(fileVideo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("File saved: " + filePath.toAbsolutePath());

        videoProducer.sendVideoTask(filePath.toAbsolutePath().toString(), videoId);
    }

    private void setThumbnailPath(Long videoID, String thumbnailPath) throws UnableToSetResourcePath{
        Optional<Video> video = videoRepository.getVideoById(videoID);
        if(video.isPresent()){
            Video videoEntity = video.get();
            videoEntity.setThumbnailPath(thumbnailPath);
            videoRepository.save(videoEntity);
        }else{
            throw new UnableToSetResourcePath("Video could not be found by id, path to resources not set");
        }
    }

    private static BufferedImage GetBufferedImage(MultipartFile file) throws InvalidImageFormatException{
        try{
            BufferedImage image = ImageIO.read(file.getInputStream());
            if(image == null){
                throw  new InvalidImageFormatException("Can not process file");
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
                BufferedImage.TYPE_INT_RGB
        );
        Graphics2D g = rgbImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        g.drawImage(image, 0, 0, null);
        g.dispose();
        return rgbImage;
    }

    private static String saveAsJpg(BufferedImage image) throws CouldNotSaveFileException{
        try{
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
