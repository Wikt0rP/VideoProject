package org.example.videoapi21.Service;

import org.apache.commons.io.FilenameUtils;
import org.example.videoapi21.Entity.Video;
import org.example.videoapi21.Exception.*;
import org.example.videoapi21.Kafka.VideoProducer;
import org.example.videoapi21.Repository.VideoRepository;
import org.example.videoapi21.Request.CreateVidoeEntityRequest;
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

    public VideoService(VideoProducer videoProducer, VideoRepository videoRepository) {
        this.videoProducer = videoProducer;
        this.videoRepository = videoRepository;
    }

    public void createVideoWithFilesUpload(MultipartFile fileVideo, MultipartFile fileThumbnail, CreateVidoeEntityRequest createVidoeEntityRequest) throws IOException, SendVideoTaskException, InvalidVideoFormatException {
        validateFileFormat(fileVideo);
        Video video = handleVideoUpload(fileVideo, createVidoeEntityRequest);
        handleThumbnailUpload(fileThumbnail, video.getId());
    }

    private Video handleVideoUpload(MultipartFile fileVideo, CreateVidoeEntityRequest createVidoeEntityRequest) throws IOException {
        String filename = UUID.randomUUID() + "_" + fileVideo.getOriginalFilename();
        Path filePath = videoStorage.resolve(filename);
        Files.copy(fileVideo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("File saved: " + filePath.toAbsolutePath());

        Video video = new Video(createVidoeEntityRequest.title(), createVidoeEntityRequest.description(), "", "");
        videoRepository.save(video);
        videoProducer.sendVideoTask(filePath.toAbsolutePath().toString(), video.getId());
        System.out.println("Task queued for conversion: " + filePath.getFileName());
        return video;
    }

    public void handleThumbnailUpload(MultipartFile file, Long videoId) throws InvalidImageFormatException, CouldNotSaveFileException {
        BufferedImage image = GetBufferedImage(file);
        String thumbnailPath = saveAsJpg(image);
        setThumbnailPath(videoId, thumbnailPath);
    }

    private static void validateFileFormat(MultipartFile file) throws InvalidVideoFormatException {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if(!"mp4".equalsIgnoreCase(extension)){
            throw new InvalidVideoFormatException("Uploaded video should be in accepted format ex. MP4");
        }
    }

    public String mp4ToHLS(String inputPath) {
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
        String segmentPattern = outputDir.getAbsolutePath() + File.separator + "segment%d.ts";


        String[] command = {
                ffmpegPath,                        // pełna ścieżka do ffmpeg.exe
                "-i", source.getAbsolutePath(),    // plik wejściowy

                // 🎥 WIDEO
                "-c:v", "h264_nvenc",              // kodek wideo (NVENC na GPU NVIDIA)
                "-preset", "p1",                 // preset kompresji (patrz lista poniżej)
                "-b:v", "9M",                     // bitrate wideo: 15 megabitów na sekundę
                "-s", "3440x1440",                 // rozdzielczość: 3440x1440 (UWAGA: wymusza skalowanie)
                "-r", "60",                        // liczba klatek na sekundę: 60 fps

                // 🔊 AUDIO
                "-c:a", "aac",                     // kodek audio: AAC
                "-b:a", "128k",                    // bitrate audio: 128 kbps
                "-ar", "44100",                    // częstotliwość próbkowania: 44.1 kHz
                "-ac", "2",                        // liczba kanałów: stereo

                // 📦 FORMAT WYJŚCIOWY
                "-f", "hls",                       // format wyjściowy: HLS
                "-hls_time", "6",                  // długość segmentu w sekundach
                "-hls_list_size", "0",             // pełna lista segmentów
                "-hls_segment_filename", segmentPattern, // wzorzec nazw segmentów
                playlistPath                       // ścieżka do playlisty .m3u8
        };



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
        Optional<Video> video = videoRepository.findVideoById(videoID);
        if(video.isPresent()){
            Video videoEntity = video.get();
            videoEntity.setVideoPath(vidoPath);
            videoRepository.save(videoEntity);
        }else{
            throw new UnableToSetResourcePath("Video could not be found by id, path to resources not set");
        }
    }

    public void setThumbnailPath(Long videoID, String thumbnailPath) throws UnableToSetResourcePath{
        Optional<Video> video = videoRepository.findVideoById(videoID);
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
