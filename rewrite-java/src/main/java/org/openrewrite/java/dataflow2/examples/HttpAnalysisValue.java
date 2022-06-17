package org.openrewrite.java.dataflow2.examples;

import lombok.AllArgsConstructor;
import org.openrewrite.java.dataflow2.Joiner;
import org.openrewrite.java.tree.Expression;

import java.util.Collection;

@AllArgsConstructor
public class HttpAnalysisValue {

    public final understanding name;
    public final Expression literal;

    public static enum understanding {UNKNOWN, SECURE, NOT_SECURE, CONFLICT};
    public static final HttpAnalysisValue UNKNOWN = new HttpAnalysisValue(understanding.UNKNOWN, null);
    public static final HttpAnalysisValue SECURE = new HttpAnalysisValue(understanding.SECURE, null);
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
