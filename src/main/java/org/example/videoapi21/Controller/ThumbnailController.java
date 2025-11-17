package org.example.videoapi21.Controller;


import org.example.videoapi21.Service.ThumbnailService;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/thumbnails")
public class ThumbnailController {
    private final ThumbnailService thumbnailService;

    public ThumbnailController(ThumbnailService thumbnailService){
        this.thumbnailService = thumbnailService;
    }

}
