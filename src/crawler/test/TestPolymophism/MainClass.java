package crawler.test.TestPolymophism;

public class MainClass {
    public static void main(String[] args) {
        ChildClass testclass = new ChildClass(true);
        ParentClass commonobj = testclass.returnParent();
        commonobj.printSelf();
        commonobj = testclass.returnChild();
        commonobj.printSelf();
    }
}
