package com.leon.xplayer.Lib;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class MusicInfo implements Parcelable {
    public final String id, title, artist, album, dataUri, duration;

    public MusicInfo(int id, String title, String artist, String album, String dataUri, int duration) {
        this.id = String.valueOf(id);
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.dataUri = dataUri;
        this.duration = new SimpleDateFormat("mm:ss", Locale.getDefault()).format((long) duration);
    }

    protected MusicInfo(Parcel in) {
        id = in.readString();
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        dataUri = in.readString();
        duration = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeString(dataUri);
        dest.writeString(duration);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MusicInfo> CREATOR = new Creator<MusicInfo>() {
        @Override
        public MusicInfo createFromParcel(Parcel in) {
            return new MusicInfo(in);
        }

        @Override
        public MusicInfo[] newArray(int size) {
            return new MusicInfo[size];
        }
    };

    @NonNull
    @Override
    public String toString() {

        return "MusicInfo{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", dataUri='" + dataUri + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MusicInfo musicInfo = (MusicInfo) obj;
        return id.equals(musicInfo.id)
                && title.equals(musicInfo.title)
                && artist.equals(musicInfo.artist)
                && album.equals(musicInfo.album)
                && dataUri.equals(musicInfo.dataUri)
                && duration.equals(musicInfo.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, artist, album, dataUri, duration);
    }
}
