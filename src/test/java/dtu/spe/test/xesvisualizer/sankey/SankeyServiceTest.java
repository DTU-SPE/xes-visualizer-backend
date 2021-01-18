package dtu.spe.test.xesvisualizer.sankey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.junit.Before;
import org.junit.Test;

import dtu.spe.xesvisualizer.sankey.SankeyModel;
import dtu.spe.xesvisualizer.sankey.SankeyService;
import dtu.spe.xesvisualizer.sankey.TraceGroup;
import dtu.spe.xesvisualizer.sankey.TraceGroupsMap;
import dtu.spe.xesvisualizer.shared.LogProcessor;
import dtu.spe.xesvisualizer.shared.Relation;
import dtu.spe.xesvisualizer.shared.RelationToValuesMap;

public class SankeyServiceTest {
	XLog testLog;
	XLog smallLog;

	@Before
	public void setUp() throws Exception {
		File testFile = new File("src/test/java/TestResources/call_center_log.xes");
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
	public void createSankey() {
		SankeyService service = new SankeyService();

		// Activity + COUNT + SUM
		List<SankeyModel> outputModels = service.createSankey(smallLog, "Activity", "COUNT", "SUM", false);

		SankeyModel ungroupedModel = outputModels.get(0);
		String ungroupedJSON = ungroupedModel.toJSONString();
		String expectedUngroupedJSON = "{\"nodes\":[{\"name\":\"Handle Case_1\"},{\"name\":\"Call Outbound_2\"},{\"name\":\"Handle Case_3\"},{\"name\":\"Inbound Call_4\"},{\"name\":\"Handle Case_2\"},{\"name\":\"Inbound Call_0\"}]"
				+ ","
				+ "\"links\":[{\"source\":\"Handle Case_1\",\"value\":1,\"target\":\"Call Outbound_2\"},{\"source\":\"Handle Case_3\",\"value\":1,\"target\":\"Inbound Call_4\"},{\"source\":\"Handle Case_1\",\"value\":1,\"target\":\"Handle Case_2\"},{\"source\":\"Handle Case_2\",\"value\":1,\"target\":\"Handle Case_3\"},{\"source\":\"Inbound Call_0\",\"value\":2,\"target\":\"Handle Case_1\"}]}";
		assertEquals(expectedUngroupedJSON, ungroupedJSON);

		SankeyModel groupedModel = outputModels.get(1);
		String groupedJSON = groupedModel.toJSONString();
		String expectedGroupedJSON = "{\"nodes\":[{\"name\":\"Inbound Call_0_(G0_Inbound Call)\"},{\"name\":\"Handle Case_1_(G1_Inbound CallHandle Case)\"},{\"name\":\"Call Outbound_2_(Case1)\"},{\"name\":\"Handle Case_2_(Case17)\"},{\"name\":\"Handle Case_3_(Case17)\"},{\"name\":\"Inbound Call_4_(Case17)\"}]"
				+ ","
				+ "\"links\":[{\"source\":\"Inbound Call_0_(G0_Inbound Call)\",\"value\":2,\"target\":\"Handle Case_1_(G1_Inbound CallHandle Case)\"},{\"source\":\"Handle Case_1_(G1_Inbound CallHandle Case)\",\"value\":1,\"target\":\"Call Outbound_2_(Case1)\"},{\"source\":\"Handle Case_2_(Case17)\",\"value\":1,\"target\":\"Handle Case_3_(Case17)\"},{\"source\":\"Handle Case_1_(G1_Inbound CallHandle Case)\",\"value\":1,\"target\":\"Handle Case_2_(Case17)\"},{\"source\":\"Handle Case_3_(Case17)\",\"value\":1,\"target\":\"Inbound Call_4_(Case17)\"}]}";
		assertEquals(expectedGroupedJSON, groupedJSON);

		// time:timestamp + DIFF + AVG

		List<SankeyModel> outputModelsTime = service.createSankey(smallLog, "time:timestamp", "DIFF", "AVG", false);
		SankeyModel ungroupedModelTime = outputModelsTime.get(0);
		String ungroupedJSONTime = ungroupedModelTime.toJSONString();
		String expectedUngroupedJSONTime = "{\"nodes\":[{\"name\":\"Handle Case_1\"},{\"name\":\"Call Outbound_2\"},{\"name\":\"Handle Case_3\"},{\"name\":\"Inbound Call_4\"},{\"name\":\"Handle Case_2\"},{\"name\":\"Inbound Call_0\"}],"
				+ ""
				+ "\"links\":[{\"source\":\"Handle Case_1\",\"value\":75,\"target\":\"Call Outbound_2\"},{\"source\":\"Handle Case_3\",\"value\":4718,\"target\":\"Inbound Call_4\"},{\"source\":\"Handle Case_1\",\"value\":5963,\"target\":\"Handle Case_2\"},{\"source\":\"Handle Case_2\",\"value\":4319,\"target\":\"Handle Case_3\"},{\"source\":\"Inbound Call_0\",\"value\":1521.5,\"target\":\"Handle Case_1\"}]}";
		assertEquals(expectedUngroupedJSONTime, ungroupedJSONTime);

		SankeyModel groupedModelTime = outputModelsTime.get(1);
		String groupedJSONTime = groupedModelTime.toJSONString();
		String expectedGroupedJSONTime = "{\"nodes\":[{\"name\":\"Inbound Call_0_(G0_Inbound Call)\"},{\"name\":\"Handle Case_1_(G1_Inbound CallHandle Case)\"},{\"name\":\"Call Outbound_2_(Case1)\"},{\"name\":\"Handle Case_2_(Case17)\"},{\"name\":\"Handle Case_3_(Case17)\"},{\"name\":\"Inbound Call_4_(Case17)\"}],"
				+ "\"links\":[{\"source\":\"Inbound Call_0_(G0_Inbound Call)\",\"value\":1521.5,\"target\":\"Handle Case_1_(G1_Inbound CallHandle Case)\"},{\"source\":\"Handle Case_1_(G1_Inbound CallHandle Case)\",\"value\":75,\"target\":\"Call Outbound_2_(Case1)\"},{\"source\":\"Handle Case_2_(Case17)\",\"value\":4319,\"target\":\"Handle Case_3_(Case17)\"},{\"source\":\"Handle Case_1_(G1_Inbound CallHandle Case)\",\"value\":5963,\"target\":\"Handle Case_2_(Case17)\"},{\"source\":\"Handle Case_3_(Case17)\",\"value\":4718,\"target\":\"Inbound Call_4_(Case17)\"}]}";
		assertEquals(expectedGroupedJSONTime, groupedJSONTime);

	}

	@Test
	public void groupedRelationToValues() {
		SankeyService service = new SankeyService();
		RelationToValuesMap map = new RelationToValuesMap("Activity", "COUNT");
		TraceGroupsMap groups = service.findTraceGroups(smallLog, "Activity", smallLog.getClassifiers().get(0));
		map = service.groupedRelationToValues(smallLog.get(0), "Activity", "COUNT", groups, map,
				smallLog.getClassifiers().get(0), false);

		assertEquals(2, map.map.size());

		Relation firstRelation = new Relation();
		firstRelation.eventNames.add("Inbound Call_0_(G0_Inbound Call)");
		firstRelation.eventNames.add("Handle Case_1_(G1_Inbound CallHandle Case)");

		Relation secondRelation = new Relation();
		secondRelation.eventNames.add("Handle Case_1_(G1_Inbound CallHandle Case)");
		secondRelation.eventNames.add("Call Outbound_2_(Case1)");

		assertTrue(map.map.containsKey(firstRelation));
		assertTrue(map.map.containsKey(secondRelation));

	}

	@Test
	public void findTraceGroups() {
		SankeyService service = new SankeyService();
		TraceGroupsMap groups = service.findTraceGroups(smallLog, "concept:name", LogProcessor.getClassifier(testLog));
		assertEquals(2, groups.getMap().size());
		List<TraceGroup> zeroIndexGroups = groups.getMap().get(0);
		List<TraceGroup> zeroAndFirstIndexGroups = groups.getMap().get(1);

		assertEquals(1, zeroIndexGroups.size());
		assertEquals(1, zeroAndFirstIndexGroups.size());

		assertEquals(zeroIndexGroups.get(0).getTraces().size(), 2);
		assertEquals(zeroAndFirstIndexGroups.get(0).getTraces().size(), 2);

	}

	@Test
	public void groupBynthActivity() {
		SankeyService service = new SankeyService();
		TraceGroup traceGroup = new TraceGroup();
		traceGroup.addTrace(smallLog.get(0));
		traceGroup.addTrace(smallLog.get(1));
		traceGroup.setIndex(0);
		List<TraceGroup> traceGroups = service.groupBynthActivity(traceGroup, "Activity", 1,
				smallLog.getClassifiers().get(0));
		assertEquals(1, traceGroups.size());

		List<TraceGroup> traceGroups2 = service.groupBynthActivity(traceGroup, "Activity", 2,
				smallLog.getClassifiers().get(0));
		assertEquals(2, traceGroups2.size());

	}
}