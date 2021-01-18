package dtu.spe.xesvisualizer.sankey;

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
import java.util.List;

@RestController
public class SankeyController {
	private final FileStorageService storageService;
	private final SankeyService sankeyService;

	@Autowired
	public SankeyController(FileStorageService storageService, SankeyService sankeyService) {
		this.storageService = storageService;
		this.sankeyService = sankeyService;
	}

	@CrossOrigin
	@PostMapping("/createSankey")
	public ResponseEntity<String> createSankey(
			@RequestParam("fileName") String fileName,
			@RequestParam("attributeKey") String attributeKey,
			@RequestParam("operation") String operator,
			@RequestParam("aggregationFunc") String aggregationFunc,
			@RequestParam("noEnd") boolean noEnd) {
		try {
			File file = storageService.loadFile(fileName);
			XLog log = storageService.parseFile(file);
			List<SankeyModel> sankeyModels = sankeyService.createSankey(log, attributeKey, operator, aggregationFunc, noEnd);
			String body = "{\"ungroupedModel\" : " + sankeyModels.get(0).toJSONString() + " , \"groupedModel\" : " + sankeyModels.get(1).toJSONString() + "}";
			return new ResponseEntity<>(body, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
}
