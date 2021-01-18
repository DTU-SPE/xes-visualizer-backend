package dtu.spe.xesvisualizer.shared;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that represents the set of mappings that have the following form:
 * App.Shared.Relation --> [Double 1, Double 2, ..] The semantic of the double
 * depends two factors:
 * 1) The chosen event attribute, e.g. Activity, time:timestamp, etc 
 * 2) The operation applied to the values of the chosen attribute, e.g. difference
 */
public class RelationToValuesMap {
	// The HashMap made by the App.Shared.LogProcessor
	public HashMap<Relation, List<Double>> map;
	// The attribute key and operator that were used to create the HashMap
	String attrKey;
	String operator;

	public RelationToValuesMap(String attrKey, String operator) {
		this.attrKey = attrKey;
		this.operator = operator;
		this.map = new HashMap<>();
	}

	public HashMap<Relation, Double> aggregate(String function) {
		HashMap<Relation, Double> links = new HashMap<>();
		switch (function) {
		case "SUM":
			links = this.aggregateSum();
			break;
		case "MIN":
			links = this.aggregateMin();
			break;

		case "MAX":
			links = this.aggregateMax();
			break;

		case "AVG":
			links = this.aggregateAvg();
			break;

		default:
			System.out.println("Aggregation function not found");
		}
		return links;
	}

	/**
	 * Sums the list of values associated with each relation in this.map
	 *
	 * @return Object of type RelationToAggregatedValueMap that holds the relations
	 *         and their aggregated values i.a.
	 */
	public HashMap<Relation, Double> aggregateSum() {
		HashMap<Relation, Double> result = new HashMap<>();
		// Iterating through entries of this.map
		for (Map.Entry<Relation, List<Double>> entry : this.map.entrySet()) {
			// Populating the result HashMap with the relation and its summed values
			result.put(entry.getKey(), entry.getValue().stream().mapToDouble(v -> v).sum());
		}
		return result;
	}

	/**
	 * Finds the minimum value for each relation in this.map
	 *
	 * @return Object of type RelationToAggregatedValueMap that holds the relations
	 *         and their aggregated values i.a.
	 */
	public HashMap<Relation, Double> aggregateMin() {
		HashMap<Relation, Double> result = new HashMap<>();
		// Iterating through entries of this.map
		for (Map.Entry<Relation, List<Double>> entry : this.map.entrySet()) {
			// Populating the result HashMap with the relation and its minimum value
			result.put(entry.getKey(), Collections.min(entry.getValue()));
		}

		return result;
	}

	/**
	 * Finds the maximum value for each relation in the HashMap.
	 *
	 * @return Object of type RelationToAggregatedValueMap that holds the relations
	 *         and their aggregated values i.a.
	 */
	public HashMap<Relation, Double> aggregateMax() {
		HashMap<Relation, Double> result = new HashMap<>();
		// Iterating through entries of this.map
		for (Map.Entry<Relation, List<Double>> entry : this.map.entrySet()) {
			// Populating the result HashMap with the relation and its maximum value
			result.put(entry.getKey(), Collections.max(entry.getValue()));
		}

		return result;
	}

	public HashMap<Relation, Double> aggregateAvg() {
		HashMap<Relation, Double> result = new HashMap<>();
		// Iterating through entries of this.map
		for (Map.Entry<Relation, List<Double>> entry : this.map.entrySet()) {
			// Populating the result HashMap with the relation and its average value
			result.put(entry.getKey(),
					LogProcessor.round(entry.getValue().stream().mapToDouble(a -> a).average().orElse(Double.NaN), 2));
		}

		return result;

	}

}
