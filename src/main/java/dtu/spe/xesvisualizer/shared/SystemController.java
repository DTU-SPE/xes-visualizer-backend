package dtu.spe.xesvisualizer.shared;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class SystemController {

	@GetMapping("/ping")
	public @ResponseBody String ping() {
		return "pong";
	}
}