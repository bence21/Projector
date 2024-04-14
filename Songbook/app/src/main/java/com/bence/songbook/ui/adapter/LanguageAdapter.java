package com.bence.songbook.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bence.songbook.R;
import com.bence.songbook.models.Language;

import java.util.ArrayList;
import java.util.List;

public class LanguageAdapter extends ArrayAdapter<Language> {

    private final List<Language> languageList;
    private final LayoutInflater layoutInflater;

    public LanguageAdapter(Context context, int textViewResourceId,
                           List<Language> languageList, LayoutInflater layoutInflater) {
        super(context, textViewResourceId, languageList);
        this.layoutInflater = layoutInflater;
        this.languageList = new ArrayList<>();
        this.languageList.addAll(languageList);
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        LanguageAdapter.ViewHolder holder;
        Log.v("ConvertView", String.valueOf(position));

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.activity_language_checkbox_row, null);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(R.id.code);
            holder.checkBox = convertView.findViewById(R.id.checkBox1);
            convertView.setTag(holder);

            holder.textView.setOnClickListener(view -> {
                TextView textView = (TextView) view;
                CheckBox checkBox = (CheckBox) textView.getTag();
                checkBox.setChecked(!checkBox.isChecked());
                setSelection(checkBox);
            });
            holder.checkBox.setOnClickListener(view -> setSelection((CheckBox) view));
            RelativeLayout relativelayout = convertView.findViewById(R.id.layout);
            relativelayout.setOnClickListener(view -> {
                holder.checkBox.setChecked(!holder.checkBox.isChecked());
                setSelection(holder.checkBox);
            });
        } else {
            holder = (LanguageAdapter.ViewHolder) convertView.getTag();
        }

        Language language = languageList.get(position);
        holder.textView.setText(" - " + language.getNativeName() + getSizeString(language));
        holder.checkBox.setText(language.getEnglishName());
        holder.checkBox.setChecked(language.isSelected());
        holder.textView.setTag(holder.checkBox);
        holder.checkBox.setTag(language);

        return convertView;
    }

    private String getSizeString(Language language) {
        Long size = language.getSize();
        if (size == null || size < 0) {
            return "";
        }
        long almostEqualSize = getAlmostEqualSize(size);
        return " (~" + almostEqualSize + ")";
    }

    private long getAlmostEqualSize(long size) {
        if (size < 10) {
            return size;
        }
        int countDigits = getCountDigits(size);
        int resolution;
        if (countDigits < 3) {
            resolution = countDigits - 2;
        } else if (countDigits < 4) {
            resolution = countDigits - 1;
        } else {
            resolution = countDigits - 2;
        }
        return getResolutionNumber(size, resolution);
    }

    private long getResolutionNumber(long size, int resolution) {
        long x = (long) Math.pow(10, resolution);
        long a = size / x;
        return a * x;
    }

    private int getCountDigits(long size) {
        int countDigits = 0;
        while (size > 0) {
            ++countDigits;
            size /= 10;
        }
        return countDigits;
    }

    private void setSelection(CheckBox checkBox) {
        Language language = (Language) checkBox.getTag();
        language.setSelected(checkBox.isChecked());
    }

    public List<Language> getLanguageList() {
        return languageList;
    }

    private static class ViewHolder {
        TextView textView;
        CheckBox checkBox;
    }

}

