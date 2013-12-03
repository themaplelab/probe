package probe;
import java.io.*;
import net.sourceforge.gxl.*;
import java.util.*;

/** Converts GXL schema to a graphical representation in dot. */

public class GXL2Dot {
    static class Node {
        Node(String id) { this.id = id; }
        String id;
        String name;
        boolean abs;
        List attrs = new ArrayList();
        List supers = new ArrayList();
        public String toString() {
            StringBuffer ret = new StringBuffer();
            ret.append(id);
            ret.append("[ shape=\"record\", fontname=\"");
            if( abs ) ret.append("Helvetica-Italic");
            else ret.append("Helvetica");
            ret.append("\", label=\"{");
            ret.append(name);
            ret.append(" | ");
            for( Iterator attrIt = attrs.iterator(); attrIt.hasNext(); ) {
                final Attr attr = (Attr) attrIt.next();
                ret.append(attr);
                ret.append("\\l");
            }
            //ret.append(" | }\" ];\n");
            ret.append(" }\" ];\n");
            for( Iterator spIt = supers.iterator(); spIt.hasNext(); ) {
                final Node sp = (Node) spIt.next();
                ret.append(sp.id);
                ret.append(" -- ");
                ret.append(id);
                ret.append(" [arrowtail=empty];\n");
            }
            return ret.toString();
        }
    }
    Map nodes = new HashMap();
    Node node(String id) {
        Node ret = (Node) nodes.get(id);
        if( ret == null ) {
            nodes.put(id, ret = new Node(id));
        }
        return ret;
    }
    static class Edge {
        Edge(String id) { this.id = id; }
        String id;
        String name;
        boolean abs;
        boolean dir;
        int srcLow;
        int srcHigh;
        int tgtLow;
        int tgtHigh;
        Node src;
        Node tgt;
        public String limit(int lim) {
            if( lim < 0 ) return "*";
            else return ""+lim;
        }
        public String toString() {
            StringBuffer ret = new StringBuffer();
            ret.append(src.id);
            ret.append(" -- ");
            ret.append(tgt.id);
            ret.append("[ fontname=\"Helvetica\", label=\"" );
            ret.append(name);
            ret.append("\" ");
            if( dir ) ret.append( ", arrowhead=open, dir=forward " );
            ret.append( ", taillabel=\"" );
            ret.append( limit(srcLow) );
            ret.append( ".." );
            ret.append( limit(srcHigh) );
            ret.append( "\", headlabel=\"" );
            ret.append( limit(tgtLow) );
            ret.append( ".." );
            ret.append( limit(tgtHigh) );
            ret.append("\" ];\n");
            return ret.toString();
        }
    }
    Map edges = new HashMap();
    Edge edge(String id) {
        Edge ret = (Edge) edges.get(id);
        if( ret == null ) {
            edges.put(id, ret = new Edge(id));
        }
        return ret;
    }
    static class Attr {
        Attr(String id) { this.id = id; }
        String id;
        String name;
        Domain domain;
        public String toString() {
            return name + " : "+domain.name;
        }
    }
    Map attrs = new HashMap();
    Attr attr(String id) {
        Attr ret = (Attr) attrs.get(id);
        if( ret == null ) {
            attrs.put(id, ret = new Attr(id));
        }
        return ret;
    }
    static class Domain {
        Domain(String id) { this.id = id; }
        String id;
        String name;
    }
    Map domains = new HashMap();
    Domain domain(String id) {
        Domain ret = (Domain) domains.get(id);
        if( ret == null ) {
            domains.put(id, ret = new Domain(id));
        }
        return ret;
    }
    public boolean type( GXLTypedElement elem, String desiredType ) {
        return elem.getType().getURI().getFragment().equals(desiredType);
    }
    public String getString( GXLAttributedElement elem, String desiredAttr ) {
        return ((GXLString) elem.getAttr(desiredAttr).getValue()).getValue();
    }
    public boolean getBoolean( GXLAttributedElement elem, String desiredAttr ) {
        return ((GXLBool) elem.getAttr(desiredAttr).getValue()).getBooleanValue();
    }
    public int getTupleInt( GXLAttributedElement elem, String desiredAttr, 
            int offset ) {
        return ((GXLInt) ((GXLTup) elem.getAttr(desiredAttr).getValue()).getValueAt(offset)).getIntValue();
    }
    public static final void main(String[] args) throws Exception {
        new GXL2Dot().run();
    }
    public void run() throws Exception {
        GXLDocument doc = new GXLDocument(System.in);
        GXLGraph graph = (GXLGraph) doc.getDocumentElement().getGraphAt(0);
        for( int i = 0; i < graph.getGraphElementCount(); i++ ) {
            GXLElement elem = graph.getGraphElementAt(i);
            if( elem instanceof GXLNode ) {
                GXLNode node = (GXLNode) elem;
                if(false) {
                } else if( type(node, "GraphClass") ) {
                    // nothing
                } else if( type(node, "NodeClass") ) {
                    node(node.getID()).name = getString(node, "name");
                    node(node.getID()).abs = getBoolean(node, "isabstract");
                } else if( type(node, "EdgeClass") ) {
                    edge(node.getID()).name = getString(node, "name");
                    edge(node.getID()).abs = getBoolean(node, "isabstract");
                    edge(node.getID()).dir = getBoolean(node, "isdirected");
                } else if( type(node, "AttributeClass") ) {
                    attr(node.getID()).name = getString(node, "name");
                } else if( type(node, "String") ) {
                    domain(node.getID()).name = "String";
                } else if( type(node, "Int") ) {
                    domain(node.getID()).name = "int";
                } else throw new RuntimeException("Unknown type: "+node.getType().getURI());
            } else if( elem instanceof GXLEdge ) {
                GXLEdge edge = (GXLEdge) elem;
                if(false) {
                } else if( type(edge, "from") ) {
                    edge(edge.getSourceID()).srcLow = getTupleInt(edge, "limits", 0);
                    edge(edge.getSourceID()).srcHigh = getTupleInt(edge, "limits", 1);
                    edge(edge.getSourceID()).src = node(edge.getTargetID());
                } else if( type(edge, "to") ) {
                    edge(edge.getSourceID()).tgtLow = getTupleInt(edge, "limits", 0);
                    edge(edge.getSourceID()).tgtHigh = getTupleInt(edge, "limits", 1);
                    edge(edge.getSourceID()).tgt = node(edge.getTargetID());
                } else if( type(edge, "hasAttribute") ) {
                    node(edge.getSourceID()).attrs.add(attr(edge.getTargetID()));
                } else if( type(edge, "hasDomain") ) {
                    attr(edge.getSourceID()).domain = domain(edge.getTargetID());
                } else if( type(edge, "isA") ) {
                    node(edge.getSourceID()).supers.add(node(edge.getTargetID()));
                } else if( type(edge, "contains") ) {
                    // ignore
                } else throw new RuntimeException("Unknown type: "+edge.getType().getURI());
            } else throw new RuntimeException(elem.toString());
        }
        System.out.println( "graph G {" );
        for( Iterator nodeIt = nodes.keySet().iterator(); nodeIt.hasNext(); ) {
            final String node = (String) nodeIt.next();
            System.out.println( nodes.get(node) );
        }
        for( Iterator edgeIt = edges.keySet().iterator(); edgeIt.hasNext(); ) {
            final String edge = (String) edgeIt.next();
            System.out.println( edges.get(edge) );
        }
        System.out.println( "};" );
    }
}
