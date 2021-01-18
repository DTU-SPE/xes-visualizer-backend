package dtu.spe.test.xesvisualizer.shared;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.junit.Before;
import org.junit.Test;

import dtu.spe.xesvisualizer.shared.AttributeOperations;

public class AttributeOperationsTest {
	XLog testLog;

	@Before
	public void setUp() throws Exception {
		File testFile = new File("src/test/java/TestResources/call_center_log.xes");
		XesXmlParser parser = new XesXmlParser();
		testLog = parser.parse(testFile).get(0);
	}

	@Test
	public void sum() {
		XTrace testTrace = testLog.get(0);
		XEvent testEvent1 = testTrace.get(0);
		XEvent testEvent2 = testTrace.get(1);

		// String Sum
		double stringResult = AttributeOperations.sum(testEvent1.getAttributes().get("concept:name"),
				testEvent2.getAttributes().get("concept:name"));
		assertEquals(Double.NaN, stringResult, 0);

		// Date Sum
		double dateResult = AttributeOperations.sum(testEvent1.getAttributes().get("time:timestamp"),
				testEvent2.getAttributes().get("time:timestamp"));
		assertEquals(4.2273635E7, dateResult, 0);

		// Int Sum
		double intResult = AttributeOperations.sum(testLog.get(0).getAttributes().get("variant-index"),
				testLog.get(1).getAttributes().get("variant-index"));
		assertEquals(6.0, intResult, 0);

	}

	@Test
	public void diff() {

		XTrace testTrace = testLog.get(0);
		XEvent testEvent1 = testTrace.get(0);
		XEvent testEvent2 = testTrace.get(1);

		// String Diff
		double stringResult = AttributeOperations.diff(testEvent1.getAttributes().get("concept:name"),
				testEvent2.getAttributes().get("concept:name"));
		assertEquals(Double.NaN, stringResult, 0);

		// Date Diff
		double dateResult = AttributeOperations.diff(testEvent1.getAttributes().get("time:timestamp"),
				testEvent2.getAttributes().get("time:timestamp"));
		assertEquals(3025.0, dateResult, 0);

		// Int Diff
		double intResult = AttributeOperations.diff(testLog.get(0).getAttributes().get("variant-index"),
				testLog.get(1).getAttributes().get("variant-index"));
		assertEquals(4.0, intResult, 0);
	}
}