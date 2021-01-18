package dtu.spe.xesvisualizer.sankey;

import org.json.JSONArray;
import org.json.JSONObject;

import dtu.spe.xesvisualizer.shared.Relation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds all the information needed for the frontend to generate a App.Sankey
 * diagram.
 */
public class SankeyModel {
	private List<String> nodes;
	private HashMap<Relation, Double> links;

	public SankeyModel(List<String> nodes, HashMap<Relation, Double> links) {
		this.nodes = nodes;
		this.links = links;
	}

	public String toJSONString() {
		// Making the nodes JSONArray
		JSONArray nodesJSONArray = new JSONArray();
		for (String n : this.nodes) {
			JSONObject nodeObj = new JSONObject();
			nodeObj.put("name", n);
			nodesJSONArray.put(nodeObj);
		}

		// Making the links JSONArray
		JSONArray linksJSONArray = new JSONArray();
		for (Map.Entry<Relation, Double> entry : this.links.entrySet()) {
			JSONObject linkObj = new JSONObject();
			linkObj.put("source", entry.getKey().eventNames.get(0));
			linkObj.put("target", entry.getKey().eventNames.get(1));
			linkObj.put("value", entry.getValue());
			linksJSONArray.put(linkObj);
		}

		// Combining nodes and links into one JSONObject
		JSONObject result = new JSONObject();
		result.put("links", linksJSONArray);
		result.put("nodes", nodesJSONArray);
		return result.toString();

	}

}
