package com.lausn.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api")
@RestController
public class FileController {

	private static final Logger log = LoggerFactory.getLogger(FileController.class.getName());
	
	@PostMapping("/save/{filename}")
	public ResponseEntity<?> saveFile(@PathVariable String filename, @RequestBody Map<String, Object> callback) throws IOException {
		log.info("📥 Callback received: " + callback);

		int status = (int) callback.get("status");

		// Status 2 or 6 means OnlyOffice finished saving and the file is ready to download
		if (status == 2 || status == 6) {
			String fileUrl = (String) callback.get("url");
			String key = (String) callback.get("key");
			log.info(fileUrl);
			log.info("key:"+key);
			try (InputStream in = new URL(fileUrl).openStream()) {
				Path path = Paths.get("src/main/resources/saved_" + key + ".docx");
				Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
				log.info("Document saved to: " + path);
			}
			catch (Exception e) {
				log.error("❌ Error saving file: ", e);
			}
		}
		return ResponseEntity.ok(Map.of("error", 0));
	}
}
