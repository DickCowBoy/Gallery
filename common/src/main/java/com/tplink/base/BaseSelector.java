/*
 * Copyright (C) 2017, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * BaseSelector
 *
 *　通用选择器，避免List实现全选等效率地下问题
 *
 * Author linjl
 *
 * Ver 1.0, 10/26/2017, linjl, Create file
 */
package com.tplink.base;

import com.tplink.utils.NoneBoundArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BaseSelector<I, T> {
    public static final int ENTER_SELECTION_MODE = 1;
    public static final int LEAVE_SELECTION_MODE = 2;
    public static final int SELECT_ALL_MODE = 3;
    public static final int SELECT_NONE_MODE = 4;

    protected boolean mInverseSelection;
    protected boolean mAutoLeave = true;
    protected SelectionListener<I, T> mListener;
    protected boolean mInSelectionMode;
    protected int mTotal;

    protected Set<I> mClickedSet;
    // 保存已选择item的position
    private HashSet<Integer> mSelected;
    // 用于临时保存反选状态下的position,避免多次创建
    private HashSet<Integer> mIndex;
    // 获取item的位置
    private PositionProvider mPositonProvider = new PositionProvider() {
        @Override
        public int getPositionByItemIndex(int index) {
            return index;
        }
    };

    public void setDataSource(DataSource<I> mDataSource) {
        this.mDataSource = mDataSource;
    }

    protected DataSource<I> mDataSource = null;

    // Save and restore selection in thread pool to avoid ANR
    private NoneBoundArrayList<I> mSelectionPath = null;

    public interface SelectionListener<I, T> {
        public void onSelectionModeChange(int mode);

        public void onSelectionChange(I path, boolean selected, T tag);

        void onSelectionRestoreDone();
    }

    public interface PositionProvider {
        int getPositionByItemIndex(int index);
    }

    public BaseSelector() {
        mClickedSet = new HashSet<I>();
        mSelected = new HashSet<>();
        mIndex = new HashSet<>();
        mTotal = -1;
    }

    // Whether we will leave selection mode automatically once the number of
    // selected items is down to zero.
    public void setAutoLeaveSelectionMode(boolean enable) {
        mAutoLeave = enable;
    }

    public void setSelectionListener(SelectionListener<I, T> listener) {
        mListener = listener;
    }

    // 清除不必要数据
    protected void clearSet() {
    }

    public void selectAll() {
        mInverseSelection = true;
        mClickedSet.clear();
        mSelected.clear();
        clearSet();
        mTotal = -1;
        enterSelectionMode();
        if (mListener != null) mListener.onSelectionModeChange(SELECT_ALL_MODE);
    }

    public boolean inSelectionMode() {
        return mInSelectionMode;
    }

    public void enterSelectionMode() {
        if (mInSelectionMode) return;

        mInSelectionMode = true;
        if (mListener != null) mListener.onSelectionModeChange(ENTER_SELECTION_MODE);
    }

    public void deSelectAll() {
        mInverseSelection = false;
        mClickedSet.clear();
        mSelected.clear();
        clearSet();
        if (mAutoLeave) {
            leaveSelectionMode();
        } else {
            if (mListener != null) mListener.onSelectionModeChange(SELECT_NONE_MODE);
        }
    }

    public void leaveSelectionMode() {
        if (!mInSelectionMode) return;

        mInSelectionMode = false;
        mInverseSelection = false;
        mClickedSet.clear();
        mSelected.clear();
        clearSet();
        /// M: [BUG.ADD] @{
        // Clear mTotal so that it will be re-calculated
        // next time user enters selection mode
        mTotal = -1;
        /// @}
        if (mListener != null) mListener.onSelectionModeChange(LEAVE_SELECTION_MODE);
    }

    public boolean inSelectAllMode() {
        /// M: [BUG.ADD] @{
        // Not in select all mode, if not all items are selected now
        if (getTotalCount() != 0) {
            return getTotalCount() == getSelectedCount();
        }
        /// @}
        return mInverseSelection;
    }

    protected int getTotalCount() {
        if (mDataSource == null) return -1;

        if (mTotal < 0) {
            mTotal = mDataSource.getItemCount();
        }
        return mTotal;
    }

    public interface DataSource<I> {
        int getItemCount();

        List<I> getItem(int start, int count);
    }

    public int getSelectedCount() {
        int count = mClickedSet.size();
        if (mInverseSelection) {
            count = getTotalCount() - count;
        }
        return count;
    }

    public HashSet<Integer> getSelectedPosition() {
        if (mInverseSelection && mIndex != null) {
            mIndex.clear();
            for (int i = 0; i < getTotalCount(); i++) {
                mIndex.add(mPositonProvider.getPositionByItemIndex(i));
            }
            mIndex.removeAll(mSelected);
            return mIndex;
        }
        return mSelected;
    }

    public boolean isItemSelected(I itemId) {
        return mInverseSelection ^ mClickedSet.contains(itemId);
    }

    public boolean isItemSelected(Set<I> setPath, I itemId) {
        return mInverseSelection ^ setPath.contains(itemId);
    }

    //TODO LJL 将PATH改成 MediaItem提高查找效率
    public void toggle(I path, T tag) {
        toggle(mClickedSet, path, tag);
    }

    protected void toggle(Set<I> setPath, I path, T tag) {
        if (setPath.contains(path)) {
            removePath(path, tag);
        } else {
            enterSelectionMode();
            addPath(path, tag);
        }

        // Convert to inverse selection mode if everything is selected.
        int count = getSelectedCount();
        if (count == getTotalCount()) {
            selectAll();
        }

        if (count == 0 && mAutoLeave) {
            leaveSelectionMode();
            //退出选择模式  不用设置标题个数
            return;
        }
        if (mListener != null)
            mListener.onSelectionChange(path, isItemSelected(setPath, path), tag);
    }

    protected void addPath(I path, T tag) {
        mClickedSet.add(path);
        mSelected.add((Integer) tag);
    }

    protected void removePath(I path, T tag) {
        mClickedSet.remove(path);
        mSelected.remove(tag);
    }

    public void setClickedSet(Set<I> mClickedSet) {
        this.mClickedSet = mClickedSet;
    }

    public NoneBoundArrayList<I> getSelected() {
        return getSelected(Integer.MAX_VALUE);
    }

    public NoneBoundArrayList<I> getSelected(final int maxSelection) {
        final NoneBoundArrayList<I> selected = new NoneBoundArrayList<I>();
        if (mInverseSelection) {
            int total = getTotalCount();
            int index = 0;
            while (index < total) {
                int count = total;
                List<I> list = mDataSource.getItem(index, count);
                for (I item : list) {
                    if (!mClickedSet.contains(item)) {
                        selected.add(item);
                        if (selected.size() > maxSelection) {
                            return null;
                        }
                    }
                }
                index += count;
            }
        } else {
            // Check if items in click set are still in mSourceMediaSet,
            // if not, we do not add it to selected list.
            selected.addAll(mClickedSet);
        }
        return selected;
    }

    public void onSourceContentChanged() {
        // reset and reload total count since source set data has changed
        mTotal = -1;
        int count = getTotalCount();
        if (count == 0) {
            leaveSelectionMode();
        }
    }

    private void exitInverseSelectionAfterSave() {
        if (mInverseSelection && mSelectionPath != null) {
            mClickedSet.clear();
            mSelected.clear();
            clearSet();
            int restoreSize = mSelectionPath.size();
            for (int i = 0; i < restoreSize; i++) {
                mClickedSet.add(mSelectionPath.get(i));
            }
            mInverseSelection = false;
        }
    }

    public void setPositonProvider(PositionProvider provider) {
        this.mPositonProvider = provider;
    }
}
