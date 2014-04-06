package com.praetoriandroid.cameraremote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class HttpClient {

    public static enum Method {
        GET,
        POST
    }

    private static final int CONNECTION_TIMEOUT = 3000;

    public InputStream fetch(String urlString) throws IOException, BadHttpResponseException {
        return fetch(urlString, Method.GET, null);
    }

    public InputStream fetch(String urlString, Method method, String postData) throws IOException, BadHttpResponseException {
        if (postData != null && method != Method.POST) {
            throw new IllegalArgumentException();
        }

        URL url = new URL(urlString);
        String protocol = url.getProtocol();
        if (!"http".equals(protocol) && !"https".equals(protocol)) {
            throw new MalformedURLException(urlString);
        }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method.name());
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
//        connection.setReadTimeout(timeout);
        connection.setDoInput(true);

        if (postData != null) {
            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(postData.getBytes());
            outputStream.flush();
            outputStream.close();
        }

        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new BadHttpResponseException(connection.getResponseCode(), connection.getResponseMessage());
        }

        return connection.getInputStream();
    }

    public String fetchText(String url, Method method, String postData) throws IOException, BadHttpResponseException {
        InputStream inputStream = null;
        try {
            inputStream = fetch(url, method, postData);
            return streamToString(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public void fetchFile(String url, String outputPath) throws IOException, BadHttpResponseException {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            File file = new File(outputPath).getAbsoluteFile();
            File dir = file.getParentFile();
            if (!dir.isDirectory() && !dir.mkdirs()) {
                throw new IOException("Could not find/create directory: " + dir.getAbsolutePath());
            }
            outputStream = new FileOutputStream(file);
            inputStream = fetch(url);
            copyFile(inputStream, outputStream);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
    }

    private String streamToString(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int totalLength = 0;
        int length;
        while ((length = inputStream.read(buffer, totalLength, buffer.length - totalLength)) != -1) {
            totalLength += length;
            if (length == buffer.length - totalLength) {
                buffer = Arrays.copyOf(buffer, buffer.length * 2);
            }
        }
        if (totalLength == 0) {
            throw new IOException("Input is empty");
        } else {
            return new String(buffer, 0, totalLength, "UTF-8");
        }
    }

    private void copyFile(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int length;
        while ((length = input.read(buffer)) != -1) {
            output.write(buffer, 0, length);
        }
    }

    public static class BadHttpResponseException extends Exception {

        private int code;

        private String message;

        public BadHttpResponseException(int code, String message) {
            super(code + ": " + message);
            this.code = code;
            this.message = message;
        }

        @Override
        public String toString() {
            return getMessage();
        }
    }
}
