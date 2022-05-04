package ru.liner.vr360client.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

import ru.liner.vr360client.R;
import ru.liner.vr360client.utils.ViewUtils;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 02.05.2022, понедельник
 **/
public class VrDialog extends BaseItem{
    public static class Builder{
        private VrDialog vrDialog;
        @DrawableRes
        private int iconRes = R.drawable.info_icon;
        private String title = "";
        private String text = "";
        private boolean indeterminate = true;
        private int progress = 0;

        public Builder(VrDialog vrDialog) {
            this.vrDialog = vrDialog;
            this.vrDialog.close();
        }

        public Builder setIconRes(@DrawableRes int iconRes) {
            this.iconRes = iconRes;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public Builder setIndeterminate(boolean indeterminate) {
            this.indeterminate = indeterminate;
            return this;
        }

        public Builder setProgress(int progress) {
            this.progress = progress;
            return this;
        }

        public void show(){
            vrDialog.show(iconRes, title, text, indeterminate, progress);
        }
        public void show(int closeAfter){
            vrDialog.post(() -> vrDialog.show(iconRes, title, text, indeterminate, progress));
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    vrDialog.post(() -> vrDialog.close());
                }
            }, closeAfter);
        }
    }


    private TextView dialogTitleL;
    private TextView dialogTitleR;
    private TextView dialogTextL;
    private TextView dialogTextR;
    private ProgressBar dialogProgressL;
    private ProgressBar dialogProgressR;

    public VrDialog(Context context) {
        this(context, null);
    }

    public VrDialog(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode())
            close();
    }

    @Override
    protected void onFindViewById() {
        dialogTitleL = findViewById(R.id.dialogTitleL);
        dialogTitleR= findViewById(R.id.dialogTitleR);
        dialogTextL= findViewById(R.id.dialogTextL);
        dialogTextR= findViewById(R.id.dialogTextR);
        dialogProgressL = findViewById(R.id.dialogProgressL);
        dialogProgressR = findViewById(R.id.dialogProgressR);
    }

    private void setTitle(String text){
        dialogTitleL.setText(text);
        dialogTitleR.setText(text);
    }
    private void setIcon(@DrawableRes int icon){
        dialogTitleL.setCompoundDrawablesWithIntrinsicBounds(0, icon, 0,0);
        dialogTitleR.setCompoundDrawablesWithIntrinsicBounds(0, icon, 0,0);
    }

    private void setText(String text){
        dialogTextL.setText(text);
        dialogTextR.setText(text);
    }

    public void setIndeterminate(boolean indeterminate){
        dialogProgressL.setVisibility(VISIBLE);
        dialogProgressR.setVisibility(VISIBLE);
        dialogProgressL.setIndeterminate(indeterminate);
        dialogProgressR.setIndeterminate(indeterminate);
    }

    public void setProgress(int progress){
        setIndeterminate(false);
        dialogProgressL.setProgress(progress);
        dialogProgressR.setProgress(progress);
    }

    public void show(@DrawableRes int icon, String title, String text, boolean indeterminate, int progress){
        setIcon(icon);
        setTitle(title);
        setText(text);
        if(indeterminate){
            setIndeterminate(true);
        } else {
            setProgress(progress);
        }
        setVisibility(VISIBLE);
    }

    public void close(){
        setVisibility(GONE);
    }

    @Override
    protected void onInflate() {
        inflater.inflate(R.layout.base_dialog, this);
    }
}
