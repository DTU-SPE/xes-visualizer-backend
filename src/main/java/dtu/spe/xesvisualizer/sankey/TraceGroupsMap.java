package dtu.spe.xesvisualizer.sankey;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import dtu.spe.xesvisualizer.shared.LogProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contains a map that maps an integer to a list of App.Sankey.TraceGroup. The
 * integer represents the index to which the trace groups have the same
 * activities. e.g. The entry 3 -> [[T1, T2], [T3, T4, T5]] means that [T1, T2]
 * have the same first 4 activities, [T3, T4, T5] also share their first 4
 * activities, however they don't match with [T1, T2].
 */
public class TraceGroupsMap {
	HashMap<Integer, List<TraceGroup>> map;

	/**
	 * Constructs an string for an activity using the given trace and activity's
	 * position. If the activity is a part of a group, the returned group name is of
	 * the form: G{position}{activities up to position} If an activity does not
	 * belong to any group, the function returns the trace name instead. The
	 * returned string is used to label nodes in the App.Sankey.SankeyModel diagram
	 * so that activities belonging to the same group get the same node.
	 *
	 * @param trace    The trace of the activity
	 * @param position The position of the activity
	 * @return A String which either represents the group name or the trace name (if
	 *         the activity has no group).
	 */
	public String makeGroupName(XTrace trace, String attrKey, int position, XEventClassifier classifier) {
		// Initialize the groupName to be the trace name
		String groupName = trace.getAttributes().get("concept:name").toString();
		// Check whether 2 or more traces share their activities up to "position", if
		// not return the trace name.
		if (!map.containsKey(position))
			return groupName;
		else {
			// Get the trace groups
			List<TraceGroup> traceGroups = map.get(position);
			for (TraceGroup traceGroup : traceGroups) {
				// Checks whether the given trace is part of the groups that share these
				// activities
				if (traceGroup.traces.contains(trace)) {
					// Constructing the group name
					List<String> groupNameList = new ArrayList<>();
					groupNameList.add("G");
					groupNameList.add(String.valueOf(position));
					groupNameList.add("_");
					// Getting the sequence of the common activities and adding them to the name
					// list
					for (int i = 0; i <= position; i++) {
						XEvent event = traceGroup.traces.get(0).get(i);
						String name = LogProcessor.makeNodeLabel(event, attrKey, classifier);
						groupNameList.add(name);
					}
					groupName = groupNameList.stream().collect(Collectors.joining());
				}
			}
		}
		return groupName;
	}
	
	public Map<Integer, List<TraceGroup>> getMap() {
		return map;
	}
}