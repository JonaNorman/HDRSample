package com.jonanorman.android.hdrsample;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.jonanorman.android.hdrsample.util.AppUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Class> classList = new ArrayList<>();
    private List<String> labelList = new ArrayList<>();

    public MainActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            PackageInfo packageInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
            for (ActivityInfo activity : packageInfo.activities) {
                if (activity.metaData != null && activity.metaData.containsKey("example")) {
                    String label = (String) activity.loadLabel(getPackageManager());
                    try {
                        classList.add(Class.forName(activity.name));
                        labelList.add(label);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        initListView();
    }

    private void initListView() {
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(new ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                labelList));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                startActivity(new Intent(MainActivity.this, classList.get(position)));
            }
        });
    }
}