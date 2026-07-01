package com.bence.songbook.actions;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import com.bence.songbook.ui.activity.MainActivity;

import org.hamcrest.Matcher;

public final class QueueDragActions {

    private QueueDragActions() {
    }

    public static ViewAction reorderItem(int fromPosition, int toPosition) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(RecyclerView.class);
            }

            @Override
            public String getDescription() {
                return "Reorder queue item from position " + fromPosition + " to " + toPosition;
            }

            @Override
            public void perform(UiController uiController, View view) {
                RecyclerView recyclerView = (RecyclerView) view;
                RecyclerView.ViewHolder fromHolder =
                        recyclerView.findViewHolderForAdapterPosition(fromPosition);
                if (fromHolder == null) {
                    throw new IllegalStateException("No ViewHolder at position " + fromPosition);
                }
                RecyclerView.ViewHolder toHolder =
                        recyclerView.findViewHolderForAdapterPosition(toPosition);
                if (toHolder == null) {
                    throw new IllegalStateException("No ViewHolder at position " + toPosition);
                }

                float startX = fromHolder.itemView.getX() + fromHolder.itemView.getWidth() / 2f;
                float startY = fromHolder.itemView.getY() + fromHolder.itemView.getHeight() / 2f;
                float endX = toHolder.itemView.getX() + toHolder.itemView.getWidth() / 2f;
                float endY = toHolder.itemView.getY() + toHolder.itemView.getHeight() / 2f;
                if (toPosition > fromPosition) {
                    endY = toHolder.itemView.getY() + toHolder.itemView.getHeight() * 0.85f;
                } else if (toPosition < fromPosition) {
                    endY = toHolder.itemView.getY() + toHolder.itemView.getHeight() * 0.15f;
                }

                dragOnRecyclerView(recyclerView, fromPosition, uiController,
                        startX, startY, endX, endY);
            }
        };
    }

    public static ViewAction deleteItemByHorizontalDrag(int position) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(RecyclerView.class);
            }

            @Override
            public String getDescription() {
                return "Delete queue item at position " + position + " via horizontal drag";
            }

            @Override
            public void perform(UiController uiController, View view) {
                RecyclerView recyclerView = (RecyclerView) view;
                RecyclerView.ViewHolder fromHolder =
                        recyclerView.findViewHolderForAdapterPosition(position);
                if (fromHolder == null) {
                    throw new IllegalStateException("No ViewHolder at position " + position);
                }

                startQueueDrag(recyclerView, position, uiController);

                View itemView = fromHolder.itemView;
                long downTime = System.currentTimeMillis();
                float centerX = itemView.getWidth() / 2f;
                float centerY = itemView.getHeight() / 2f;
                float deleteX = centerX + itemView.getWidth() * 0.75f;
                dispatchLocalTouch(itemView, downTime, MotionEvent.ACTION_DOWN, centerX, centerY);
                dispatchLocalTouch(itemView, downTime, MotionEvent.ACTION_MOVE, deleteX, centerY);
                dispatchLocalTouch(itemView, downTime, MotionEvent.ACTION_UP, deleteX, centerY);
                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    private static void startQueueDrag(RecyclerView recyclerView,
                                       int position,
                                       UiController uiController) {
        MainActivity activity = findMainActivity(recyclerView.getContext());
        activity.testingStartQueueItemDrag(position);
        uiController.loopMainThreadForAtLeast(100);
    }

    private static MainActivity findMainActivity(Context context) {
        Context current = context;
        while (current instanceof ContextWrapper) {
            if (current instanceof MainActivity) {
                return (MainActivity) current;
            }
            current = ((ContextWrapper) current).getBaseContext();
        }
        throw new IllegalStateException("Queue RecyclerView is not hosted by MainActivity");
    }

    private static void dragOnRecyclerView(RecyclerView recyclerView,
                                           int fromPosition,
                                           UiController uiController,
                                           float startX,
                                           float startY,
                                           float endX,
                                           float endY) {
        long downTime = SystemClock.uptimeMillis();
        dispatchLocalTouch(recyclerView, downTime, MotionEvent.ACTION_DOWN, startX, startY);
        uiController.loopMainThreadForAtLeast(50);
        startQueueDrag(recyclerView, fromPosition, uiController);

        int steps = 10;
        for (int i = 1; i <= steps; i++) {
            float fraction = i / (float) steps;
            float x = startX + (endX - startX) * fraction;
            float y = startY + (endY - startY) * fraction;
            dispatchLocalTouch(recyclerView, downTime, MotionEvent.ACTION_MOVE, x, y);
            uiController.loopMainThreadForAtLeast(16);
        }
        dispatchLocalTouch(recyclerView, downTime, MotionEvent.ACTION_UP, endX, endY);
        uiController.loopMainThreadUntilIdle();
    }

    private static void dispatchLocalTouch(View view,
                                           long downTime,
                                           int action,
                                           float localX,
                                           float localY) {
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(
                downTime,
                eventTime,
                action,
                localX,
                localY,
                0
        );
        view.dispatchTouchEvent(event);
        event.recycle();
    }
}
