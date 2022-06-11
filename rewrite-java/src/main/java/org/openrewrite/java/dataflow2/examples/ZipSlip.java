package org.openrewrite.java.dataflow2.examples;

import org.openrewrite.Incubating;
import org.openrewrite.java.dataflow2.DataFlowGraph;
import org.openrewrite.java.dataflow2.TaintAnalysis;

@Incubating(since = "7.24.0")
public class ZipSlip extends TaintAnalysis {

    public ZipSlip(DataFlowGraph dfg) {
        super(dfg);
    }

}
