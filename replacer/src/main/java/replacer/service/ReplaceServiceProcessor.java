package replacer.service;

import org.springframework.stereotype.Service;
import replacer.model.Request;
import replacer.model.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static replacer.utils.Constants.*;
import static replacer.utils.Uitls.*;

@Service
public class ReplaceServiceProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ReplaceServiceProcessor.class);

    @Autowired
    private HttpServletRequest httpServletRequest;

    public Response processRequest(Request request) {
        Response response = new Response();
        String generatedFileName = UUID.randomUUID().toString();
        logger.info("Generated filename : {}", generatedFileName);

        try {
            replace(normalizeMap(request.getReplace()),  createFilePath(request.getFile(), DIR + DIR_TEMPLATES), createFilePath(generatedFileName, DIR + DIR_TEMPS));
            logger.info("Created file : {}", generatedFileName);
            response.setUrl(createURL(generatedFileName, httpServletRequest));
            response.setStatus(HttpStatus.OK.name());
            return response;
        } catch (Exception e) {
            logger.error("Failed to replace/create file : {} with error {}", generatedFileName, e);
            response.setUrl(null);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
            return response;
        }
    }

    public void uploadFile(MultipartFile file) throws Exception {
        String fileName = createFilePathWithoutExtention(file.getOriginalFilename(), DIR + DIR_TEMPLATES);
        logger.info("Uploading file : {}", fileName);

        Path targetLocation = Paths.get(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

    }

    public Resource loadFileAsResource(String fileName) throws Exception {
        String fullPath = createFilePathWithoutExtention(fileName, DIR);
        logger.info("Downloading file : {}", fullPath);
        Path filePath = Paths.get(fullPath);
        Resource resource = new UrlResource(filePath.toUri());
        if(resource.exists()) {
            return resource;
        } else {
            throw new FileNotFoundException();
        }
    }

    private static void replace(Map<String, String> map, String file, String out) throws Exception {
        XWPFDocument doc = new XWPFDocument(OPCPackage.open(file));
        for (XWPFParagraph p : doc.getParagraphs()) {
            List<XWPFRun> runs = p.getRuns();
            if (!CollectionUtils.isEmpty(runs)) {
                for (XWPFRun r : runs) {
                    final String text = r.getText(0);
                    if (StringUtils.isEmpty(text)) continue;
                    map.entrySet()
                            .stream()
                            .filter(a -> text.contains(convertToKey(a.getKey())))
                            .forEach(a -> r.setText(r.getText(0).replace(convertToKey(a.getKey()), a.getValue()), 0));
                }
            }
        }
        doc.write(new FileOutputStream(out));
    }

    public void deleteFile(String fileName) throws Exception{
        String fullPath = createFilePathWithoutExtention(fileName, DIR);
        logger.info("Deleting file : {}", fullPath);
        Path filePath = Paths.get(fullPath);
        Files.delete(filePath);
    }

    public List<String> getAllFiles() throws Exception {
        return Files.list(Paths.get(createTemplatesDirectoryPath()))
                .filter(a -> a.toFile().isFile())
                .map(a -> a.getFileName().toString())
                .collect(Collectors.toList());
    }
}
