package dtu.spe.xesvisualizer.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FileUploadInit implements CommandLineRunner {
	private FileStorageService storageService;

	@Autowired
	public FileUploadInit(FileStorageService storageService) {
		this.storageService = storageService;
	}

	// Runs before spring boot application starts up
	@Override
	public void run(String... args) throws Exception {
		storageService.deleteDir();
		storageService.initDir();

	}
}
