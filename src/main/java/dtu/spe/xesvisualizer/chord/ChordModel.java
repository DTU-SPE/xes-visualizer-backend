package dtu.spe.xesvisualizer.chord;

import java.util.List;

import org.json.JSONObject;

/**
 * Holds all the information needed for the frontend to generate a App.Chord
 * diagram.
 */
public class ChordModel {
	private double[][] matrix;
	private List<String> nodes;

	public ChordModel(double[][] matrix, List<String> nodes) {
		this.matrix = matrix;
		this.nodes = nodes;
	}

	public String toJSONString() {
		JSONObject result = new JSONObject();
		result.put("matrix", this.matrix);
		result.put("nodes", this.nodes);
		return result.toString();
	}
}
