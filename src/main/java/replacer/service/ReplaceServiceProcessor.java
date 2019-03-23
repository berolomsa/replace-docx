package replacer.service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import replacer.model.Request;
import replacer.model.Response;

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
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
            replace2(normalizeMap(request.getReplace()),  createFilePath(request.getFile(), DIR + DIR_TEMPLATES), createFilePath(generatedFileName, DIR + DIR_TEMPS));
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

    //$KEY$
	private void replaceOld(Map<String, String> map, XWPFDocument doc) throws Exception {
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
	}

    private void replace2(Map<String, String> map, String file, String out) throws Exception{
    	XWPFDocument xwpfDocument = openDocument(file);
		replaceOld(map, xwpfDocument);
		replaceNew(map, xwpfDocument);
		writeAndClose(out,  xwpfDocument);
	}

	private static XWPFDocument openDocument(String filePath) throws Exception {
		return new XWPFDocument(OPCPackage.open(filePath));
	}

	private void writeAndClose(String fileResult, XWPFDocument document) throws IOException {
		document.write(new FileOutputStream(fileResult));
	}

	private void iterateParagraphs(XWPFDocument doc, Consumer<XWPFParagraph> consumer) {
		for (XWPFParagraph p : doc.getParagraphs())
			consumer.accept(p);
		for (XWPFTable tbl : doc.getTables())
			for (XWPFTableRow row : tbl.getRows())
				for (XWPFTableCell cell : row.getTableCells())
					for (XWPFParagraph p : cell.getParagraphs())
						consumer.accept(p);
	}

	//${KEY}
	private void replaceNew(Map<String, String> fieldsForReport, XWPFDocument document) {
		iterateParagraphs(document, p -> replaceParagraph(p, fieldsForReport));
	}

	private void replaceParagraph(XWPFParagraph paragraph, Map<String, String> fieldsForReport) throws POIXMLException {
		String find, text, runsText;
		List<XWPFRun> runs;
		XWPFRun run, nextRun;
		for (String key : fieldsForReport.keySet()) {
			text = paragraph.getText();
			if (!text.contains("${"))
				return;
			find = "${" + key + "}";
			if (!text.contains(find))
				continue;
			runs = paragraph.getRuns();
			for (int i = 0; i < runs.size(); i++) {
				run = runs.get(i);
				runsText = run.getText(0);
				if (runsText != null) {
					if (runsText.contains("${") ||
							(runsText.contains("$") && runs.size() > 1 && runs.get(i + 1).getText(0).substring(0, 1).equals("{"))) {
						while (!runsText.contains("}")) {
							nextRun = runs.get(i + 1);
							runsText = runsText + nextRun.getText(0);
							paragraph.removeRun(i + 1);
						}
						run.setText(runsText.contains(find) ?
								runsText.replace(find, fieldsForReport.get(key)) :
								runsText, 0);
					}
				}
			}
		}
	}

}
