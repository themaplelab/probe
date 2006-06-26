package probe;
/** Represents a call edge in a call graph. */

public class CallEdge implements Comparable {
    /** @param src The method that is the source of the call.
     * @param dst The method that is the target of the call. */
    public CallEdge( ProbeMethod src, ProbeMethod dst ) {
        this.src = src;
        this.dst = dst;
    }
    /** @param src The method that is the source of the call.
     * @param dst The method that is the target of the call.
     * @param weight Optional value expressing the importance of this edge
     * for sorting purposes. */
    public CallEdge( ProbeMethod src, ProbeMethod dst, double weight ) {
        this.src = src;
        this.dst = dst;
        this.weight = weight;
    }
    /** Returns the method that is the source of the call. */
    public ProbeMethod src() { return src; }
    /** Returns the method that is the target of the call. */
    public ProbeMethod dst() { return dst; }
    /** An optional weight value expressing how important this edge is
     * for sorting purposes. */
    public double weight() { return weight; }

    public int hashCode() { return src.hashCode()+dst.hashCode(); }
    public boolean equals( Object o ) {
        if( !(o instanceof CallEdge) ) return false;
        CallEdge other = (CallEdge) o;
        if( !src.equals(other.src) ) return false;
        if( !dst.equals(other.dst) ) return false;
        return true;
    }
    public String toString() {
        if( weight != 0 ) return src.toString() + " ===> " + dst.toString() + 
            " " + weight;
        return src.toString() + " ===> " + dst.toString();
    }
    public int compareTo(Object o) {
        if( !(o instanceof CallEdge) ) throw new RuntimeException();
        CallEdge e = (CallEdge) o;
        if( weight < e.weight ) return -1;
        if( weight > e.weight ) return 1;
        if( System.identityHashCode(this) < System.identityHashCode(e) ) return -1;
        if( System.identityHashCode(this) > System.identityHashCode(e) ) return 1;
        return 0;
    }
    /* End of public methods. */

    private ProbeMethod src;
    private ProbeMethod dst;
    private double weight;
}
