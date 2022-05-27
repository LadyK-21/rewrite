import static java.util.Objects.isNull;

public class Test {
    public void test() {
        boolean a = true;
        if (isNull(a)) {
            System.out.println("a is null");
        };
    }
}
