package com.bence.songbook.ui.fragment;

import android.content.Intent;
import android.text.Html;
import android.widget.TextView;

import com.bence.songbook.Memory;
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.ui.activity.SongActivity;

import static com.bence.songbook.ui.utils.StringUtils.stripAccents;

public class MainSongFragment extends BaseSongFragment {
    private final Memory memory = Memory.getInstance();

    @Override
    protected void onSongVerseClick(int position) {
        final Intent intent = new Intent(getActivity(), SongActivity.class);
        Memory.getInstance().setPassingSong(song);
        intent.putExtra("verseIndex", 0);
        startActivityForResult(intent, 3);
    }

    private String getColorizedStringByLastSearchedText(String text) {
        StringBuilder s = new StringBuilder();
        String lastSearchedInText = memory.getLastSearchedInText();
        if (lastSearchedInText == null) {
            return text;
        }
        char[] lastSearch = stripAccents(lastSearchedInText.toLowerCase()).toCharArray();
        if (lastSearch.length == 0) {
            return text;
        }
        int matchCount = 0;
        char[] chars = text.toCharArray();
        StringBuilder tmp = new StringBuilder();
        int whitespaceCount = 0;
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            String s1 = stripAccents((c + "").toLowerCase());
            if (!s1.isEmpty()) {
                if (s1.charAt(0) == lastSearch[matchCount]) {
                    if (matchCount == 0) {
                        whitespaceCount = 0;
                    }
                    ++matchCount;
                    if (matchCount == lastSearch.length) {
                        matchCount = 0;
                        s.append("<color=\"0xFFC600FF\">").append(tmp).append(c).append("</color>");
                        tmp = new StringBuilder();
                        continue;
                    }
                } else {
                    if (matchCount > 0) {
                        i -= matchCount + whitespaceCount;
                        s.append(chars[i]);
                        matchCount = 0;
                        tmp = new StringBuilder();
                        continue;
                    }
                }
            } else {
                ++whitespaceCount;
            }
            if (matchCount == 0) {
                s.append(c);
            } else {
                tmp.append(c);
            }
        }
        return s.append(tmp).toString();
    }

    @Override
    protected void setText(SongVerse songVerse, TextView textView) {
        String text = songVerse.getText();
        text = getColorizedStringByLastSearchedText(text);
        String s = text.replaceAll("<color=\"0x(.{0,6})..\">", "<font color='0x$1'>")
                .replaceAll("</color>", "</font>")
                .replaceAll("\\[", "<i>")
                .replaceAll("]", "</i>")
                .replaceAll("\n", "<br>");
        textView.setText(Html.fromHtml(s), TextView.BufferType.SPANNABLE);
    }
}
