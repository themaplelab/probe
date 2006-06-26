package probe;

import java.io.*;
import java.util.*;

public interface AbsEdgeWeights {
    public double weight(ProbeMethod pm);
    public double weight(CallEdge fe);
}
