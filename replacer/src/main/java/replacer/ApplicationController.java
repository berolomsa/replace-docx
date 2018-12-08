package replacer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import replacer.model.Request;
import replacer.model.Response;
import replacer.service.ReplaceServiceProcessor;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static replacer.utils.Constants.*;

@RestController
public class ApplicationController {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    @Autowired
    private ReplaceServiceProcessor replaceServiceProcessor;

    @PostMapping(value = REPLACE_ENDPOINT)
    public Response replace(@RequestBody Request request) {
        logger.info("/replace Incoming request : {}", request.toString());

        Response response = replaceServiceProcessor.processRequest(request);

        logger.info("/replace Outgoing response : {}", response.toString());
        return response;
    }

    @GetMapping(FILES_ENDPOINT)
    public ResponseEntity<Resource> getReplacedFile(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        String fileName = requestURL.split(FILES_PATH)[1];
        fileName = fileName.replaceAll("%20", " ");

        logger.info("/files Incoming request : {}", fileName);
        Resource resource;
        String contentType;
        try {
            resource = replaceServiceProcessor.loadFileAsResource(fileName);
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (Exception e) {
            logger.info("Error writing file to output stream. Filename was '{}'", fileName, e);
            throw new RuntimeException("IOError writing file to output stream");
        }

        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        logger.info("/files Outgoing response : {}", fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping(UPLOAD_ENDPOINT)
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        logger.info("/uploadFile Incoming request : {}", file.getOriginalFilename());
        try {
            replaceServiceProcessor.uploadFile(file);
        } catch (Exception e) {
            logger.error("Error during file uploading ",e);
            return HttpStatus.INTERNAL_SERVER_ERROR.name();
        }
        logger.info("/uploadFile Outgoing response : {}", file.getOriginalFilename());
        return HttpStatus.OK.name();
    }

    @DeleteMapping(DELETE_ENDPOINT)
    public String deleteFile(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        String fileName = requestURL.split(DELETE_PATH)[1];
        fileName = fileName.replaceAll("%20", " ");

        logger.info("/deleteFile Incoming request : {}", fileName);

        try {
            replaceServiceProcessor.deleteFile(fileName);
        } catch (Exception e) {
            logger.error("Error during file deleting ",e);
            return HttpStatus.INTERNAL_SERVER_ERROR.name();
        }

        logger.info("/deleteFile Outgoing response : {}", fileName);
        return HttpStatus.OK.name();
    }

    @GetMapping(GET_ALL_FILES_ENDPOINT)
    public List<String> getAllFiles() throws Exception {
        return replaceServiceProcessor.getAllFiles();
    }
}
