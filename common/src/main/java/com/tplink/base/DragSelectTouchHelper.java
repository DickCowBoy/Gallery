/*
 * Copyright 2017 Mupceet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tplink.base;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class DragSelectTouchHelper {
    private static final String TAG = "DSTH";
    private static final boolean DEBUG = false;
    private static final float MAX_HOTSPOT_RATIO = 0.5f;

    private InterceptController mInterceptController;

    /**
     * Edge type that specifies an activation area starting at the view bounds
     * and extending inward. Moving outside the view bounds will stop scrolling.
     *
     * @see #setEdgeType
     */
    public static final int EDGE_TYPE_INSIDE = 0;

    /**
     * Edge type that specifies an activation area starting at the view bounds
     * and extending inward. After activation begins, moving outside the view
     * bounds will continue scrolling.
     *
     * @see #setEdgeType
     */
    public static final int EDGE_TYPE_INSIDE_EXTEND = 1;

    // Default values.

    private static final int DEFAULT_EDGE_TYPE = EDGE_TYPE_INSIDE_EXTEND;
    private static final float DEFAULT_HOTSPOT_RATIO = 0.2f;
    private static final int DEFAULT_HOTSPOT_OFFSET = 0;
    private static final int DEFAULT_MAX_SCROLL_VELOCITY = 9;
/**
 *                        !autoChangeMode           +-------------------+
 *           +------------------------------------> |                   |
 *           |                                      |      Disable      |
 *           |            activeSelect(position)    |                   | activeSelect()
 *           |      +------------------------------ |                   | ---+
 *           |      v                               +-------------------+    v
 *  +-------------------+                              autoChangeMode    +-------+
 *  | Drag From Disable | ---------------------------------------------> |       |
 *  +-------------------+                                                |       |
 *  |                   |                                                |       |
 *  |                   |  activeSelect(position) && allowDragInSlide    | Slide |
 *  |                   | <--------------------------------------------- |       |
 *  |  Drag From Slide  |                                                |       |
 *  |                   |                            allowDragInSlide    |       |
 *  |                   | ---------------------------------------------> |       |
 *  +-------------------+                                                +-------+
 */
    private static final int SELECT_MODE_SLIDE = 2;
    private static final int SELECT_MODE_DISABLE = 3;
    private static final int SELECT_MODE_DRAG_FROM_DISABLE = 4;
    private static final int SELECT_MODE_DRAG_FROM_SLIDE = 5;

    // -------------------- Configuration Parameters --------------------------------
    /**
     * Start of the slide area.
     */
    private float mSlideAreaLeft;

    /**
     * End of the slide area.
     */
    private float mSlideAreaRight;

    /**
     * The hotspot height by the ratio of RecyclerView.
     */
    private float mHotspotHeightRatio;

    /**
     * The hotspot height.
     */
    private float mHotspotHeight = -1f;

    /**
     * The hotspot offset.
     */
    private float mHotspotOffset;

    /**
     * Whether should continue scrolling when move outside top hotspot region.
     */
    private boolean mScrollAboveTopRegion;

    /**
     * Whether should continue scrolling when move outside bottom hotspot region.
     */
    private boolean mScrollBelowBottomRegion;

    /**
     * The maximum velocity of auto scrolling.
     */
    private int mMaximumVelocity;

    /**
     * Whether should auto enter slide mode after drag select finished.
     */
    private boolean mShouldAutoChangeMode;

    /**
     * Whether can drag selection in slide select mode.
     */
    private boolean mIsAllowDragInSlideMode;

    // -------------------- Used Parameters ----------------------------

    private RecyclerView mRecyclerView = null;

    /**
     * The coordinate of hotspot area.
     */
    private float mTopRegionFrom = -1f;
    private float mTopRegionTo = -1f;
    private float mBottomRegionFrom = -1f;
    private float mBottomRegionTo = -1f;

    /**
     * The current mode of selection.
     */
    private int mSelectMode = SELECT_MODE_DISABLE;

    /**
     * Whether is in top hotspot area.
     */
    private boolean mIsInTopHotspot = false;

    /**
     * Whether is in bottom hotspot area.
     */
    private boolean mIsInBottomHotspot = false;

    /**
     * Scroller used to automatically scroll at a specific velocity.
     */
    private Scroller mScroller = null;

    /**
     * The actual speed of the current moment.
     */
    private int mScrollDistance = 0;

    /**
     * The reference coordinate for the action start, used to avoid reverse scrolling.
     */
    private float mDownY = Float.MIN_VALUE;

    /**
     * The reference coordinates for the last action.
     */
    private float mLastX = Float.MIN_VALUE;
    private float mLastY = Float.MIN_VALUE;

    /**
     * The selected items position.
     */
    private int mStart = RecyclerView.NO_POSITION;
    private int mEnd = RecyclerView.NO_POSITION;
    private int mLastStart = RecyclerView.NO_POSITION;
    private int mLastEnd = RecyclerView.NO_POSITION;
    private int mSlideDownStart = RecyclerView.NO_POSITION;
    private boolean mHaveCalledSelectStart = false;

    /**
     * Developer callback which controls the behavior of DragSelectTouchHelper.
     */
    private Callback mCallback;
    private final DisplayMetrics mDisplayMetrics;

    private final RecyclerView.OnItemTouchListener mOnItemTouchListener = new RecyclerView.OnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            if (DEBUG) {
                Log.d(TAG, "intercept: x:" + e.getX() + ",y:" + e.getY() + ", " + e);
            }
            if (rv.getAdapter().getItemCount() == 0) {
                return false;
            }
            boolean intercept = false;
            int action = e.getAction();
            int actionMask = action & MotionEvent.ACTION_MASK;
            // TODO: 8/3/17 It seems that it's unnecessary to process multiple pointers?
            switch (actionMask) {
                case MotionEvent.ACTION_DOWN:
                    mDownY = e.getY();
                    // call the selection start's callback before moving
                    if (mSelectMode == SELECT_MODE_SLIDE && isInSlideArea(e)) {
                        mSlideDownStart = getItemPosition(rv, e);
                        if (mSlideDownStart != RecyclerView.NO_POSITION) {
                            mCallback.onSelectionStarted(mSlideDownStart);
                            mHaveCalledSelectStart = true;
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mSlideDownStart != RecyclerView.NO_POSITION) {
                        intercept = selectFirstItem(mSlideDownStart);
                        // selection is triggered
                        mSlideDownStart = RecyclerView.NO_POSITION;
                    } else if (mSelectMode == SELECT_MODE_DRAG_FROM_DISABLE
                            || mSelectMode == SELECT_MODE_DRAG_FROM_SLIDE) {
                        intercept = true;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (mInterceptController != null) {
                        mInterceptController.setIntercept(true);
                    }
                    // finger is lifted before moving
                    if (mSlideDownStart != RecyclerView.NO_POSITION) {
                        selectFinished(mSlideDownStart);
                        mSlideDownStart = RecyclerView.NO_POSITION;
                    }
                    // selection has triggered
                    if (mStart != RecyclerView.NO_POSITION) {
                        selectFinished();
                    }
                    break;
                default:
                    // do nothing
            }

            // Intercept only when the selection is triggered
            return intercept;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            if (DEBUG) {
                Log.d(TAG,
                        "on touch: x:" + e.getX() + ",y:" + e.getY() + ", :" + e);
            }
            if (!isActivated()) {
                return;
            }
            int action = e.getAction();
            int actionMask = action & MotionEvent.ACTION_MASK;
            switch (actionMask) {
                case MotionEvent.ACTION_MOVE:
                    processAutoScroll(e);
                    if (!mIsInTopHotspot && !mIsInBottomHotspot) {
                        updateSelectedRange(rv, e);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    selectFinished();
                    break;
                default:
                    // do nothing
            }
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            if (disallowIntercept) {
                inactiveSelect();
            }
        }
    };

    private final View.OnLayoutChangeListener mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                   int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (oldLeft != left || oldRight != right || oldTop != top || oldBottom != bottom) {
                if (v == mRecyclerView) {
                    if (DEBUG) {
                        Log.d(TAG, "onLayoutChange:new: "
                                + left + " " + top + " " + right + " " + bottom);
                        Log.d(TAG, "onLayoutChange:old: "
                                + oldLeft + " " + oldTop + " " + oldRight + " " + oldBottom);
                    }
                    init((RecyclerView) v);
                }
            }
        }
    };

    public DragSelectTouchHelper(Callback callback) {
        mCallback = callback;
        mDisplayMetrics = Resources.getSystem().getDisplayMetrics();
        setHotspotRatio(DEFAULT_HOTSPOT_RATIO);
        setHotspotOffset(DEFAULT_HOTSPOT_OFFSET);
        setMaximumVelocity(DEFAULT_MAX_SCROLL_VELOCITY);
        setEdgeType(DEFAULT_EDGE_TYPE);
        setAutoEnterSlideMode(false);
        setAllowDragInSlideMode(false);
        setSlideArea(0, 0);
    }

    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) {
        if (mRecyclerView == recyclerView) {
            return; // nothing to do
        }
        if (mRecyclerView != null) {
            mRecyclerView.removeOnItemTouchListener(mOnItemTouchListener);
        }
        mRecyclerView = recyclerView;
        if (mRecyclerView != null) {
            mRecyclerView.addOnItemTouchListener(mOnItemTouchListener);
            mRecyclerView.addOnLayoutChangeListener(mOnLayoutChangeListener);
        }
    }

    /**
     * Activate the slide selection mode
     */
    public void activeSelect() {
        mSelectMode = SELECT_MODE_SLIDE;
    }

    /**
     * Activate the slide selection mode
     *
     * @param isAlwaysAllowDragSelect true allow drag selection in slide mode
     */
    public void activeSelect(boolean isAlwaysAllowDragSelect) {
        mSelectMode = SELECT_MODE_SLIDE;
        mIsAllowDragInSlideMode = isAlwaysAllowDragSelect;
    }

    /**
     * Activate the selection mode with selected item position.
     *
     * @param position position of selected item
     */
    public void activeSelect(int position) {
        if (!mHaveCalledSelectStart) {
            mCallback.onSelectionStarted(position);
            mHaveCalledSelectStart = true;
        }
        if (selectFirstItem(position)) {
            if (mSelectMode == SELECT_MODE_SLIDE) {
                if (mIsAllowDragInSlideMode) {
                    mSelectMode = SELECT_MODE_DRAG_FROM_SLIDE;
                } else {
                    mSelectMode = SELECT_MODE_SLIDE;
                }
            } else {// mSelectMode == SELECT_MODE_DISABLE
                mSelectMode = SELECT_MODE_DRAG_FROM_DISABLE;
            }
        }
    }

    /**
     * Exit the selection mode
     */
    public void inactiveSelect() {
        selectFinished();
        mSelectMode = SELECT_MODE_DISABLE;
    }

    /**
     * To determine whether it is in the selection mode
     *
     * @return true if is in the selection mode
     */
    public boolean isActivated() {
        return (mSelectMode != SELECT_MODE_DISABLE);
    }

    /**
     * Sets hotspot height by ratio of RecyclerView
     *
     * @param ratio range (0, 0.5)
     * @return The select helper, which may used to chain setter calls.
     */
    public DragSelectTouchHelper setHotspotRatio(float ratio) {
        mHotspotHeightRatio = ratio;
        return this;
    }

    /**
     * Sets hotspot height
     *
     * @param hotspotHeight hotspot height which unit is dp
     * @return The select helper, which may used to chain setter calls.
     */
    public DragSelectTouchHelper setHotspotHeight(int hotspotHeight) {
        mHotspotHeight = dp2px(hotspotHeight);
        return this;
    }

    /**
     * Sets hotspot offset. It don't need to be set if no special requirement
     *
     * @param hotspotOffset hotspot offset which unit is dp
     * @return The select helper, which may used to chain setter calls.
     */
    public DragSelectTouchHelper setHotspotOffset(int hotspotOffset) {
        mHotspotOffset = dp2px(hotspotOffset);
        return this;
    }

    /**
     * Sets the activation edge type, one of:
     * <ul>
     * <li>{@link #EDGE_TYPE_INSIDE} for edges that respond to touches inside
     * the bounds of the host view. If touch moves outside the bounds, scrolling
     * will stop.
     * <li>{@link #EDGE_TYPE_INSIDE_EXTEND} for inside edges that continued to
     * scroll when touch moves outside the bounds of the host view.
     * </ul>
     *
     * @param type The type of edge to use.
     * @return The select helper, which may used to chain setter calls.
     */
    public DragSelectTouchHelper setEdgeType(int type) {
        switch (type) {
            case EDGE_TYPE_INSIDE:
                mScrollAboveTopRegion = false;
                mScrollBelowBottomRegion = false;
                break;
            case EDGE_TYPE_INSIDE_EXTEND:
                mScrollAboveTopRegion = true;
                mScrollBelowBottomRegion = true;
                break;
            default:
                mScrollAboveTopRegion = true;
                mScrollBelowBottomRegion = true;
        }
        return this;
    }

    /**
     * Sets sliding area's start and end, has been considered RTL situation
     *
     * @param startDp The start of the sliding area
     * @param endDp   The end of the sliding area
     * @return The select helper, which may used to chain setter calls.
     */
    public DragSelectTouchHelper setSlideArea(int startDp, int endDp) {
        if (!isRtl()) {
            mSlideAreaLeft = dp2px(startDp);
            mSlideAreaRight = dp2px(endDp);
        } else {
            int displayWidth = mDisplayMetrics.widthPixels;
            mSlideAreaLeft = displayWidth - dp2px(endDp);
            mSlideAreaRight = displayWidth - dp2px(startDp);
        }
        return this;
    }

    /**
     * Sets the maximum velocity for scrolling
     *
     * @param velocity maximum velocity
     * @return The select helper, which may used to chain setter calls.
     */
    public DragSelectTouchHelper setMaximumVelocity(int velocity) {
        mMaximumVelocity = (int) (velocity * mDisplayMetrics.density + 0.5f);
        return this;
    }

    /**
     * Sets whether should auto enter slide mode after drag select finished.
     * It's usefully for LinearLayout RecyclerView.
     *
     * @param autoEnterSlideMode should auto enter slide mode
     * @return The select helper, which may used to chain setter calls.
     */
    public DragSelectTouchHelper setAutoEnterSlideMode(boolean autoEnterSlideMode) {
        mShouldAutoChangeMode = autoEnterSlideMode;
        return this;
    }

    /**
     * Sets whether can drag selection in slide select mode.
     * It's usefully for LinearLayout RecyclerView.
     *
     * @param allowDragInSlideMode allow drag selection in slide select mode
     * @return The select helper, which may used to chain setter calls.
     */
    public DragSelectTouchHelper setAllowDragInSlideMode(boolean allowDragInSlideMode) {
        mIsAllowDragInSlideMode = allowDragInSlideMode;
        return this;
    }

    private int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, mDisplayMetrics);
    }

    private boolean isRtl() {
        return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())
                == View.LAYOUT_DIRECTION_RTL;
    }

    /**
     * @return whether the touch finger is in the slide area
     */
    private boolean isInSlideArea(MotionEvent e) {
        float x = e.getX();
        return (x > mSlideAreaLeft && x < mSlideAreaRight);
    }

    /**
     * Process motion event, according to the location to determine whether to scroll
     */
    private void processAutoScroll(MotionEvent e) {
        float y = e.getY();
        if (DEBUG) {
            Log.d(TAG, "processAutoScroll: y = " + y
                    + ", mTopRegionFrom = " + mTopRegionFrom
                    + ", mTopRegionTo = " + mTopRegionTo
                    + ", mBottomRegionFrom = " + mBottomRegionFrom
                    + ", mBottomRegionTo = " + mBottomRegionTo
                    + ", mDownY = " + mDownY);
        }
        if (y >= mTopRegionFrom && y <= mTopRegionTo && y < mDownY) {
            mLastX = e.getX();
            mLastY = e.getY();
            float scrollDistanceFactor = (y - mTopRegionTo) / mHotspotHeight;
            mScrollDistance = (int) (mMaximumVelocity * scrollDistanceFactor);
            if (!mIsInTopHotspot) {
                mIsInTopHotspot = true;
                startAutoScroll();
                mDownY = mTopRegionTo;
            }
        } else if (mScrollAboveTopRegion && y < mTopRegionFrom && mIsInTopHotspot) {
            mLastX = e.getX();
            mLastY = mTopRegionFrom;
            // Use the maximum speed
            mScrollDistance = mMaximumVelocity * -1;
            startAutoScroll();
        } else if (y >= mBottomRegionFrom && y <= mBottomRegionTo && y > mDownY) {
            mLastX = e.getX();
            mLastY = e.getY();
            float scrollDistanceFactor = (y - mBottomRegionFrom) / mHotspotHeight;
            mScrollDistance = (int) (mMaximumVelocity * scrollDistanceFactor);
            if (!mIsInBottomHotspot) {
                mIsInBottomHotspot = true;
                startAutoScroll();
                mDownY = mBottomRegionFrom;
            }
        } else if (mScrollBelowBottomRegion && y > mBottomRegionTo && mIsInBottomHotspot) {
            mLastX = e.getX();
            mLastY = mBottomRegionTo;
            // Use the maximum speed
            mScrollDistance = mMaximumVelocity;
            startAutoScroll();
        } else {
            mIsInTopHotspot = false;
            mIsInBottomHotspot = false;
            mLastX = Float.MIN_VALUE;
            mLastY = Float.MIN_VALUE;
            stopAutoScroll();
        }

    }

    private void startAutoScroll() {
        if (mScroller == null) {
            mScroller = new Scroller(mRecyclerView.getContext(), new LinearInterpolator());
        }

        if (mScroller.isFinished()) {
            mRecyclerView.removeCallbacks(scrollRunnable);
            mScroller.startScroll(0, mScroller.getCurrY(), 0, 500, 100000);
            ViewCompat.postOnAnimation(mRecyclerView, scrollRunnable);
        }
    }

    private void stopAutoScroll() {
        if (mScroller != null && !mScroller.isFinished()) {
            mRecyclerView.removeCallbacks(scrollRunnable);
            mScroller.abortAnimation();
        }
    }

    private Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (mScroller != null && mScroller.computeScrollOffset()) {
                scrollBy(mScrollDistance);
                ViewCompat.postOnAnimation(mRecyclerView, scrollRunnable);
            }
        }
    };

    private void scrollBy(int distance) {
        int scrollDistance;
        if (distance > 0) {
            scrollDistance = Math.min(distance, mMaximumVelocity);
        } else {
            scrollDistance = Math.max(distance, -mMaximumVelocity);
        }
        mRecyclerView.scrollBy(0, scrollDistance);
        if (mLastX != Float.MIN_VALUE && mLastY != Float.MIN_VALUE) {
            updateSelectedRange(mRecyclerView, mLastX, mLastY);
        }
    }

    private void updateSelectedRange(RecyclerView rv, MotionEvent e) {
        updateSelectedRange(rv, e.getX(), e.getY());
    }

    private void updateSelectedRange(RecyclerView rv, float x, float y) {
        int position = getItemPosition(rv, x, y);
        if (position != RecyclerView.NO_POSITION && mEnd != position) {
            mEnd = position;
            notifySelectRangeChange();
        }
    }

    private void notifySelectRangeChange() {
        if (mStart == RecyclerView.NO_POSITION || mEnd == RecyclerView.NO_POSITION) {
            return;
        }

        int newStart, newEnd;
        newStart = Math.min(mStart, mEnd);
        newEnd = Math.max(mStart, mEnd);
        if (mLastStart == RecyclerView.NO_POSITION || mLastEnd == RecyclerView.NO_POSITION) {
            if (newEnd - newStart == 1) {
                selectChange(newStart, newStart, true);
            } else {
                selectChange(newStart, newEnd, true);
            }
        } else {
            if (newStart > mLastStart) {
                selectChange(mLastStart, newStart - 1, false);
            } else if (newStart < mLastStart) {
                selectChange(newStart, mLastStart - 1, true);
            }

            if (newEnd > mLastEnd) {
                selectChange(mLastEnd + 1, newEnd, true);
            } else if (newEnd < mLastEnd) {
                selectChange(newEnd + 1, mLastEnd, false);
            }
        }

        mLastStart = newStart;
        mLastEnd = newEnd;
    }

    private void selectChange(int start, int end, boolean newState) {
        for (int i = start; i <= end; i++) {
            mCallback.onSelectChange(i, newState);
        }
    }

    private void init(RecyclerView rv) {
        int rvHeight = rv.getHeight();

        if (mHotspotOffset >= rvHeight * MAX_HOTSPOT_RATIO) {
            mHotspotOffset = rvHeight * MAX_HOTSPOT_RATIO;
        }
        // The height of hotspot area is not set, using (RV height x ratio)
        if (mHotspotHeight < 0) {
            if (mHotspotHeightRatio <= 0 || mHotspotHeightRatio >= MAX_HOTSPOT_RATIO) {
                mHotspotHeightRatio = DEFAULT_HOTSPOT_RATIO;
            }
            mHotspotHeight = rvHeight * mHotspotHeightRatio;
        } else {
            if (mHotspotHeight >= rvHeight * MAX_HOTSPOT_RATIO) {
                mHotspotHeight = rvHeight * MAX_HOTSPOT_RATIO;
            }
        }

        mTopRegionFrom = mHotspotOffset;
        mTopRegionTo = mTopRegionFrom + mHotspotHeight;
        mBottomRegionTo = rvHeight - mHotspotOffset;
        mBottomRegionFrom = mBottomRegionTo - mHotspotHeight;

        if (mTopRegionTo > mBottomRegionFrom) {
            mTopRegionTo = mBottomRegionFrom = rvHeight / 2;
        }

        if (DEBUG) {
            Log.d(TAG, "Hotspot: [" + mTopRegionFrom + ", " + mTopRegionTo + "], ["
                    + mBottomRegionFrom + ", " + mBottomRegionTo + "]");
        }
    }

    private void selectFinished() {
        selectFinished(mEnd);
    }

    private void selectFinished(int lastItem) {
        mCallback.onSelectionFinished(lastItem);
        mStart = RecyclerView.NO_POSITION;
        mEnd = RecyclerView.NO_POSITION;
        mLastStart = RecyclerView.NO_POSITION;
        mLastEnd = RecyclerView.NO_POSITION;
        mHaveCalledSelectStart = false;
        mIsInTopHotspot = false;
        mIsInBottomHotspot = false;
        stopAutoScroll();
        switch (mSelectMode) {
            case SELECT_MODE_DRAG_FROM_DISABLE:
                if (mShouldAutoChangeMode) {
                    mSelectMode = SELECT_MODE_SLIDE;
                } else {
                    mSelectMode = SELECT_MODE_DISABLE;
                }
                break;
            case SELECT_MODE_DRAG_FROM_SLIDE:
                mSelectMode = SELECT_MODE_SLIDE;
                break;
            default:
                // doesn't change the selection mode
                break;
        }
    }

    private int getItemPosition(RecyclerView rv, MotionEvent e) {
        return getItemPosition(rv, e.getX(), e.getY());
    }

    private int getItemPosition(RecyclerView rv, float x, float y) {
        final View v = rv.findChildViewUnder(x, y);
        if (v == null) {
            return RecyclerView.NO_POSITION;
        }
        return rv.getChildAdapterPosition(v);
    }


    private boolean selectFirstItem(int position) {
        boolean selectFirstItemSucceed = mCallback.onSelectChange(position, true);
        // The drag select feature is only available if the first item is available for selection
        if (selectFirstItemSucceed) {
            mStart = position;
            mEnd = position;
            mLastStart = position;
            mLastEnd = position;
        }
        return selectFirstItemSucceed;
    }

    /**
     * This class is the contract between DragSelectTouchHelper and your application. It lets you
     * update adapter when selection start/end and state changed.
     */
    public abstract static class Callback {
        /**
         * Called when changing item state
         *
         * @param position this item want to change the state to new state
         * @param newState new state
         * @return Whether to change the state successfully
         */
        public abstract boolean onSelectChange(int position, boolean newState);

        /**
         * Called when selection start.
         *
         * @param start the first selected item
         */
        public void onSelectionStarted(int start) {

        }

        /**
         * Called when selection end.
         *
         * @param end the last selected item
         */
        public void onSelectionFinished(int end) {

        }
    }

    /**
     * An advance Callback which provide 4 useful selection modes {@link Mode}.
     * <p>
     * Note: Since the state of item may be repeatedly set, in order to improve efficiency,
     * please process it in the Adapter
     */
    public abstract static class AdvanceCallback extends Callback {

        private Mode mMode;
        private Set<Integer> mOriginalSelection;
        private boolean mFirstWasSelected;

        /**
         * Creates a AdvanceCallback with select mode.
         *
         * @param mode the initial select mode
         * @see Mode
         */
        public AdvanceCallback(Mode mode) {
            mMode = mode;
        }

        /**
         * Sets the select mode, one of:
         * <ul>
         * <li>{@link Mode#Simple}
         * <li>{@link Mode#ToggleAndUndo}
         * <li>{@link Mode#FirstItemDependent}
         * <li>{@link Mode#FirstItemDependentToggleAndUndo}
         * </ul>
         *
         * @param mode The type of select mode.
         * @see Mode
         */
        public void setMode(Mode mode) {
            mMode = mode;
        }

        @Override
        public void onSelectionStarted(int start) {
            mOriginalSelection = new HashSet<>();
            Set<Integer> selected = getSelection();
            if (selected != null) {
                mOriginalSelection.addAll(selected);
            }
            mFirstWasSelected = mOriginalSelection.contains(start);
        }

        @Override
        public void onSelectionFinished(int end) {
            mOriginalSelection = null;
        }

        @Override
        public boolean onSelectChange(int position, boolean newState) {
            boolean stateChanged;
            switch (mMode) {
                case Simple: {
                    stateChanged = updateSelection(position, newState);
                    break;
                }
                case ToggleAndUndo: {
                    stateChanged = updateSelection(position,
                            newState ? !mOriginalSelection.contains(position) : mOriginalSelection.contains(position));
                    break;
                }
                case FirstItemDependent: {
                    stateChanged = updateSelection(position,
                            newState ? !mFirstWasSelected : mFirstWasSelected);
                    break;
                }
                case FirstItemDependentToggleAndUndo: {
                    stateChanged = updateSelection(position,
                            newState ? !mFirstWasSelected : mOriginalSelection.contains(position));
                    break;
                }
                default:
                    // Simple Mode
                    stateChanged = updateSelection(position, newState);
            }
            return stateChanged;
        }

        /**
         * Different existing selection modes
         */
        public enum Mode {
            /**
             * simply selects each item you go by and un-selects on move back
             */
            Simple,
            /**
             * toggles each items original state, reverts to the original state on move back
             */
            ToggleAndUndo,
            /**
             * toggles the first item and applies the same state to each item you go by
             * and applies inverted state on move back
             */
            FirstItemDependent,
            /**
             * toggles the item and applies the same state to each item you go by
             * and reverts to the original state on move back
             */
            FirstItemDependentToggleAndUndo
        }

        /**
         * get the currently selected items when selecting first item, can be ignored for
         * {@link Mode#Simple}
         *
         * @return the currently selected items
         */
        public abstract Set<Integer> getSelection();

        /**
         * update your adapter and select/un-select the passed position
         *
         * @param position the position who's selection state changed
         * @param newState true, if the range should be selected, false otherwise
         * @return Whether to change the state successfully
         */
        public abstract boolean updateSelection(int position, boolean newState);

    }

    public void interceptParent() {
        if (mInterceptController != null) {
            mInterceptController.setIntercept(false);
        }
    }

    public interface InterceptController{
        void setIntercept(boolean intercept);
    }

    public void setInterceptController(InterceptController interceptController) {
        this.mInterceptController = interceptController;
    }
}