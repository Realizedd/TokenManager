package me.realized.tokenmanager.util;

public interface Loadable {

    void handleLoad() throws Exception;

    void handleUnload() throws Exception;
}
