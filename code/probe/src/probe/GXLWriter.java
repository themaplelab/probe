package probe;
import java.util.*;
import java.io.*;
import java.net.*;
import net.sourceforge.gxl.*;

/** Writes a call graph to a GXL file. */
public class GXLWriter {
    /** Write a call graph to a GXL file. */
    public void write( CallGraph cg, OutputStream file ) throws IOException {
        uri = new URIs( "/~olhotak/probe/schemas/callgraph.gxl" );
        initializeMaps();

        // Collect up all the methods and classes appearing in the call graph.
        for( Iterator mIt = cg.entryPoints().iterator(); mIt.hasNext(); ) {
            final ProbeMethod m = (ProbeMethod) mIt.next();
            addMethod( m );
        }
        for( Iterator eIt = cg.edges().iterator(); eIt.hasNext(); ) {
            final CallEdge e = (CallEdge) eIt.next();
            addMethod( e.src() );
            addMethod( e.dst() );
        }
        
        // Assign ids to all method and class nodes.
        assignIDs();

        // Create the GXL nodes in the graph.
        GXLDocument gxlDocument = new GXLDocument();
        GXLGraph graph = new GXLGraph( "callgraph" );
        graph.setType(uri.uCallGraph());
        GXLNode root = new GXLNode("root");
        root.setType(uri.uRoot());
        graph.add(root);

        addClasses(graph);
        addMethods(graph);

        // Add the call edges to the GXL graph.
        for( Iterator mIt = cg.entryPoints().iterator(); mIt.hasNext(); ) {
            final ProbeMethod m = (ProbeMethod) mIt.next();
            GXLEdge edge = new GXLEdge( "root", getId(m) );
            edge.setType(uri.entryPoint());
            graph.add( edge );
        }
        for( Iterator eIt = cg.edges().iterator(); eIt.hasNext(); ) {
            final CallEdge e = (CallEdge) eIt.next();
            GXLEdge edge = new GXLEdge( getId(e.src()), getId(e.dst()) );
            edge.setType(uri.calls());
            if(e.weight() != 0.0) {
                edge.setAttr("weight", new GXLFloat((float)e.weight()));
            }
            graph.add( edge );
        }

        // Write out the GXL graph.
        gxlDocument.getDocumentElement().add(graph);
        gxlDocument.write(file);
    }

    /** Write a set of recursive methods to a GXL file. */
    public void write( Recursive rec, OutputStream file ) throws IOException {
        uri = new URIs( "/~olhotak/probe/schemas/recursive.gxl" );
        initializeMaps();

        // Collect up all the methods and classes appearing in the set.
        for( Iterator mIt = rec.methods().iterator(); mIt.hasNext(); ) {
            final ProbeMethod m = (ProbeMethod) mIt.next();
            addMethod( m );
        }
        
        // Assign ids to all method and class nodes.
        assignIDs();

        // Create the GXL nodes in the graph.
        GXLDocument gxlDocument = new GXLDocument();
        GXLGraph graph = new GXLGraph( "recursive" );
        graph.setType(uri.uRecursive());

        addClasses(graph);
        addMethods(graph);

        // Write out the GXL graph.
        gxlDocument.getDocumentElement().add(graph);
        gxlDocument.write(file);
    }

    /** Write a set of allocation sites that may execute more than once to a
     * GXL file. */
    public void write( ExecutesMany eo, OutputStream file ) throws IOException {
        uri = new URIs( "/~olhotak/probe/schemas/executesmany.gxl" );
        initializeMaps();

        // Collect up all the stmts, methods and classes appearing in the set.
        for( Iterator sIt = eo.stmts().iterator(); sIt.hasNext(); ) {
            final ProbeStmt s = (ProbeStmt) sIt.next();
            addStmt( s );
        }
        
        // Assign ids to all stmt, method and class nodes.
        assignIDs();

        // Create the GXL nodes in the graph.
        GXLDocument gxlDocument = new GXLDocument();
        GXLGraph graph = new GXLGraph( "executesmany" );
        graph.setType(uri.uExecutesMany());

        addClasses(graph);
        addMethods(graph);
        addStmts(graph);

        // Write out the GXL graph.
        gxlDocument.getDocumentElement().add(graph);
        gxlDocument.write(file);
    }

    /** Write a set polymorphic invoke instructions to a GXL file. */
    public void write( Polymorphic eo, OutputStream file ) throws IOException {
        uri = new URIs( "/~olhotak/probe/schemas/polymorphic.gxl" );
        initializeMaps();

        // Collect up all the stmts, methods and classes appearing in the set.
        for( Iterator sIt = eo.stmts().iterator(); sIt.hasNext(); ) {
            final ProbeStmt s = (ProbeStmt) sIt.next();
            addStmt( s );
        }
        
        // Assign ids to all stmt, method and class nodes.
        assignIDs();

        // Create the GXL nodes in the graph.
        GXLDocument gxlDocument = new GXLDocument();
        GXLGraph graph = new GXLGraph( "polymorphic" );
        graph.setType(uri.uPolymorphic());

        addClasses(graph);
        addMethods(graph);
        addStmts(graph);

        // Write out the GXL graph.
        gxlDocument.getDocumentElement().add(graph);
        gxlDocument.write(file);
    }

    /** Write a set of cast instructions that fail to a GXL file. */
    public void write( FailCast fc, OutputStream file ) throws IOException {
        uri = new URIs( "/~olhotak/probe/schemas/failcast.gxl" );
        initializeMaps();

        // Collect up all the stmts, methods and classes appearing in the set.
        for( Iterator sIt = fc.stmts().iterator(); sIt.hasNext(); ) {
            final ProbeStmt s = (ProbeStmt) sIt.next();
            addStmt( s );
        }
        for( Iterator sIt = fc.executes().iterator(); sIt.hasNext(); ) {
            final ProbeStmt s = (ProbeStmt) sIt.next();
            addStmt( s );
        }
        
        // Assign ids to all stmt, method and class nodes.
        assignIDs();

        // Create the GXL nodes in the graph.
        GXLDocument gxlDocument = new GXLDocument();
        GXLGraph graph = new GXLGraph( "failcast" );
        graph.setType(uri.uFailCast());

        GXLNode root = new GXLNode("root");
        root.setType(uri.uRoot());
        graph.add(root);

        // Add edges.
        for( Iterator stmtIt = fc.stmts().iterator(); stmtIt.hasNext(); ) {
            final ProbeStmt stmt = (ProbeStmt) stmtIt.next();
            GXLEdge edge = new GXLEdge( "root", getId(stmt) );
            edge.setType(uri.fails());
            graph.add( edge );
        }
        for( Iterator stmtIt = fc.executes().iterator(); stmtIt.hasNext(); ) {
            final ProbeStmt stmt = (ProbeStmt) stmtIt.next();
            GXLEdge edge = new GXLEdge( "root", getId(stmt) );
            edge.setType(uri.executes());
            graph.add( edge );
        }

        addClasses(graph);
        addMethods(graph);
        addStmts(graph);

        // Write out the GXL graph.
        gxlDocument.getDocumentElement().add(graph);
        gxlDocument.write(file);
    }

    /** Write a set of allocation sites whose objects escape their allocating
     * thread/method to a GXL file. */
    public void write( Escape esc, OutputStream file ) throws IOException {
        uri = new URIs( "/~olhotak/probe/schemas/escape.gxl" );
        initializeMaps();

        // Collect up all the stmts, methods and classes appearing in the sets.
        for( Iterator sIt = esc.escapesThread().iterator(); sIt.hasNext(); ) {
            final ProbeStmt s = (ProbeStmt) sIt.next();
            addStmt( s );
        }
        for( Iterator sIt = esc.escapesMethod().iterator(); sIt.hasNext(); ) {
            final ProbeStmt s = (ProbeStmt) sIt.next();
            addStmt( s );
        }
        for( Iterator sIt = esc.anyAlloc().iterator(); sIt.hasNext(); ) {
            final ProbeStmt s = (ProbeStmt) sIt.next();
            addStmt( s );
        }
        
        // Assign ids to all stmt, method and class nodes.
        assignIDs();

        // Create the GXL nodes in the graph.
        GXLDocument gxlDocument = new GXLDocument();
        GXLGraph graph = new GXLGraph( "escape" );
        graph.setType(uri.uEscape());

        GXLNode root = new GXLNode("root");
        root.setType(uri.uRoot());
        graph.add(root);

        // Add edges.
        for( Iterator stmtIt = esc.escapesThread().iterator(); stmtIt.hasNext(); ) {
            final ProbeStmt stmt = (ProbeStmt) stmtIt.next();
            GXLEdge edge = new GXLEdge( "root", getId(stmt) );
            edge.setType(uri.escapesThread());
            graph.add( edge );
        }
        for( Iterator stmtIt = esc.escapesMethod().iterator(); stmtIt.hasNext(); ) {
            final ProbeStmt stmt = (ProbeStmt) stmtIt.next();
            GXLEdge edge = new GXLEdge( "root", getId(stmt) );
            edge.setType(uri.escapesMethod());
            graph.add( edge );
        }
        for( Iterator stmtIt = esc.anyAlloc().iterator(); stmtIt.hasNext(); ) {
            final ProbeStmt stmt = (ProbeStmt) stmtIt.next();
            GXLEdge edge = new GXLEdge( "root", getId(stmt) );
            edge.setType(uri.anyAlloc());
            graph.add( edge );
        }

        addClasses(graph);
        addMethods(graph);
        addStmts(graph);

        // Write out the GXL graph.
        gxlDocument.getDocumentElement().add(graph);
        gxlDocument.write(file);
    }

    /** Write a points-to graph to a GXL file. */
    public void write( PointsTo ptGraph, OutputStream file ) throws IOException {
        uri = new URIs( "/~olhotak/probe/schemas/pointsto.gxl" );
        initializeMaps();

        // Collect up all the stmts, methods and classes.
        for( Iterator ptIt = ptGraph.pointsTo().keySet().iterator(); ptIt.hasNext(); ) {
            final Pointer pt = (Pointer) ptIt.next();
            addPointer( pt );
        }
        for( Iterator setIt = new HashSet(ptGraph.pointsTo().values()).iterator(); setIt.hasNext(); ) {
            final ProbePtSet set = (ProbePtSet) setIt.next();
            addPtSet(set);
            for( Iterator hoIt = set.heapObjects().iterator(); hoIt.hasNext(); ) {
                final HeapObject ho = (HeapObject) hoIt.next();
                addHeapObject( ho );
            }
        }
        
        // Assign ids to all method and class nodes.
        assignIDs();

        // Create the GXL nodes in the graph.
        GXLDocument gxlDocument = new GXLDocument();
        GXLGraph graph = new GXLGraph( "pointsto" );
        graph.setType(uri.uPointsTo());
        GXLNode external = new GXLNode("External");
        external.setType(uri.uExternal());
        graph.add(external);

        addClasses(graph);
        addMethods(graph);
        addStmts(graph);
        addParameters(graph);
        addPtSets(graph);

        // Add the points-to edges to the GXL graph.
        for( Iterator ptIt = ptGraph.pointsTo().keySet().iterator(); ptIt.hasNext(); ) {
            final Pointer pt = (Pointer) ptIt.next();
            ProbePtSet set = (ProbePtSet) ptGraph.pointsTo().get(pt);
            GXLEdge edge = new GXLEdge( getId(pt), getId(set) );
            edge.setType(uri.pointsTo());
            graph.add( edge );
        }
        for( Iterator setIt = new HashSet(ptGraph.pointsTo().values()).iterator(); setIt.hasNext(); ) {
            final ProbePtSet set = (ProbePtSet) setIt.next();
            for( Iterator hoIt = set.heapObjects().iterator(); hoIt.hasNext(); ) {
                final HeapObject ho = (HeapObject) hoIt.next();
                String id;
                if( ho instanceof External ) {
                    id = external.getID();
                } else {
                    id = getId( (ProbeStmt) ho );
                }
                GXLEdge edge = new GXLEdge( id, getId(set) );
                edge.setType(uri.inSet());
                graph.add(edge);
            }
        }

        // Write out the GXL graph.
        gxlDocument.getDocumentElement().add(graph);
        gxlDocument.write(file);
    }

    /** Write side-effect information to a GXL file. */
    public void write( SideEffect sideEffect, OutputStream file ) throws IOException {
        uri = new URIs( "/~olhotak/probe/schemas/sideeffect.gxl" );
        initializeMaps();

        // Collect up all the stmts, methods and classes.
        Set sets = new HashSet();
        sets.addAll(sideEffect.reads().values());
        sets.addAll(sideEffect.writes().values());
        for( Iterator setIt = sets.iterator(); setIt.hasNext(); ) {
            final ProbeFieldSet set = (ProbeFieldSet) setIt.next();
            addFieldSet(set);
            for( Iterator fieldIt = set.fields().iterator(); fieldIt.hasNext(); ) {
                final ProbeField field = (ProbeField) fieldIt.next();
                addField( field );
            }
        }

        Set stmts = new HashSet();
        stmts.addAll(sideEffect.reads().keySet());
        stmts.addAll(sideEffect.writes().keySet());
        for( Iterator stmtIt = stmts.iterator(); stmtIt.hasNext(); ) {
            final ProbeStmt stmt = (ProbeStmt) stmtIt.next();
            addStmt(stmt);
        }
        
        // Assign ids to all method and class nodes.
        assignIDs();

        // Create the GXL nodes in the graph.
        GXLDocument gxlDocument = new GXLDocument();
        GXLGraph graph = new GXLGraph( "sideeffect" );
        graph.setType(uri.uSideEffect());

        addClasses(graph);
        addMethods(graph);
        addStmts(graph);
        addFields(graph);
        addFieldSets(graph);

        // Add the reads edges to the GXL graph.
        for( Iterator stIt = sideEffect.reads().keySet().iterator(); stIt.hasNext(); ) {
            final ProbeStmt st = (ProbeStmt) stIt.next();
            ProbeFieldSet set = (ProbeFieldSet) sideEffect.reads().get(st);
            GXLEdge edge = new GXLEdge( getId(st), getId(set) );
            edge.setType(uri.reads());
            graph.add( edge );
        }
        for( Iterator stIt = sideEffect.writes().keySet().iterator(); stIt.hasNext(); ) {
            final ProbeStmt st = (ProbeStmt) stIt.next();
            ProbeFieldSet set = (ProbeFieldSet) sideEffect.writes().get(st);
            GXLEdge edge = new GXLEdge( getId(st), getId(set) );
            edge.setType(uri.writes());
            graph.add( edge );
        }
        for( Iterator setIt = sets.iterator(); setIt.hasNext(); ) {
            final ProbeFieldSet set = (ProbeFieldSet) setIt.next();
            for( Iterator fieldIt = set.fields().iterator(); fieldIt.hasNext(); ) {
                final ProbeField field = (ProbeField) fieldIt.next();
                String id;
                GXLEdge edge = new GXLEdge( getId(field), getId(set) );
                edge.setType(uri.inSet());
                graph.add(edge);
            }
        }

        // Write out the GXL graph.
        gxlDocument.getDocumentElement().add(graph);
        gxlDocument.write(file);
    }


    /* End of public methods. */

    private URIs uri;

    private Set fields;
    private Set parameters;
    private Set stmts;
    private Set methods;
    private Set classes;
    private Set ptsets;
    private Set fieldsets;
    private Map idMap;

    private void addFieldSet( ProbeFieldSet set ) {
        fieldsets.add(set);
    }
    private void addPtSet( ProbePtSet set ) {
        ptsets.add(set);
    }
    private void addHeapObject( HeapObject ho ) {
        if( ho instanceof ProbeStmt ) addStmt((ProbeStmt) ho);
    }
    private void addPointer( Pointer pt ) {
        if( pt instanceof ProbeStmt ) addStmt((ProbeStmt) pt);
        else if( pt instanceof ProbeParameter ) addParameter((ProbeParameter) pt);
    }
    private void addParameter( ProbeParameter p ) {
        parameters.add(p);
        addMethod(p.method());
    }
    private void addStmt( ProbeStmt s ) {
        stmts.add(s);
        addMethod( s.method() );
    }
    private void addMethod( ProbeMethod m ) {
        methods.add( m );
        addClass( m.cls() );
    }
    private void addField( ProbeField f ) {
        fields.add( f );
        addClass( f.cls() );
    }
    private void addClass( ProbeClass c ) {
        classes.add( c );
    }
    private String getId( Pointer s ) {
        Integer id = (Integer) idMap.get(s);
        return "id"+id.toString();
    }
    private String getId( ProbeMethod m ) {
        Integer id = (Integer) idMap.get(m);
        return "id"+id.toString();
    }
    private String getId( ProbeField f ) {
        Integer id = (Integer) idMap.get(f);
        return "id"+id.toString();
    }
    private String getId( ProbeClass cl ) {
        Integer id = (Integer) idMap.get(cl);
        return "id"+id.toString();
    }
    private String getId( ProbePtSet p ) {
        Integer id = (Integer) idMap.get(p);
        return "id"+id.toString();
    }
    private String getId( ProbeFieldSet p ) {
        Integer id = (Integer) idMap.get(p);
        return "id"+id.toString();
    }
    private void addClasses( GXLGraph graph ) {
        for( Iterator clIt = classes.iterator(); clIt.hasNext(); ) {
            final ProbeClass cl = (ProbeClass) clIt.next();
            addClass( graph, cl );
        }
    }
    private void addClass( GXLGraph graph, ProbeClass cl ) {
        GXLNode node = new GXLNode( getId(cl) );
        node.setType(uri.uClass());
        node.setAttr("package", new GXLString(cl.pkg()));
        node.setAttr("name", new GXLString(cl.name()));
        graph.add(node);
    }
    private void addMethods( GXLGraph graph ) {
        for( Iterator mIt = methods.iterator(); mIt.hasNext(); ) {
            final ProbeMethod m = (ProbeMethod) mIt.next();
            addMethod( graph, m );
        }
    }
    private void addFields( GXLGraph graph ) {
        for( Iterator fIt = fields.iterator(); fIt.hasNext(); ) {
            final ProbeField f = (ProbeField) fIt.next();
            addField( graph, f );
        }
    }
    private void addMethod( GXLGraph graph, ProbeMethod m ) {
        GXLNode node = new GXLNode( getId(m) );
        node.setType(uri.uMethod());
        node.setAttr("name", new GXLString(m.name()));
        node.setAttr("signature", new GXLString(m.signature()));
        graph.add(node);
        GXLEdge classEdge = new GXLEdge( getId(m), getId(m.cls()) );
        classEdge.setType(uri.declaredIn());
        graph.add(classEdge);
    }
    private void addField( GXLGraph graph, ProbeField f ) {
        GXLNode node = new GXLNode( getId(f) );
        node.setType(uri.uField());
        node.setAttr("name", new GXLString(f.name()));
        graph.add(node);
        GXLEdge classEdge = new GXLEdge( getId(f), getId(f.cls()) );
        classEdge.setType(uri.declaredIn());
        graph.add(classEdge);
    }
    private void addStmts( GXLGraph graph ) {
        for( Iterator sIt = stmts.iterator(); sIt.hasNext(); ) {
            final ProbeStmt s = (ProbeStmt) sIt.next();
            addStmt( graph, s );
        }
    }
    private void addStmt( GXLGraph graph, ProbeStmt s ) {
        GXLNode node = new GXLNode( getId(s) );
        node.setType(uri.uStmt());
        node.setAttr("offset", new GXLInt(s.offset()));
        graph.add(node);
        GXLEdge inBodyEdge = new GXLEdge( getId(s), getId(s.method()) );
        inBodyEdge.setType(uri.inBody());
        graph.add(inBodyEdge);
    }
    private void addParameters( GXLGraph graph ) {
        for( Iterator pIt = parameters.iterator(); pIt.hasNext(); ) {
            final ProbeParameter p = (ProbeParameter) pIt.next();
            addParameter( graph, p );
        }
    }
    private void addParameter( GXLGraph graph, ProbeParameter p ) {
        GXLNode node = new GXLNode( getId(p) );
        node.setType(uri.uParameter());
        node.setAttr("number", new GXLInt(p.number()));
        graph.add(node);
        GXLEdge ofMethodEdge = new GXLEdge( getId(p), getId(p.method()) );
        ofMethodEdge.setType(uri.ofMethod());
        graph.add(ofMethodEdge);
    }
    private void addPtSets( GXLGraph graph ) {
        for( Iterator pIt = ptsets.iterator(); pIt.hasNext(); ) {
            final ProbePtSet p = (ProbePtSet) pIt.next();
            addPtSet( graph, p );
        }
    }
    private void addFieldSets( GXLGraph graph ) {
        for( Iterator pIt = fieldsets.iterator(); pIt.hasNext(); ) {
            final ProbeFieldSet p = (ProbeFieldSet) pIt.next();
            addFieldSet( graph, p );
        }
    }
    private void addPtSet( GXLGraph graph, ProbePtSet p ) {
        GXLNode node = new GXLNode( getId(p) );
        node.setType(uri.uPtSet());
        graph.add(node);
    }
    private void addFieldSet( GXLGraph graph, ProbeFieldSet p ) {
        GXLNode node = new GXLNode( getId(p) );
        node.setType(uri.uFieldSet());
        graph.add(node);
    }
    private void initializeMaps() {
        stmts = new HashSet();
        fields = new HashSet();
        methods = new HashSet();
        classes = new HashSet();
        parameters = new HashSet();
        ptsets = new HashSet();
        fieldsets = new HashSet();
    }
    /** Assign ids to all method and class nodes. */
    private void assignIDs() {
        int id = 1;
        idMap = new HashMap();
        for( Iterator sIt = stmts.iterator(); sIt.hasNext(); ) {
            final ProbeStmt s = (ProbeStmt) sIt.next();
            idMap.put( s, new Integer(id++) );
        }
        for( Iterator mIt = methods.iterator(); mIt.hasNext(); ) {
            final ProbeMethod m = (ProbeMethod) mIt.next();
            idMap.put( m, new Integer(id++) );
        }
        for( Iterator fIt = fields.iterator(); fIt.hasNext(); ) {
            final ProbeField f = (ProbeField) fIt.next();
            idMap.put( f, new Integer(id++) );
        }
        for( Iterator clIt = classes.iterator(); clIt.hasNext(); ) {
            final ProbeClass cl = (ProbeClass) clIt.next();
            idMap.put( cl, new Integer(id++) );
        }
        for( Iterator pIt = parameters.iterator(); pIt.hasNext(); ) {
            final ProbeParameter p = (ProbeParameter) pIt.next();
            idMap.put( p, new Integer(id++) );
        }
        for( Iterator pIt = ptsets.iterator(); pIt.hasNext(); ) {
            final ProbePtSet p = (ProbePtSet) pIt.next();
            idMap.put( p, new Integer(id++) );
        }
        for( Iterator pIt = fieldsets.iterator(); pIt.hasNext(); ) {
            final ProbeFieldSet p = (ProbeFieldSet) pIt.next();
            idMap.put( p, new Integer(id++) );
        }
    }
}
