package probe;
import java.util.*;

/** A representation of a call graph. */
public class CallGraph {
    /** @return The (mutable) set of ProbeMethod's that are the entry points
     * of the call graph. */
    public Set/*ProbeMethod*/ entryPoints() {
        return entryPoints;
    }
    /** @return The (mutable) set of call edges in the call graph. */
    public Set/*CallEdge*/ edges() {
        return edges;
    }

    /** Returns a set of those methods that are transitively reachable in the
     * call graph from its entry points. */
    public Set findReachables() {
        Set reachables = new HashSet(entryPoints());
        while(true) {
            Set newReachables = new HashSet();
            for( Iterator eIt = edges().iterator(); eIt.hasNext(); ) {
                final CallEdge e = (CallEdge) eIt.next();
                if(reachables.contains(e.src()) && !reachables.contains(e.dst())) {
                    newReachables.add(e.dst());
                }
            }
            if( newReachables.isEmpty() ) break;
            reachables.addAll(newReachables);
        }
        return reachables;
    }

    /* End of public methods. */

    private Set entryPoints = new HashSet();
    private Set edges = new HashSet();
}

