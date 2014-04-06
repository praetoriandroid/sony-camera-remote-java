package com.praetoriandroid.cameraremote;

public interface Logger {

    public void debug(Object data);

    public void debug(String format, Object... args);

    public void info(Object data);

    public void error(Object object);
}
