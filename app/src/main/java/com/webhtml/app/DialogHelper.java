package com.webhtml.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.lang.reflect.Field;

public class DialogHelper {

    public static void showCustomAlert(Activity activity, String message, JsResult result) {
        activity.runOnUiThread(() -> {
            float density = activity.getResources().getDisplayMetrics().density;

            final FrameLayout overlayContainer = new FrameLayout(activity);

            // ZATAMNJENJE POZADINE
            overlayContainer.setBackgroundColor(Color.parseColor("#80000000"));
            overlayContainer.setClickable(true);
            overlayContainer.setFocusable(true);

            LinearLayout layout = new LinearLayout(activity);
            layout.setOrientation(LinearLayout.VERTICAL);

            int padTopBottom = (int) (8 * density);
            int padHorizontal = (int) (12 * density);
            layout.setPadding(padHorizontal, 0, padHorizontal, padTopBottom);

            GradientDrawable backgroundDrawable = new GradientDrawable();
            backgroundDrawable.setColor(Color.parseColor("#1C1C1E"));
            backgroundDrawable.setCornerRadius(18 * density);
            backgroundDrawable.setStroke((int) (1 * density), Color.parseColor("#353535"));
            layout.setBackground(backgroundDrawable);

            // Naslov (fiksiran na vrhu)
            TextView titleView = new TextView(activity);
            titleView.setText("Message");
            titleView.setTextColor(Color.parseColor("#c5c5c5"));
            titleView.setTextSize(16);
            titleView.setTypeface(null, Typeface.BOLD);
            titleView.setGravity(Gravity.START);
            titleView.setIncludeFontPadding(false);
            
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            titleParams.setMargins((int) (5 * density), (int) (25 * density), 0, (int) (25 * density));
            titleView.setLayoutParams(titleParams);
            layout.addView(titleView);

            // ScrollView za tekst - Android samostalno upravlja visinom i fleksibilnošću
            ScrollView scrollView = new ScrollView(activity);
            scrollView.setVerticalScrollBarEnabled(true);
            scrollView.setScrollbarFadingEnabled(true);
            
            // Korišćenje weight=1 u kombinaciji sa visinom 0 omogućava da ScrollView uzme samo onoliko 
            // prostora koliko mu treba, ali da se automatski skupi ako ponestane mesta na ekranu
            LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1.0f 
            );
            scrollView.setLayoutParams(scrollParams);

            TextView messageView = new TextView(activity);
            messageView.setText(message);
            messageView.setTextColor(Color.parseColor("#c5c5c7"));
            messageView.setTextSize(14);
            messageView.setIncludeFontPadding(false);
            messageView.setGravity(Gravity.START);
            
            LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            msgParams.setMargins((int) (3 * density), 0, (int) (5 * density), 0);
            messageView.setLayoutParams(msgParams);
            scrollView.addView(messageView);

            layout.addView(scrollView);

            // Dugme OK (fiksirano na dnu dijaloga)
            TextView closeButton = createStyledButton(activity, "OK", density);

            LinearLayout buttonLayout = new LinearLayout(activity);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
            buttonLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            buttonLayout.setPadding(0, (int) (20 * density), 0, 0);
            buttonLayout.addView(closeButton);
            
            LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            buttonLayout.setLayoutParams(buttonLayoutParams);
            layout.addView(buttonLayout);

            // Dinamička širina i visina preko WRAP_CONTENT-a sa bezbednim marginama
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER;
            int horizontalMargin = (int) (activity.getResources().getDisplayMetrics().widthPixels * (1 - 0.824) / 2);
            params.leftMargin = horizontalMargin;
            params.rightMargin = horizontalMargin;
            
            int verticalMargin = (int) (40 * density);
            params.topMargin = verticalMargin;
            params.bottomMargin = verticalMargin;

            overlayContainer.addView(layout, params);

            ViewGroup rootLayout = activity.findViewById(android.R.id.content);
            rootLayout.addView(overlayContainer, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));

            closeButton.setOnClickListener(v -> {
                result.confirm();
                rootLayout.removeView(overlayContainer);
            });
        });
    }

    public static void showNativeDownloadDialog(Activity activity, String suggestedFileName, String url, String mimetype, boolean isBlob, DownloadCallback callback) {
        activity.runOnUiThread(() -> {
            float density = activity.getResources().getDisplayMetrics().density;

            final FrameLayout overlayContainer = new FrameLayout(activity);
            overlayContainer.setBackgroundColor(Color.parseColor("#80000000"));
            overlayContainer.setClickable(true);
            overlayContainer.setFocusable(true);

            final LinearLayout layout = new LinearLayout(activity);
            layout.setOrientation(LinearLayout.VERTICAL);

            layout.setFocusable(true);
            layout.setFocusableInTouchMode(true);

            int padHorizontal = (int) (12 * density);
            int padBottom = (int) (8 * density);
            layout.setPadding(padHorizontal, 0, padHorizontal, padBottom);

            GradientDrawable backgroundDrawable = new GradientDrawable();
            backgroundDrawable.setColor(Color.parseColor("#1C1C1E"));
            backgroundDrawable.setCornerRadius(18 * density);
            backgroundDrawable.setStroke((int) (1 * density), Color.parseColor("#353535"));
            layout.setBackground(backgroundDrawable);

            TextView titleView = new TextView(activity);
            titleView.setText("Would you like to save this file?");
            titleView.setTextColor(Color.parseColor("#c5c5c5"));
            titleView.setTextSize(16);
            titleView.setTypeface(null, Typeface.BOLD);
            titleView.setGravity(Gravity.START);
            titleView.setIncludeFontPadding(false);

            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            titleParams.setMargins((int) (5 * density), (int) (25 * density), 0, (int) (25 * density));
            titleView.setLayoutParams(titleParams);
            layout.addView(titleView);

            final EditText input = new EditText(activity);
            input.setText(suggestedFileName);
            input.setTextSize(14);
            input.setTextColor(Color.parseColor("#c5c5c7"));
            input.setSingleLine(true);
            input.setBackground(null);
            input.setPadding((int) (2 * density), (int) (5 * density), (int) (2 * density), (int) (5 * density));
            
            setCursorColorAndWidth(input, Color.parseColor("#617AC2"), (int) (1.5f * density));

            LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            inputParams.setMargins((int) (5 * density), 0, (int) (5 * density), 0);
            layout.addView(input, inputParams);

            View lineView = new View(activity);
            LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (1 * density)
            );
            lineParams.setMargins((int) (5 * density), (int) (5 * density), (int) (5 * density), 0);
            lineView.setLayoutParams(lineParams);
            lineView.setBackgroundColor(Color.parseColor("#272729"));
            layout.addView(lineView);

            TextView saveButton = createStyledButton(activity, "OK", density);
            TextView closeButton = createStyledButton(activity, "Cancel", density);

            LinearLayout buttonLayout = new LinearLayout(activity);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
            buttonLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            
            LinearLayout.LayoutParams buttonWrapperParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            buttonWrapperParams.setMargins(0, (int) (44 * density), 0, 0);
            
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            btnParams.setMargins((int) (8 * density), 0, 0, 0);

            buttonLayout.addView(closeButton);
            buttonLayout.addView(saveButton, btnParams);
            
            layout.addView(buttonLayout, buttonWrapperParams);

            input.clearFocus();

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER;
            int horizontalMargin = (int) (activity.getResources().getDisplayMetrics().widthPixels * (1 - 0.824) / 2);
            params.leftMargin = horizontalMargin;
            params.rightMargin = horizontalMargin;

            overlayContainer.addView(layout, params);

            ViewGroup rootLayout = activity.findViewById(android.R.id.content);
            rootLayout.addView(overlayContainer, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));

            closeButton.setOnClickListener(v -> rootLayout.removeView(overlayContainer));

            saveButton.setOnClickListener(v -> {
                String finalName = input.getText().toString().trim();
                if (finalName.isEmpty()) {
                    finalName = suggestedFileName;
                }
                callback.onSave(finalName);
                rootLayout.removeView(overlayContainer);
            });
        });
    }

    private static TextView createStyledButton(Activity activity, String text, float density) {
        TextView button = new TextView(activity);
        button.setText(text);
        button.setTextColor(Color.parseColor("#617AC2"));
        button.setTextSize(14);
        button.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        button.setGravity(Gravity.CENTER);
        button.setIncludeFontPadding(false);
        button.getPaint().setSubpixelText(true);

        int minWidth = (int) (70 * density);
        int height = (int) (40 * density);
        button.setMinimumWidth(minWidth);
        button.setHeight(height);
        button.setPadding((int) (16 * density), 0, (int) (16 * density), 0);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.TRANSPARENT);
        bg.setCornerRadius(32 * density);
        button.setBackground(bg);

        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    bg.setColor(Color.parseColor("#1FFFFFFF"));
                    v.invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    bg.setColor(Color.TRANSPARENT);
                    v.invalidate();
                    break;
            }
            return false;
        });

        return button;
    }

    private static void setCursorColorAndWidth(EditText editText, int color, int widthPx) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ShapeDrawable cursorDrawable = new ShapeDrawable(new RectShape());
                cursorDrawable.setIntrinsicWidth(widthPx);
                cursorDrawable.setBounds(0, 0, widthPx, editText.getLineHeight());
                cursorDrawable.getPaint().setColor(color);
                editText.setTextCursorDrawable(cursorDrawable);
            } else {
                Field editorField = TextView.class.getDeclaredField("mEditor");
                editorField.setAccessible(true);
                Object editor = editorField.get(editText);
                Field cursorField = editor.getClass().getDeclaredField("mCursorDrawable");
                cursorField.setAccessible(true);
                Object drawables = cursorField.get(editor);
                if (drawables instanceof android.graphics.drawable.Drawable[]) {
                    GradientDrawable drawable = new GradientDrawable();
                    drawable.setColor(color);
                    drawable.setSize(widthPx, editText.getLineHeight());
                    ((android.graphics.drawable.Drawable[]) drawables)[0] = drawable;
                    ((android.graphics.drawable.Drawable[]) drawables)[1] = drawable;
                }
            }
        } catch (Exception ignored) {}
    }

    public interface DownloadCallback {
        void onSave(String fileName);
    }
}
