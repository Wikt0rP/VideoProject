package org.example.videoapi21.Repository;

import org.example.videoapi21.Entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video,Long> {
    Optional<Video> findByTitle(String title);
    Optional<Video> findByVideoPath(String videoPath);
    Optional<Video> findVideoById(Long id);
}
