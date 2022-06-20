package org.openrewrite.java.dataflow2.examples;

import lombok.Value;
import org.openrewrite.java.dataflow2.Joiner;
import org.openrewrite.java.tree.Expression;

import java.util.Collection;

@Value
public class HttpAnalysisValue {
    Understanding name;
    Expression literal;

    public enum Understanding {
        UNKNOWN, SECURE, NOT_SECURE, CONFLICT
    }

    public static final HttpAnalysisValue UNKNOWN = new HttpAnalysisValue(Understanding.UNKNOWN, null);
    public static final HttpAnalysisValue SECURE = new HttpAnalysisValue(Understanding.SECURE, null);

    public static final Joiner<HttpAnalysisValue> JOINER = new Joiner<HttpAnalysisValue>() {
        @Override
        public HttpAnalysisValue join(Collection<HttpAnalysisValue> values) {
            HttpAnalysisValue result = UNKNOWN;
            for (HttpAnalysisValue value : values) {
                if (value == UNKNOWN) {
                    result = value;
                } else {
                    return UNKNOWN;
                }
            }
            return result;
        }

        @Override
        public HttpAnalysisValue lowerBound() {
            return UNKNOWN;
        }

        @Override
        public HttpAnalysisValue defaultInitialization() {
            return SECURE;
        }
    };
}
