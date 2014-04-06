package com.praetoriandroid.cameraremote.tool;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Cache {

    private Map<String, String> data = new HashMap<String, String>();

    private Gson gson = new Gson();

    private String pathName;

    private String fileName;

    public Cache(String pathName, String fileName) throws IOException {
        this.fileName = fileName;
        this.pathName = pathName;
        restore();
    }

    public synchronized String get(String key) {
        synchronized (Cache.class) {
            return data.get(key);
        }
    }

    public synchronized void put(String key, String value) throws IOException {
        synchronized (Cache.class) {
            data.put(key, value);
            save();
        }
    }

    private void restore() throws IOException {
        File path = new File(pathName);
        if (!path.exists()) {
            return;
        }
        if (!path.isDirectory()) {
            throw new IOException(pathName + " is not a directory.");
        }
        File file = new File(path, fileName);
        if (!file.exists()) {
            return;
        }

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int length = inputStream.read(buffer);
            String configText = new String(buffer, 0, length, "UTF-8");
            data = gson.fromJson(configText, new TypeToken<Map<String, String>>() {}.getType());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private void save() throws IOException {
        File path = new File(pathName);
        if (!path.exists()) {
            if (!path.mkdirs()) {
                throw new IOException("Could not create directory " + pathName);
            }
        }
        if (!path.isDirectory()) {
            throw new IOException(pathName + " is not a directory.");
        }
        File file = new File(path, fileName);

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(gson.toJson(data).getBytes());
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

}
