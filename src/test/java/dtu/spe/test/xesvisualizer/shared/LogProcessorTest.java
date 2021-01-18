package dtu.spe.test.xesvisualizer.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.junit.Before;
import org.junit.Test;

import dtu.spe.xesvisualizer.shared.LogProcessor;
import dtu.spe.xesvisualizer.shared.Relation;
import dtu.spe.xesvisualizer.shared.RelationToValuesMap;
import dtu.spe.xesvisualizer.shared.Variant;
import dtu.spe.xesvisualizer.shared.VariantMap;

public class LogProcessorTest {
	XLog testLog;
	XLog smallLog;

	@Before
	public void setUp() throws Exception {
		File testFile = new File("src/test/resources/call_center_log.xes");
		XesXmlParser parser = new XesXmlParser();
		testLog = parser.parse(testFile).get(0);
		// Trace 1 (Case 1): Inbound Call -> Handle Case -> Call Outbound
		XTrace trace1 = testLog.get(0);
		// Trace 2 (Case 17): Inbound Call -> Handle Case -> Handle Case -> Handle Case
		// Inbound Call
		XTrace trace2 = testLog.get(16);

		// Small log with only 2 traces
		XFactoryBufferedImpl factory = new XFactoryBufferedImpl();
		smallLog = factory.createLog();
		smallLog.getClassifiers().add(testLog.getClassifiers().get(0));
		smallLog.add(trace1);
		smallLog.add(trace2);

	}

	@Test
	public void getValidAttributeKeys() {
		List<String> actual = LogProcessor.getValidAttributeKeys(testLog);
		List<String> expected = new ArrayList<>();
		expected.add("concept:name");
		expected.add("lifecycle:transition");
		expected.add("org:resource");
		expected.add("time:timestamp");
		expected.add("Activity");
		expected.add("Product");
		expected.add("Service_Type");
		expected.add("Resource");

		assertEquals(expected, actual);

	}

	@Test
	public void makeNodeLabel() {
		XEvent event = testLog.get(0).get(0);
		String stringAttrLabel = LogProcessor.makeNodeLabel(event, "Activity", testLog.getClassifiers().get(0));
		assertEquals("Inbound Call", stringAttrLabel);

		String timeLabel = LogProcessor.makeNodeLabel(event, "time:timestamp", testLog.getClassifiers().get(0));
		assertEquals("Inbound Call", timeLabel);

	}

	@Test
	public void getClassifier() {
		XEventClassifier activityClassifier = LogProcessor.getClassifier(testLog);
		assertEquals(activityClassifier, testLog.getClassifiers().get(0));

	}

	@Test
	public void sameTrace() {
		boolean sameTrace = LogProcessor.sameTrace(testLog, testLog.get(0), testLog.get(0));
		boolean differentTrace = LogProcessor.sameTrace(testLog, testLog.get(0), testLog.get(1));

		assertTrue(sameTrace);
		assertTrue(!differentTrace);
	}

	@Test
	public void findVariants() {
		VariantMap smallVariantMap = LogProcessor.findVariants(smallLog);
		assertEquals(2, smallVariantMap.variants.size());

		VariantMap variantMap = LogProcessor.findVariants(testLog);
		assertEquals(20, variantMap.variants.size());

	}

	@Test
	public void combineVariants() {
		VariantMap smallVariantMap = LogProcessor.findVariants(smallLog);
		List<Variant> variants = smallVariantMap.variants.values().stream().collect(Collectors.toList());
		HashMap<Double, XLog> combineVariants = LogProcessor.combineVariants(variants, smallLog);
		double percentage = combineVariants.keySet().iterator().next();
		assertEquals(1.0, percentage, 0);

	}

	@Test
	public void createSubLogs() {
		VariantMap smallVariantMap = LogProcessor.findVariants(smallLog);
		HashMap<Double, XLog> sublogs = LogProcessor.createSubLogs(smallLog, smallVariantMap);
		assertEquals(2, sublogs.size());
		assertTrue(sublogs.containsKey(0.5));
		assertTrue(sublogs.containsKey(1.0));

	}

	@Test
	public void relationToValues() {
		RelationToValuesMap relationToValuesMap = new RelationToValuesMap("Activity", "COUNT");

		relationToValuesMap = LogProcessor.relationToValues(smallLog.get(0), "Activity", "COUNT", false,
				relationToValuesMap, smallLog.getClassifiers().get(0), false);

		List<String> firstRelationNames = new ArrayList<>();
		firstRelationNames.add("Inbound Call");
		firstRelationNames.add("Handle Case");
		Relation firstRelation = new Relation();
		firstRelation.eventNames = firstRelationNames;

		List<String> secondRelationNames = new ArrayList<>();
		secondRelationNames.add("Handle Case");
		secondRelationNames.add("Call Outbound");
		Relation secondRelation = new Relation();
		secondRelation.eventNames = secondRelationNames;

		assertTrue(relationToValuesMap.map.containsKey(secondRelation));

	}

	@Test
	public void round() {
		double roundedNumber = LogProcessor.round(3.1415, 2);
		assertEquals(3.14, roundedNumber, 0);

		double roundedNumber2 = LogProcessor.round(3.99999, 2);
		assertEquals(4.0, roundedNumber2, 0);
	}
}