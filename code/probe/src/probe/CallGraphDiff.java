package probe;
import java.util.*;
import java.io.*;
import java.util.zip.*;

/** Calculates and reports the differences between two call graphs. */
public class CallGraphDiff {
    public static void usage() {
        System.out.println( "Usage: java probe.CallGraphDiff [options] supergraph.gxl subgraph.gxl" );
        System.out.println( "  -e : ignore edges in supergraph whose targets are entry points in subgraph" );
        System.out.println( "  -r : ignore edges in supergraph whose targets are reachable in subgraph" );
        System.out.println( "  -f : perform flow computation to rank edges by importance: edge algorithm" );
        System.out.println( "  -ff : perform flow computation to rank edges by importance: node algorithm" );
        System.out.println( "  -a : show all spurious edges rather than just those from reachable methods" );
        System.out.println( "  -m : print names of missing methods" );
        System.out.println( "  -p : ignore edges out of doPrivileged methods" );
        System.out.println( "  -switch : switch supergraph and subgraph" );
        System.exit(1);
    }
    public static boolean dashE = false;
    public static boolean dashR = false;
    public static boolean dashF = false;
    public static boolean dashFF = false;
    public static boolean dashA = false;
    public static boolean dashM = false;
    public static boolean dashP = false;
    public static boolean dashSwitch = false;
    public static final void main( String[] args ) {
        if( args.length < 2 ) {
            usage();
        }
        boolean doneOptions = false;
        String superFile = null;
        String subFile = null;
        for( int i = 0; i < args.length; i++ ) {
            if( !doneOptions && args[i].equals("-e") ) dashE = true; 
            else if( !doneOptions && args[i].equals("-r") ) dashR = true; 
            else if( !doneOptions && args[i].equals("-f") ) dashF = true; 
            else if( !doneOptions && args[i].equals("-ff") ) dashFF = true; 
            else if( !doneOptions && args[i].equals("-a") ) dashA = true; 
            else if( !doneOptions && args[i].equals("-m") ) dashM = true; 
            else if( !doneOptions && args[i].equals("-p") ) dashP = true; 
            else if( !doneOptions && args[i].equals("-switch") ) dashSwitch = !dashSwitch;
            else if( !doneOptions && args[i].equals("--") ) doneOptions = true;
            else if( superFile == null ) superFile = args[i];
            else if( subFile == null ) subFile = args[i];
            else usage();
        }
        if( subFile == null ) usage();
        CallGraph supergraph;
        CallGraph subgraph;
        if(dashSwitch) {
            String temp = superFile;
            superFile = subFile;
            subFile = temp;
        }
        supergraph = readCallGraph(superFile);
        subgraph = readCallGraph(subFile);

        if( dashP ) {
            for( Iterator edgeIt = supergraph.edges().iterator(); edgeIt.hasNext(); ) {
                final Edge edge = (Edge) edgeIt.next();
                if( edge.src().name().equals("doPrivileged") ) edgeIt.remove();
            }
            for( Iterator edgeIt = subgraph.edges().iterator(); edgeIt.hasNext(); ) {
                final Edge edge = (Edge) edgeIt.next();
                if( edge.src().name().equals("doPrivileged") ) edgeIt.remove();
            }
        }
        AbsEdgeWeights weights = null;
        if( dashF ) {
            weights = new EdgeWeights(supergraph, subgraph);
        } else if( dashFF ) {
            weights = new EdgeWeights2(supergraph, subgraph);
        }
        CallGraph diff = diff(supergraph, subgraph);
        System.out.println( "===========================================================================");
        System.out.println( "Missing entry points in "+subFile+":");
        System.out.println( "===========================================================================");
        if( weights != null ) {
            final AbsEdgeWeights weightsF = weights;
            TreeSet ts = new TreeSet(new Comparator() {
                public int compare(Object o1, Object o2) {
                    ProbeMethod pm1 = (ProbeMethod) o1;
                    ProbeMethod pm2 = (ProbeMethod) o2;
                    if( weightsF.weight(pm1) < weightsF.weight(pm2) ) return -1;
                    if( weightsF.weight(pm1) > weightsF.weight(pm2) ) return 1;
                    return 0;
                }
            });
            ts.addAll( diff.entryPoints() );
            for( Iterator mIt = ts.iterator(); mIt.hasNext(); ) {
                final ProbeMethod m = (ProbeMethod) mIt.next();
                System.out.println( weights.weight(m)+" "+m );
            }
        } else {
            for( Iterator mIt = diff.entryPoints().iterator(); mIt.hasNext(); ) {
                final ProbeMethod m = (ProbeMethod) mIt.next();
                System.out.println( m.toString() );
            }
        }

        System.out.println( "===========================================================================");
        System.out.println( "Missing call edges in "+subFile+":");
        System.out.println( "===========================================================================");
        if( weights != null ) {
            final AbsEdgeWeights weightsF = weights;
            TreeSet ts = new TreeSet(new Comparator() {
                public int compare(Object o1, Object o2) {
                    Edge pm1 = (Edge) o1;
                    Edge pm2 = (Edge) o2;
                    if( weightsF.weight(pm1) < weightsF.weight(pm2) ) return -1;
                    if( weightsF.weight(pm1) > weightsF.weight(pm2) ) return 1;
                    return 0;
                }
            });
            ts.addAll( diff.edges() );
            for( Iterator mIt = ts.iterator(); mIt.hasNext(); ) {
                final Edge m = (Edge) mIt.next();
                System.out.println( weights.weight(m)+" "+m );
            }
        } else {
            for( Iterator mIt = diff.edges().iterator(); mIt.hasNext(); ) {
                final Edge m = (Edge) mIt.next();
                System.out.println( m.toString() );
            }
        }

        Set missingReachables = new HashSet();
        missingReachables.addAll( supergraph.findReachables() );
        missingReachables.removeAll( subgraph.findReachables() );
        System.out.println( "===========================================================================");
        System.out.println( "Number of reachable methods missing in "+subFile+": "+missingReachables.size());
        System.out.println( "===========================================================================");
        if( dashM ) {
            for( Iterator pmIt = missingReachables.iterator(); pmIt.hasNext(); ) {
                final ProbeMethod pm = (ProbeMethod) pmIt.next();
                System.out.println(pm);
            }
        }

    }

    /** Computes the difference of call graph subgraph subtracted from
     * call graph supergraph. Specifically, the entry points of the
     * resulting call graph are those entry points of supergraph which
     * are not entry points of subgraph, and the call edges of the
     * resulting call graph are those call edges of supergraph that are
     * not call edges of subgraph, and whose source method is reachable
     * in subgraph. */
    public static CallGraph diff( CallGraph supergraph, CallGraph subgraph ) {
        CallGraph ret = new CallGraph();

        ret.entryPoints().addAll( supergraph.entryPoints() );
        ret.entryPoints().removeAll( subgraph.entryPoints() );

        Set reachables = subgraph.findReachables();

        ret.edges().addAll( supergraph.edges() );
        ret.edges().removeAll( subgraph.edges() );
        Iterator it = ret.edges().iterator();
        while(it.hasNext()) {
            Edge e = (Edge) it.next();
            if( (!dashA && !reachables.contains(e.src()))
            || (dashE && subgraph.entryPoints().contains(e.dst()))
            || (dashR && reachables.contains(e.dst())) )
                it.remove();
        }

        return ret;
    }
    private static CallGraph readCallGraph(String filename) {
        CallGraph ret;
        try {
            try {
                ret = new GXLReader().readCallGraph(new FileInputStream(filename));
            } catch( RuntimeException e ) {
                ret = new GXLReader().readCallGraph(new GZIPInputStream(new FileInputStream(filename)));
            }
        } catch( IOException e ) {
            throw new RuntimeException( "caught IOException "+e );
        }
        return ret;
    }
}

