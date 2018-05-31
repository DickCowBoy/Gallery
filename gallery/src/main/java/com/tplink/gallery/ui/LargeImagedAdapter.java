package com.tplink.gallery.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.util.FixedPreloadSizeProvider;
import com.bumptech.glide.util.Synthetic;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.tplink.gallery.GlideApp;
import com.tplink.gallery.GlideRequest;
import com.tplink.gallery.GlideRequests;
import com.tplink.gallery.gallery.R;
import com.tplink.gallery.utils.MediaUtils;
import com.tplink.gallery.view.BigImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class LargeImagedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public int imageWidthPixels = 640;
    public int imageHeightPixels = 640;


    private ListPreloader.PreloadSizeProvider sizeProvider;
    private ListPreloader.PreloadModelProvider preloadModelProvider;

    private final RecyclerView mView;
    private List<ImageSource> mList = new ArrayList<>();
    private Context context;
    private GlideRequests glideRequests;
    public LargeImagedAdapter(Context context, RecyclerView view) {
        this.context = context;
        glideRequests = GlideApp.with(context);
        this.mView = view;

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        imageWidthPixels = imageHeightPixels = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);

        sizeProvider =
                new FixedPreloadSizeProvider(imageWidthPixels, imageHeightPixels);
        preloadModelProvider = new ListPreloader.PreloadModelProvider<ImageSource>() {

            @NonNull
            @Override
            public List<ImageSource> getPreloadItems(int position) {
                return Collections.singletonList(mList.get(position));
            }

            @Nullable
            @Override
            public RequestBuilder<?> getPreloadRequestBuilder(ImageSource item) {
                return GlideApp.with(context).asBitmap().load(item.getUri())
                        .override(imageWidthPixels, imageHeightPixels)
                        .placeholder(R.mipmap.ic_launcher);
            }
        };

        RecyclerViewPreloader<ImageSource> preloader =
                new RecyclerViewPreloader<>(GlideApp.with(context), preloadModelProvider, sizeProvider, 3);
        view.addOnScrollListener(preloader);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof ViewHolder) {
            ((ViewHolder)holder).mSubImageView.reuse();
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_card_item, parent, false);
                onCreateViewHolder(parent, itemView);
                return new ViewHolder(itemView);
            case 1:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_card_item1, parent, false);
                onCreateViewHolder(parent, itemView);
                return new ViewHolderImage(itemView);

        }
        return null;
    }

    public void setList(List<ImageSource> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    public void updateList(List<ImageSource> list) {
        if (mList == null || mList.size() == 0) {
            setList(list);
            return;
        }
        LargeImageDiffUtil largeImageDiffUtil = new LargeImageDiffUtil(mList);
        mList = list;
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(largeImageDiffUtil);
        diffResult.dispatchUpdatesTo(this);
    }

    private class LargeImageDiffUtil extends DiffUtil.Callback{

        private List<ImageSource> oldList;

        public LargeImageDiffUtil(List<ImageSource> oldList) {
            this.oldList = oldList;
        }

        @Override
        public int getOldListSize() {
            return oldList == null ? 0 : oldList.size();
        }

        @Override
        public int getNewListSize() {
            return mList.size();
        }

        // 条目是否一样
        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getUri().equals(mList.get(newItemPosition).getUri());
        }

        // 内容一致
        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getUri().equals(mList.get(newItemPosition).getUri());
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        // mCardAdapterHelper.onBindViewHolder(holder.itemView, position, getItemCount());
        switch (getItemViewType(position)) {
            case 0:
                ViewHolder holdersub = (ViewHolder) holder;
                onCreateViewHolder(mView, holdersub.itemView);
                holdersub.itemView.requestLayout();
                holdersub.mSubImageView.setImage(mList.get(position));
                GlideApp.with(context).asBitmap().load(mList.get(position).getUri())
                        .override(imageWidthPixels, imageHeightPixels)
                        .placeholder(R.mipmap.ic_launcher)
                        .into(new CustomTarget(holdersub.mSubImageView));
                if (mList.get(position) == null) {

                }
                holdersub.mSubImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
                break;
            case 1:
                ViewHolderImage holderimg = (ViewHolderImage) holder;
                onCreateViewHolder(mView, holderimg.itemView);

                holderimg.itemView.requestLayout();

                ImageSource imageSource = mList.get(position);
                holderimg.mImageView.setBackDx(imageSource.getSWidth() / 1.0F / imageSource.getSHeight());

                if (mList.get(position).getMimeType().equals("image/gif")) {
                  glideRequests.asGif()
                            .load(mList.get(position).getUri())
                            .override(imageWidthPixels, imageHeightPixels)
                            .into(holderimg.mImageView);
                } else {
                    glideRequests.asDrawable()
                            .load(mList.get(position).getUri())
                            .override(imageWidthPixels, imageHeightPixels)
                            .into(holderimg.mImageView);
                }

                break;
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final SubsamplingScaleImageView mSubImageView;
        public ViewHolder(final View itemView) {
            super(itemView);
            mSubImageView = (SubsamplingScaleImageView) itemView.findViewById(R.id.imageView);
            //mSubView = (SubsamplingScaleImageView) itemView.findViewById(R.id.subimageView);
        }

    }

    public class ViewHolderImage extends RecyclerView.ViewHolder {
        public final BigImageView mImageView;
        public ViewHolderImage(final View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.imageView);
            //mSubView = (SubsamplingScaleImageView) itemView.findViewById(R.id.subimageView);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (mList.get(position).supportScale()) {
            return 0;
        } else {
            return 1;
        }
    }

    public void onCreateViewHolder(ViewGroup parent,  View itemView) {
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        lp.width = parent.getWidth();// - dip2px(itemView.getContext(), 2 * (mPagePadding + mShowLeftCardWidth));
        lp.height = parent.getHeight();
        lp.topMargin = 0;
        itemView.setLayoutParams(lp);
    }

    public void onCreateHalfHolder(ViewGroup parent,  View itemView) {
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        lp.width = parent.getWidth() * 2 / 3;// - dip2px(itemView.getContext(), 2 * (mPagePadding + mShowLeftCardWidth));
        lp.height = parent.getHeight() * 2 / 3;
        lp.topMargin = parent.getHeight() / 6;
        itemView.setLayoutParams(lp);
    }

    /**
     * 预先加载下一条
     */
    private static final class PreloadTarget extends BaseTarget<Object> {
        @Synthetic
        int photoHeight;
        @Synthetic int photoWidth;

        @Synthetic
        PreloadTarget() { }

        @Override
        public void onResourceReady(Object resource, Transition<? super Object> transition) {
            // Do nothing.
        }

        @Override
        public void getSize(SizeReadyCallback cb) {
            cb.onSizeReady(photoWidth, photoHeight);
        }

        @Override
        public void removeCallback(SizeReadyCallback cb) {
            // Do nothing because we don't retain references to SizeReadyCallbacks.
        }
    }
}
