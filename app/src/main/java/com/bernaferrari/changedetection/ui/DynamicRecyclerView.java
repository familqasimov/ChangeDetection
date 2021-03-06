package com.bernaferrari.changedetection.ui;

/**
 * Created by bernardoferrari on 09/11/17.
 * Inspired on FastHub implementation.
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;

import com.orhanobut.logger.Logger;


/**
 * Created by Kosh on 9/24/2015. copyrights are reserved
 * <p>
 * recyclerview which will showParentOrSelf/showParentOrSelf itself base on adapter
 */
public class DynamicRecyclerView extends RecyclerView {

    private StateLayout emptyView;
    @Nullable
    private View parentView;
    private BottomPaddingDecoration bottomPaddingDecoration;

    @NonNull
    private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            showEmptyView();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            showEmptyView();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            showEmptyView();
        }
    };

    public DynamicRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public DynamicRecyclerView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DynamicRecyclerView(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        super.setAdapter(adapter);
        if (isInEditMode()) return;
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
            observer.onChanged();
        }
    }

    public void removeBottomDecoration() {
        if (bottomPaddingDecoration != null) {
            removeItemDecoration(bottomPaddingDecoration);
            bottomPaddingDecoration = null;
        }
    }

    public void addDecoration() {
        bottomPaddingDecoration = BottomPaddingDecoration.with(getContext());
        addItemDecoration(bottomPaddingDecoration);
    }

    private void showEmptyView() {
        Adapter<?> adapter = getAdapter();
        if (adapter != null) {
            if (emptyView != null) {
//                Logger.d("state ItemCount: " + adapter.getItemCount());
                if (adapter.getItemCount() == 0) {
                    showParentOrSelf(false);
                } else {
                    showParentOrSelf(true);
                }
            }
        } else {
            if (emptyView != null) {
//                Logger.d("state emptyView != null");
                showParentOrSelf(false);
            }
        }
    }

    private void showParentOrSelf(boolean showRecyclerView) {
//        Logger.d("state showParentOrSelf");

        if (parentView != null)
            parentView.setVisibility(VISIBLE);
        setVisibility(VISIBLE);
        emptyView.setVisibility(!showRecyclerView ? VISIBLE : GONE);
    }

    public void setEmptyView(@NonNull StateLayout emptyView, @Nullable View parentView) {
        this.emptyView = emptyView;
        this.parentView = parentView;
        showEmptyView();
    }

    public void setEmptyView(@NonNull StateLayout emptyView) {
        Logger.d("statelayout: " + emptyView);
        setEmptyView(emptyView, null);
    }

//    public void hideProgress(@NonNull StateLayout view) {
//        view.hideProgress();
//    }
//
//    public void showNoChangesDetectedError(@NonNull StateLayout view) {
//        view.showNoChangesDetectedError();
//    }

//    public void addKeyLineDivider() {
//        if (canAddDivider()) {
//            Resources resources = getResources();
//            addItemDecoration(new InsetDividerDecoration(resources.getDimensionPixelSize(R.dimen.divider_height),
//                    resources.getDimensionPixelSize(R.dimen.keyline_2), ViewHelper.getListDivider(getContext())));
//        }
//    }

//    public void addDivider() {
//        if (canAddDivider()) {
//            Resources resources = getResources();
//            addItemDecoration(new InsetDividerDecoration(resources.getDimensionPixelSize(R.dimen.divider_height), 0,
//                    ViewHelper.getListDivider(getContext())));
//        }
//    }

//    public void addNormalSpacingDivider() {
//        addDivider();
//    }

//    public void addDivider(@NonNull Class toDivide) {
//        if (canAddDivider()) {
//            Resources resources = getResources();
//            addItemDecoration(new InsetDividerDecoration(resources.getDimensionPixelSize(R.dimen.divider_height), 0,
//                    ViewHelper.getListDivider(getContext()), toDivide));
//        }
//    }

    private boolean canAddDivider() {
        if (getLayoutManager() != null) {
            if (getLayoutManager() instanceof LinearLayoutManager) {
                return true;
            } else if (getLayoutManager() instanceof GridLayoutManager) {
                return ((GridLayoutManager) getLayoutManager()).getSpanCount() == 1;
            } else if (getLayoutManager() instanceof StaggeredGridLayoutManager) {
                return ((StaggeredGridLayoutManager) getLayoutManager()).getSpanCount() == 1;
            }
        }
        return false;
    }
}
