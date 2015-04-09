package net.programania;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class VaultTest {

  public static final String IT_WORKS = "It Works!";
  private static final String ITS_STOPPED = "It's stopped now!";

  @Test
  public void vaults_should_register_suppliers_and_get_singletons() {
    Vault vault = new Vault();
    vault.register(A.class, v -> new A());
    vault.register(B.class, v -> new B(v.get(A.class)));
    vault.register(C.class, v -> new C(v.get(B.class)));

    assertThat(vault.get(C.class).test(), is(IT_WORKS));
    assertThat(vault.get(C.class), is(vault.get(C.class)));
    assertThat(vault.get(C.class).getB(), is(vault.get(B.class)));
    assertThat(vault.get(C.class).getB().getA(), is(vault.get(A.class)));
  }

  @Test
  public void vaults_should_be_composable() throws Exception {
    Vault parentOne = new Vault();
    parentOne.register(D.class, v -> new D());

    Vault parentTwo = new Vault(parentOne);
    parentTwo.register(E.class, v -> new E());

    Vault child = new Vault(parentOne, parentTwo);
    child.register(F.class, v -> new F(v.get(D.class), v.get(E.class)));

    assertThat(child.get(F.class).getD(), is(parentOne.get(D.class)));
    assertThat(child.get(F.class).getE(), is(parentTwo.get(E.class)));
  }

  @Test
  public void vaults_can_start_or_stop_LifeCycle_extending_instances() {
    Vault vault = new Vault();
    vault.register(SomeService.class, v -> new SomeService());

    SomeConsumer someConsumer = new SomeConsumer(vault);
    someConsumer.start();
    assertThat(someConsumer.doSomething(), is(IT_WORKS));
    someConsumer.stop();
    assertThat(someConsumer.doSomething(), is(ITS_STOPPED));
  }

  public static class A {
    public A() {
    }
  }

  public static class B {
    private final A a;

    public B(A a) {
      this.a = a;
    }

    public A getA() {
      return a;
    }
  }

  public static class C {
    private final B b;

    public C(B b) {
      this.b = b;
    }

    public String test() {
      return IT_WORKS;
    }

    public B getB() {
      return b;
    }

  }

  public static class D {

  }

  public static class E {

  }

  public static class F {
    private final D d;
    private final E e;

    public F(D d, E e) {
      this.d = d;
      this.e = e;
    }

    public D getD() {
      return d;
    }

    public E getE() {
      return e;
    }
  }

  public static class SomeConsumer extends Vault {

    private final SomeService someService;

    public SomeConsumer(Vault vault) {
      super(vault);
      someService = get(SomeService.class);
    }

    public String doSomething() {
      return this.someService.doSomething();
    }
  }

  private class SomeService implements LifeCycle {
    public Status status = Status.IDLE;

    @Override
    @SuppressWarnings("unchecked")
    public SomeService start() {
      status = Status.STARTED;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SomeService stop() {
      status = Status.STOPPED;
      return this;
    }

    public String doSomething() {
      if (status.equals(Status.IDLE))
        throw new RuntimeException("Tried to do something while in IDLE status ");
      return status.equals(Status.STARTED) ? IT_WORKS : ITS_STOPPED;
    }
  }
}
