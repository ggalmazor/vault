package net.programania;

import java.util.function.Function;

@FunctionalInterface
public interface VaultSupplier<T> extends Function<Vault,T> {
}
