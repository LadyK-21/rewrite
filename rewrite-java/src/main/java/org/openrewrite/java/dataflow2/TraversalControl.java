package org.openrewrite.java.dataflow2;

import org.openrewrite.Incubating;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Incubating(since = "7.24.0")
public class TraversalControl<S extends ProgramState> {

    public static TraversalControl DEFAULT = new TraversalControl<>();
}
