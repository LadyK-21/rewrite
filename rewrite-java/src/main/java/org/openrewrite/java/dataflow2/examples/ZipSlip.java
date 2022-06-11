package org.openrewrite.java.dataflow2.examples;

import lombok.AllArgsConstructor;
import org.openrewrite.Cursor;
import org.openrewrite.Incubating;
import org.openrewrite.java.dataflow2.*;
import org.openrewrite.java.tree.Expression;

import java.util.Collection;

@Incubating(since = "7.24.0")
public class ZipSlip extends DataFlowAnalysis<ProgramState<ZipSlipInfo>> {

    public ZipSlip(DataFlowGraph dfg) {
        super(dfg);
    }

    @Override
    public ProgramState<ZipSlipInfo> join(Collection<ProgramState<ZipSlipInfo>> outs) {
        return ProgramState.join(ZipSlipInfo.JOINER, outs);
    }

    @Override
    public ProgramState<ZipSlipInfo> defaultTransfer(Cursor pp, TraversalControl<ProgramState<ZipSlipInfo>> t) {
        return null;
    }

}

@AllArgsConstructor
class ZipSlipInfo {
    // File file = new File(dir, name);
    // FileOutputStream os = new FileOutputStream(file); // ZipSlip

    // We're interested in values returned by 'new File(dir, ..)', together with the expression 'dir'.

    Expression dir; // non-null if the value is the result of 'new File(dir, ..)'

    public static final Joiner<ZipSlipInfo> JOINER = new Joiner<ZipSlipInfo>() {
        @Override
        public ZipSlipInfo join(Collection<ZipSlipInfo> values) {
            return ZipSlipInfo.join(values);
        }

        @Override
        public ZipSlipInfo lowerBound() {
            return LOWER;
        }
    };

    // all other values have a non-null dir
    public static final ZipSlipInfo LOWER = new ZipSlipInfo(null);
    public static final ZipSlipInfo UPPER = new ZipSlipInfo(null);

    private static ZipSlipInfo join(Collection<ZipSlipInfo> values) {
        ZipSlipInfo result = LOWER;
        for(ZipSlipInfo value : values) {
            if(value == UPPER) {
                return UPPER;
            } else if(result == LOWER) {
                result = value;
            } else if(value != LOWER && !result.dir.equals(value.dir)) {
                return UPPER;
            }
        }
        return result;
    }
}