package probe;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

/** Calculates and reports the differences between two call graphs. */
public class CallGraphDiff {
	public static void usage() {
		System.out.println("Usage: java probe.CallGraphDiff [options] supergraph.gxl subgraph.gxl");
		System.out.println("  -e      : ignore edges in supergraph whose targets are entry points in subgraph");
		System.out.println("  -r      : ignore edges in supergraph whose targets are reachable in subgraph");
		System.out.println("  -c      : ignore call edge context");
		System.out.println("  -s      : ignore edges to static initializer");
		System.out.println("  -j      : ignore edges to methods in the Java standard library");
		System.out.println("  -i      : do not output entrypoint information");
		System.out.println("  -f      : perform flow computation to rank edges by importance: edge algorithm");
		System.out.println("  -ff     : perform flow computation to rank edges by importance: node algorithm");
		System.out.println("  -a      : show all spurious edges rather than just those from reachable methods");
		System.out.println("  -m      : print names of missing methods");
		System.out.println("  -p      : ignore edges out of doPrivileged methods");
		System.out.println("  -d      : output dot graphs");
		System.out.println("  -switch : switch supergraph and subgraph");
		System.exit(1);
	}

	public static boolean dashE = false;
	public static boolean dashR = false;
	public static boolean dashC = false;
	public static boolean dashS = false;
	public static boolean dashJ = false;
	public static boolean dashI = false;
	public static boolean dashF = false;
	public static boolean dashFF = false;
	public static boolean dashA = false;
	public static boolean dashM = false;
	public static boolean dashP = false;
	public static boolean dashD = false;
	public static boolean dashSwitch = false;

	public static final void main(String[] args) {
		if (args.length < 2) {
			usage();
		}
		boolean doneOptions = false;
		String superFile = null;
		String subFile = null;
		for (int i = 0; i < args.length; i++) {
			if (!doneOptions && args[i].equals("-e"))
				dashE = true;
			else if (!doneOptions && args[i].equals("-r"))
				dashR = true;
			else if (!doneOptions && args[i].equals("-c"))
				dashC = true;
			else if (!doneOptions && args[i].equals("-s"))
				dashS = true;
			else if (!doneOptions && args[i].equals("-j"))
				dashJ = true;
			else if (!doneOptions && args[i].equals("-i"))
				dashI = true;
			else if (!doneOptions && args[i].equals("-f"))
				dashF = true;
			else if (!doneOptions && args[i].equals("-ff"))
				dashFF = true;
			else if (!doneOptions && args[i].equals("-a"))
				dashA = true;
			else if (!doneOptions && args[i].equals("-m"))
				dashM = true;
			else if (!doneOptions && args[i].equals("-p"))
				dashP = true;
			else if (!doneOptions && args[i].equals("-d"))
				dashD = true;
			else if (!doneOptions && args[i].equals("-switch"))
				dashSwitch = !dashSwitch;
			else if (!doneOptions && args[i].equals("--"))
				doneOptions = true;
			else if (superFile == null)
				superFile = args[i];
			else if (subFile == null)
				subFile = args[i];
			else
				usage();
		}
		if (subFile == null)
			usage();
		CallGraph supergraph;
		CallGraph subgraph;
		if (dashSwitch) {
			String temp = superFile;
			superFile = subFile;
			subFile = temp;
		}
		supergraph = readCallGraph(superFile);
		subgraph = readCallGraph(subFile);

		if (dashP || dashS || dashJ) {
			for (Iterator<CallEdge> edgeIt = supergraph.edges().iterator(); edgeIt.hasNext();) {
				final CallEdge edge = edgeIt.next();
				if ((dashP && edge.src().name().equals("doPrivileged"))
						|| (dashS && edge.dst().name().equals("<clinit>"))
						|| (dashJ && edge.dst().cls().pkg().startsWith("java.")))
					edgeIt.remove();
			}
			for (Iterator<CallEdge> edgeIt = subgraph.edges().iterator(); edgeIt.hasNext();) {
				final CallEdge edge = edgeIt.next();
				if ((dashP && edge.src().name().equals("doPrivileged"))
						|| (dashS && edge.dst().name().equals("<clinit>"))
						|| (dashJ && edge.dst().cls().pkg().startsWith("java.")))
					edgeIt.remove();
			}
		}
		AbsEdgeWeights weights = null;
		if (dashF) {
			weights = new EdgeWeights(supergraph, subgraph, dashD);
		} else if (dashFF) {
			weights = new EdgeWeights2(supergraph, subgraph, dashD);
		}
		CallGraph diff = diff(supergraph, subgraph);

		if (!dashI) {
			System.out.println("===========================================================================");
			System.out.println("Missing entry points in " + subFile + ": " + diff.entryPoints().size());
			System.out.println("===========================================================================");
			if (weights != null) {
				final AbsEdgeWeights weightsF = weights;
				TreeSet<ProbeMethod> ts = new TreeSet<ProbeMethod>(new Comparator<ProbeMethod>() {
					public int compare(ProbeMethod pm1, ProbeMethod pm2) {
						if (weightsF.weight(pm1) < weightsF.weight(pm2))
							return -1;
						if (weightsF.weight(pm1) > weightsF.weight(pm2))
							return 1;
						return 0;
					}
				});
				ts.addAll(diff.entryPoints());
				for (ProbeMethod m : ts) {
					System.out.println(weights.weight(m) + " " + m);
				}
			} else {
				for (ProbeMethod m : diff.entryPoints()) {
					System.out.println(m.toString());
				}
			}
		}

		System.out.println("===========================================================================");
		System.out.println("Missing call edges in " + subFile + ": " + diff.edges().size());
		System.out.println("===========================================================================");
		if (weights != null) {
			final AbsEdgeWeights weightsF = weights;
			TreeSet<CallEdge> ts = new TreeSet<CallEdge>(new Comparator<CallEdge>() {
				public int compare(CallEdge e1, CallEdge e2) {
					if (weightsF.weight(e1) < weightsF.weight(e2))
						return -1;
					if (weightsF.weight(e1) > weightsF.weight(e2))
						return 1;
					return 0;
				}
			});
			ts.addAll(diff.edges());
			for (CallEdge e : ts) {
				System.out.println(weights.weight(e) + " " + e);
			}
		} else {
			for (CallEdge e : diff.edges()) {
				System.out.println(e.toString());
			}
		}

		Set<ProbeMethod> missingReachables = new HashSet<ProbeMethod>();
		missingReachables.addAll(supergraph.findReachables());
		missingReachables.removeAll(subgraph.findReachables());
		if (dashJ) {
			for (Iterator<ProbeMethod> methodIt = missingReachables.iterator(); methodIt.hasNext();) {
				final ProbeMethod method = methodIt.next();
				if (method.cls().pkg().startsWith("java."))
					methodIt.remove();
			}
		}

		System.out.println("===========================================================================");
		System.out.println("Number of reachable methods missing in " + subFile + ": " + missingReachables.size());
		System.out.println("===========================================================================");
		if (dashM) {
			List<String> lines = new ArrayList<String>();
			for (ProbeMethod pm : missingReachables) {
				lines.add(pm.toString());
			}
			Collections.sort(lines);
			for (String line : lines) {
				System.out.println(line);
			}
		}

	}

	/**
	 * Computes the difference of call graph subgraph subtracted from call graph supergraph. Specifically, the entry
	 * points of the resulting call graph are those entry points of supergraph which are not entry points of subgraph,
	 * and the call edges of the resulting call graph are those call edges of supergraph that are not call edges of
	 * subgraph, and whose source method is reachable in subgraph.
	 */
	public static CallGraph diff(CallGraph supergraph, CallGraph subgraph) {
		CallGraph ret = new CallGraph();

		ret.entryPoints().addAll(supergraph.entryPoints());
		ret.entryPoints().removeAll(subgraph.entryPoints());

		Set<ProbeMethod> reachables = subgraph.findReachables();

		ret.edges().addAll(supergraph.edges());
		ret.edges().removeAll(subgraph.edges());
		Iterator<CallEdge> it = ret.edges().iterator();
		while (it.hasNext()) {
			CallEdge e = it.next();
			if ((!dashA && !reachables.contains(e.src())) || (dashE && subgraph.entryPoints().contains(e.dst()))
					|| (dashR && reachables.contains(e.dst())))
				it.remove();
		}

		return ret;
	}

	private static CallGraph readCallGraph(String filename) {
		CallGraph ret;
		try {
			try {
				ret = new TextReader().readCallGraph(new FileInputStream(filename));
			} catch (RuntimeException e) {
				try {
					ret = new TextReader().readCallGraph(new GZIPInputStream(new FileInputStream(filename)));
				} catch (RuntimeException e2) {
					try {
						ret = new GXLReader().readCallGraph(new FileInputStream(filename));
					} catch (RuntimeException e3) {
						ret = new GXLReader().readCallGraph(new GZIPInputStream(new FileInputStream(filename)));
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("caught IOException " + e + " on file " + filename);
		}
		return ret;
	}
}
