package org.example.videoapi21.Service;

import org.example.videoapi21.Exception.CouldNotSaveFileException;
import org.example.videoapi21.Exception.InvalidImageFormatException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class ThumbnailService {
    private final static String thumbnailPath = "thumbnails/";

    public void handleThumbnailUpload(MultipartFile file) throws InvalidImageFormatException, CouldNotSaveFileException{
        BufferedImage image = GetBufferedImage(file);
        saveAsJpg(image);
    }

    private static BufferedImage GetBufferedImage(MultipartFile file) throws InvalidImageFormatException{
        try{
            BufferedImage image = ImageIO.read(file.getInputStream());
            if(image == null){
                throw  new InvalidImageFormatException("Can not process file");
            }
            return removeTransparency(image);
        } catch (IOException e) {
            throw new InvalidImageFormatException("Can not process file, recommended format: JPG");
        }
    }

    private static BufferedImage removeTransparency(BufferedImage image) {
        BufferedImage rgbImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );
        Graphics2D g = rgbImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        g.drawImage(image, 0, 0, null);
        g.dispose();
        return rgbImage;
    }

    private static void saveAsJpg(BufferedImage image) throws CouldNotSaveFileException{
        try{
            File dir = new File(thumbnailPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filename = UUID.randomUUID() + ".jpg";
            File outputFile = new File(thumbnailPath + filename);
            ImageIO.write(image, "jpg", outputFile);
        } catch (IOException e) {
            throw new CouldNotSaveFileException(e.getMessage());
        }
    }
}
