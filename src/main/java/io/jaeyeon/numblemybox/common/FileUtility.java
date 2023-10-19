package io.jaeyeon.numblemybox.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

@Component
public class FileUtility {

	public boolean exists(Path path) {
		return Files.exists(path);
	}

	public String probeContentType(Path path) throws IOException {
		return Files.probeContentType(path);
	}

	public void createDirectories(Path path) throws IOException {
		Files.createDirectories(path);
	}
}
