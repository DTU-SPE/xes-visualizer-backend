package dtu.spe.xesvisualizer.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

/**
 * Holds different methods to process an event log of type XLog
 */
public class LogProcessor {

	public static List<String> getValidAttributeKeys(XLog log) {
		List<String> validAttr = new ArrayList<>();
		for (XAttribute attr : log.getGlobalEventAttributes()) {
			if ((attr instanceof XAttributeLiteral) || (attr instanceof XAttributeDiscrete)
					|| (attr instanceof XAttributeContinuous) || (attr instanceof XAttributeTimestamp)) {
				validAttr.add(attr.getKey());
			}
		}
		return validAttr;
	}

	public static String makeNodeLabel(XEvent event, String attrKey, XEventClassifier classifier) {
		XAttribute attr = event.getAttributes().get(attrKey);
		if (attr instanceof XAttributeLiteral) {
			return attr.toString();
		} else {
			return classifier.getClassIdentity(event);
		}
	}

	public static XEventClassifier getClassifier(XLog log) {
		List<XEventClassifier> classifiers = log.getClassifiers();
		// If there's only 1 classifier, return it
		if (classifiers.size() == 1) {
			return classifiers.get(0);
		} else {
			// Grouping classifiers based on the number of attributes they use
			List<XEventClassifier> singleAttrClassifiers = new ArrayList<>();
			List<XEventClassifier> multAttrClassifiers = new ArrayList<>();
			for (XEventClassifier classifier : classifiers) {
				if (classifier.getDefiningAttributeKeys().length == 1) {
					singleAttrClassifiers.add(classifier);
				} else {
					multAttrClassifiers.add(classifier);
				}
			}

			// Looping through the singles first
			for (XEventClassifier classifier : singleAttrClassifiers) {
				String attrKey = classifier.getDefiningAttributeKeys()[0];
				if (attrKey.equals("concept:name") || attrKey.equals("Activity") || attrKey.equals("activity")
						|| attrKey.equals("name")) {
					return classifier;
				}

			}

			// Looping through the mults
			for (XEventClassifier classifier : multAttrClassifiers) {
				for (String key : classifier.getDefiningAttributeKeys()) {
					if (key.equals("concept:name") || key.equals("Activity") || key.equals("activity")
							|| key.equals("name")) {
						return classifier;
					}
				}
			}

			// Have looped through all classifeirs, and none of them have a concept or
			// activity attribute
			return classifiers.get(0);

		}
	}


	public static boolean sameTrace(XLog log, XTrace t1, XTrace t2) {
		if (t1.size() != t2.size())
			return false;
		XEventClassifier classifier = log.getClassifiers().get(0);
		for (int i = 0; i < t1.size(); i++) {
			XEvent e1 = t1.get(i);
			XEvent e2 = t2.get(i);
			if (!classifier.sameEventClass(e1, e2))
				return false;
		}
		return true;
	}

	public static VariantMap findVariants(XLog log) {
		VariantMap variants = new VariantMap(log);
		for (XTrace trace : log) {
			variants.update(trace);
		}
		return variants;
	}

	public static XLog combineTraces(List<XTrace> traces) {
		XFactoryBufferedImpl xFactory = new XFactoryBufferedImpl();
		XLog log = xFactory.createLog();
		for (XTrace trace : traces) {
			log.add(trace);
		}
		return log;
	}

	public static HashMap<Double, XLog> combineVariants(List<Variant> variants, XLog originalLog) {
		XFactoryBufferedImpl xFactory = new XFactoryBufferedImpl();
		XLog log = xFactory.createLog();
		double percentage = 0.0;
		for (Variant v : variants) {
			percentage += v.percentage;
			for (XTrace trace : v.traces) {
				log.add(trace);
			}
		}

		List<XEventClassifier> classifiers = originalLog.getClassifiers();
		for (XEventClassifier c : classifiers) {
			log.getClassifiers().add(c);
		}
		HashMap<Double, XLog> result = new HashMap<>();
		result.put(percentage, log);
		return result;
		/*    List<XTrace> traces = variants.stream().flatMap(v -> v.traces.stream()).collect(Collectors.toList());
		return LogProcessor.combineTraces(traces);    */
	}

	public static HashMap<Double, XLog> createSubLogs(XLog originalLog, VariantMap variants) {
		HashMap<Double, XLog> result = new HashMap<>();
		List<Variant> rankedVariants = variants.variants.values().stream()
				.sorted(Comparator.comparingDouble(Variant::getPercentage).reversed()).collect(Collectors.toList());
		for (int i = 1; i <= rankedVariants.size(); i++) {
			List<Variant> subList = rankedVariants.subList(0, i);
			HashMap<Double, XLog> subLog = LogProcessor.combineVariants(subList, originalLog);
			Double percentage = LogProcessor.round(subLog.keySet().iterator().next(), 2);
			XLog log = subLog.values().iterator().next();
			result.put(percentage, log);
		}

		return result;
	}



/*    public static HashMap<Double, XLog> createSubLogs(XLog originalLog, VariantMap variants){
        int numOfTraces = originalLog.size();
        XFactoryBufferedImpl xFactory = new XFactoryBufferedImpl();
        HashMap<Double, XLog> subLogs = new HashMap<>();
        for(Map.Entry<String, Variant> entry : variants.variants.entrySet()){
            double percentage = entry.getValue().frequency / numOfTraces;
            XTrace trace = entry.getValue().traces.get(0);
            XLog log = xFactory.createLog();
            log.add(trace);
        }
    }*/


    /**
     * Iterates through a trace and populates the App.Shared.RelationToValuesMap with entries in the form
     * [attribute 1, attribute 2] --> [double 1, double 2,..] based on the chosen attribute and
     * the chosen operation to preform on their values.
     *
     * @param trace               An event log trace of type XTrace
     * @param attrKey             The key for the chosen attribute.
     * @param operator            The operator to apply on attribute' values.
     * @param duplicates          Specifies whether duplicate event should be uniquely identified.
     * @param relationToValuesMap The HashMap that gets populated with the above described entries.
     * @return A App.Shared.RelationToValuesMap HashMap with the new entries extracted from the trace.
     */

    public static RelationToValuesMap relationToValues(XTrace trace, String attrKey, String operator, boolean duplicates,
                                                       RelationToValuesMap relationToValuesMap, XEventClassifier classifier,boolean noEnd) {
        // If trace has 1 event only
        if (trace.size() <= 1 && !noEnd) {
            if (!operator.equals("COUNT")) return relationToValuesMap;
            // Creating the link
            XEvent sourceEvent = trace.get(0);
//            XAttribute sourceAttribute = sourceEvent.getAttributes().get(attrKey);
            Relation relation = new Relation();
            relation.events.add(sourceEvent);
            relation.events.add(null);

            // Naming the source node based on the given configuration
            String sourceAttributeName = makeNodeLabel(sourceEvent, attrKey, classifier);

            if (duplicates) {
                relation.eventNames.add(sourceAttributeName + "_0");

            } else {
                relation.eventNames.add(sourceAttributeName);
            }

            relation.eventNames.add("End");
            // The value (1.0 is the only valid value)
            double value = 1.0;
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

                // Naming the nodes based on the given configuration

                String sourceAttributeName = makeNodeLabel(sourceEvent, attrKey, classifier);
                String targetAttributeName = makeNodeLabel(targetEvent, attrKey, classifier);



                if (duplicates) {
                    relation.eventNames.add(sourceAttributeName + "_" + (i));
                    relation.eventNames.add(targetAttributeName + "_" + (i + 1));
                } else {
                    relation.eventNames.add(sourceAttributeName);
                    relation.eventNames.add(targetAttributeName);
                }


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

                // If its the last relation in the trace, create an extra relation from the last event to the End event
/*                if(i == trace.size() - 2){
                    App.Shared.Relation endRelation = new App.Shared.Relation();
                    endRelation.events.add(targetEvent);
                    endRelation.events.add(null);
                    endRelation.eventNames.add(relation.eventNames.get(1));
                    endRelation.eventNames.add("End");

                    if (relationToValuesMap.map.containsKey(endRelation)) {
                        List<Double> values = relationToValuesMap.map.get(endRelation);
                        values.add(1.0);
                        relationToValuesMap.map.put(endRelation, values);
                    } else {
                        List<Double> values = new ArrayList<>();
                        values.add(1.0);
                        relationToValuesMap.map.put(endRelation, values);
                    }
                }*/
            }
            return relationToValuesMap;
        }
    }

	// Credit: https://www.baeldung.com/java-round-decimal-number
	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(Double.toString(value));
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

}

