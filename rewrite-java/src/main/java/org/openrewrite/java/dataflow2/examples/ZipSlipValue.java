package org.openrewrite.java.dataflow2.examples;

import lombok.AllArgsConstructor;
import org.openrewrite.java.dataflow2.Joiner;
import org.openrewrite.java.tree.Expression;

import java.util.Collection;

@AllArgsConstructor
public
class ZipSlipValue {

    public final String name;
    public final Expression dir; // non-null if the value is the result of 'new File(dir, ..)'

    public static final Joiner<ZipSlipValue> JOINER = new Joiner<ZipSlipValue>() {
        @Override
        public ZipSlipValue join(Collection<ZipSlipValue> values) {
            return ZipSlipValue.join(values);
        }

        @Override
        public ZipSlipValue lowerBound() {
            return LOWER;
        }

        @Override
        public ZipSlipValue defaultInitialization() {
            return NULL;
        }
    };

    // all other values have a non-null dir
    public static final ZipSlipValue LOWER = new ZipSlipValue("LOWER", null);
    public static final ZipSlipValue UPPER = new ZipSlipValue("UPPER", null);
    public static final ZipSlipValue NULL = new ZipSlipValue("NULL", null);

    private static ZipSlipValue join(Collection<ZipSlipValue> values) {
        ZipSlipValue result = LOWER;
        for (ZipSlipValue value : values) {
            if (value == UPPER) {
                return UPPER;
            } else if (result == LOWER) {
                result = value;
            } else if (value != LOWER && !result.dir.equals(value.dir)) {
                return UPPER;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        if(name != null) {
            return name;
        } else {
            return dir.print();
        }
    }
}
