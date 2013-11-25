package probe;

import java.util.*;

/** A representation of a call graph. */
public class CallGraph {
	/**
	 * @return The (mutable) set of ProbeMethod's that are the entry points of the call graph.
	 */
	public Set<ProbeMethod> entryPoints() {
		return entryPoints;
	}

	/** @return The (mutable) set of call edges in the call graph. */
	public Set<CallEdge> edges() {
		return edges;
	}

	/**
	 * Returns a set of those methods that are transitively reachable in the call graph from its entry points.
	 */
	public Set<ProbeMethod> findReachables() {
		Set<ProbeMethod> reachables = new HashSet<ProbeMethod>(entryPoints());
		while (true) {
			Set<ProbeMethod> newReachables = new HashSet<ProbeMethod>();
			for (CallEdge edge : edges()) {
				if (reachables.contains(edge.src()) && !reachables.contains(edge.dst())) {
					newReachables.add(edge.dst());
				}
			}
			if (newReachables.isEmpty()) break;
			reachables.addAll(newReachables);
		}
		return reachables;
	}

	/* End of public methods. */

	private Set<ProbeMethod> entryPoints = new HashSet<ProbeMethod>();
	private Set<CallEdge> edges = new HashSet<CallEdge>();
}
