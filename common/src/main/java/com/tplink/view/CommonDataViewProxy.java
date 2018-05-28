/*
 * Copyright (C) 2017, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * CommonDataViewProxy.java
 *
 * Description
 *
 * Author LJL
 *
 * Ver 1.0, Feb 15, 2017, LJL, Create file
 */
package com.tplink.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.tplink.base.AnimatorUtils;
import com.tplink.base.BaseItemAnimator;
import com.tplink.base.BaseSelector;
import com.tplink.base.Consts;
import com.tplink.base.DragSelectTouchHelper;
import com.tplink.base.TPFadeInAnimator;
import com.tplink.common.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 通用数据adapter,数据相关的处理都由该类实现, 所有的操作只需要通过这个类完成
 * 1.实现多选功能
 */

public abstract class CommonDataViewProxy<T, M extends CommonDataViewHolder>
        extends RecyclerView.Adapter<M>
        implements View.OnClickListener, View.OnLongClickListener, BaseSelector.DataSource<T> {

    protected List<T> mData;
    protected final Context mContext;
    private RecyclerView mRecyclerView;
    private OnDataItemClick<T> mListener;
    private CommonDataView mCommonDataView;
    // 标志删除动效是否执行完毕
    private boolean mIsPlayingRemoveAnimation = false;
    private Handler mHandler = new Handler();
    private final int REMOVE_ANIMATION_END_TIME = 800;

    //=============================选择相关定义==============================
    protected BaseSelector<T, Integer> mSelector = new BaseSelector<>();

    private OnSelectStatusChanged mOnSelectModeChanged;

    private SelectController<T> mSelectController;


    //============================滑动多选相关内容===========================
    private DragSelectTouchHelper mDragHelper;
    private DragSelectTouchHelper.Callback mCallBack =
            new DragSelectTouchHelper.AdvanceCallback(DragSelectTouchHelper.AdvanceCallback.Mode
                    .FirstItemDependent) {
                // getSelection 与 updateSelection 是必须要实现的方法
                @Override
                public Set<Integer> getSelection() {
                    return mSelector.getSelectedPosition();
                }

                @Override
                public boolean updateSelection(int position, boolean newState) {
                    select(position, newState);
                    return true;
                }

                // onSelectionStarted 与 onSelectionFinished 视需求而定
                @Override
                public void onSelectionStarted(int start) {
                    super.onSelectionStarted(start);
                }

                @Override
                public void onSelectionFinished(int end) {
                    super.onSelectionFinished(end);
                }
            };

    public CommonDataViewProxy(Context context, CommonDataView commonDataView) {
        mContext = context;
        initDragListener();
        mCommonDataView = commonDataView;
        mRecyclerView = mCommonDataView.getDataView();
        mRecyclerView.setHasFixedSize(true);
        // 不同页面动画不同，需要根据具体页面而定
        setItemAnimator();
        mRecyclerView.setLayoutManager(getLayoutManager());
        mRecyclerView.setAdapter(this);
        mDragHelper.attachToRecyclerView(mRecyclerView);
        mSelector.setDataSource(this);
        mSelector.setAutoLeaveSelectionMode(false);
    }

    public void setParentView(DragSelectTouchHelper.InterceptController interceptController) {
        mDragHelper.setInterceptController(interceptController);
    }

    protected abstract RecyclerView.LayoutManager getLayoutManager();


    //=============================选择相关实现==============================
    @SuppressLint("UniqueConstants")
    @Retention(RetentionPolicy.SOURCE)
    public @interface Payload {
        int NORMAL_MODE = 0, MULTI_MODE = 1;
    }

    public List<T> getSelectedItems() {
        return this.mSelector.getSelected();
    }

    public boolean isSelected(int position) {
        if (position >= mData.size()) {
            return false;
        }
        return position != -1 && this.mSelector.isItemSelected(mData.get(position));
    }

    // 通过滑动多选触发的选中状态更新会调用该方法
    // 长按进入批量模态时，因为触发了滑动多选，也会触发到该方法，其中toggle方法会更新因为长按选中的第一个item
    // 所以长按就不需要去更新第一个item了
    public void select(int position, boolean selected) {
        if (position >= mData.size() || position < 0) {
            return;
        }
        T selectedItem = mData.get(position);
        if (selected && mSelectController != null && !mSelectController.canSelectItem(selectedItem)) {
            return;
        } else if (!selected && mSelectController != null) {
            mSelectController.delSelectItem(selectedItem);
        }

        // 只有和之前选中状态不一致时才进行反选
        if (mSelector.isItemSelected(selectedItem) != selected) {
            this.mSelector.toggle(selectedItem, position);
        }
        notifyItemChanged(position, Payload.MULTI_MODE);
        if (mOnSelectModeChanged != null) {
            mOnSelectModeChanged.onSelectCountChanged(this.mSelector.getSelectedCount(), this
                    .mData.size());
        }
    }

    public void exitMultiMode() {
        // 非选择模式不需要退出处理
        if (!mSelector.inSelectionMode() || mIsPlayingRemoveAnimation) {
            return;
        }

        // 先让selector退出，再进行notifySelectModeChanged
        mSelector.leaveSelectionMode();
        notifySelectModeChanged(-1);
        setItemChangeAnimActive(false);
        startSelectionAnimation();
    }

    // 删除单条
    private void deleteItem(int position) {
        // 执行删除动画
        notifyItemRemoved(position);
        // 删除数据源list中的记录
        mData.remove(position);
        // 移除记录所选中条目中的记录
        if (mSelector.getSelectedPosition().size() > 0) {
            mSelector.getSelectedPosition().remove(position);
        }
    }

    /**
     * 批量删除动效
     */
    public void deleteSelectedItems() {
        setItemChangeAnimActive(true);
        mIsPlayingRemoveAnimation = true;
        // 从后往前删除
        for (int i = mData.size() - 1; i >= 0; i--) {
            if (mSelector.getSelectedPosition().contains(i)) {
                deleteItem(i);
            }
        }

        if (mData.size() != 0) {
            notifyItemRangeChanged(0, mData.size(), Payload.MULTI_MODE);
        } else {
            // 全部删除，需要通知界面显示空白页面提示
            showEmptyView();
        }

        mSelector.leaveSelectionMode();
        notifySelectModeChanged(-1);

        // 动效结束后需要更新动效结束标志mIsPlayingRemoveAnimation，并且刷新页面
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIsPlayingRemoveAnimation = false;
                setItemChangeAnimActive(false);
                // 解决因为ViewHolder复用以及删除动效执行过程中快速滑动出现的checkbox未消失问题
                notifyItemRangeChanged(0, mData.size(), Payload.MULTI_MODE);
            }
        }, REMOVE_ANIMATION_END_TIME);
    }

    private void notifySelectModeChanged(int index) {
        boolean mIsMultiMode = mSelector.inSelectionMode();
        if (mOnSelectModeChanged != null) {
            mOnSelectModeChanged.onSelectModeChanged(mIsMultiMode);
        }

        if (mDragHelper != null) {
            if (mIsMultiMode) {
                mDragHelper.activeSelect(index);
            } else {
                mDragHelper.inactiveSelect();
            }
        }
    }

    public void selectAll() {
        if (mSelector.inSelectionMode()) {
            mSelector.selectAll();
            if (mOnSelectModeChanged != null) {
                mOnSelectModeChanged.onSelectCountChanged(mSelector.getSelectedCount(), mData
                        .size());
            }
            notifyItemRangeChanged(0, getItemCount(), Payload.MULTI_MODE);
        }
    }

    public void cancelSelectAll() {
        if (mSelector.inSelectionMode()) {
            mSelector.deSelectAll();
            if (mOnSelectModeChanged != null) {
                mOnSelectModeChanged.onSelectCountChanged(mSelector.getSelectedCount(), mData
                        .size());
            }
            notifyItemRangeChanged(0, getItemCount(), Payload.MULTI_MODE);
        }
    }

    /**
     * Perform animation of entering/leaving selection mode.
     */
    public void startSelectionAnimation() {

        if (!needAnim()) {
            notifyItemRangeChanged(0, getItemCount(), Payload.MULTI_MODE);
            return;
        }

        View view;
        CommonDataViewHolder holder;
        CheckBox box;
        int childCount = mRecyclerView.getChildCount();
        View container;
        for (int i = 0; i < childCount; i++) {
            view = mRecyclerView.getChildAt(i);
            holder = (CommonDataViewHolder) mRecyclerView.getChildViewHolder(view);
            box = holder.getCheckBox();
            container = holder.getContainer();
            if (container == null) {
                container = holder.itemView;
            }
            if (holder != null) {
                if (mSelector.inSelectionMode()) {
                    // 进入批量操作
                    AnimatorUtils.enterSelectionModeWithAnim(mContext, box, container);
                    box.setVisibility(View.VISIBLE);
                } else {
                    //退出批量操作
                    AnimatorUtils.exitSelectionModeWithAnim(mContext, box, container);
                    // 动画结束时再进行checkbox属性设定，不然checkbox直接变为GONE状态，无退出动画
                    // box.setVisibility(View.GONE);
                    // box.setChecked(false);
                    holder.itemView.setBackgroundResource(R.drawable.common_rectangle_ripple_bg);
                }
            }
        }
        refreshHideItem();
    }

    /**
     * 更新不可见(屏幕外、键盘挡住)item的状态
     */
    private void refreshHideItem() {
        LinearLayoutManager llm = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        int fVisible = llm.findFirstVisibleItemPosition();
        int lVisible = llm.findLastVisibleItemPosition();

        if (fVisible > 0) {
            notifyItemRangeChanged(0, fVisible);
        }

        if (lVisible < getItemCount() - 1) {
            notifyItemRangeChanged(lVisible + 1, getItemCount() - (lVisible + 1));
        }
    }

    /**
     * Animation of item change performs when removing a single item.
     *
     * @param isActive
     */
    public void setItemChangeAnimActive(boolean isActive) {
        RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
        if (animator instanceof BaseItemAnimator) {
            ((BaseItemAnimator) animator).setAnimationActive(isActive);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    @Override
    public M onCreateViewHolder(ViewGroup viewGroup, int i) {
        M m = onCreateViewHolderImpl(viewGroup);
        m.itemView.setOnClickListener(this);
        m.itemView.setOnLongClickListener(this);
        return m;
    }

    public abstract M onCreateViewHolderImpl(ViewGroup viewGroup);

    @Override
    public void onClick(View view) {
        RecyclerView.ViewHolder containingViewHolder = mRecyclerView.findContainingViewHolder(view);
        if (containingViewHolder == null) {
            return;
        }
        T downloadInfo = mData.get(containingViewHolder.getAdapterPosition());

        if (mSelector.inSelectionMode()) {

            if (view instanceof ClickParser) {
                if (((ClickParser)view).clickRegion()) {
                    // 如果是在点击区域直接点击处理
                    if (mListener != null) {
                        mListener.onItemClick(downloadInfo, mData.indexOf(downloadInfo));
                    }
                    return;
                }
            }
            boolean selected = mSelector.isItemSelected(downloadInfo);
            if (!selected && !mSelector.isItemSelected(downloadInfo) && mSelectController != null && !mSelectController.canSelectItem(downloadInfo)) {
                return;
            } else if (selected && mSelectController != null) {
                mSelectController.delSelectItem(downloadInfo);
            }

            mSelector.toggle(downloadInfo, containingViewHolder.getAdapterPosition());
            if (mOnSelectModeChanged != null) {
                mOnSelectModeChanged.onSelectCountChanged(mSelector.getSelectedCount(), mData
                        .size());
            }
            notifyItemChanged(containingViewHolder.getAdapterPosition(), Payload.MULTI_MODE);
        } else if (mListener != null) {
            mListener.onItemClick(downloadInfo, mData.indexOf(downloadInfo));
        }
    }

    @Override
    public boolean onLongClick(View view) {
        RecyclerView.ViewHolder containingViewHolder = mRecyclerView.findContainingViewHolder(view);
        if (containingViewHolder == null) {
            return true;
        }
        if (canEnterSelectMode()) {
            if (!mSelector.inSelectionMode()) {
                mSelector.enterSelectionMode();
                // 需要通过触发滑动多选来更新item状态，不需要手动去更新
                notifySelectModeChanged(containingViewHolder.getAdapterPosition());
                setItemChangeAnimActive(false);
                startSelectionAnimation();
            } else {
                if (mDragHelper != null) {
                    mDragHelper.activeSelect(containingViewHolder.getAdapterPosition());
                }
                notifyItemRangeChanged(0, getItemCount(), Payload.MULTI_MODE);
            }
        }

        return true;
    }

    protected abstract boolean canEnterSelectMode();

    public interface OnDataItemClick<T> {
        void onItemClick(T data, int index);
    }

    public interface OnSelectStatusChanged {
        void onSelectModeChanged(boolean inSelectMode);

        void onSelectCountChanged(int count, int amount);
    }

    public void setListener(OnDataItemClick<T> mListener) {
        this.mListener = mListener;
    }

    public void setOnSelectModeChanged(OnSelectStatusChanged mOnSelectModeChanged) {
        this.mOnSelectModeChanged = mOnSelectModeChanged;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public List<T> getItem(int start, int count) {
        return mData;
    }

    @Override
    public void onBindViewHolder(M viewHolder, final int i) {
        if (mSelector.inSelectionMode()) {
            if (mSelector.isItemSelected(mData.get(i))) {
                viewHolder.mCheckBox.setChecked(true);
                viewHolder.itemView.setBackgroundResource(R.color.common_color_select_item);
            } else {
                viewHolder.mCheckBox.setChecked(false);
                viewHolder.itemView.setBackgroundResource(R.drawable.common_rectangle_ripple_bg);
            }
            if (!mIsPlayingRemoveAnimation) {
                viewHolder.mCheckBox.setVisibility(View.VISIBLE);
            }
        } else {
            viewHolder.mCheckBox.setChecked(false);
            if (!mIsPlayingRemoveAnimation) {
                viewHolder.mCheckBox.setVisibility(View.GONE);
            }
            viewHolder.itemView.setBackgroundResource(R.drawable.common_rectangle_ripple_bg);
        }

        viewHolder.mCheckBox.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mDragHelper.activeSelect(i);
                        mDragHelper.interceptParent();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    default:
                        break;
                }
                // 返回true,拦截item的onItemClick事件,不需要再去触发,否则会2次执行toggle,导致异常
                return true;
            }
        });
        // 子类根据自身情况绑定数据
    }

    @Override
    public void onBindViewHolder(M viewHolder, int i, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(viewHolder, i);
            return;
        } else if ((int) payloads.get(0) == Payload.MULTI_MODE) {
            if (mSelector.inSelectionMode()) {
                if (mSelector.isItemSelected(mData.get(i))) {
                    viewHolder.mCheckBox.setChecked(true);
                    viewHolder.itemView.setBackgroundResource(R.color.common_color_select_item);
                } else {
                    viewHolder.mCheckBox.setChecked(false);
                    viewHolder.itemView.setBackgroundResource(R.drawable.common_rectangle_ripple_bg);
                }
                if (!mIsPlayingRemoveAnimation) {
                    viewHolder.mCheckBox.setVisibility(View.VISIBLE);
                }
                onMultiNode(viewHolder, i);
            } else {
                // 需要更新checkbox状态选中状态为false，因为进入批量模态是通过动画进入，不是通过notifyItemChanged进入，
                // 在退出批量模态动画结束时调用notifyItemChanged来更新checkbox状态，保证下次进入批量模态时状态正常
                viewHolder.mCheckBox.setChecked(false);
                if (!mIsPlayingRemoveAnimation) {
                    viewHolder.mCheckBox.setVisibility(View.GONE);
                }
                viewHolder.itemView.setBackgroundResource(R.drawable.common_rectangle_ripple_bg);
            }
            return;
        }
        // 子类根据局部刷新情况自己操作
    }

    /**
     * 选中状态改变时处理，需子类覆盖
     */
    protected void onMultiNode(M viewHolder, int i) {

    }

    /**
     * 子类根据情况初始draglistener
     */
    private void initDragListener() {

        if (mDragHelper == null) {
            mDragHelper = getDragHelper();
        }
    }

    private DragSelectTouchHelper getDragHelper() {
        DragSelectTouchHelper dragSelectTouchHelper = new DragSelectTouchHelper(mCallBack);
        dragSelectTouchHelper.setHotspotRatio(0.2f)      // 默认滚动区高度为列表的 1/5
                .setHotspotOffset(0)        // 指定滚动区离控件的距离，默认为 0
                .setMaximumVelocity(32)     // 默认滚动最大速度为 32
                .setAutoEnterSlideMode(true)         // 设置自动进入滑动选择模式，默认为不允许
                .setAllowDragInSlideMode(true);       // 设置在滑动选择模式下允许长按拖动选择，默认为不允许

        switch (getRecycleType()) {
            case Consts.RECYCLE_TYPE_LIST:
                // 滑动选择模式下滑动区域 start~end
                dragSelectTouchHelper.setSlideArea(0, Consts.EDGE_SLIDE_DISTANCE);
                break;
            case Consts.RECYCLE_TYPE_GRID:
                dragSelectTouchHelper.setAutoEnterSlideMode(false);
                break;
            case Consts.RECYCLE_TYPE_VIDEO:
                dragSelectTouchHelper.setSlideArea(0, Consts.EDGE_SLIDE_DISTANCE_VIDEO);
                break;
            default:
                break;
        }

        return dragSelectTouchHelper;
    }

    private void setItemAnimator() {
        switch (getRecycleType()) {
            case Consts.RECYCLE_TYPE_LIST:
                mRecyclerView.setItemAnimator(new TPFadeInAnimator(getCheckBoxId(), getContainerId()));
                break;
            default:
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                break;
        }
    }

    protected abstract int getContainerId();

    protected abstract int getCheckBoxId();

    /**
     * 子类根据情况返回列表类型，默认为一列，滑动区域为64
     *
     * @return 0表示一列, 1表示Grid, 2表示视频页面
     */
    protected int getRecycleType() {
        return Consts.RECYCLE_TYPE_LIST;
    }

    public void updateData(final List<T> newDatas) {
        this.mData = newDatas;
        // TODO: 如果时全选模式，需要强制退出?
        notifyDataSetChanged();
        if (this.mData == null || this.mData.size() == 0) {
            showEmptyView();
        } else {
            hideEmptyView();
        }
    }

    public void setSelectItems(Collection<T> items) {
        if (mData == null || items == null) return;
        int index = -1;
        for (T item : items) {
            if (mSelector.isItemSelected(item)) {
                continue;
            }
            index = mData.indexOf(item);
            if (index >= 0) {
                mSelector.toggle(mData.get(index), index);
                notifyItemChanged(index, Payload.MULTI_MODE);
            }
        }
    }

    public void addSelectItem(T item) {
        if (mData == null) return;
        if (mSelector.isItemSelected(item)) return;
        int index = mData.indexOf(item);
        if (index >= 0) {
            mSelector.toggle(item, index);
            notifyItemChanged(index, Payload.MULTI_MODE);
        }
    }

    public void delSelectItem(T item) {
        if (mData == null) return;
        if (!mSelector.isItemSelected(item)) return;
        int index = mData.indexOf(item);
        if (index >= 0) {
            mSelector.toggle(item, index);
            notifyItemChanged(index, Payload.MULTI_MODE);
        }
    }

    public void delSelectItems(List<T> items) {
        if (mData == null) return;
        int index = -1;
        for (T item : items) {
            if (!mSelector.isItemSelected(item)) {
                continue;
            }
            index = mData.indexOf(item);
            if (index >= 0) {
                mSelector.toggle(mData.get(index), index);
                notifyItemChanged(index, Payload.MULTI_MODE);
            }
        }
    }

    public void setSelectItems(Set<T> items) {
        if (items == null) {
            return;
        }
        int index = -1;
        for (T item : items) {
            if (mSelector.isItemSelected(item)) {
                continue;
            }
            index = mData.indexOf(item);
            if (index >= 0) {
                mSelector.toggle(mData.get(index), index);
            }
        }
    }

    public void updateData(final List<T> newDatas, DiffUtil.DiffResult result) {
        if (result == null) {
            updateData(newDatas);
            return;
        }
        this.mData = newDatas;
        if (this.mData == null || this.mData.size() == 0) {
            showEmptyView();
        } else {
            result.dispatchUpdatesTo(this);
            hideEmptyView();
        }
    }

    public List<T> getData() {
        return mData;
    }

    public void setSelectController(SelectController<T> mSelectController) {
        this.mSelectController = mSelectController;
    }

    protected void showEmptyView() {
        setItemChangeAnimActive(false);
        mCommonDataView.showEmptyView(showEmptyIcon(), showEmptyText(), getEmptyIcon(),
                getEmptyText());
    }

    public abstract int getEmptyIcon();

    protected void hideEmptyView() {
        mCommonDataView.hideEmptyView();
    }

    protected boolean showEmptyIcon() {
        return true;
    }

    protected boolean showEmptyText() {
        return true;
    }

    protected int getEmptyText() {
        return R.string.common_no_file;
    }

    public T getItem(int index) {
        if (this.mData != null && index < this.mData.size()) {
            return this.mData.get(index);
        }
        return null;
    }

    public boolean getMultiMode() {
        return mSelector.inSelectionMode();
    }

    public List<T> getAllData() {
        return mData;
    }

    public boolean needAnim() {
        return true;
    }

    public interface SelectController<T> {
        // 是否可以选择
        boolean canSelectItem(T item);
        void delSelectItem(T item);
    }
}
