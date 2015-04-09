package net.programania;

import java.util.*;

public class Vault {
  private final List<Vault> parents;
  private final Map<Class, VaultSupplier<?>> suppliers = new HashMap<>();
  private final Map<VaultSupplier, Object> instances = new HashMap<>();

  public Vault(List<Vault> parents) {
    this.parents = parents;
  }

  public static Vault empty() {
    return new Vault(new ArrayList<>());
  }

  public static Vault with(Vault... parents) {
    return new Vault(Arrays.asList(parents));
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
  public <T> VaultSupplier<T> memoize(VaultSupplier<T> supplier) {
    return vault -> (T) instances.computeIfAbsent(supplier, s -> s.apply(this));
  }

  @SuppressWarnings("unchecked")
  public <T> Optional<VaultSupplier<T>> getSupplierOf(Class<T> clazz) {
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
}
