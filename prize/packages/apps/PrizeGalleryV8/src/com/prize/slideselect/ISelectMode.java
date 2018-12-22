package com.prize.slideselect;

/**
 * Created by Cony on 2017/6/5.
 */

public interface ISelectMode {
    boolean isSelectMode();
    boolean isSelectItem(int index);
    void slideControlSelect(boolean isAdd, int fromSlotIndex, int toSlotIndex);
    void slideControlEnd();
    void slideControlStart();
}
