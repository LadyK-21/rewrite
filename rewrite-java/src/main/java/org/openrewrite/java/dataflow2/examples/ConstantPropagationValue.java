package org.openrewrite.java.dataflow2.examples;

import lombok.Value;
import org.openrewrite.java.dataflow2.Joiner;
import org.openrewrite.java.tree.Expression;

import java.util.Collection;

@Value
public class ConstantPropagationValue {
    Understanding understanding;
    Object value;

    public enum Understanding {
        UNKNOWN, KNOWN, CONFLICT
    }

    public static final ConstantPropagationValue UNKNOWN = new ConstantPropagationValue(Understanding.UNKNOWN, null);
    public static final ConstantPropagationValue CONFLICT = new ConstantPropagationValue(Understanding.CONFLICT, null);

    public static final Joiner<ConstantPropagationValue> JOINER = new Joiner<ConstantPropagationValue>() {
        @Override
        public ConstantPropagationValue join(Collection<ConstantPropagationValue> values) {
            ConstantPropagationValue result = UNKNOWN;
            for (ConstantPropagationValue value : values) {
                if(result == UNKNOWN) {
                    result = value;
                } else if (value == UNKNOWN) {
                    result = value;
                } else if(value == CONFLICT) {
                    return CONFLICT;
                } else if(value.getUnderstanding() == Understanding.KNOWN) {
                    if(result.getUnderstanding() != Understanding.KNOWN || !result.value.equals(value.value)) {
                        return CONFLICT;
                    }
                    result = value;
                }
            }
            return result;
        }

        @Override
        public ConstantPropagationValue lowerBound() {
            return UNKNOWN;
        }

        @Override
        public ConstantPropagationValue defaultInitialization() {
            return UNKNOWN;
        }
    };
}
