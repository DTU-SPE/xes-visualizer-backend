package dtu.spe.xesvisualizer.chord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.springframework.stereotype.Service;

import dtu.spe.xesvisualizer.shared.LogProcessor;
import dtu.spe.xesvisualizer.shared.Relation;
import dtu.spe.xesvisualizer.shared.RelationToValuesMap;

/**
 * Holds an XLog and a App.Shared.LogProcessor and generates
 * App.Chord.ChordModel(s).
 */

@Service
public class ChordService {

	public ChordModel createChord(XLog log, String attributeKey, String operator, String aggregationFunc,
			boolean noEnd) {
		RelationToValuesMap relationsToValues = new RelationToValuesMap(attributeKey, operator);
		XEventClassifier classifier = LogProcessor.getClassifier(log);
		for (XTrace trace : log) {
			relationsToValues = LogProcessor.relationToValues(trace, attributeKey, operator, false, relationsToValues, classifier, noEnd);
		}

		HashMap<Relation, Double> links = relationsToValues.aggregate(aggregationFunc);

		// Nodes
		List<String> nodes = links.keySet().stream().flatMap(l -> l.eventNames.stream()).distinct().collect(Collectors.toList());

		// Matrix
		double[][] matrix = populateMatrix(links, nodes);
		return new ChordModel(matrix, nodes);

	}

	private double[][] populateMatrix(HashMap<Relation, Double> links, List<String> nodes) {
		int numberOfGroups = nodes.size();
		double[][] matrix = new double[numberOfGroups][numberOfGroups];
		for (Map.Entry<Relation, Double> relation : links.entrySet()) {
			String source = relation.getKey().eventNames.get(0);
			int sourceIndex = nodes.indexOf(source);
			String target = relation.getKey().eventNames.get(1);
			int targetIndex = nodes.indexOf(target);
			Double value = relation.getValue();
			matrix[sourceIndex][targetIndex] = value;
		}
		return matrix;
	}

}
