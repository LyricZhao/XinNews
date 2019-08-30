package com.example.xinnews;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.annotation.NonNull;

import java.util.ArrayList;

public class SearchDialog extends Dialog {

    public SearchDialog(@NonNull Context context, ArrayList<String> history, View.OnClickListener confirmButtonLister, View.OnClickListener cancelButtonLister) {
        super(context);

        ListView mListView = findViewById(R.id.search_history_list);
        ArrayList<String> historyList = history;
        if (history.size() == 0) {
            historyList = new ArrayList<>();
            historyList.add("无历史记录");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, historyList);
        mListView.setAdapter(adapter);
        Button confirmButton = findViewById(R.id.search_confirm_button);
        confirmButton.setOnClickListener(confirmButtonLister);
        Button cancelButton = findViewById(R.id.search_cancel_button);
        cancelButton.setOnClickListener(cancelButtonLister);
    }
}
