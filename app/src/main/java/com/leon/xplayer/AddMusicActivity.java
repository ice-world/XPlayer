package com.leon.xplayer;

import static com.leon.xplayer.Lib.config.ADD_MUSIC_EXTRA_ALL;
import static com.leon.xplayer.Lib.config.ADD_MUSIC_EXTRA_FAVORITE;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.leon.xplayer.Lib.MusicAdapter;
import com.leon.xplayer.Lib.MusicInfo;

import java.util.ArrayList;

public class AddMusicActivity extends AppCompatActivity {

    ArrayList<MusicInfo> musicInfoList;
    ArrayList<MusicInfo> favoriteList;
    RecyclerView list_add_musics;
    Button bt_confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_music);

        BindWidgets();
        CreateInstances();
        SetWidgets();
    }

    void BindWidgets() {
        list_add_musics = findViewById(R.id.list_add_musics);
        bt_confirm = findViewById(R.id.addMusicA_bt_confirm);
    }

    void SetWidgets() {
        list_add_musics.setAdapter(new MusicAdapter(
                musicInfoList,
                position -> {
                    MusicInfo info = musicInfoList.get(position);
                    if (favoriteList.contains(info))
                        favoriteList.remove(info);
                    else
                        favoriteList.add(info);
                }
        ));

        list_add_musics.setLayoutManager(new LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false)
        );

        bt_confirm.setOnClickListener(view -> {
            setResult(
                    Activity.RESULT_OK,
                    new Intent() {{
                        putExtra(ADD_MUSIC_EXTRA_FAVORITE, favoriteList);
                    }}
            );
            finish();
        });
    }

    void CreateInstances() {
        musicInfoList = getIntent().getParcelableArrayListExtra(ADD_MUSIC_EXTRA_ALL);
        favoriteList = new ArrayList<>();
    }

}