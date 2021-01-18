package dtu.spe.xesvisualizer.storage;

import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import dtu.spe.xesvisualizer.shared.LogProcessor;
import dtu.spe.xesvisualizer.shared.VariantMap;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles file uploading
 * Credit: https://spring.io/guides/gs/uploading-files/
 */
@Service
public class FileStorageService {
	private Path path = null; //Paths.get("src/main/uploads");
	
	public FileStorageService() {
		try {
			this.path = Files.createTempDirectory("xes-visualizer");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void storeFile(MultipartFile file, String saveAs) throws IOException {
		// Check if file is empty (could break dir otherwise)
		if (file.isEmpty()) {
			throw new EmptyFileException("The uploaded file: " + file.getOriginalFilename() + " is empty.");
		}
		// Create a path for the new file
		Path destinationPath = this.path.resolve(Paths.get(saveAs)).normalize().toAbsolutePath();
		// Saving the MultipartFile using a try-with-resources statement
		try (InputStream inputStream = file.getInputStream()) {
			Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw e;
		}
	}


	public void storeFile(File file, String saveAs) throws IOException {
		// Check if file is empty (could break dir otherwise)
		if (file.length() == 0) {
			throw new EmptyFileException("The uploaded file: " + file.getName() + " is empty.");
		}

		// Create a path for the new file
		Path destinationPath = this.path.resolve(Paths.get(saveAs)).normalize().toAbsolutePath();

		InputStream fileInputStream = new FileInputStream(file);
		try (InputStream inputStream = fileInputStream) {
			Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw e;
		}
	}


	public FileMetadataModel processFile(String name) {
		// Retrieving the original file
		File newFile = this.loadFile(name);
		// Getting the valid attributes
		XLog log = this.parseFile(newFile);
		List<String> validAttributes = LogProcessor.getValidAttributeKeys(log);
		// Creating sublogs
		VariantMap variants = LogProcessor.findVariants(log);
		HashMap<Double, XLog> sublogs = LogProcessor.createSubLogs(log, variants);
		List<Double> percentages = new ArrayList<>();
		for (Map.Entry<Double, XLog> entry : sublogs.entrySet()) {
			XLog sublog = entry.getValue();
			double percentage = entry.getKey();
			String fileName = percentage + "_" + name;
			System.out.println(fileName);
			this.storeXLog(sublog, fileName);
			percentages.add(percentage);
		}

		List<String> stringPercentages = percentages.stream().sorted().map(d -> String.valueOf(d))
				.collect(Collectors.toList());

		// Creating the model
		FileMetadataModel metadata = new FileMetadataModel(validAttributes, stringPercentages);

		return metadata;

	}


	public void storeXLog(XLog log, String name) {
		File destinationPath = this.path.resolve(Paths.get(name)).normalize().toAbsolutePath().toFile();
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(destinationPath);
			XSerializer serializer = new XesXmlGZIPSerializer();
			serializer.serialize(log, outputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public File loadFile(String fileName) {
		try {
			File file = path.resolve(fileName).toFile();
			return file;
		} catch (Exception e) {
			throw new CouldNotLoadFileException("Could not find the file to load.", e);
		}
	}

	public XLog parseFile(File file) {
		XParser parser = new XesXmlGZIPParser();
		try {
			List<XLog> logs = parser.parse(file);
			return logs.get(0);
		} catch (Exception e) {
			throw new CouldNotParseException("Could not parse the uploaded log.", e);
		}

	}

	public void deleteDir() {
		FileSystemUtils.deleteRecursively(path.toFile());
	}

	public void initDir() {
		try {
			Files.createDirectories(path);
		} catch (Exception e) {
			System.out.println("Could not initialize new directory in : " + path);
			e.printStackTrace();
		}
	}



/*
    public List<String> getValidAttributes(File file) throws Exception {
        XesXmlParser parser = new XesXmlParser();
            List<XLog> logs = parser.parse(file);
            XLog log = logs.get(0);
            return LogProcessor.getValidAttributeKeys(log);
    }
*/









/*    public List<String> store(MultipartFile file) {
        List<String> badResult = new ArrayList<>();
        // Create a path for the new file
        Path destinationPath = this.path.resolve(Paths.get(file.getOriginalFilename()))
                .normalize().toAbsolutePath();

        // Saving the MultipartFile using a try-with-resources statement
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationPath,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Something went wrong during file saving");
            return badResult;
        }

        // Parsing the file
        File newFile = destinationPath.toFile();
        XesXmlParser parser = new XesXmlParser();
        try {
            List<XLog> logs = parser.parse(newFile);
            XLog log = logs.get(0);
            List<String> validAttributes = LogProcessor.getValidAttributeKeys(log);
            return validAttributes;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something went wrong during XES parsing");
            return badResult;
        }

    }*/


}
