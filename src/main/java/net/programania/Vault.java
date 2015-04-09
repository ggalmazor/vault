package net.programania;

import java.util.*;
import java.util.function.Consumer;

public class Vault implements LifeCycle {
  private final List<Vault> parents;
  private final Map<Class, VaultSupplier<?>> suppliers = new HashMap<>();
  private final Map<VaultSupplier, Object> instances = new HashMap<>();

  public Vault() {
    this.parents = new ArrayList<>();
  }

  public Vault(Vault... parents) {
    this.parents = Arrays.asList(parents);
  }

  public Vault(List<Vault> parents) {
    this.parents = parents;
  }

  public <T> Vault register(Class<T> clazz, VaultSupplier<T> supplier) {
    suppliers.put(clazz, memoize(supplier));
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> clazz) {
    return getSupplierOf(clazz).map(s -> s.apply(this))
        .orElseThrow(() -> new VaultException("There is no supplier for " + clazz));
  }

  @SuppressWarnings("unchecked")
  private <T> VaultSupplier<T> memoize(VaultSupplier<T> supplier) {
    return vault -> (T) instances.computeIfAbsent(supplier, s -> s.apply(this));
  }

  @SuppressWarnings("unchecked")
  private <T> Optional<VaultSupplier<T>> getSupplierOf(Class<T> clazz) {
    Optional<VaultSupplier<T>> ownSupplier = Optional.ofNullable((VaultSupplier<T>) suppliers.get(clazz));
    return ownSupplier.isPresent() ? ownSupplier : getParentSupplierOf(clazz);
  }

  private <T> Optional<VaultSupplier<T>> getParentSupplierOf(Class<T> clazz) {
    return parents.stream()
        .map(vault -> vault.getSupplierOf(clazz))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  @Override
  public void start() {
    changeLifeCycle(LifeCycle::start);
  }

  @Override
  public void stop() {
    changeLifeCycle(LifeCycle::stop);
  }

  private void changeLifeCycle(final Consumer<LifeCycle> lifeCycleChanger) {
    parents.stream().forEach(lifeCycleChanger);
    instances.values().stream()
        .filter(o -> o instanceof LifeCycle)
        .map(o -> (LifeCycle) o)
        .forEach(lifeCycleChanger);
  }
}
