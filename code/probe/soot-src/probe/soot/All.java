package probe.soot;
import soot.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import soot.jimple.*;
import soot.util.queue.*;
import soot.jimple.paddle.*;
import probe.ObjectManager;
import probe.ProbeMethod;
import probe.ProbeClass;

public class All {
    public static void main(String[] args) 
    {
	PackManager.v().getPack("wjtp").add(
	    new Transform("wjtp.probecg", new CallGraph()));
	PackManager.v().getPack("wjtp").add(
            new Transform("wjtp.probeescape", new Escape()));
	/*PackManager.v().getPack("wjtp").add(
            new Transform("wjtp.em", new ExecutesMany()));*/
	PackManager.v().getPack("wjtp").add(
            new Transform("wjtp.fc", new FailCast()));
	PackManager.v().getPack("wjtp").add(
            new Transform("wjtp.fci", new FailCastIntra()));
	PackManager.v().getPack("wjtp").add(
            new Transform("wjtp.probept", new PointsTo()));
	PackManager.v().getPack("wjtp").add(
            new Transform("wjtp.probepoly", new Polymorphic()));
	PackManager.v().getPack("wjtp").add(
            new Transform("wjtp.proberec", new Recursive()));
	PackManager.v().getPack("wjtp").add(
            new Transform("wjtp.se", new SideEffect()));

        LinkedList al = new LinkedList();
        for( int i = 0; i < args.length; i++ ) {
            if( args[i].equals("-ddcallgraph") ) {
                CallGraph.filename = args[++i];
                continue;
            }
            if( args[i].equals("-ddescape") ) {
                Escape.filename = args[++i];
                continue;
            }
            /*
            if( args[i].equals("-ddexecutesmany") ) {
                ExecutesMany.filename = args[++i];
                continue;
            }
            */
            if( args[i].equals("-ddfailcast") ) {
                FailCast.filename = args[++i];
                continue;
            }
            if( args[i].equals("-ddfailcastintra") ) {
                FailCastIntra.filename = args[++i];
                continue;
            }
            if( args[i].equals("-ddpointsto") ) {
                PointsTo.filename = args[++i];
                continue;
            }
            if( args[i].equals("-ddpolymorphic") ) {
                Polymorphic.filename = args[++i];
                continue;
            }
            if( args[i].equals("-ddrecursive") ) {
                Recursive.filename = args[++i];
                continue;
            }
            if( args[i].equals("-ddsideeffect") ) {
                SideEffect.filename = args[++i];
                continue;
            }
            al.add(args[i]);
        }
        args = new String[al.size()];
        for( int i = 0; i < args.length; i++ ) {
            args[i] = (String) al.removeFirst();
        }
	soot.Main.main(args);
    }
}


