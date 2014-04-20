package com.praetoriandroid.widget;

import android.view.View;
import android.view.ViewGroup;

public interface ItemViewCreator<ItemView extends View, Value> {
    public ItemView createItemView(ViewGroup parent, Value value);
}
