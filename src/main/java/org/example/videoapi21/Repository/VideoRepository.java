package org.example.videoapi21.Repository;

import org.example.videoapi21.Entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video,Long> {
    Optional<Video> getVideoById(Long id);
    Optional<Video> getVideoByUuid(UUID uuid);
    List<Video> findAll();

    @Query(value = "SELECT * FROM video WHERE LENGTH(video_path) >= 3 ORDER BY created_at DESC", nativeQuery = true)
    Page<Video> findAllWithValidPath(Pageable pageable);


}
