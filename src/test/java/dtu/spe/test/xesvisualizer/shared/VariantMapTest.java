package dtu.spe.test.xesvisualizer.shared;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.junit.Before;
import org.junit.Test;

import dtu.spe.xesvisualizer.shared.LogProcessor;
import dtu.spe.xesvisualizer.shared.VariantMap;

public class VariantMapTest {

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
	public void update() {
		VariantMap variantMap = LogProcessor.findVariants(smallLog);
		XTrace existingTrace = smallLog.get(0);
		XTrace newTrace = testLog.get(1);

		assertEquals(2, variantMap.variants.size());
		variantMap.update(existingTrace);
		assertEquals(2, variantMap.variants.size());

		variantMap.update(newTrace);
		assertEquals(3, variantMap.variants.size());

	}
}