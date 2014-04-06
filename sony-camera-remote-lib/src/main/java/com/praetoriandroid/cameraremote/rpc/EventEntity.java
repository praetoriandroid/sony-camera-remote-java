package com.praetoriandroid.cameraremote.rpc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnusedDeclaration")
public abstract class EventEntity {

    enum Type {
        availableApiList(AvailableApiList.class),
        cameraStatus(CameraStatus.class),
        zoomInformation(ZoomInformation.class),
        liveviewStatus(LiveViewStatus.class),
        // items 4-18 are skipped
        postviewImageSize(PostviewImageSize.class),
        selfTimer(SelfTimer.class),
        shootMode(ShootMode.class);

        private final Class<? extends EventEntity> entityClass;

        private Type(Class<? extends EventEntity> entityClass) {
            this.entityClass = entityClass;
        }

        public Class<? extends EventEntity> getEntityClass() {
            return entityClass;
        }
    }

    private Type type;

    public Type getType() {
        return type;
    }

    public static class AvailableApiList extends EventEntity {

        private String[] names;

        public String[] getApiList() {
            return names;
        }

        @Override
        public String toString() {
            return getType() + ": " + Arrays.toString(names);
        }
    }

    public static class CameraStatus extends EventEntity {

        public enum Status {
            Error,
            NotReady,
            IDLE,
            StillCapturing,
            StillSaving,
            MovieWaitRecStart,
            MovieRecording,
            MovieWaitRecStop,
            MovieSaving,
            AudioWaitRecStart,
            AudioRecording,
            AudioWaitRecStop,
            AudioSaving
        }

        private Status cameraStatus;

        public Status getStatus() {
            return cameraStatus;
        }

        @Override
        public String toString() {
            return getType() + ": " + cameraStatus;
        }
    }

    public static class ZoomInformation extends EventEntity {

        public static int ZOOM_INVALID = -1;

        private int zoomPosition;

        private int zoomNumberBox;

        private int zoomIndexCurrentBox;

        private int zoomPositionCurrentBox;

        public int getPosition() {
            return zoomPosition;
        }

        public int getNumberBox() {
            return zoomNumberBox;
        }

        public int getIndexCurrentBox() {
            return zoomIndexCurrentBox;
        }

        public int getPositionCurrentBox() {
            return zoomPositionCurrentBox;
        }

        @Override
        public String toString() {
            return getType() + ": position=" + zoomPosition
                    + ", numberBox=" + zoomNumberBox
                    + ", indexCurrentBox=" + zoomIndexCurrentBox
                    + ", positionCurrentBox=" + zoomPositionCurrentBox;
        }

    }

    public static class LiveViewStatus extends EventEntity {

        private boolean liveviewStatus;

        public boolean isReady() {
            return liveviewStatus;
        }

        @Override
        public String toString() {
            return getType() + ": " + liveviewStatus;
        }

    }

    public static class PostviewImageSize extends EventEntity {

        public enum Size {
            ORIGINAL,
            TWO_M,
            UNKNOWN
        }

        private static final String SIZE_ORIGINAL = "Original";
        private static final String SIZE_2M = "2M";

        private static final Map<String, Size> sizeMap = new HashMap<String, Size>();
        static {
            sizeMap.put(SIZE_ORIGINAL, Size.ORIGINAL);
            sizeMap.put(SIZE_2M, Size.TWO_M);
        }

        private String currentPostviewImageSize;

        private String[] postviewImageSizeCandidates;

        private Size getSize(String value) {
            Size size = sizeMap.get(value);
            if (size == null) {
                return Size.UNKNOWN;
            }
            return size;
        }

        public Size getCurrentSize() {
            return getSize(currentPostviewImageSize);
        }

        public Size[] getSizeCandidates() {
            Size[] candidates = new Size[postviewImageSizeCandidates.length];
            for (int i = 0; i < candidates.length; i++) {
                candidates[i] = getSize(postviewImageSizeCandidates[i]);
            }
            return candidates;
        }

        @Override
        public String toString() {
            return getType() + ": " + currentPostviewImageSize + ' ' + Arrays.toString(postviewImageSizeCandidates);
        }

    }

    public static class SelfTimer extends EventEntity {

        private int currentSelfTimer;

        private Integer[] selfTimerCandidates;

        public Integer getCurrentValue() {
            return currentSelfTimer;
        }

        public Integer[] getCandidates() {
            return selfTimerCandidates;
        }

        @Override
        public String toString() {
            return getType() + ": " + currentSelfTimer + ' ' + Arrays.toString(selfTimerCandidates);
        }
    }

    public static class ShootMode extends EventEntity {

        enum Mode {
            still,
            movie,
            audio
        }

        private Mode currentShootMode;

        private Mode[] shootModeCandidates;

        public Mode getCurrentValue() {
            return currentShootMode;
        }

        public Mode[] getCandidates() {
            return shootModeCandidates;
        }

        @Override
        public String toString() {
            return getType() + ": " + currentShootMode + ' ' + Arrays.toString(shootModeCandidates);
        }

    }

}
