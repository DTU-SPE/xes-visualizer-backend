package dtu.spe.xesvisualizer.shared;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeTimestamp;

/**
 * Defines the operations that can be done between log attribute values based on
 * the attribute type.
 */
public class AttributeOperations {

	public static double sum(XAttribute attr1, XAttribute attr2) {
		double result = Double.NaN;

		// int type
		if ((attr1 instanceof XAttributeDiscrete) && attr2 instanceof XAttributeDiscrete) {
			long tempResult = ((XAttributeDiscrete) attr1).getValue() + ((XAttributeDiscrete) attr2).getValue();
			result = (double) tempResult;
		}

		// float type
		else if ((attr1 instanceof XAttributeContinuous) && attr2 instanceof XAttributeContinuous) {
			result = ((XAttributeContinuous) attr1).getValue() + ((XAttributeContinuous) attr2).getValue();
		}

		// date type
		else if ((attr1 instanceof XAttributeTimestamp) && (attr2 instanceof XAttributeTimestamp)) {
			long tempResult = ((XAttributeTimestamp) attr1).getValueMillis()
					+ ((XAttributeTimestamp) attr2).getValueMillis();
			result = (double) (tempResult / 1000.0) / 60.0;
		}
		return result;
	}

	public static double diff(XAttribute attr1, XAttribute attr2) {
		double result = Double.NaN;
		// int type
		if ((attr1 instanceof XAttributeDiscrete) && attr2 instanceof XAttributeDiscrete) {
			long tempResult = ((XAttributeDiscrete) attr1).getValue() - ((XAttributeDiscrete) attr2).getValue();
			result = (double) tempResult;
		}

		// float type
		else if ((attr1 instanceof XAttributeContinuous) && attr2 instanceof XAttributeContinuous) {
			result = ((XAttributeContinuous) attr1).getValue() - ((XAttributeContinuous) attr2).getValue();
		}

		// date type
		else if ((attr1 instanceof XAttributeTimestamp) && (attr2 instanceof XAttributeTimestamp)) {
			long tempResult = ((XAttributeTimestamp) attr1).getValueMillis()
					- ((XAttributeTimestamp) attr2).getValueMillis();
			result = (double) (tempResult / 1000.0) / 60.0;

		}
		return Math.abs(result);
	}

}
