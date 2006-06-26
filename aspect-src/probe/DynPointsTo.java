import java.util.*;
import java.io.*;
import probe.*;
import uk.ac.ox.comlab.abc.eaj.lang.*;
import uk.ac.ox.comlab.abc.eaj.lang.reflect.*;
import org.aspectj.lang.reflect.MethodSignature;

public aspect DynPointsTo {
    private ProbeStmt stmt(org.aspectj.lang.JoinPoint.StaticPart sjpIn) {
        JoinPoint.StaticPart sjp = (JoinPoint.StaticPart) sjpIn;
        BytecodeLocation bl = sjp.getBytecodeLocation();
        return ObjectManager.v().getStmt( method(sjp), bl.getBytecodeOffset() );
    }
    private ProbeMethod method(org.aspectj.lang.JoinPoint.StaticPart sjp) {
        MethodSignature ms = (MethodSignature) sjp.getSignature();
        Class[] parms = ms.getParameterTypes();
        StringBuffer sig = new StringBuffer();
        for( int i = 0; i < parms.length; i++ ) {
            String name = parms[i].getName();
            if( name.charAt(0) == '[' ) sig.append(name);
            else if( name.equals("byte") ) sig.append('B');
            else if( name.equals("char") ) sig.append('C');
            else if( name.equals("double") ) sig.append('D');
            else if( name.equals("float") ) sig.append('F');
            else if( name.equals("int") ) sig.append('I');
            else if( name.equals("long") ) sig.append('J');
            else if( name.equals("short") ) sig.append('S');
            else {
                sig.append('L'); sig.append(name); sig.append(';');
            }
        }
        return ObjectManager.v().
            getMethod(cls(sjp), ms.getName(), sig.toString());
    }
    private ProbeClass cls(org.aspectj.lang.JoinPoint.StaticPart sjp) {
        return ObjectManager.v().getClass(sjp.getSignature().
                getDeclaringType().getName());
    }

    before(Object x)
        : execution(!static * *.*(..)) && this(x) {
        addToPointsTo(
            ObjectManager.v().getParameter(
                method(thisJoinPointStaticPart),
                -1),
            x
        );
    }

    before(Object x) 
        : set(Object+ *.*) && target(x) {
        addToPointsTo(
            stmt((uk.ac.ox.comlab.abc.eaj.lang.JoinPoint.StaticPart)
                    thisJoinPointStaticPart),
            x
        );
    }

    before(Object x)
        : execution(!static * *.*(..)) && args(x,..) {
        addToPointsTo(
            ObjectManager.v().getParameter(
                method(thisJoinPointStaticPart),
                1),
            x
        );
    }



    pointcut main() 
        : execution(void *.main(String[]));
    after()
        : main() && !cflowbelow(main()) {
        PointsTo ptGraph = new PointsTo();
        for( Iterator ptIt = pointsTo.keySet().iterator(); ptIt.hasNext(); ) {
            final Pointer pt = (Pointer) ptIt.next();
            Collection c = (Collection) pointsTo.get(pt);
            ptGraph.pointsTo().put(pt, ObjectManager.v().getPtSet(c));
        }
        try {
            new GXLWriter().write(ptGraph, System.out);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    after(Object x) returning 
        : call(*.new(..)) && target(x) {
        objectMap.put( new Integer(System.identityHashCode(x)),
                stmt((uk.ac.ox.comlab.abc.eaj.lang.JoinPoint.StaticPart)
                        thisJoinPointStaticPart));
    }

    after(Object x) returning
        : execution(!static * *.*(..)) && target(x) {
        addToPointsTo(
            ObjectManager.v().getParameter(
                method(thisJoinPointStaticPart),
                -2),
            x
        );
    }

    after() returning (Object x)
        : get(Object+ *.*) {
        addToPointsTo(
            stmt((uk.ac.ox.comlab.abc.eaj.lang.JoinPoint.StaticPart)
                    thisJoinPointStaticPart),
            x
        );
    }

    private static Map objectMap = new HashMap();
    private static Map pointsTo = new HashMap();
    private void addToPointsTo(Pointer pointer, Object target) {
        HeapObject tgtObj = (HeapObject)
            objectMap.get(new Integer(System.identityHashCode(target)));
        if( tgtObj == null ) tgtObj = External.v();
        Collection set = (Collection) pointsTo.get(pointer);
        if(set == null) pointsTo.put(pointer, set = new HashSet());
        set.add(tgtObj);
    }
}

