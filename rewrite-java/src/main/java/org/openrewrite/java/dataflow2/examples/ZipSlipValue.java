package org.openrewrite.java.dataflow2.examples;

import lombok.AllArgsConstructor;
import org.openrewrite.java.dataflow2.Joiner;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import java.util.Collection;

@AllArgsConstructor
public class ZipSlipValue {

    // ZipEntry entry, File dir
    // String fileName = entry.getName();
    // File file = new File(dir, fileName);

    public final String name;
    public final Expression dir; // non-null if the value is the result of 'new File(dir, ..)'

    public static final Joiner<ZipSlipValue> JOINER = new Joiner<ZipSlipValue>() {
        @Override
        public ZipSlipValue join(Collection<ZipSlipValue> values) {
            return ZipSlipValue.join(values);
        }

        @Override
        public ZipSlipValue lowerBound() {
            return UNKNOWN;
        }

        @Override
        public ZipSlipValue defaultInitialization() {
            return SAFE;
        }
    };

    // all other values have a non-null dir
    // lower bound (initial value) : nothing is know about the value
    public static final ZipSlipValue UNKNOWN = new ZipSlipValue("UNKNOWN", null);
    // upper bound : conflicting information about the value
    public static final ZipSlipValue UNSAFE = new ZipSlipValue("UNSAFE", null);
    // value is known to be the name of a zip entry
    public static final ZipSlipValue ZIP_ENTRY_NAME = new ZipSlipValue("ZIP_ENTRY_NAME", null);
    // the value is known to be safe
    public static final ZipSlipValue SAFE = new ZipSlipValue("SAFE", null);

    private static ZipSlipValue join(Collection<ZipSlipValue> values) {
        ZipSlipValue result = UNKNOWN;
        for (ZipSlipValue value : values) {
            if (result == UNKNOWN) {
                result = value;
            } else if (value == SAFE && result == SAFE) {
                // do nothing
            } else if (value == ZIP_ENTRY_NAME && result == ZIP_ENTRY_NAME) {
                // do nothing
            } else if(value.dir != null && result.dir != null && value.dir.equals(result.dir)) {
                // do nothing
            } else {
                return UNSAFE;
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

    public static boolean equal(Expression a, Expression b) {
        if(a instanceof J.Identifier && b instanceof J.Identifier) {
            return ((J.Identifier)a).getFieldType() == ((J.Identifier)b).getFieldType();
        }
        return false;
    }
}
