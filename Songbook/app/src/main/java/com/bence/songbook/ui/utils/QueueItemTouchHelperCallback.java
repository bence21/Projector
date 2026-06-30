package com.bence.songbook.ui.utils;

import android.annotation.SuppressLint;
import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bence.songbook.ui.queue.QueueViewModel;

public class QueueItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final QueueViewModel queueViewModel;
    private float dragStartX;
    private int dragStartPosition = RecyclerView.NO_POSITION;
    private boolean deleteTriggered;

    public QueueItemTouchHelperCallback(QueueViewModel queueViewModel) {
        this.queueViewModel = queueViewModel;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder source,
                          @NonNull RecyclerView.ViewHolder target) {
        int from = source.getBindingAdapterPosition();
        int to = target.getBindingAdapterPosition();
        if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) {
            return false;
        }
        queueViewModel.swap(from, to);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Swipe disabled; horizontal delete handled during drag.
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
            dragStartX = 0;
            deleteTriggered = false;
            dragStartPosition = viewHolder.getBindingAdapterPosition();
            viewHolder.itemView.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        dragStartX = event.getX();
                        break;
                    case android.view.MotionEvent.ACTION_MOVE:
                        if (!deleteTriggered && dragStartPosition != RecyclerView.NO_POSITION) {
                            float deltaX = Math.abs(event.getX() - dragStartX);
                            int halfWidth = v.getWidth() / 2;
                            if (halfWidth > 0 && deltaX > halfWidth) {
                                deleteTriggered = true;
                                queueViewModel.removeAt(dragStartPosition);
                            }
                        }
                        break;
                    default:
                        break;
                }
                return false;
            });
        } else if (viewHolder != null) {
            viewHolder.itemView.setOnTouchListener(null);
            dragStartPosition = RecyclerView.NO_POSITION;
            deleteTriggered = false;
        }
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setOnTouchListener(null);
        dragStartPosition = RecyclerView.NO_POSITION;
        deleteTriggered = false;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && !deleteTriggered) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

}
