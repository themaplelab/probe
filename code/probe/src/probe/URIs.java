package probe;
import java.util.*;
import java.io.*;
import java.net.*;
import net.sourceforge.gxl.*;

/** Manages the GXL URIs of the various schemas. */
public class URIs {
    final private String scheme = "http";
    final private String host = "www.sable.mcgill.ca";
    private String path;

    // Graphs
    private URI uCallGraph;
    public URI uCallGraph() { return uCallGraph; }
    private URI uRecursive;
    public URI uRecursive() { return uRecursive; }
    private URI uExecutesMany;
    public URI uExecutesMany() { return uExecutesMany; }
    private URI uFailCast;
    public URI uFailCast() { return uFailCast; }
    private URI uPolymorphic;
    public URI uPolymorphic() { return uPolymorphic; }
    private URI uPointsTo;
    public URI uPointsTo() { return uPointsTo; }
    private URI uSideEffect;
    public URI uSideEffect() { return uSideEffect; }
    private URI uEscape;
    public URI uEscape() { return uEscape; }

    // Nodes
    private URI uStmt;
    public URI uStmt() { return uStmt; }
    private URI uMethod;
    public URI uMethod() { return uMethod; }
    private URI uField;
    public URI uField() { return uField; }
    private URI uClass;
    public URI uClass() { return uClass; }
    private URI uRoot;
    public URI uRoot() { return uRoot; }
    private URI uPtSet;
    public URI uPtSet() { return uPtSet; }
    private URI uFieldSet;
    public URI uFieldSet() { return uFieldSet; }
    private URI uParameter;
    public URI uParameter() { return uParameter; }
    private URI uExternal;
    public URI uExternal() { return uExternal; }

    // Edges
    private URI inBody;
    public URI inBody() { return inBody; }
    private URI declaredIn;
    public URI declaredIn() { return declaredIn; }
    private URI calls;
    public URI calls() { return calls; }
    private URI entryPoint;
    public URI entryPoint() { return entryPoint; }
    private URI pointsTo;
    public URI pointsTo() { return pointsTo; }
    private URI reads;
    public URI reads() { return reads; }
    private URI writes;
    public URI writes() { return writes; }
    private URI inSet;
    public URI inSet() { return inSet; }
    private URI ofMethod;
    public URI ofMethod() { return ofMethod; }
    private URI escapesThread;
    public URI escapesThread() { return escapesThread; }
    private URI escapesMethod;
    public URI escapesMethod() { return escapesMethod; }
    private URI anyAlloc;
    public URI anyAlloc() { return anyAlloc; }
    private URI anyCast;
    public URI anyCast() { return anyCast; }
    private URI mayFail;
    public URI mayFail() { return mayFail; }

    public URIs( String path ) {
        this.path = path;
        try {
            uCallGraph = new URI( scheme, host, path, "CallGraph" );
            uRecursive = new URI( scheme, host, path, "Recursive" );
            uExecutesMany = new URI( scheme, host, path, "ExecutesMany" );
            uFailCast = new URI( scheme, host, path, "FailCast" );
            uPolymorphic = new URI( scheme, host, path, "Polymorphic" );
            uPointsTo = new URI( scheme, host, path, "PointsTo" );
            uSideEffect = new URI( scheme, host, path, "SideEffect" );
            uEscape = new URI( scheme, host, path, "Escape" );

            uStmt = new URI( scheme, host, path, "Stmt" );
            uMethod = new URI( scheme, host, path, "Method" );
            uField = new URI( scheme, host, path, "Field" );
            uClass = new URI( scheme, host, path, "Class" );
            uRoot = new URI( scheme, host, path, "Root" );
            uPtSet = new URI( scheme, host, path, "PtSet" );
            uFieldSet = new URI( scheme, host, path, "FieldSet" );
            uParameter = new URI( scheme, host, path, "Parameter" );
            uExternal = new URI( scheme, host, path, "External" );

            inBody = new URI( scheme, host, path, "inBody" );
            declaredIn = new URI( scheme, host, path, "declaredIn" );
            calls = new URI( scheme, host, path, "calls" );
            entryPoint = new URI( scheme, host, path, "entryPoint" );
            pointsTo = new URI( scheme, host, path, "pointsTo" );
            reads = new URI( scheme, host, path, "reads" );
            writes = new URI( scheme, host, path, "writes" );
            inSet = new URI( scheme, host, path, "inSet" );
            ofMethod = new URI( scheme, host, path, "ofMethod" );
            escapesThread = new URI( scheme, host, path, "escapesThread" );
            escapesMethod = new URI( scheme, host, path, "escapesMethod" );
            anyAlloc = new URI( scheme, host, path, "anyAlloc" );
            anyCast = new URI( scheme, host, path, "anyCast" );
            mayFail = new URI( scheme, host, path, "mayFail" );
        } catch( URISyntaxException e ) {
            throw new RuntimeException("Caught URISyntaxException: "+e);
        }
    }
}
