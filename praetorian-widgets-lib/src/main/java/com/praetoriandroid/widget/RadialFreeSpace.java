package com.praetoriandroid.widget;

import java.util.HashMap;
import java.util.Map;

/**
 * There are 16 possible cases of free space in the parent:
 * <pre>
 *      |    ---    |
 *  O   |O    O    O|
 * ---  |           |
 *
 * |    +--  --+    |
 * |O   |O    O|   O|
 * +--  |      |  --+
 *
 * | |  +--  +-+  --+
 * |O|  |O   |O|   O|
 * +-+  +--  | |  --+
 *
 * ---  | |  +-+
 *  O   |O|  |O|   O
 * ---  | |  +-+
 * </pre>
 * Assuming the free space is at least as much as selector size multiplied by 1.5, proposed
 * that selector and selection items are approximately the same size.
 */
@SuppressWarnings("UnusedDeclaration")
public enum RadialFreeSpace {
    LEFT_TOP_RIGHT {
        @Override
        public float mainAxis() {
            return (float) (Math.PI * 1.5);
        }

        @Override
        public float sectorSize() {
            return (float) Math.PI;
        }
    },
    TOP_RIGHT_BOTTOM {
        @Override
        public float mainAxis() {
            return 0;
        }

        @Override
        public float sectorSize() {
            return (float) Math.PI;
        }
    },
    RIGHT_BOTTOM_LEFT {
        @Override
        public float mainAxis() {
            return (float) (Math.PI * .5);
        }

        @Override
        public float sectorSize() {
            return (float) Math.PI;
        }
    },
    BOTTOM_LEFT_TOP {
        @Override
        public float mainAxis() {
            return (float) Math.PI;
        }

        @Override
        public float sectorSize() {
            return (float) Math.PI;
        }
    },

    TOP_RIGHT {
        @Override
        public float mainAxis() {
            return (float) (Math.PI * 1.75);
        }

        @Override
        public float sectorSize() {
            return (float) (Math.PI / 2);
        }
    },
    RIGHT_BOTTOM {
        @Override
        public float mainAxis() {
            return (float) (Math.PI / 4);
        }

        @Override
        public float sectorSize() {
            return (float) (Math.PI / 2);
        }
    },
    BOTTOM_LEFT {
        @Override
        public float mainAxis() {
            return (float) (Math.PI * .75);
        }

        @Override
        public float sectorSize() {
            return (float) (Math.PI / 2);
        }
    },
    LEFT_TOP {
        @Override
        public float mainAxis() {
            return (float) (Math.PI * 1.25);
        }

        @Override
        public float sectorSize() {
            return (float) (Math.PI / 2);
        }
    },

    TOP {
        @Override
        public float mainAxis() {
            return (float) (Math.PI * 1.5);
        }

        @Override
        public float sectorSize() {
            return 0;
        }
    },
    RIGHT {
        @Override
        public float mainAxis() {
            return 0;
        }

        @Override
        public float sectorSize() {
            return 0;
        }
    },
    BOTTOM {
        @Override
        public float mainAxis() {
            return (float) (Math.PI * .5);
        }

        @Override
        public float sectorSize() {
            return 0;
        }
    },
    LEFT {
        @Override
        public float mainAxis() {
            return (float) Math.PI;
        }

        @Override
        public float sectorSize() {
            return 0;
        }
    },

    LEFT_RIGHT {
        @Override
        public float mainAxis() {
            throw new NotImplementedException(); // TODO here and below: need something more acceptable...
        }

        @Override
        public float sectorSize() {
            throw new NotImplementedException();
        }
    },
    TOP_BOTTOM {
        @Override
        public float mainAxis() {
            throw new NotImplementedException();
        }

        @Override
        public float sectorSize() {
            throw new NotImplementedException();
        }
    },
    ZERO {
        @Override
        public float mainAxis() {
            throw new NotImplementedException();
        }

        @Override
        public float sectorSize() {
            throw new NotImplementedException();
        }
    },
    ALL {
        @Override
        public float mainAxis() {
            return (float) (Math.PI * 1.5);
        }

        @Override
        public float sectorSize() {
            return (float) (Math.PI * 2);
        }
    };

    public static final int FREE_TOP = 0x01;
    public static final int FREE_RIGHT = 0x02;
    public static final int FREE_BOTTOM = 0x04;
    public static final int FREE_LEFT = 0x08;
    public static final float MAX_RAYS_ANGLE = (float) (Math.PI / 3.);

    private static Map<Integer, RadialFreeSpace> flagMap = new HashMap<Integer, RadialFreeSpace>(16);

    static {
        flagMap.put(FREE_LEFT | FREE_TOP | FREE_RIGHT, LEFT_TOP_RIGHT);
        flagMap.put(FREE_TOP | FREE_RIGHT | FREE_BOTTOM, TOP_RIGHT_BOTTOM);
        flagMap.put(FREE_RIGHT | FREE_BOTTOM | FREE_LEFT, RIGHT_BOTTOM_LEFT);
        flagMap.put(FREE_BOTTOM | FREE_LEFT | FREE_TOP, BOTTOM_LEFT_TOP);
        flagMap.put(FREE_TOP, TOP);
        flagMap.put(FREE_RIGHT, RIGHT);
        flagMap.put(FREE_BOTTOM, BOTTOM);
        flagMap.put(FREE_LEFT, LEFT);
        flagMap.put(FREE_TOP | FREE_RIGHT, TOP_RIGHT);
        flagMap.put(FREE_RIGHT | FREE_BOTTOM, RIGHT_BOTTOM);
        flagMap.put(FREE_BOTTOM | FREE_LEFT, BOTTOM_LEFT);
        flagMap.put(FREE_LEFT | FREE_TOP, LEFT_TOP);
        flagMap.put(FREE_LEFT | FREE_RIGHT, LEFT_RIGHT);
        flagMap.put(FREE_TOP | FREE_BOTTOM, TOP_BOTTOM);
        flagMap.put(0, ZERO);
        flagMap.put(FREE_TOP | FREE_RIGHT | FREE_BOTTOM | FREE_LEFT, ALL);
    }

    public abstract float mainAxis();

    public abstract float sectorSize();

    public static RadialFreeSpace fromFlags(int flags) {
        RadialFreeSpace result = flagMap.get(flags);
        if (result == null) {
            throw new IllegalArgumentException();
        }
        return result;
    }

    public static RadialFreeSpace fromCoordinates(int parentWidth, int parentHeight, int cx, int cy, int size) {
        int flags = 0;
        if (cy > size * 2) {
            flags |= FREE_TOP;
        }
        if (cy + size * 2 < parentHeight) {
            flags |= FREE_BOTTOM;
        }
        if (cx > size * 2) {
            flags |= FREE_LEFT;
        }
        if (cx + size * 2 < parentWidth) {
            flags |= FREE_RIGHT;
        }
        return fromFlags(flags);
    }

    public static class NotImplementedException extends RuntimeException {
    }
}
