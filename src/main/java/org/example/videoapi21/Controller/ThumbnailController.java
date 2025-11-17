package org.example.videoapi21.Controller;

import org.example.videoapi21.Exception.CouldNotSaveFileException;
import org.example.videoapi21.Exception.InvalidImageFormatException;
import org.example.videoapi21.Exception.InvalidVideoFormatException;
import org.example.videoapi21.Exception.SendVideoTaskException;
import org.example.videoapi21.Response.CustomErrorResponse;
import org.example.videoapi21.Service.ThumbnailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/thumbnails")
public class ThumbnailController {
    private final ThumbnailService thumbnailService;

    public ThumbnailController(ThumbnailService thumbnailService){
        this.thumbnailService = thumbnailService;
    }


//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadThumbnail(@RequestParam("file") MultipartFile file){
//        thumbnailService.handleThumbnailUpload(file);
//        return ResponseEntity.ok().body("Thumbnail uploaded");
//    }
}
