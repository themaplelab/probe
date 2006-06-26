package probe;
import java.util.*;
import java.io.*;
import java.util.zip.*;

/** Outputs statistics about potentially escaping objects.
 * */
public class EscapeInfo {
    public static boolean dashA = false;
    public static boolean dashV = false;
    public static String dashLib = null;
    public static String filename = null;
    public static void usage() {
        System.out.println( "Usage: java probe.EscapeInfo [options] escape.gxl" );
        System.out.println( "  -v : print list of escaping objects" );
        System.out.println( "  -a : print list of all allocation sites" );
        System.out.println( "  -lib file : ignore methods in packages listed in file" );
        System.exit(1);
    }
    public static final void main( String[] args ) {
        boolean doneOptions = false;
        for( int i = 0; i < args.length; i++ ) {
            if( !doneOptions && args[i].equals("-v") ) dashV = true;
            else if( !doneOptions && args[i].equals("-lib") ) dashLib = args[++i];
            else if( !doneOptions && args[i].equals("--") ) doneOptions = true;

            else if( filename == null ) filename = args[i];
            else usage();
        }
        if( filename == null ) usage();

        Collection libs = Util.readLib(dashLib);

        Escape a;
        try {
            try {
                a = new GXLReader().readEscape(new FileInputStream(filename));
            } catch( RuntimeException e ) {
                a = new GXLReader().readEscape(new GZIPInputStream(new FileInputStream(filename)));
            }
        } catch( IOException e ) {
            throw new RuntimeException( "caught IOException "+e );
        }

        Collection escapesThread = Util.filterLibs(libs, a.escapesThread());
        System.out.println( "Thread-escaping : "+escapesThread.size() );

        if(dashV) {
            for( Iterator sIt = escapesThread.iterator(); sIt.hasNext(); ) {
                final ProbeStmt s = (ProbeStmt) sIt.next();
                System.out.println(s);
            }
        }

        Collection escapesMethod = Util.filterLibs(libs, a.escapesMethod());
        System.out.println( "Method-escaping : "+escapesMethod.size() );

        if(dashV) {
            for( Iterator sIt = escapesMethod.iterator(); sIt.hasNext(); ) {
                final ProbeStmt s = (ProbeStmt) sIt.next();
                System.out.println(s);
            }
        }

        Collection anyAlloc = Util.filterLibs(libs, a.anyAlloc());
        System.out.println( "Number of allocation sites : "+anyAlloc.size() );

        if(dashA) {
            for( Iterator sIt = anyAlloc.iterator(); sIt.hasNext(); ) {
                final ProbeStmt s = (ProbeStmt) sIt.next();
                System.out.println(s);
            }
        }
    }

}

