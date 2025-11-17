package org.example.videoapi21.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID uuid;
    private String title;
    private String description;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private AppUser author;
    private String videoPath;
    private String thumbnailPath;

    public Video(String title, String description, String videoPath, String thumbnailPath) {
        this.title = title;
        this.description = description;
        this.videoPath = videoPath;
        this.thumbnailPath = thumbnailPath;
    }
}
