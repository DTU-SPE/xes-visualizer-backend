package dtu.spe.xesvisualizer.chord;

import org.deckfour.xes.model.XLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dtu.spe.xesvisualizer.storage.FileStorageService;

import java.io.File;

@RestController
public class ChordController {
	private final FileStorageService storageService;
	private final ChordService chordService;

	@Autowired
	public ChordController(FileStorageService storageService, ChordService chordService) {
		this.storageService = storageService;
		this.chordService = chordService;
	}

	@CrossOrigin
	@PostMapping("/createChord")
	public ResponseEntity<String> createChord(
			@RequestParam("fileName") String fileName,
			@RequestParam("attributeKey") String attributeKey,
			@RequestParam("operation") String operator,
			@RequestParam("aggregationFunc") String aggregationFunc,
			@RequestParam("noEnd") boolean noEnd) {
		try {
			File file = storageService.loadFile(fileName);
			XLog log = storageService.parseFile(file);
			ChordModel chordModel = chordService.createChord(log, attributeKey, operator, aggregationFunc, noEnd);
			return new ResponseEntity<>(chordModel.toJSONString(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
