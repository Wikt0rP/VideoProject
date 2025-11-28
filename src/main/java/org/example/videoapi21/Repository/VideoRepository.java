package org.example.videoapi21.Repository;

import org.example.videoapi21.Entity.AppUser;
import org.example.videoapi21.Entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findVideoById(Long id);

    Optional<Video> findVideoByUuid(UUID uuid);

    List<Video> findAll();

    @Query("SELECT v FROM Video v " +
            "WHERE v.videoPath IS NOT NULL AND LENGTH(v.videoPath) >= 3 " +
            "AND v.thumbnailPath IS NOT NULL AND LENGTH(v.thumbnailPath) >= 3" +
            "AND v.uuid IS NOT NULL " +
            "AND v.author IS NOT NULL " +
            "AND v.createdAt IS NOT NULL " +
            "ORDER BY v.createdAt DESC")
    Page<Video> findAllWithValidPath(Pageable pageable);
    Page<Video> findVideosByAuthor(AppUser author, Pageable pageable);

}
