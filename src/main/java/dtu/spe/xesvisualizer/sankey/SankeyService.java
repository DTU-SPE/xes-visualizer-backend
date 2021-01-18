package dtu.spe.xesvisualizer.sankey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.springframework.stereotype.Service;

import dtu.spe.xesvisualizer.shared.AttributeOperations;
import dtu.spe.xesvisualizer.shared.LogProcessor;
import dtu.spe.xesvisualizer.shared.Relation;
import dtu.spe.xesvisualizer.shared.RelationToValuesMap;

/**
 * Class to generate SankeyModel(s).
 */

@Service
public class SankeyService {

	public List<SankeyModel> createSankey(XLog log, String attributeKey, String operator, String aggregationFunc,
			boolean noEnd) {
		SankeyModel ungroupedSankey = createSankey(log, attributeKey, operator, aggregationFunc, false, noEnd);
		SankeyModel groupedSankey = createSankey(log, attributeKey, operator, aggregationFunc, true, noEnd);
		return List.of(ungroupedSankey, groupedSankey);
	}

	public SankeyModel createSankey(XLog log, String attributeKey, String operator, String aggregationFunc,
			boolean grouping, boolean noEnd) {
		RelationToValuesMap relationsToValues = new RelationToValuesMap(attributeKey, operator);
		XEventClassifier classifier = LogProcessor.getClassifier(log);
		if (grouping) {
			TraceGroupsMap groups = findTraceGroups(log, attributeKey, classifier);
			for (XTrace trace : log) {
				relationsToValues = this.groupedRelationToValues(trace, attributeKey, operator, groups,
						relationsToValues, classifier, noEnd);
			}

		} else {
			for (XTrace trace : log) {
				relationsToValues = LogProcessor.relationToValues(trace, attributeKey, operator, true,
						relationsToValues, classifier, noEnd);
			}
		}

		// Links
		HashMap<Relation, Double> links = relationsToValues.aggregate(aggregationFunc);

		// Nodes
		List<String> nodesList = links.keySet().stream().flatMap(l -> l.eventNames.stream()).distinct()
				.collect(Collectors.toList());

		return new SankeyModel(nodesList, links);

	}

//	private XLog reverseTraces(XLog log) {
//		XLog reversedLog = new XLogImpl(new XAttributeMapImpl());
//		for (XTrace trace : log) {
//			Collections.reverse(trace);
//			reversedLog.add(trace);
//
//		}
//		return reversedLog;
//	}

	public RelationToValuesMap groupedRelationToValues(XTrace trace, String attrKey, String operator,
			TraceGroupsMap groups, RelationToValuesMap relationToValuesMap, XEventClassifier classifier,
			boolean noEnd) {
		// If trace has 1 event only
		if (trace.size() <= 1 && !noEnd) {
			if (!operator.equals("COUNT"))
				return relationToValuesMap;
			// Creating the link
			XEvent sourceEvent = trace.get(0);
//			XAttribute sourceAttribute = sourceEvent.getAttributes().get(attrKey);
			Relation relation = new Relation();
			relation.events.add(sourceEvent);
			relation.events.add(null);

			// Naming the source node based on the grouping
			String sourceAttributeName = LogProcessor.makeNodeLabel(sourceEvent, attrKey, classifier);

			String sourceGroupName = groups.makeGroupName(trace, attrKey, 0, classifier);
			relation.eventNames.add(sourceAttributeName + "_0_" + "(" + sourceGroupName + ")");

			// Adding a target node
			relation.eventNames.add("End");

			// The value (1.0 is the only valid value)
//			double value = 1.0;

			// Adding the relation to the map
			if (relationToValuesMap.map.containsKey(relation)) {
				List<Double> values = relationToValuesMap.map.get(relation);
				values.add(1.0);
				relationToValuesMap.map.put(relation, values);
			} else {
				List<Double> values = new ArrayList<>();
				values.add(1.0);
				relationToValuesMap.map.put(relation, values);
			}

			return relationToValuesMap;
		}

		// If the trace has 2 or more events
		else {
			// Iterating over events
			for (int i = 0; i < trace.size() - 1; i++) {
				XEvent sourceEvent = trace.get(i);
				XEvent targetEvent = trace.get(i + 1);
				XAttribute sourceAttribute = sourceEvent.getAttributes().get(attrKey);
				XAttribute targetAttribute = targetEvent.getAttributes().get(attrKey);

				// Creating the link using activity name
				Relation relation = new Relation();
				relation.events.add(sourceEvent);
				relation.events.add(targetEvent);

				// Naming the nodes based on the grouping
				String sourceAttributeName = LogProcessor.makeNodeLabel(sourceEvent, attrKey, classifier);
				String targetAttributeName = LogProcessor.makeNodeLabel(targetEvent, attrKey, classifier);

				String sourceGroupName = groups.makeGroupName(trace, attrKey, i, classifier);
				String targetGroupName = groups.makeGroupName(trace, attrKey, i + 1, classifier);
				relation.eventNames.add(sourceAttributeName + "_" + (i) + "_" + "(" + sourceGroupName + ")");
				relation.eventNames.add(targetAttributeName + "_" + (i + 1) + "_" + "(" + targetGroupName + ")");

				// Computing the value
				double value = Double.NaN;
				if (operator.equals("SUM")) {
					value = AttributeOperations.sum(sourceAttribute, targetAttribute);
				} else if (operator.equals("DIFF")) {
					value = AttributeOperations.diff(sourceAttribute, targetAttribute);
				} else if (operator.equals("COUNT")) {
					value = 1.0;
				}

				// Adding the relation to the map
				if (relationToValuesMap.map.containsKey(relation)) {
					List<Double> values = relationToValuesMap.map.get(relation);
					values.add(value);
					relationToValuesMap.map.put(relation, values);
				} else {
					List<Double> values = new ArrayList<>();
					values.add(value);
					relationToValuesMap.map.put(relation, values);
				}

				// If its the last relation in the trace, create an extra relation from the last
				// event to the End event
				/*
				 * if(i == trace.size() - 2){ App.Shared.Relation endRelation = new
				 * App.Shared.Relation(); endRelation.events.add(targetEvent);
				 * endRelation.events.add(null);
				 * endRelation.eventNames.add(relation.eventNames.get(1));
				 * endRelation.eventNames.add("End");
				 * 
				 * if (relationToValuesMap.map.containsKey(endRelation)) { List<Double> values =
				 * relationToValuesMap.map.get(endRelation); values.add(1.0);
				 * relationToValuesMap.map.put(endRelation, values); } else { List<Double>
				 * values = new ArrayList<>(); values.add(1.0);
				 * relationToValuesMap.map.put(endRelation, values); }
				 * 
				 * }
				 */

			}

			return relationToValuesMap;

		}

	}

	/**
	 * Finds the TraceGroups for a given event log and returns a TraceGroupsMap with
	 * the found trace groups.
	 *
	 * @param log An event log of type XLog or List<XTrace>
	 * @return A TraceGroupMap that maps the index to which the traces share their
	 *         first activities to the list of App.Sankey.TraceGroup
	 */
	public TraceGroupsMap findTraceGroups(List<XTrace> log, String attrKey, XEventClassifier classifier) {
		HashMap<Integer, List<TraceGroup>> groupsMap = new HashMap<>();
		List<TraceGroup> groupsMaster = new ArrayList<>();
		TraceGroup initialGroup = new TraceGroup();
		initialGroup.traces = log;
		groupsMaster.add(initialGroup);
		int position = 0;
		while (!groupsMaster.isEmpty()) {
			List<TraceGroup> newMaster = new ArrayList<>();
			for (TraceGroup group : groupsMaster) {
				List<TraceGroup> newGroups = groupBynthActivity(group, attrKey, position, classifier);
				for (TraceGroup newGroup : newGroups) {
					if (newGroup.traces.size() > 1) {
						// Updating the groupsMap with the new groups
						if (groupsMap.containsKey(position)) {
							List<TraceGroup> traceGroups = groupsMap.get(position);
							traceGroups.add(newGroup);
							groupsMap.put(position, traceGroups);
						} else {
							List<TraceGroup> newTraceGroups = new ArrayList<>();
							newTraceGroups.add(newGroup);
							groupsMap.put(position, newTraceGroups);
						}
						// Adding the new groups to the newMaster so they can be divided further
						newMaster.add(newGroup);
					}
				}
			}
			groupsMaster = newMaster;
			position++;
		}
		TraceGroupsMap traceGroupsMap = new TraceGroupsMap();
		traceGroupsMap.map = groupsMap;
		return traceGroupsMap;
	}

	/**
	 * Divides a given App.Sankey.TraceGroup into more refined trace groups. i.e. if
	 * the given App.Sankey.TraceGroup shares activities up to index 3, The returned
	 * list of groups that contain >=2 traces share activities up to index 4.
	 *
	 * @param group The App.Sankey.TraceGroup to be divided/refined
	 * @param n     The index that the grouping is based on, which equals the
	 *              current index of the given App.Sankey.TraceGroup + 1
	 * @return A list of App.Sankey.TraceGroup. Each App.Sankey.TraceGroup in that
	 *         list that contains 2 or more traces share the first n activities.
	 */
	public List<TraceGroup> groupBynthActivity(TraceGroup group, String attrKey, int n, XEventClassifier classifier) {
		// Initializing the list to be returned
		List<TraceGroup> traceGroups = new ArrayList<>();
		// Traces that are shorter or equal to n are immediately made into a new
		// App.Sankey.TraceGroup and added to the
		// list to be returned. Longer traces are kept for the grouping.
		TraceGroup longGroups = new TraceGroup();
		for (XTrace trace : group.traces) {
			if (trace.size() <= n) {
				TraceGroup shortGroup = new TraceGroup();
				shortGroup.traces.add(trace);
				traceGroups.add(shortGroup);
			} else {
				longGroups.traces.add(trace);
			}
		}
		// Grouping the longer traces
		List<List<XTrace>> groups = longGroups.traces.stream()
				.collect(Collectors.groupingBy(trace -> LogProcessor.makeNodeLabel(trace.get(n), attrKey, classifier)))
				.values().stream().collect(Collectors.toList());

		// Constructing the new trace groups that share their attribute up to index n.
		for (List<XTrace> newGroup : groups) {
			TraceGroup newTraceGroup = new TraceGroup();
			newTraceGroup.traces = newGroup;
			newTraceGroup.index = n;
			traceGroups.add(newTraceGroup);
		}

		return traceGroups;
	}
}
