package probe;
import java.util.*;
import java.io.*;

/** Outputs statistics about the number of statements potentially executing
 * multiple times. */
public class ExecutesManyInfo {
    public static boolean dashV = false;
    public static String filename = null;
    public static void usage() {
        System.out.println( "Usage: java probe.ExecutesManyInfo [options] executesmany.gxl" );
        System.out.println( "  -v : print list of potentially recursive methods" );
        System.exit(1);
    }
    public static final void main( String[] args ) {
        for( int i = 0; i < args.length; i++ ) {
            if( args[i].equals("-v") ) dashV = true;
            else if( filename == null ) filename = args[i];
            else usage();
        }
        if( filename == null ) usage();

        ExecutesMany a;
        try {
            a = new GXLReader().readExecutesMany(new FileInputStream(filename));
        } catch( IOException e ) {
            throw new RuntimeException( "caught IOException "+e );
        }

        System.out.println( "Potentially multiply-executing statements : "+a.stmts().size() );

        if(dashV) {
            for( Iterator sIt = a.stmts().iterator(); sIt.hasNext(); ) {
                final ProbeStmt s = (ProbeStmt) sIt.next();
                System.out.println(s);
            }
        }
    }

}

