package org.example.videoapi21.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;
    private String title;
    private String description;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private AppUser author;
    private String videoPath;
    private String thumbnailPath;
    private LocalDateTime createdAt;

    public Video(String title, String description, AppUser author, String videoPath, String thumbnailPath) {
        this.title = title;
        this.description = description;
        this.author = author;
        this.videoPath = videoPath;
        this.thumbnailPath = thumbnailPath;

        this.uuid = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }
}
