package vector.StockManagement.controllers;




import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vector.StockManagement.services.FileService;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public String uploadFile(
            @RequestParam("file")
            MultipartFile file) {

        try {

            String fileName =
                    fileService.uploadFile(file);

            return "Uploaded successfully: "
                    + fileName;

        } catch (Exception e) {

            return "Upload failed: "
                    + e.getMessage();
        }
    }
}
