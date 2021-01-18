package dtu.spe.xesvisualizer.storage;

import java.util.HashMap;
import java.util.List;

public class FileMetadataModel {
	private HashMap<String, List<String>> metadata;

	public FileMetadataModel(List<String> validAttrs, List<String> percentages) {
		this.metadata = new HashMap<>();
		this.metadata.put("validAttributes", validAttrs);
		this.metadata.put("percentages", percentages);
	}

	public HashMap<String, List<String>> getMetadata() {
		return this.metadata;
	}
}
