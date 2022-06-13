package sandbox;

import org.openrewrite.java.Java11Parser;
import org.openrewrite.java.tree.J;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Main {

    public static boolean debug = false;

    public static void main(String[] args)
    {
        //TestPrevious.test();
        //TestNullAnalysis.test();
        TestZipSlip.test();
    }





    public static void main2()
    {
        Java11Parser parser = new Java11Parser.Builder().build();
        String source =
            "class C {\n" +
            "   void m() {\n" +
            "       a(); int x = 1 + 2; b();" +
//            "       int x = 0, y = 1;\n" +
//            "       for(int i=0, j=2*i+1; i<x; i++, j++) {\n" +
//            "           f(x); g(x); h(x);\n" +
//            "       }\n" +
//            "       a();" +
//            "       if(x == y+1) { u(x); } else { v(x); }\n" +
//            "       while(x == 0) { w(x); }\n" +
//            "       o.m(p,q,r);\n" +
//            "       b();\n" +
//            "       o.m(1+x++, 2+(y++ *3));\n" +
//            "       x = 1+(2*y);\n" +
            "   }\n" +
            "}\n"
            ;

        J.CompilationUnit cu = TestUtils.parse(source);

        // new MyVisitor().visit(cu, null);

        /*
        J.Block classBody = cu.getClasses().get(0).getBody();
        J.MethodDeclaration methodDecl = (J.MethodDeclaration) classBody.getStatements().get(0);
        List<Statement> methodStatements = methodDecl.getBody().getStatements();
        Statement stmt1 = methodStatements.get(1);
        Statement stmt2 = methodStatements.get(2);

        J.VariableDeclarations.NamedVariable xVar = ((J.VariableDeclarations)methodStatements.get(0)).getVariables().get(0);
        J.VariableDeclarations.NamedVariable yVar = ((J.VariableDeclarations)methodStatements.get(0)).getVariables().get(1);

        JavaType.Variable x = xVar.getVariableType();
        JavaType.Variable y = yVar.getVariableType();

        System.out.println("stmt1 = " + stmt1);
        System.out.println("stmt1 reads x = " + stmt1.reads(x));
        System.out.println("stmt1 writes x = " + stmt1.writes(x));
        System.out.println("stmt1 reads y = " + stmt1.reads(y));
        System.out.println("stmt1 writes y = " + stmt1.writes(y));

        System.out.println("stmt2 = " + stmt2);
        System.out.println("stmt2 reads x = " + stmt2.reads(x));
        System.out.println("stmt2 writes x = " + stmt2.writes(x));
        System.out.println("stmt2 reads y = " + stmt2.reads(y));
        System.out.println("stmt2 writes y = " + stmt2.writes(y));

        cu.print();
         */
    }

}


