package crawler.test.TestPolymophism;

public class ChildClass extends ParentClass {
    ParentClass pc;
    ChildClass cc;

    public ChildClass(boolean cont) {
        if(cont) {
            pc = new ParentClass();
            cc = new ChildClass(false);
        }
    }

    @Override
    public void printSelf() {
        System.out.println("Child");
    }

    public ParentClass returnParent() {
        return pc;
    }

    public ParentClass returnChild() {
        return cc;
    }
}
