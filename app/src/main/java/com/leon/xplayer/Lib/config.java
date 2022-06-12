package com.leon.xplayer.Lib;

public class config {
    // MusicService
    public static final String MUSIC_SERVICE_MUSIC_FINISHED = "MusicService::Music_Finished";

    // MusicInfoCatcher
    public final static int CATCHER_ASKING = 0;
    public final static int CATCHER_RESPONSE = 1;

    // LoginActivity
    public final static String LOGIN_EXTRA_USERNAME = "username";
    public final static String LOGIN_EXTRA_PASSWORD = "password";

    // AddMusicActivity
    public final static String ADD_MUSIC_EXTRA_FAVORITE = "favorite";
    public final static String ADD_MUSIC_EXTRA_ALL = "all";

    // Types
    public enum ModeEnum {
        order,
        random,
        single
    }
}
