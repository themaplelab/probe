package probe;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Writes a call graph to a text file. */
public class TextWriter {
	/** Write a call graph to a Text file. */
	public void write(CallGraph cg, OutputStream file) throws IOException {
		PrintWriter out = new PrintWriter(file, true);

		initializeMaps();

		// Collect up all the methods and classes appearing in the call graph.
		for (ProbeMethod m : cg.entryPoints()) {
			addMethod(m);
		}

		for (CallEdge e : cg.edges()) {
			addMethod(e.src());
			addMethod(e.dst());
		}

		// Assign ids to all method and class nodes.
		assignIDs();

		outputClasses(out);
		outputMethods(out);

		// Output entry points.
		for (ProbeMethod m : cg.entryPoints()) {
			out.println(Util.EntrypointTag);
			out.println(getId(m));
		}

		// Output call edges.
		for (CallEdge e : cg.edges()) {
			out.println(Util.EdgeTag);
			out.println(getId(e.src()));
			out.println(getId(e.dst()));
			out.println(e.weight());
			out.println(e.context());
		}

		file.close();
		out.close();
	}

	/** Write a set of recursive methods to a GXL file. */
	public void write(Recursive rec, OutputStream file) throws IOException {
		throw new RuntimeException("not implemented");
		/*
		 * uri = new URIs( "/~olhotak/probe/schemas/recursive.gxl" ); initializeMaps();
		 * 
		 * // Collect up all the methods and classes appearing in the set. for( Iterator mIt = rec.methods().iterator();
		 * mIt.hasNext(); ) { final ProbeMethod m = (ProbeMethod) mIt.next(); addMethod( m ); }
		 * 
		 * // Assign ids to all method and class nodes. assignIDs();
		 * 
		 * // Create the GXL nodes in the graph. GXLDocument gxlDocument = new GXLDocument(); GXLGraph graph = new
		 * GXLGraph( "recursive" ); graph.setType(uri.uRecursive());
		 * 
		 * addClasses(graph); addMethods(graph);
		 * 
		 * // Write out the GXL graph. gxlDocument.getDocumentElement().add(graph); gxlDocument.write(file);
		 */
	}

	/**
	 * Write a set of allocation sites that may execute more than once to a GXL file.
	 */
	public void write(ExecutesMany eo, OutputStream file) throws IOException {
		throw new RuntimeException("not implemented");
		/*
		 * uri = new URIs( "/~olhotak/probe/schemas/executesmany.gxl" ); initializeMaps();
		 * 
		 * // Collect up all the stmts, methods and classes appearing in the set. for( Iterator sIt =
		 * eo.stmts().iterator(); sIt.hasNext(); ) { final ProbeStmt s = (ProbeStmt) sIt.next(); addStmt( s ); }
		 * 
		 * // Assign ids to all stmt, method and class nodes. assignIDs();
		 * 
		 * // Create the GXL nodes in the graph. GXLDocument gxlDocument = new GXLDocument(); GXLGraph graph = new
		 * GXLGraph( "executesmany" ); graph.setType(uri.uExecutesMany());
		 * 
		 * addClasses(graph); addMethods(graph); addStmts(graph);
		 * 
		 * // Write out the GXL graph. gxlDocument.getDocumentElement().add(graph); gxlDocument.write(file);
		 */
	}

	/** Write a set polymorphic invoke instructions to a GXL file. */
	public void write(Polymorphic eo, OutputStream file) throws IOException {
		throw new RuntimeException("not implemented");
		/*
		 * uri = new URIs( "/~olhotak/probe/schemas/polymorphic.gxl" ); initializeMaps();
		 * 
		 * // Collect up all the stmts, methods and classes appearing in the set. for( Iterator sIt =
		 * eo.stmts().iterator(); sIt.hasNext(); ) { final ProbeStmt s = (ProbeStmt) sIt.next(); addStmt( s ); }
		 * 
		 * // Assign ids to all stmt, method and class nodes. assignIDs();
		 * 
		 * // Create the GXL nodes in the graph. GXLDocument gxlDocument = new GXLDocument(); GXLGraph graph = new
		 * GXLGraph( "polymorphic" ); graph.setType(uri.uPolymorphic());
		 * 
		 * addClasses(graph); addMethods(graph); addStmts(graph);
		 * 
		 * // Write out the GXL graph. gxlDocument.getDocumentElement().add(graph); gxlDocument.write(file);
		 */
	}

	/** Write a set of cast instructions that fail to a GXL file. */
	public void write(FailCast fc, OutputStream file) throws IOException {
		throw new RuntimeException("not implemented");
		/*
		 * uri = new URIs( "/~olhotak/probe/schemas/failcast.gxl" ); initializeMaps();
		 * 
		 * // Collect up all the stmts, methods and classes appearing in the set. for( Iterator sIt =
		 * fc.stmts().iterator(); sIt.hasNext(); ) { final ProbeStmt s = (ProbeStmt) sIt.next(); addStmt( s ); } for(
		 * Iterator sIt = fc.executes().iterator(); sIt.hasNext(); ) { final ProbeStmt s = (ProbeStmt) sIt.next();
		 * addStmt( s ); }
		 * 
		 * // Assign ids to all stmt, method and class nodes. assignIDs();
		 * 
		 * // Create the GXL nodes in the graph. GXLDocument gxlDocument = new GXLDocument(); GXLGraph graph = new
		 * GXLGraph( "failcast" ); graph.setType(uri.uFailCast());
		 * 
		 * GXLNode root = new GXLNode("root"); root.setType(uri.uRoot()); graph.add(root);
		 * 
		 * // Add edges. for( Iterator stmtIt = fc.stmts().iterator(); stmtIt.hasNext(); ) { final ProbeStmt stmt =
		 * (ProbeStmt) stmtIt.next(); GXLEdge edge = new GXLEdge( "root", getId(stmt) ); edge.setType(uri.fails());
		 * graph.add( edge ); } for( Iterator stmtIt = fc.executes().iterator(); stmtIt.hasNext(); ) { final ProbeStmt
		 * stmt = (ProbeStmt) stmtIt.next(); GXLEdge edge = new GXLEdge( "root", getId(stmt) );
		 * edge.setType(uri.executes()); graph.add( edge ); }
		 * 
		 * addClasses(graph); addMethods(graph); addStmts(graph);
		 * 
		 * // Write out the GXL graph. gxlDocument.getDocumentElement().add(graph); gxlDocument.write(file);
		 */
	}

	/**
	 * Write a set of allocation sites whose objects escape their allocating thread/method to a GXL file.
	 */
	public void write(Escape esc, OutputStream file) throws IOException {
		throw new RuntimeException("not implemented");
		/*
		 * uri = new URIs( "/~olhotak/probe/schemas/escape.gxl" ); initializeMaps();
		 * 
		 * // Collect up all the stmts, methods and classes appearing in the sets. for( Iterator sIt =
		 * esc.escapesThread().iterator(); sIt.hasNext(); ) { final ProbeStmt s = (ProbeStmt) sIt.next(); addStmt( s );
		 * } for( Iterator sIt = esc.escapesMethod().iterator(); sIt.hasNext(); ) { final ProbeStmt s = (ProbeStmt)
		 * sIt.next(); addStmt( s ); } for( Iterator sIt = esc.anyAlloc().iterator(); sIt.hasNext(); ) { final ProbeStmt
		 * s = (ProbeStmt) sIt.next(); addStmt( s ); }
		 * 
		 * // Assign ids to all stmt, method and class nodes. assignIDs();
		 * 
		 * // Create the GXL nodes in the graph. GXLDocument gxlDocument = new GXLDocument(); GXLGraph graph = new
		 * GXLGraph( "escape" ); graph.setType(uri.uEscape());
		 * 
		 * GXLNode root = new GXLNode("root"); root.setType(uri.uRoot()); graph.add(root);
		 * 
		 * // Add edges. for( Iterator stmtIt = esc.escapesThread().iterator(); stmtIt.hasNext(); ) { final ProbeStmt
		 * stmt = (ProbeStmt) stmtIt.next(); GXLEdge edge = new GXLEdge( "root", getId(stmt) );
		 * edge.setType(uri.escapesThread()); graph.add( edge ); } for( Iterator stmtIt =
		 * esc.escapesMethod().iterator(); stmtIt.hasNext(); ) { final ProbeStmt stmt = (ProbeStmt) stmtIt.next();
		 * GXLEdge edge = new GXLEdge( "root", getId(stmt) ); edge.setType(uri.escapesMethod()); graph.add( edge ); }
		 * for( Iterator stmtIt = esc.anyAlloc().iterator(); stmtIt.hasNext(); ) { final ProbeStmt stmt = (ProbeStmt)
		 * stmtIt.next(); GXLEdge edge = new GXLEdge( "root", getId(stmt) ); edge.setType(uri.anyAlloc()); graph.add(
		 * edge ); }
		 * 
		 * addClasses(graph); addMethods(graph); addStmts(graph);
		 * 
		 * // Write out the GXL graph. gxlDocument.getDocumentElement().add(graph); gxlDocument.write(file);
		 */
	}

	/** Write a points-to graph to a GXL file. */
	public void write(PointsTo ptGraph, OutputStream file) throws IOException {
		throw new RuntimeException("not implemented");
		/*
		 * uri = new URIs( "/~olhotak/probe/schemas/pointsto.gxl" ); initializeMaps();
		 * 
		 * // Collect up all the stmts, methods and classes. for( Iterator ptIt =
		 * ptGraph.pointsTo().keySet().iterator(); ptIt.hasNext(); ) { final Pointer pt = (Pointer) ptIt.next();
		 * addPointer( pt ); } for( Iterator setIt = new HashSet(ptGraph.pointsTo().values()).iterator();
		 * setIt.hasNext(); ) { final ProbePtSet set = (ProbePtSet) setIt.next(); addPtSet(set); for( Iterator hoIt =
		 * set.heapObjects().iterator(); hoIt.hasNext(); ) { final HeapObject ho = (HeapObject) hoIt.next();
		 * addHeapObject( ho ); } }
		 * 
		 * // Assign ids to all method and class nodes. assignIDs();
		 * 
		 * // Create the GXL nodes in the graph. GXLDocument gxlDocument = new GXLDocument(); GXLGraph graph = new
		 * GXLGraph( "pointsto" ); graph.setType(uri.uPointsTo()); GXLNode external = new GXLNode("External");
		 * external.setType(uri.uExternal()); graph.add(external);
		 * 
		 * addClasses(graph); addMethods(graph); addStmts(graph); addParameters(graph); addPtSets(graph);
		 * 
		 * // Add the points-to edges to the GXL graph. for( Iterator ptIt = ptGraph.pointsTo().keySet().iterator();
		 * ptIt.hasNext(); ) { final Pointer pt = (Pointer) ptIt.next(); ProbePtSet set = (ProbePtSet)
		 * ptGraph.pointsTo().get(pt); GXLEdge edge = new GXLEdge( getId(pt), getId(set) );
		 * edge.setType(uri.pointsTo()); graph.add( edge ); } for( Iterator setIt = new
		 * HashSet(ptGraph.pointsTo().values()).iterator(); setIt.hasNext(); ) { final ProbePtSet set = (ProbePtSet)
		 * setIt.next(); for( Iterator hoIt = set.heapObjects().iterator(); hoIt.hasNext(); ) { final HeapObject ho =
		 * (HeapObject) hoIt.next(); String id; if( ho instanceof External ) { id = external.getID(); } else { id =
		 * getId( (ProbeStmt) ho ); } GXLEdge edge = new GXLEdge( id, getId(set) ); edge.setType(uri.inSet());
		 * graph.add(edge); } }
		 * 
		 * // Write out the GXL graph. gxlDocument.getDocumentElement().add(graph); gxlDocument.write(file);
		 */
	}

	/** Write side-effect information to a GXL file. */
	public void write(SideEffect sideEffect, OutputStream file) throws IOException {
		throw new RuntimeException("not implemented");
		/*
		 * uri = new URIs( "/~olhotak/probe/schemas/sideeffect.gxl" ); initializeMaps();
		 * 
		 * // Collect up all the stmts, methods and classes. Set sets = new HashSet();
		 * sets.addAll(sideEffect.reads().values()); sets.addAll(sideEffect.writes().values()); for( Iterator setIt =
		 * sets.iterator(); setIt.hasNext(); ) { final ProbeFieldSet set = (ProbeFieldSet) setIt.next();
		 * addFieldSet(set); for( Iterator fieldIt = set.fields().iterator(); fieldIt.hasNext(); ) { final ProbeField
		 * field = (ProbeField) fieldIt.next(); addField( field ); } }
		 * 
		 * Set stmts = new HashSet(); stmts.addAll(sideEffect.reads().keySet());
		 * stmts.addAll(sideEffect.writes().keySet()); for( Iterator stmtIt = stmts.iterator(); stmtIt.hasNext(); ) {
		 * final ProbeStmt stmt = (ProbeStmt) stmtIt.next(); addStmt(stmt); }
		 * 
		 * // Assign ids to all method and class nodes. assignIDs();
		 * 
		 * // Create the GXL nodes in the graph. GXLDocument gxlDocument = new GXLDocument(); GXLGraph graph = new
		 * GXLGraph( "sideeffect" ); graph.setType(uri.uSideEffect());
		 * 
		 * addClasses(graph); addMethods(graph); addStmts(graph); addFields(graph); addFieldSets(graph);
		 * 
		 * // Add the reads edges to the GXL graph. for( Iterator stIt = sideEffect.reads().keySet().iterator();
		 * stIt.hasNext(); ) { final ProbeStmt st = (ProbeStmt) stIt.next(); ProbeFieldSet set = (ProbeFieldSet)
		 * sideEffect.reads().get(st); GXLEdge edge = new GXLEdge( getId(st), getId(set) ); edge.setType(uri.reads());
		 * graph.add( edge ); } for( Iterator stIt = sideEffect.writes().keySet().iterator(); stIt.hasNext(); ) { final
		 * ProbeStmt st = (ProbeStmt) stIt.next(); ProbeFieldSet set = (ProbeFieldSet) sideEffect.writes().get(st);
		 * GXLEdge edge = new GXLEdge( getId(st), getId(set) ); edge.setType(uri.writes()); graph.add( edge ); } for(
		 * Iterator setIt = sets.iterator(); setIt.hasNext(); ) { final ProbeFieldSet set = (ProbeFieldSet)
		 * setIt.next(); for( Iterator fieldIt = set.fields().iterator(); fieldIt.hasNext(); ) { final ProbeField field
		 * = (ProbeField) fieldIt.next(); String id; GXLEdge edge = new GXLEdge( getId(field), getId(set) );
		 * edge.setType(uri.inSet()); graph.add(edge); } }
		 * 
		 * // Write out the GXL graph. gxlDocument.getDocumentElement().add(graph); gxlDocument.write(file);
		 */
	}

	/* End of public methods. */

	private Set<ProbeField> fields;
	private Set<ProbeParameter> parameters;
	private Set<ProbeStmt> stmts;
	private Set<ProbeMethod> methods;
	private Set<ProbeClass> classes;
	private Set<ProbePtSet> ptsets;
	private Set<ProbeFieldSet> fieldsets;
	private Map<Object, Integer> idMap;

	private void addMethod(ProbeMethod m) {
		methods.add(m);
		addClass(m.cls());
	}

	private void addField(ProbeField f) {
		fields.add(f);
		addClass(f.cls());
	}

	private void addClass(ProbeClass c) {
		classes.add(c);
	}

	private String getId(Pointer s) {
		Integer id = idMap.get(s);
		return "id" + id.toString();
	}

	private String getId(ProbeMethod m) {
		Integer id = idMap.get(m);
		return "id" + id.toString();
	}

	private String getId(ProbeField f) {
		Integer id = idMap.get(f);
		return "id" + id.toString();
	}

	private String getId(ProbeClass cl) {
		Integer id = idMap.get(cl);
		return "id" + id.toString();
	}

	private String getId(ProbePtSet p) {
		Integer id = idMap.get(p);
		return "id" + id.toString();
	}

	private String getId(ProbeFieldSet p) {
		Integer id = idMap.get(p);
		return "id" + id.toString();
	}

	private void outputClasses(PrintWriter out) {
		for (ProbeClass cl : classes) {
			outputClass(out, cl);
		}
	}

	private void outputClass(PrintWriter out, ProbeClass cl) {
		out.println(Util.ClassTag);
		out.println(getId(cl));
		out.println(cl.pkg());
		out.println(cl.name());
	}

	private void outputMethods(PrintWriter out) {
		for (ProbeMethod m : methods) {
			outputMethod(out, m);
		}
	}

	private void outputMethod(PrintWriter out, ProbeMethod m) {
		out.println(Util.MethodTag);
		out.println(getId(m));
		out.println(m.name());
		out.println(m.signature());
		out.println(getId(m.cls()));
	}

	private void initializeMaps() {
		stmts = new HashSet<ProbeStmt>();
		fields = new HashSet<ProbeField>();
		methods = new HashSet<ProbeMethod>();
		classes = new HashSet<ProbeClass>();
		parameters = new HashSet<ProbeParameter>();
		ptsets = new HashSet<ProbePtSet>();
		fieldsets = new HashSet<ProbeFieldSet>();
	}

	/** Assign ids to all method and class nodes. */
	private void assignIDs() {
		int id = 1;
		idMap = new HashMap<Object, Integer>();
		for (ProbeStmt s : stmts) {
			idMap.put(s, new Integer(id++));
		}
		for (ProbeMethod m : methods) {
			idMap.put(m, new Integer(id++));
		}
		for (ProbeField f : fields) {
			idMap.put(f, new Integer(id++));
		}
		for (ProbeClass cl : classes) {
			idMap.put(cl, new Integer(id++));
		}
		for (ProbeParameter p : parameters) {
			idMap.put(p, new Integer(id++));
		}
		for (ProbePtSet p : ptsets) {
			idMap.put(p, new Integer(id++));
		}
		for (ProbeFieldSet p : fieldsets) {
			idMap.put(p, new Integer(id++));
		}
	}
}
