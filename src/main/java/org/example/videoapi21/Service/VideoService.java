package org.example.videoapi21.Service;

import org.apache.commons.io.FilenameUtils;
import org.example.videoapi21.Exception.InvalidVideoFormatException;
import org.example.videoapi21.Exception.SendVideoTaskException;
import org.example.videoapi21.Kafka.VideoProducer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class VideoService {
    private static final String VIDEO_DIR = "video";
    private static final String VIDEO_HLS = "hls_output/videos";
    private final Path videoStorage = Paths.get(VIDEO_DIR);

    private final VideoProducer videoProducer;
    private final String ffmpegPath = "C:\\ffmpeg-2025-11-06-git-222127418b-full_build\\bin\\ffmpeg.exe";

    public VideoService(VideoProducer videoProducer){
        this.videoProducer = videoProducer;
    }



    public void HandleFlieUpload(MultipartFile file) throws IOException, SendVideoTaskException, InvalidVideoFormatException {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if(!"mp4".equalsIgnoreCase(extension)){
            throw new InvalidVideoFormatException("Uploaded video should be in accepted format ex. MP4");
        }

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = videoStorage.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("File saved: " + filePath.toAbsolutePath());
        videoProducer.sendVideoTask(filePath.toAbsolutePath().toString());
        System.out.println("Task queued for conversion: " + filePath.getFileName());

    }

    public void Mp4ToHLS(String inputPath) {
        File source = new File(inputPath);
        if (!source.exists()) {
            System.err.println("Source file does not exist: " + inputPath);
            return;
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

            if (exitCode == 0) {
                System.out.println("Success!");
                System.out.println("Output: " + outputDir.getAbsolutePath());
            } else {
                System.err.println("ffmpeg finished with error code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
