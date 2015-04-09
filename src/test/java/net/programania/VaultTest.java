package net.programania;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class VaultTest {

  public static final String IT_WORKS = "It Works!";

  @Test
  public void test_basic_use() {
    Vault vault = Vault.empty();
    vault.register(A.class, v -> new A());
    vault.register(B.class, v -> new B(v.get(A.class)));
    vault.register(C.class, v -> new C(v.get(B.class)));

    assertThat(vault.get(C.class).test(), is(IT_WORKS));
    assertThat(vault.get(C.class), is(vault.get(C.class)));
    assertThat(vault.get(C.class).getB(), is(vault.get(B.class)));
    assertThat(vault.get(C.class).getB().getA(), is(vault.get(A.class)));
  }

  @Test
  public void test_composable() throws Exception {
    Vault parentOne = Vault.empty();
    parentOne.register(D.class, v -> new D());

    Vault parentTwo = Vault.with(parentOne);
    parentTwo.register(E.class, v -> new E());

    Vault child = Vault.with(parentOne, parentTwo);
    child.register(F.class, v->new F(v.get(D.class), v.get(E.class)));

    assertThat(child.get(F.class).getD(), is(parentOne.get(D.class)));
    assertThat(child.get(F.class).getE(), is(parentTwo.get(E.class)));

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
  /*
    //adapters.db
    DBPort dbPortReal = DBPort.empty();
    dbPort.register(DataSource.class, v -> null);
    dbPort.register(PatientsRepo.class, v -> new PatientsRepo(v.get(DataSource.class)));
    dbPort.register(PatientsRepo.class, v -> new PatientsRepo(v.get(DataSource.class)));
    dbPort.register(PatientsRepo.class, v -> new PatientsRepo(v.get(DataSource.class)));
    dbPort.register(PatientsRepo.class, v -> new PatientsRepo(v.get(DataSource.class)));

    //hexagono
    DBPort dbPortInMemory = InMemoryDBPort.empty();
    dbPort.register(PatientsRepo.class, v -> new InMemoryPatientsRepo());
    dbPort.register(PatientsRepo.class, v -> new InMemoryPatientsRepo());

    Vault webAdapters = Vault.empty()
        .register(Bender.class, v -> Bender.at(5467))
        .register(ActionsPort.class, v -> new ActionsPort(v.get(Bender.class)))
        .register(QueriesPort.class, v -> new QueriesPort(v.get(Bender.class)));

    MyApp myApp = MyApp.with(dbPortInMemory, webAdapters);
    myApp.register(PatientsModule.class, v -> new PatientsModule(v.get(QueriesPort.class), v.get(PatientsRepo.class)));
    myApp.register(AuthModule.class, v -> new AuthModule(v.get(ActionsPort.class), v.get(PatientsRepo.class)));

    myApp.initAndRun();

    // EN LOS TESTS
    Vault mitest = Vault.with(dbPortInMemory, webAdaptersInMemory)
        .register(PatientsModule.class, v -> new PatientsModule(v.get(QueriesPort.class), v.get(PatientsRepo.class)));

    InMemoryQueriesPort fakeeee = mitest.get(QueriesPort.class);

    fakeeee.simulate("/cocotero");
    */
}
