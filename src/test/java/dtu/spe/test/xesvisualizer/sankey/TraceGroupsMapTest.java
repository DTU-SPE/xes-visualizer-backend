package dtu.spe.test.xesvisualizer.sankey;

import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.junit.Before;
import org.junit.Test;

import dtu.spe.xesvisualizer.sankey.SankeyService;
import dtu.spe.xesvisualizer.sankey.TraceGroupsMap;

import java.io.File;

import static org.junit.Assert.*;

public class TraceGroupsMapTest {

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
	public void makeGroupName() {
		SankeyService service = new SankeyService();
		TraceGroupsMap groups = service.findTraceGroups(smallLog, "Activity", smallLog.getClassifiers().get(0));
		String name0 = groups.makeGroupName(smallLog.get(0), "Activity", 0, smallLog.getClassifiers().get(0));
		assertEquals("G0_Inbound Call", name0);
		String name2 = groups.makeGroupName(smallLog.get(0), "Activity", 2, smallLog.getClassifiers().get(0));
		assertEquals("Case1", name2);

	}
}