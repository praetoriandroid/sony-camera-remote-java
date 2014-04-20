package com.praetoriandroid.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Blah-blah-blah...
 *
 * @param <Value> item data value type.
 */
public abstract class RadialSelector<ItemView extends View, Value>
        extends ImageButton
        implements ItemViewCreator<ItemView, Value> {
    public interface OnValueSelectedListener<Value> {
        public void onValueSelected(Value value);
    }

    private static final int DEFAULT_COVER_COLOR = 0x80000000;
    private static final float DEFAULT_DISTANCE_MULTIPLIER = 1.5f;
    private static final float DEFAULT_ARC_SPACE_MULTIPLIER = 1.1f;

    private OnValueSelectedListener<Value> onValueSelectedListener;
    private List<Value> itemValues = Collections.emptyList();

    public RadialSelector(Context context) {
        super(context);
        init();
    }

    public RadialSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RadialSelector(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showItems(itemValues);
            }
        });
    }

    public void setItemValues(List<Value> itemValues) {
        this.itemValues = itemValues;
    }

    public void setOnValueSelectedListener(OnValueSelectedListener<Value> listener) {
        onValueSelectedListener = listener;
    }

    private void showItems(List<Value> values) {
        final ViewGroup parent = (ViewGroup) getParent();
        if (parent == null) {
            throw new NullPointerException();
        }

        int size = Math.max(getWidth(), getHeight());
        int selectorX = getLeft() + getWidth() / 2;
        int selectorY = getTop() + getHeight() / 2;

        final FrameLayout cover = new FrameLayout(getContext());
        cover.setBackgroundColor(DEFAULT_COVER_COLOR);
        cover.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.removeView(cover);
            }
        });
        parent.addView(cover, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        int itemsNumber = values.size();
        List<ItemView> itemViews = new ArrayList<ItemView>(itemsNumber);
        for (final Value value : values) {
            ItemView itemView = createItemView(parent, value);
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    parent.removeView(cover);
                    if (onValueSelectedListener != null) {
                        onValueSelectedListener.onValueSelected(value);
                    }
                }
            });
            cover.addView(itemView);
            itemViews.add(itemView);
        }

        Log.d("@@@@@", "size: " + size);
        int parentWidth = parent.getWidth();
        int parentHeight = parent.getHeight();
        RadialFreeSpace freeSpace = RadialFreeSpace
                .fromCoordinates(parentWidth, parentHeight, selectorX, selectorY, size);
        Log.d("@@@@@", "freeSpace: " + freeSpace);
        float distance = size * DEFAULT_DISTANCE_MULTIPLIER;
        int line = 1;
        for (int i = 0; i < itemsNumber; distance += size * DEFAULT_DISTANCE_MULTIPLIER, line++) {
            float arcLength = freeSpace.sectorSize() * distance; //(float) (distance * freeSpace.sectorSize() / (2 * Math.PI));
            Log.d("@@@@@", "distance: " + distance + ", sector size: " + freeSpace.sectorSize() + ", arc length: " + arcLength);
            int maxRays = (int) (arcLength / size / DEFAULT_ARC_SPACE_MULTIPLIER);
            Log.d("@@@@@", line + ": max rays: " + maxRays + ", itemsNumber: " + itemsNumber);

            int rowItemsNumber = (itemsNumber - i) >= maxRays ? maxRays : itemsNumber % maxRays;
            Log.d("@@@@@", "rowItemsNumber: " + rowItemsNumber);
            float step = rowItemsNumber == 1 ? 0 : freeSpace.sectorSize() / (rowItemsNumber - 1);
            Log.d("@@@@@", "step: " + step + ", MAX_RAYS_ANGLE: " + RadialFreeSpace.MAX_RAYS_ANGLE);
            if (step > RadialFreeSpace.MAX_RAYS_ANGLE) {
                step = RadialFreeSpace.MAX_RAYS_ANGLE;
            }
            Log.d("@@@@@", "step after correction: " + step);
            float angle = freeSpace.mainAxis() - step * (rowItemsNumber - 1) / 2;
            Log.d("@@@@@", "angle: " + angle + ", main axis: " + freeSpace.mainAxis());
            for (int ray = 0; ray < maxRays && i < itemsNumber; ray++, i++, angle += step) {
                Log.d("@@@@@", "i: " + i + ", angle: " + angle);
                int x = (int) Math.round(selectorX + distance * Math.cos(angle));
                int y = (int) Math.round(selectorY + distance * Math.sin(angle));
                setViewPosition(itemViews.get(i), x, y);
            }
        }
    }

    private void setViewPosition(View view, int cX, int cY) {
        view.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int w = view.getMeasuredWidth();
        int h = view.getMeasuredHeight();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(cX - w / 2, cY - h / 2, 0, 0);
        view.setLayoutParams(lp);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        throw new RuntimeException("Do not use setOnClickListener() with this class! See class JavaDoc for more details.");
    }
}
