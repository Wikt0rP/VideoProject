package org.example.videoapi21.Kafka;

import org.example.videoapi21.Exception.SendVideoTaskException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class VideoProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AtomicInteger counter = new AtomicInteger(0);

    public VideoProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendVideoTask(String filePath) throws SendVideoTaskException {
        int taskId = counter.incrementAndGet();
        String message = taskId + "|" + filePath;

        try {
            SendResult<String, String> result = kafkaTemplate.send("video-tasks", message).get();
            System.out.println("Task sent to Kafka: " + message +
                    " (partition=" + result.getRecordMetadata().partition() +
                    ", offset=" + result.getRecordMetadata().offset() + ")");

            kafkaTemplate.flush();
        } catch (Exception e)
        {
            throw new SendVideoTaskException(e);
        }
    }

}
