package com.poeticjustice.deeppoemsinc.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.poeticjustice.deeppoemsinc.Repository.BookRespository;
import com.poeticjustice.deeppoemsinc.models.Book;

import net.coobird.thumbnailator.Thumbnails;

@Service
public class BookService {

    @Autowired
    private BookRespository bookRespository;

    private static final String PDF_UPLOAD_DIR = "uploads/pdf/";
    private static final String IMAGE_UPLOAD_DIR = "uploads/images/";

    // public String saveFile(MultipartFile file, String type) throws IOException {
    //     String uploadDir = type.equals("pdf") ? PDF_UPLOAD_DIR : IMAGE_UPLOAD_DIR;
    //     File uploadDirFile = new File(uploadDir);
    //     if (!uploadDirFile.exists()) {
    //         uploadDirFile.mkdirs();
    //     }

    //     String filePath = uploadDir + file.getOriginalFilename();
    //     Files.copy(file.getInputStream(), Paths.get(filePath));

    //     if (type.equals("images")) {
    //         // Manipulate the image
    //         manipulateImage(filePath);
    //     }

    //     return filePath;
    // }
    @SuppressWarnings("null")
    public String saveFile(MultipartFile file, String type) throws IOException {
        String uploadDir = type.equals("pdf") ? PDF_UPLOAD_DIR : IMAGE_UPLOAD_DIR;
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        // Get the original filename and its extension
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String baseFilename = originalFilename.substring(0, originalFilename.lastIndexOf("."));

        // Generate a unique filename
        // String uniqueFilename = baseFilename + "_" + UUID.randomUUID().toString() + fileExtension;

        // Then, modify the line causing the error
        // String uniqueFilename = baseFilename + "_" + UUID.randomUUID().toString() + fileExtension;
        String uniqueFilename = baseFilename + "_" + java.util.UUID.randomUUID().toString() + fileExtension;
        
        String filePath = uploadDir + uniqueFilename;

        // Save the file to the specified path
        Files.copy(file.getInputStream(), Paths.get(filePath));

        if (type.equals("images")) {
            // Manipulate the image
            manipulateImage(filePath);
        }

        return filePath;
    }

    public void saveBook(Book book) {
        bookRespository.save(book);
    }
    
    private void manipulateImage(String imagePath) throws IOException {
        File inputFile = new File(imagePath);
        File outputFile = new File(imagePath); // Overwrite the existing file

        // Resize the image
        Thumbnails.of(inputFile)
                .size(800, 600) // Resize to 800x600
                .outputFormat("png") // Change format if needed
                .toFile(outputFile);

        // // Cropping the image
        // Thumbnails.of(inputFile)
        //     .sourceRegion(Positions.CENTER, 400, 300) // Crop region
        //     .size(400, 300)
        //     .toFile(outputFile);

        // // Rotate the image
        // Thumbnails.of(inputFile)
        //     .size(800, 600)
        //     .rotate(90) // Rotate image by 90 degrees
        //     .toFile(outputFile);

        // // Watermarking: Add watermark to the image
        // Thumbnails.of(inputFile)
        //     .size(800, 600)
        //     .watermark(Positions.BOTTOM_RIGHT, watermarkImage, 0.5f) // Add watermark
        //     .toFile(outputFile);
    }

    public List<Book> getAllBooks() {
        return bookRespository.findAll();
    }
}