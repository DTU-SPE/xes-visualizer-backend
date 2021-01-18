package dtu.spe.xesvisualizer.shared;

import org.deckfour.xes.model.XEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A relation is a collection of 2 events that are executed one after another.
 * This data structure holds the list that contains the two events. The event at
 * index 0 is executed first, the event at index 1 is executed afterwards. The
 * class also holds the names of the two events, which can contain different
 * types of labels (e.g. attribute name, variant name, etc.) based on the
 * processing of the logs.
 */
public class Relation {
	
	public List<XEvent> events;
	public List<String> eventNames;

	public Relation() {
		this.events = new ArrayList<>();
		this.eventNames = new ArrayList<>();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Relation relation = (Relation) o;
		return eventNames.equals(relation.eventNames);
	}

	@Override
	public int hashCode() {
		return Objects.hash(eventNames);
	}
}
