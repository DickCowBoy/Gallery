/*
 * Copyright (C) 2017, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * com.tplink.filemanager.animation
 *
 * Description.
 *
 * Author tanminghui
 *
 * Ver 1.0, 09/27/2017, tanminghui, Create file
 */

package com.tplink.base;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.tplink.common.R;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseItemAnimator extends SimpleItemAnimator {
    private static final boolean DEBUG = false;
    private static final String TAG = "BaseItemAnimator";

    private ArrayList<ViewHolder> mPendingRemovals = new ArrayList<>();
    private ArrayList<ViewHolder> mPendingAdditions = new ArrayList<>();
    private ArrayList<MoveInfo> mPendingMoves = new ArrayList<>();
    private ArrayList<ChangeInfo> mPendingChanges = new ArrayList<>();

    private ArrayList<ArrayList<ViewHolder>> mAdditionsList = new ArrayList<>();
    private ArrayList<ArrayList<MoveInfo>> mMovesList = new ArrayList<>();
    private ArrayList<ArrayList<ChangeInfo>> mChangesList = new ArrayList<>();

    protected ArrayList<ViewHolder> mAddAnimations = new ArrayList<>();
    private ArrayList<ViewHolder> mMoveAnimations = new ArrayList<>();
    protected ArrayList<ViewHolder> mRemoveAnimations = new ArrayList<>();
    private ArrayList<ViewHolder> mChangeAnimations = new ArrayList<>();

    protected Interpolator mInterpolator = PathInterpolatorCompat.create(0.25f, 0.1f, 0.25f, 1.0f);

    private boolean mIsAnimationActive;

    public abstract int getCheckBoxId();
    public abstract int getContainerId();

    private static class MoveInfo {

        public ViewHolder holder;
        public int fromX;
        public int fromY;
        public int toX;
        public int toY;

        private MoveInfo(ViewHolder holder, int fromX, int fromY, int toX, int toY) {
            this.holder = holder;
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }
    }

    private static class ChangeInfo {
        public ViewHolder oldHolder;
        public ViewHolder newHolder;
        public int fromX;
        public int fromY;
        public int toX;
        public int toY;

        private ChangeInfo(ViewHolder oldHolder, ViewHolder newHolder) {
            this.oldHolder = oldHolder;
            this.newHolder = newHolder;
        }

        private ChangeInfo(ViewHolder oldHolder, ViewHolder newHolder, int fromX, int fromY, int
                toX,
                           int toY) {
            this(oldHolder, newHolder);
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }

        @Override
        public String toString() {
            return "ChangeInfo{" +
                    "oldHolder=" + oldHolder +
                    ", newHolder=" + newHolder +
                    ", fromX=" + fromX +
                    ", fromY=" + fromY +
                    ", toX=" + toX +
                    ", toY=" + toY +
                    '}';
        }
    }

    public BaseItemAnimator() {
        super();
        setSupportsChangeAnimations(false);
    }

    public void setInterpolator(Interpolator mInterpolator) {
        this.mInterpolator = mInterpolator;
    }

    public void setAnimationActive(boolean isAnimationActive) {
        this.mIsAnimationActive = isAnimationActive;
    }

    public boolean isIsAnimationActive() {
        return mIsAnimationActive;
    }

    @Override
    public void runPendingAnimations() {
        boolean removalsPending = !mPendingRemovals.isEmpty();
        boolean movesPending = !mPendingMoves.isEmpty();
        boolean changesPending = !mPendingChanges.isEmpty();
        boolean additionsPending = !mPendingAdditions.isEmpty();
        if (!removalsPending && !movesPending && !additionsPending && !changesPending) {
            // nothing to animate
            return;
        }
        // First, remove stuff
        for (ViewHolder holder : mPendingRemovals) {
            doAnimateRemove(holder);
        }
        mPendingRemovals.clear();
        // Next, move stuff
        if (movesPending) {
            final ArrayList<MoveInfo> moves = new ArrayList<MoveInfo>();
            moves.addAll(mPendingMoves);
            mMovesList.add(moves);
            mPendingMoves.clear();
            Runnable mover = new Runnable() {
                @Override
                public void run() {
                    boolean removed = mMovesList.remove(moves);
                    if (!removed) {
                        // already canceled
                        return;
                    }
                    for (MoveInfo moveInfo : moves) {
                        animateMoveImpl(moveInfo.holder, moveInfo.fromX, moveInfo.fromY, moveInfo
                                        .toX,
                                moveInfo.toY);
                    }
                    moves.clear();
                }
            };
            if (removalsPending) {
                View view = moves.get(0).holder.itemView;
                ViewCompat.postOnAnimationDelayed(view, mover, ViewHelper.MOVE_DELAY);
            } else {
                mover.run();
            }
        }
        // Next, change stuff, to run in parallel with move animations
        if (changesPending) {
            final ArrayList<ChangeInfo> changes = new ArrayList<ChangeInfo>();
            changes.addAll(mPendingChanges);
            mChangesList.add(changes);
            mPendingChanges.clear();
            Runnable changer = new Runnable() {
                @Override
                public void run() {
                    boolean removed = mChangesList.remove(changes);
                    if (!removed) {
                        // already canceled
                        return;
                    }
                    for (ChangeInfo change : changes) {
                        animateChangeImpl(change);
                    }
                    changes.clear();
                }
            };
            if (removalsPending) {
                ViewHolder holder = changes.get(0).oldHolder;
                ViewCompat.postOnAnimationDelayed(holder.itemView, changer, ViewHelper.MOVE_DELAY);
            } else {
                changer.run();
            }
        }
        // Next, add stuff
        if (additionsPending) {
            final ArrayList<ViewHolder> additions = new ArrayList<ViewHolder>();
            additions.addAll(mPendingAdditions);
            mAdditionsList.add(additions);
            mPendingAdditions.clear();
            Runnable adder = new Runnable() {
                public void run() {
                    boolean removed = mAdditionsList.remove(additions);
                    if (!removed) {
                        // already canceled
                        return;
                    }
                    for (ViewHolder holder : additions) {
                        doAnimateAdd(holder);
                    }
                    additions.clear();
                }
            };
            if (removalsPending || movesPending || changesPending) {
                long removeDuration = removalsPending ? getRemoveDuration() : 0;
                long moveDuration = movesPending ? getMoveDuration() : 0;
                long changeDuration = changesPending ? getChangeDuration() : 0;
                long totalDelay = removeDuration + Math.max(moveDuration, changeDuration);
                View view = additions.get(0).itemView;
                ViewCompat.postOnAnimationDelayed(view, adder, totalDelay);
            } else {
                adder.run();
            }
        }
    }

    protected void preAnimateRemoveImpl(final ViewHolder holder) {
    }

    protected void preAnimateAddImpl(final ViewHolder holder) {
    }

    protected abstract void animateRemoveImpl(final ViewHolder holdwer);

    protected void animateAddImpl(final ViewHolder holder) {
        dispatchAddFinished(holder);
    }

    private void preAnimateRemove(final ViewHolder holder) {
        ViewHelper.clear(holder.itemView);
        preAnimateRemoveImpl(holder);
    }

    private void preAnimateAdd(final ViewHolder holder) {
        ViewHelper.clear(holder.itemView);
        preAnimateAddImpl(holder);
    }

    private void doAnimateRemove(final ViewHolder holder) {
        animateRemoveImpl(holder);
        mRemoveAnimations.add(holder);
    }

    private void doAnimateAdd(final ViewHolder holder) {
        animateAddImpl(holder);
        mAddAnimations.add(holder);
    }

    @Override
    public boolean animateRemove(final ViewHolder holder) {
        endAnimation(holder);
        preAnimateRemove(holder);
        mPendingRemovals.add(holder);
        return true;
    }

    protected long getRemoveDelay(final ViewHolder holder) {
        return Math.abs(holder.getOldPosition() * getRemoveDuration() / 4);
    }

    @Override
    public boolean animateAdd(final ViewHolder holder) {
        endAnimation(holder);
        preAnimateAdd(holder);
        mPendingAdditions.add(holder);
        return true;
    }

    protected long getAddDelay(final ViewHolder holder) {
        return Math.abs(holder.getAdapterPosition() * getAddDuration() / 4);
    }

    @Override
    public boolean animateMove(final ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        final View view = holder.itemView;
        fromX += ViewCompat.getTranslationX(holder.itemView);
        fromY += ViewCompat.getTranslationY(holder.itemView);
        endAnimation(holder);
        int deltaX = toX - fromX;
        int deltaY = toY - fromY;
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder);
            return false;
        }
        if (deltaX != 0) {
            ViewCompat.setTranslationX(view, -deltaX);
        }
        if (deltaY != 0) {
            ViewCompat.setTranslationY(view, -deltaY);
        }
        /**+++++++++++++++++++++++++++++++++++++++++++**/
        //TODO 设置checkbox可见 为了出现动画
        View cb = holder.itemView.findViewById(getCheckBoxId());
        if (mIsAnimationActive && cb != null) {
            cb.setVisibility(View.VISIBLE);
        }
        /**+++++++++++++++++++++++++++++++++++++++++++**/
        mPendingMoves.add(new MoveInfo(holder, fromX, fromY, toX, toY));
        return true;
    }

    private void animateMoveImpl(final ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        final View view = holder.itemView;
        final int deltaX = toX - fromX;
        final int deltaY = toY - fromY;
        if (deltaX != 0) {
            ViewCompat.animate(view).translationX(0);
        }
        if (deltaY != 0) {
            ViewCompat.animate(view).translationY(0);
        }
        mMoveAnimations.add(holder);

        /**+++++++++++++++++++++++++++++++++++++++++++**/
        //TODO V2.0 在move中也要加入相同处理
        final View cb = holder.itemView.findViewById(getCheckBoxId());
        final View container = holder.itemView.findViewById(getContainerId());
        final Animation cbAnimation = AnimationUtils.loadAnimation(cb.getContext(),
                R.anim.checkbox_out);
        final Animation itemAnimation = AnimationUtils.loadAnimation(cb.getContext(),
                R.anim.item_out);
        cbAnimation.setDuration(ViewHelper.MOVE_DURATION);
        cbAnimation.setInterpolator(mInterpolator);
        itemAnimation.setDuration(ViewHelper.MOVE_DURATION);
        itemAnimation.setInterpolator(mInterpolator);
        /**+++++++++++++++++++++++++++++++++++++++++++**/
        final ViewPropertyAnimatorCompat animation = ViewCompat.animate(view);
        animation.setDuration(ViewHelper.MOVE_DURATION)
                .setInterpolator(mInterpolator).setListener(new VpaListenerAdapter() {
            @Override
            public void onAnimationStart(View view) {
                dispatchMoveStarting(holder);
                /**+++++++++++++++++++++++++++++++++++++++++++**/
                //TODO 同时开始动画
                if (cb != null && container != null && mIsAnimationActive) {
                    cb.setVisibility(View.GONE);
                    container.startAnimation(itemAnimation);
                    cb.startAnimation(cbAnimation);
                }
                /**+++++++++++++++++++++++++++++++++++++++++++**/
            }

            @Override
            public void onAnimationCancel(View view) {
                if (deltaX != 0) {
                    ViewCompat.setTranslationX(view, 0);
                }
                if (deltaY != 0) {
                    ViewCompat.setTranslationY(view, 0);
                }
            }

            @Override
            public void onAnimationEnd(View view) {
                animation.setListener(null);
                dispatchMoveFinished(holder);
                mMoveAnimations.remove(holder);
                dispatchFinishedWhenDone();
            }
        }).start();
    }

    @Override
    public boolean animateChange(ViewHolder oldHolder, ViewHolder newHolder, int fromX, int fromY,
                                 int toX, int toY) {
        if (oldHolder == newHolder) {
            if (!mIsAnimationActive) {
                // Don't know how to run change animations when the same view holder is re-used.
                // run a move animation to handle position changes.
                return animateMove(oldHolder, fromX, fromY, toX, toY);
            }
        }

        final float prevTranslationX = ViewCompat.getTranslationX(oldHolder.itemView);
        final float prevTranslationY = ViewCompat.getTranslationY(oldHolder.itemView);
        endAnimation(oldHolder);
        int deltaX = (int) (toX - fromX - prevTranslationX);
        int deltaY = (int) (toY - fromY - prevTranslationY);
        // recover prev translation state after ending animation
        ViewCompat.setTranslationX(oldHolder.itemView, prevTranslationX);
        ViewCompat.setTranslationY(oldHolder.itemView, prevTranslationY);
        if (newHolder != null && newHolder.itemView != null && mIsAnimationActive) {
            // carry over translation values
            endAnimation(newHolder);
            ViewCompat.setTranslationX(newHolder.itemView, -deltaX);
            ViewCompat.setTranslationY(newHolder.itemView, -deltaY);
            //新的移除视线
            /**+++++++++++++++++++++++++++++++++++++++++++**/
            //TODO 设置checkbox可见 为了出现动画
            newHolder.itemView.findViewById(getCheckBoxId()).setVisibility(View.VISIBLE);
            /**+++++++++++++++++++++++++++++++++++++++++++**/
        }
        mPendingChanges.add(new ChangeInfo(oldHolder, newHolder, fromX, fromY, toX, toY));
        return true;
    }

    private void animateChangeImpl(final ChangeInfo changeInfo) {
        final ViewHolder holder = changeInfo.oldHolder;
        final View view = holder == null ? null : holder.itemView;
        final ViewHolder newHolder = changeInfo.newHolder;
        final View newView = newHolder != null ? newHolder.itemView : null;
        if (view != null) {
            mChangeAnimations.add(changeInfo.oldHolder);
            final ViewPropertyAnimatorCompat oldViewAnim =
                    ViewCompat.animate(view).setDuration(getChangeDuration());
            oldViewAnim.translationX(changeInfo.toX - changeInfo.fromX);
            oldViewAnim.translationY(changeInfo.toY - changeInfo.fromY);
            oldViewAnim.setListener(new VpaListenerAdapter() {

                @Override
                public void onAnimationStart(View view) {
                    dispatchChangeStarting(changeInfo.oldHolder, true);
                }

                @Override
                public void onAnimationEnd(View view) {
                    oldViewAnim.setListener(null);
                    ViewCompat.setAlpha(view, 1);
                    ViewCompat.setTranslationX(view, 0);
                    ViewCompat.setTranslationY(view, 0);
                    dispatchChangeFinished(changeInfo.oldHolder, true);
                    mChangeAnimations.remove(changeInfo.oldHolder);
                    dispatchFinishedWhenDone();
                }
            }).start();
        }
        if (newView != null) {
            mChangeAnimations.add(changeInfo.newHolder);
            //TODO V2.0 添加了插值器
            final View cb = newHolder.itemView.findViewById(getCheckBoxId());
            final View container = newHolder.itemView.findViewById(getContainerId());
            final Animation cbAnimation = AnimationUtils.loadAnimation(newView.getContext(),
                    R.anim.checkbox_out);
            final Animation itemAnimation = AnimationUtils.loadAnimation(newView.getContext(),
                    R.anim.item_out);
            cbAnimation.setDuration(ViewHelper.MOVE_DURATION);
            cbAnimation.setInterpolator(mInterpolator);
            itemAnimation.setDuration(ViewHelper.MOVE_DURATION);
            itemAnimation.setInterpolator(mInterpolator);
            /**+++++++++++++++++++++++++++++++++++++++++++**/

            final ViewPropertyAnimatorCompat newViewAnimation = ViewCompat.animate(newView);
            newViewAnimation.translationX(0).translationY(0)
                    .setInterpolator(mInterpolator).setDuration(ViewHelper.MOVE_DURATION).
                    setListener(new VpaListenerAdapter() {
                        @Override
                        public void onAnimationStart(View view) {
                            dispatchChangeStarting(changeInfo.newHolder, false);
                            if (cb != null && container != null && mIsAnimationActive) {
                                /**+++++++++++++++++++++++++++++++++++++++++++**/
                                //TODO 同时开始动画
                                cb.setVisibility(View.GONE);
                                container.startAnimation(itemAnimation);
                                cb.startAnimation(cbAnimation);
                                /**+++++++++++++++++++++++++++++++++++++++++++**/
                            }
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            newViewAnimation.setListener(null);
                            ViewCompat.setAlpha(newView, 1);
                            ViewCompat.setTranslationX(newView, 0);
                            ViewCompat.setTranslationY(newView, 0);
                            dispatchChangeFinished(changeInfo.newHolder, false);
                            mChangeAnimations.remove(changeInfo.newHolder);
                            dispatchFinishedWhenDone();
                        }
                    }).start();
        }
    }

    private void endChangeAnimation(List<ChangeInfo> infoList, ViewHolder item) {
        for (int i = infoList.size() - 1; i >= 0; i--) {
            ChangeInfo changeInfo = infoList.get(i);
            if (endChangeAnimationIfNecessary(changeInfo, item)) {
                if (changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                    infoList.remove(changeInfo);
                }
            }
        }
    }

    private void endChangeAnimationIfNecessary(ChangeInfo changeInfo) {
        if (changeInfo.oldHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder);
        }
        if (changeInfo.newHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder);
        }
    }

    private boolean endChangeAnimationIfNecessary(ChangeInfo changeInfo, ViewHolder item) {
        boolean oldItem = false;
        if (changeInfo.newHolder == item) {
            changeInfo.newHolder = null;
        } else if (changeInfo.oldHolder == item) {
            changeInfo.oldHolder = null;
            oldItem = true;
        } else {
            return false;
        }
        ViewCompat.setAlpha(item.itemView, 1);
        ViewCompat.setTranslationX(item.itemView, 0);
        ViewCompat.setTranslationY(item.itemView, 0);
        dispatchChangeFinished(item, oldItem);
        return true;
    }

    @Override
    public void endAnimation(ViewHolder item) {
        final View view = item.itemView;
        // this will trigger end callback which should set properties to their target values.
        ViewCompat.animate(view).cancel();
        for (int i = mPendingMoves.size() - 1; i >= 0; i--) {
            MoveInfo moveInfo = mPendingMoves.get(i);
            if (moveInfo.holder == item) {
                ViewCompat.setTranslationY(view, 0);
                ViewCompat.setTranslationX(view, 0);
                dispatchMoveFinished(item);
                mPendingMoves.remove(i);
            }
        }
        endChangeAnimation(mPendingChanges, item);
        if (mPendingRemovals.remove(item)) {
            ViewHelper.clear(item.itemView);
            dispatchRemoveFinished(item);
        }
        if (mPendingAdditions.remove(item)) {
            ViewHelper.clear(item.itemView);
            dispatchAddFinished(item);
        }

        for (int i = mChangesList.size() - 1; i >= 0; i--) {
            ArrayList<ChangeInfo> changes = mChangesList.get(i);
            endChangeAnimation(changes, item);
            if (changes.isEmpty()) {
                mChangesList.remove(i);
            }
        }
        for (int i = mMovesList.size() - 1; i >= 0; i--) {
            ArrayList<MoveInfo> moves = mMovesList.get(i);
            for (int j = moves.size() - 1; j >= 0; j--) {
                MoveInfo moveInfo = moves.get(j);
                if (moveInfo.holder == item) {
                    ViewCompat.setTranslationY(view, 0);
                    ViewCompat.setTranslationX(view, 0);
                    dispatchMoveFinished(item);
                    moves.remove(j);
                    if (moves.isEmpty()) {
                        mMovesList.remove(i);
                    }
                    break;
                }
            }
        }
        for (int i = mAdditionsList.size() - 1; i >= 0; i--) {
            ArrayList<ViewHolder> additions = mAdditionsList.get(i);
            if (additions.remove(item)) {
                ViewHelper.clear(item.itemView);
                dispatchAddFinished(item);
                if (additions.isEmpty()) {
                    mAdditionsList.remove(i);
                }
            }
        }

        // animations should be ended by the cancel above.
        if (mRemoveAnimations.remove(item) && DEBUG) {
            throw new IllegalStateException(
                    "after animation is cancelled, item should not be in " + "mRemoveAnimations " +
                            "list");
        }

        if (mAddAnimations.remove(item) && DEBUG) {
            throw new IllegalStateException(
                    "after animation is cancelled, item should not be in " + "mAddAnimations list");
        }

        if (mChangeAnimations.remove(item) && DEBUG) {
            throw new IllegalStateException(
                    "after animation is cancelled, item should not be in " + "mChangeAnimations " +
                            "list");
        }

        if (mMoveAnimations.remove(item) && DEBUG) {
            throw new IllegalStateException(
                    "after animation is cancelled, item should not be in " + "mMoveAnimations " +
                            "list");
        }
        dispatchFinishedWhenDone();
    }

    @Override
    public boolean isRunning() {
        return (!mPendingAdditions.isEmpty() ||
                !mPendingChanges.isEmpty() ||
                !mPendingMoves.isEmpty() ||
                !mPendingRemovals.isEmpty() ||
                !mMoveAnimations.isEmpty() ||
                !mRemoveAnimations.isEmpty() ||
                !mAddAnimations.isEmpty() ||
                !mChangeAnimations.isEmpty() ||
                !mMovesList.isEmpty() ||
                !mAdditionsList.isEmpty() ||
                !mChangesList.isEmpty());
    }

    /**
     * Check the state of currently pending and running animations. If there are none
     * pending/running, call #dispatchAnimationsFinished() to notify any
     * listeners.
     */
    private void dispatchFinishedWhenDone() {
        if (!isRunning()) {
            dispatchAnimationsFinished();
        }
    }

    @Override
    public void endAnimations() {
        int count = mPendingMoves.size();
        for (int i = count - 1; i >= 0; i--) {
            MoveInfo item = mPendingMoves.get(i);
            View view = item.holder.itemView;
            ViewCompat.setTranslationY(view, 0);
            ViewCompat.setTranslationX(view, 0);
            dispatchMoveFinished(item.holder);
            mPendingMoves.remove(i);
        }
        count = mPendingRemovals.size();
        for (int i = count - 1; i >= 0; i--) {
            ViewHolder item = mPendingRemovals.get(i);
            dispatchRemoveFinished(item);
            mPendingRemovals.remove(i);
        }
        count = mPendingAdditions.size();
        for (int i = count - 1; i >= 0; i--) {
            ViewHolder item = mPendingAdditions.get(i);
            ViewHelper.clear(item.itemView);
            dispatchAddFinished(item);
            mPendingAdditions.remove(i);
        }
        count = mPendingChanges.size();
        for (int i = count - 1; i >= 0; i--) {
            endChangeAnimationIfNecessary(mPendingChanges.get(i));
        }
        mPendingChanges.clear();
        if (!isRunning()) {
            return;
        }

        int listCount = mMovesList.size();
        for (int i = listCount - 1; i >= 0; i--) {
            ArrayList<MoveInfo> moves = mMovesList.get(i);
            count = moves.size();
            for (int j = count - 1; j >= 0; j--) {
                MoveInfo moveInfo = moves.get(j);
                ViewHolder item = moveInfo.holder;
                View view = item.itemView;
                ViewCompat.setTranslationY(view, 0);
                ViewCompat.setTranslationX(view, 0);
                dispatchMoveFinished(moveInfo.holder);
                moves.remove(j);
                if (moves.isEmpty()) {
                    mMovesList.remove(moves);
                }
            }
        }
        listCount = mAdditionsList.size();
        for (int i = listCount - 1; i >= 0; i--) {
            ArrayList<ViewHolder> additions = mAdditionsList.get(i);
            count = additions.size();
            for (int j = count - 1; j >= 0; j--) {
                ViewHolder item = additions.get(j);
                View view = item.itemView;
                ViewCompat.setAlpha(view, 1);
                dispatchAddFinished(item);
                //this check prevent exception when removal already happened during finishing
                // animation
                if (j < additions.size()) {
                    additions.remove(j);
                }
                if (additions.isEmpty()) {
                    mAdditionsList.remove(additions);
                }
            }
        }
        listCount = mChangesList.size();
        for (int i = listCount - 1; i >= 0; i--) {
            ArrayList<ChangeInfo> changes = mChangesList.get(i);
            count = changes.size();
            for (int j = count - 1; j >= 0; j--) {
                endChangeAnimationIfNecessary(changes.get(j));
                if (changes.isEmpty()) {
                    mChangesList.remove(changes);
                }
            }
        }

        cancelAll(mRemoveAnimations);
        cancelAll(mMoveAnimations);
        cancelAll(mAddAnimations);
        cancelAll(mChangeAnimations);

        dispatchAnimationsFinished();
    }

    void cancelAll(List<ViewHolder> viewHolders) {
        for (int i = viewHolders.size() - 1; i >= 0; i--) {
            ViewCompat.animate(viewHolders.get(i).itemView).cancel();
        }
    }

    private static class VpaListenerAdapter implements ViewPropertyAnimatorListener {

        @Override
        public void onAnimationStart(View view) {
        }

        @Override
        public void onAnimationEnd(View view) {
        }

        @Override
        public void onAnimationCancel(View view) {
        }
    }

    protected class DefaultAddVpaListener extends VpaListenerAdapter {

        ViewHolder mViewHolder;

        public DefaultAddVpaListener(final ViewHolder holder) {
            mViewHolder = holder;
        }

        @Override
        public void onAnimationStart(View view) {
            dispatchAddStarting(mViewHolder);
        }

        @Override
        public void onAnimationCancel(View view) {
            ViewHelper.clear(view);
        }

        @Override
        public void onAnimationEnd(View view) {
            ViewHelper.clear(view);
            dispatchAddFinished(mViewHolder);
            mAddAnimations.remove(mViewHolder);
            dispatchFinishedWhenDone();
        }
    }

    protected class DefaultRemoveVpaListener extends VpaListenerAdapter {

        ViewHolder mViewHolder;

        public DefaultRemoveVpaListener(final ViewHolder holder) {
            mViewHolder = holder;
        }

        @Override
        public void onAnimationStart(View view) {
            dispatchRemoveStarting(mViewHolder);
        }

        @Override
        public void onAnimationCancel(View view) {
            ViewHelper.clear(view);
        }

        @Override
        public void onAnimationEnd(View view) {
            ViewHelper.clear(view);
            dispatchRemoveFinished(mViewHolder);
            mRemoveAnimations.remove(mViewHolder);
            dispatchFinishedWhenDone();
        }
    }
}
