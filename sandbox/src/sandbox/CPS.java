package sandbox;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class CPS {

    public static void main(String[] args) {
        AtomicInteger res = new AtomicInteger(-1);
        trampoline(factorial(12, endCall(res::set)));
        System.out.println(res.get());
    }

    @FunctionalInterface
    private interface Cont<R> {
        Thunk apply(R result);
    }

    @FunctionalInterface
    private interface Thunk {
        Thunk run();
    }

    static void trampoline(Thunk thunk) {
        while (thunk != null) {
            thunk = thunk.run();
        }
    }

    static <T> Cont<T> endCall(Consumer<T> call) {
        return r -> {
            call.accept(r);
            return null; // computation is finished
        };
    }


    static Thunk eq(int a, int b, Cont<Boolean> cont) {
        boolean res = a == b;
        return () -> cont.apply(res);
    }

    static Thunk lt(int a, int b, Cont<Boolean> cont) {
        boolean res = a < b;
        return () -> cont.apply(res);
    }

    static Thunk multiply(int a, int b, Cont<Integer> cont) {
        int prod = a * b;
        return () -> cont.apply(prod);
    }

    static Thunk add(int a, int b, Cont<Integer> cont) {
        int sum = a + b;
        return () -> cont.apply(sum);
    }

    static Thunk add3(int a, int b, int c, Cont<Integer> cont) {
        return add(a, b, sum ->
                add(sum, c, cont));
    }

    static Thunk iff(boolean expr,
                     Cont<Boolean> trueBranch,
                     Cont<Boolean> falseBranch) {
        return (expr)
                ? () -> trueBranch.apply(true)
                : () -> falseBranch.apply(false);
    }

    private static Thunk factorial(int n, Cont<Integer> cont) {
        return eq(n, 0, isNZero ->
                iff(isNZero,
                        x -> cont.apply(1),
                        x -> add(n, -1, nm1 ->
                                factorial(nm1, fnm1 ->
                                        multiply(n, fnm1, cont)))));
    }
}
