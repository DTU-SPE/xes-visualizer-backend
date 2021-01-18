package dtu.spe.xesvisualizer.shared;

import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class VariantMap {
	
	// Variant ID -> Variant
	public HashMap<String, Variant> variants;
	XLog log;

	public VariantMap(XLog log) {
		this.variants = new HashMap<>();
		this.log = log;
	}

	public void update(XTrace trace) {
		boolean variantExists = false;

		for (Map.Entry<String, Variant> entry : this.variants.entrySet()) {
			XTrace entryTrace = entry.getValue().traces.get(0);
			if (LogProcessor.sameTrace(this.log, trace, entryTrace)) {
				variantExists = true;
				Variant variant = entry.getValue();
				variant.traces.add(trace);
				variant.frequency++;
				variant.percentage = variant.frequency / this.log.size();
				this.variants.put(variant.ID, variant);
				break;
			}
		}
		if (!variantExists) {
			Variant newVariant = new Variant();
			String ID = "V" + this.variants.size();
			newVariant.ID = ID;
			newVariant.traces.add(trace);
			newVariant.frequency++;
			newVariant.percentage = newVariant.frequency / this.log.size();
			this.variants.put(ID, newVariant);
		}
	}
}