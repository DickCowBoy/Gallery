package com.tplink.gallery.selector.wallpaper;

import android.content.Context;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.R;
import com.tplink.utils.NoneBoundArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResultContainer {

    public static final int UNLIMIT = -1;
    public static final int ERR_OVER_COUNT = 1;
    public static final int ERR_OVER_SIZE = 2;


    private Map<Long, HashSet<MediaBean>> mSelectedItems = new HashMap<>();
    public Set<Integer> mWallPapers = new HashSet<>();
    private int countLimit = UNLIMIT;
    private long sizeLimit = UNLIMIT;

    public ResultContainer(int countLimit, long sizeLimit) {
        this.countLimit = countLimit;
        this.sizeLimit = sizeLimit;
    }

    public void addItems(List<MediaBean> entities) {
        if (entities == null) {
            return;
        }
        HashSet<MediaBean> mediaEntities = null;
        for (MediaBean entity : entities) {
            mediaEntities = mSelectedItems.get(entity.bucketId);
            if (mediaEntities == null) {
                mediaEntities = new HashSet<>();
                mSelectedItems.put(entity.bucketId, mediaEntities);
            }
            mediaEntities.add(entity);
        }
    }

    private int checkAdd(Collection<MediaBean> beas) {
        int[] count = getCount();
        if (countLimit != UNLIMIT && (count[0] + beas.size()) > countLimit) {
            return ERR_OVER_COUNT;
        }
        if (sizeLimit != UNLIMIT) {
            long selectSize = getSelectSize();
            for (MediaBean bea : beas) {
                selectSize += bea.size;
            }
            if (sizeLimit < selectSize) {
                return ERR_OVER_SIZE;
            }
        }
        return 0;

    }

    private int checkAdd(MediaBean bean) {
        int[] count = getCount();
        if (countLimit != UNLIMIT && (count[0]) >= countLimit) {
            return ERR_OVER_COUNT;
        }
        if (sizeLimit != UNLIMIT) {
            long selectSize = getSelectSize() + bean.size;
            if (sizeLimit < selectSize) {
                return ERR_OVER_SIZE;
            }
        }
        return 0;

    }

    public int addItem(MediaBean entity) {
        if (entity == null) {
            return -1;
        }
        int checkResult = checkAdd(entity);
        if (checkResult != 0) {
            return checkResult;
        }
        HashSet<MediaBean> mediaEntities = mSelectedItems.get(entity.bucketId);
        if (mediaEntities == null) {
            mediaEntities = new HashSet<>();
            mSelectedItems.put(entity.bucketId, mediaEntities);
        }
        mediaEntities.add(entity);
        return 0;
    }

    public String getResultHint(Context context, int errorCode) {
        switch (errorCode) {
            case ERR_OVER_COUNT:
                return context.getString(R.string.select_count_over);
            case ERR_OVER_SIZE:
                return String.format(context.getString(R.string.select_size_over),
                        sizeLimit / 1024 / 1024);
        }
        return null;
    }




    public void delItem(MediaBean entity) {
        if (entity == null) {
            return;
        }
        HashSet<MediaBean> mediaEntities = mSelectedItems.get(entity.bucketId);
        if (mediaEntities != null) {
            mediaEntities.remove(entity);
        }
    }

    public int getAlbumCount(long bucketId) {
        HashSet<MediaBean> mediaEntities = mSelectedItems.get(bucketId);
        return mediaEntities == null ? 0 : mediaEntities.size();
    }

    public int addBucketItems(Long bucketId, List<MediaBean> entities) {

        int checkResult = checkAdd(entities);
        if (checkResult != 0) {
            return checkResult;
        }
        HashSet<MediaBean> mediaEntities = mSelectedItems.get(bucketId);
        if (mediaEntities == null) {
            mediaEntities = new HashSet<>();
            mSelectedItems.put(bucketId, mediaEntities);
        }
        for (MediaBean entity : entities) {
            mediaEntities.add(entity);
        }
        return 0;
    }

    public Collection<MediaBean> delBucketItems(long bucketId) {

        return mSelectedItems.remove(bucketId);
    }

    public void clear() {
        mSelectedItems.clear();
    }

    public int[] getCount() {
        int size = 0;
        for (Map.Entry<Long, HashSet<MediaBean>> entry : mSelectedItems.entrySet()) {
            size += entry.getValue().size();
        }
        return new int[]{size, countLimit};
    }

    public long getSelectSize() {
        long size = 0;
        for (Map.Entry<Long, HashSet<MediaBean>> entry : mSelectedItems.entrySet()) {
            for (MediaBean bean : entry.getValue()) {
                size += bean.size;
            }
        }
        return size;
    }

    public void getResult(Set<Integer> newItem, Set<Integer> delItem) {
        delItem.addAll(mWallPapers);
        for (Map.Entry<Long, HashSet<MediaBean>> entry : mSelectedItems.entrySet()) {
            for (MediaBean mediaEntity : entry.getValue()) {
                newItem.add(mediaEntity._id);
            }
        }
        delItem.removeAll(newItem); // 剩下删除的内容
        newItem.removeAll(mWallPapers);// 剩下即新添加的条目
    }

    public List<MediaBean> getMediaEntries() {
        List<MediaBean> entities = new NoneBoundArrayList<>();
        for (Map.Entry<Long, HashSet<MediaBean>> entry : mSelectedItems.entrySet()) {
            entities.addAll(entry.getValue());
        }
        return entities;
    }

    public Set<MediaBean> getSelectBucketItems(long bucketId) {
        return mSelectedItems.get(bucketId);
    }

    public int getCountLimit() {
        return countLimit;
    }
}
