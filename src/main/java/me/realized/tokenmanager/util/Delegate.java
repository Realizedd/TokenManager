package me.realized.tokenmanager.util;

public class Delegate<T> {

    private final T delegate;

    public Delegate(final T delegate) {
        this.delegate = delegate;
    }

    public T get() {
        return delegate;
    }
}
