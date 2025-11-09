package org.example.videoapi21.Kafka;

import org.example.videoapi21.Service.VideoService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class VideoConsumer {

    private final VideoService videoService;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public VideoConsumer(VideoService videoService) {
        this.videoService = videoService;
    }

    @KafkaListener(
            topics = "video-tasks",
            groupId = "video-processing-group",
            concurrency = "4"
    )
    public void consume(String message) {
        executor.submit(() -> processMessage(message));
    }

    private void processMessage(String message) {
        try {
            String[] parts = message.split("\\|", 2);
            int taskId = Integer.parseInt(parts[0]);
            File file = Paths.get(parts[1]).toFile();

            if (!file.exists()) {
                System.err.println("File does not Exist: " + file.getAbsolutePath());
                return;
            }

            System.out.println("▶️ Mp4 to HLS conversion started  : " + taskId);
            videoService.mp4ToHLS(file.getAbsolutePath());
            System.out.println("✅ Successfully converted to HLS: " + taskId);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


