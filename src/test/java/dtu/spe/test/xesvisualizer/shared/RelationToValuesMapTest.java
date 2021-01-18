package dtu.spe.test.xesvisualizer.shared;

import org.junit.Before;
import org.junit.Test;

import dtu.spe.xesvisualizer.shared.Relation;
import dtu.spe.xesvisualizer.shared.RelationToValuesMap;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class RelationToValuesMapTest {
	RelationToValuesMap relationToValuesMap;

	@Before
	public void setUp() throws Exception {
		relationToValuesMap = new RelationToValuesMap("Attribute", "OP");
		Relation mockRelation = new Relation();
		mockRelation.eventNames = Stream.of("A", "B").collect(Collectors.toList());
		List<Double> values = Stream.of(1.0, 2.0, 3.0, 4.0, 5.0).collect(Collectors.toList());
		relationToValuesMap.map.put(mockRelation, values);
	}

	@Test
	public void aggregateSum() {
		double actual = relationToValuesMap.aggregateSum().values().iterator().next();
		assertEquals(15.0, actual, 0);
	}

	@Test
	public void aggregateMin() {
		double actual = relationToValuesMap.aggregateMin().values().iterator().next();
		assertEquals(1.0, actual, 0);
	}

	@Test
	public void aggregateMax() {
		double actual = relationToValuesMap.aggregateMax().values().iterator().next();
		assertEquals(5.0, actual, 0);
	}

	@Test
	public void aggregateAvg() {
		double actual = relationToValuesMap.aggregateAvg().values().iterator().next();
		assertEquals(3.0, actual, 0);
	}

	@Test
	public void aggregate() {
		double actualSUM = relationToValuesMap.aggregate("SUM").values().iterator().next();
		double actualMIN = relationToValuesMap.aggregate("MIN").values().iterator().next();
		double actualMAX = relationToValuesMap.aggregate("MAX").values().iterator().next();
		double actualAVG = relationToValuesMap.aggregate("AVG").values().iterator().next();

		assertEquals(15.0, actualSUM, 0);
		assertEquals(1.0, actualMIN, 0);
		assertEquals(5.0, actualMAX, 0);
		assertEquals(3.0, actualAVG, 0);

	}

}