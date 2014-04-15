package com.praetoriandroid.cameraremote;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

@SuppressWarnings("UnusedDeclaration")
public class LiveViewFetcher {

    public static class Frame {

        private byte[] buffer;
        private int size;

        void update(byte[] buffer, int size) {
            this.buffer = buffer;
            this.size = size;
        }

        public byte[] getBuffer() {
            return buffer;
        }

        public int getSize() {
            return size;
        }
    }

    private static class DataBlock {

        protected ByteBuffer buffer;
        protected int dataAmount;

        public DataBlock(int size) {
            buffer = ByteBuffer.allocate(size);
        }

        /**
         * Read exactly as much bytes as <code>'amount'</code> parameter.
         * @param inputStream the stream for reading data.
         * @throws IOException if any I/O error occurred while reading data or in case of premature
         * end of stream.
         * @param amount number of bytes to read.
         */
        public void read(InputStream inputStream, int amount) throws IOException {
            int offset = 0;
            int left = amount;
            byte[] array = buffer.array();
            while (left > 0) {
                int length = inputStream.read(array, offset, left);
                if (length == -1) {
                    throw new EOFException();
                }
                left -= length;
                offset += length;
            }
            dataAmount = amount;
        }

        public void write(OutputStream outputStream) throws IOException {
            outputStream.write(buffer.array(), 0, dataAmount);
        }

        public void ensureBufferSize(int size) {
            if (buffer.array().length < size) {
                buffer = ByteBuffer.allocate(size);
            }
        }

    }

    private static class CommonHeader extends DataBlock {

        private static final int HEADER_SIZE = 8;

        private static final byte PAYLOAD_TYPE_LIVE_VIEW = 0x01;

        private short seqNumber;
        private long timestamp;

        public CommonHeader() {
            super(HEADER_SIZE);
        }

        public void read(InputStream inputStream) throws IOException, ParseException {
            super.read(inputStream, HEADER_SIZE);

            byte startByte = buffer.get(0);
            if (startByte != (byte) 0xff) {
                throw new ParseException("Illegal start byte: 0x" + Integer.toString(startByte, 16));
            }

            byte payloadType = buffer.get(1);
            if (payloadType != PAYLOAD_TYPE_LIVE_VIEW) {
                throw new ParseException("Illegal payload type: 0x" + Integer.toString(payloadType, 16));
            }

            seqNumber = buffer.getShort(2);
            timestamp = buffer.getInt(4);
        }

        public short getSeqNumber() {
            return seqNumber;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    private static class PayloadHeader extends DataBlock {

        private static final int HEADER_SIZE = 128;

        private static final int START_CODE = 0x24356879;

        private int dataSize;
        private int paddingSize;

        public PayloadHeader() {
            super(HEADER_SIZE);
        }

        public void read(InputStream inputStream) throws IOException, ParseException {
            super.read(inputStream, HEADER_SIZE);

            int startCode = buffer.getInt(0);
            if (startCode != START_CODE) {
                throw new ParseException("Illegal start code: 0x" + Integer.toString(startCode, 16));
            }

            byte[] data = buffer.array();
            dataSize = (((data[4] & 0xff) << 16) |
                    ((data[5] & 0xff) << 8) |
                    ((data[6] & 0xff)));

            paddingSize = buffer.get(7);

            byte flag = buffer.get(12);
            if (flag != 0x00) {
                throw new ParseException("Illegal flag: 0x" + Integer.toString(flag, 16));
            }
        }

        public int getDataSize() {
            return dataSize;
        }

        public int getPaddingSize() {
            return paddingSize;
        }
    }

    private HttpClient httpClient = new HttpClient();
    private InputStream inputStream;
    private CommonHeader commonHeader = new CommonHeader();
    private PayloadHeader payloadHeader = new PayloadHeader();
    private DataBlock payloadData = new DataBlock(8192);
    private DataBlock padding = new DataBlock(0);
    private Frame reusableFrame = new Frame();
    private boolean closed;

    public void setConnectionTimeout(int timeout) {
        httpClient.setConnectionTimeout(timeout);
    }

    public void connect(String url) throws IOException, HttpClient.BadHttpResponseException {
        closed = false;
        inputStream = httpClient.get(url);
    }

    public void disconnect() throws IOException {
        closed = true;
        if (inputStream != null) {
            inputStream.close();
        }
    }

    public Frame getNextFrame() throws IOException, ParseException, DisconnectedException {
        try {
            commonHeader.read(inputStream);
            payloadHeader.read(inputStream);

            payloadData.ensureBufferSize(payloadHeader.getDataSize());
            payloadData.read(inputStream, payloadHeader.getDataSize());

            padding.ensureBufferSize(payloadHeader.getPaddingSize());
            padding.read(inputStream, payloadHeader.getPaddingSize());

            reusableFrame.update(payloadData.buffer.array(), payloadData.dataAmount);
            return reusableFrame;
        } catch (IOException e) {
            if (closed) {
                throw new DisconnectedException();
            } else {
                throw e;
            }
        }
    }

    public void writeNextFrame(OutputStream outputStream) throws IOException {
        payloadData.write(outputStream);
    }

    public static class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }
    }

    public static class DisconnectedException extends Exception {
    }

}
