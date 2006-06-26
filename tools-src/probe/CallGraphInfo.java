package probe;
import java.util.*;
import java.io.*;
import java.util.zip.*;

/** Calculates and reports the differences between two call graphs. */
public class CallGraphInfo {
    public static void usage() {
        System.out.println( "Usage: java probe.CallGraphInfo [options] graph.gxl" );
        System.out.println( "  -m : print list of reachable methods" );
        System.out.println( "  -e : print list of entry points" );
        System.out.println( "  -lib file : ignore methods in packages listed in file" );
        System.exit(1);
    }
    public static String dashLib = null;
    public static boolean dashM = false;
    public static boolean dashE = false;
    public static final void main( String[] args ) {
        boolean doneOptions = false;
        String filename = null;
        for( int i = 0; i < args.length; i++ ) {
            if( !doneOptions && args[i].equals("-lib") ) dashLib = args[++i];
            else if( !doneOptions && args[i].equals("-m") ) dashM = true;
            else if( !doneOptions && args[i].equals("-e") ) dashE = true;
            else if( !doneOptions && args[i].equals("--") ) doneOptions = true;
            else if( filename == null ) filename = args[i];
            else usage();
        }
        if( filename == null ) usage();

        Collection libs = Util.readLib(dashLib);

        CallGraph a;
        try {
            try {
                a = new GXLReader().readCallGraph(new FileInputStream(filename));
            } catch( RuntimeException e ) {
                a = new GXLReader().readCallGraph(new GZIPInputStream(new FileInputStream(filename)));
            }
        } catch( IOException e ) {
            throw new RuntimeException( "caught IOException "+e );
        }

        Set methods = new HashSet();
        methods.addAll(a.entryPoints());
        for( Iterator eIt = a.edges().iterator(); eIt.hasNext(); ) {
            final CallEdge e = (CallEdge) eIt.next();
            methods.add( e.src() );
            methods.add( e.dst() );
        }

        Collection ep = Util.filterLibs(libs, a.entryPoints());
        System.out.println( "Entry points     : "+
                Util.filterLibs(libs, ep).size() );
        System.out.println( "Edges            : "+
                Util.filterLibs(libs, a.edges()).size() );
        System.out.println( "Methods          : "+
                Util.filterLibs(libs, methods).size() );
        Collection rm = Util.filterLibs(libs, a.findReachables());
        System.out.println( "Reachable methods: "+rm.size() );

        if(dashE) {
            System.out.println("Entry points: ");
            for( Iterator pmIt = ep.iterator(); pmIt.hasNext(); ) {
                final ProbeMethod pm = (ProbeMethod) pmIt.next();
                System.out.println(pm);
            }
        }

        if(dashM) {
            System.out.println("Reachable methods: ");
            for( Iterator pmIt = rm.iterator(); pmIt.hasNext(); ) {
                final ProbeMethod pm = (ProbeMethod) pmIt.next();
                System.out.println(pm);
            }
        }
    }

}

