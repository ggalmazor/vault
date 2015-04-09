package net.programania;

import java.util.function.Function;

@FunctionalInterface
interface VaultSupplier<T> extends Function<Vault,T> {
}
