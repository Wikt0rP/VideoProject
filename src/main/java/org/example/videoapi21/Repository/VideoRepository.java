package org.example.videoapi21.Repository;

import org.example.videoapi21.Entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video,Long> {
    Optional<Video> getVideoById(Long id);
    Optional<Video> getVideoByUuid(UUID uuid);
    List<Video> findAll();

}
