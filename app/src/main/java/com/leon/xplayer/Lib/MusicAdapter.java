package com.leon.xplayer.Lib;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.leon.xplayer.R;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {
    final OnItemClickListener listener;

    List<MusicInfo> musicInfo;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public MusicAdapter(List<MusicInfo> musicInfo, OnItemClickListener listener) {
        this.musicInfo = musicInfo;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup group, int viewType) {
        View view = LayoutInflater.from(group.getContext())
                .inflate(R.layout.item_music_info, group, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MusicViewHolder holder, final int position) {
        holder.setInfo(musicInfo.get(position));
        holder.itemView.setOnClickListener(view -> {
            // Click Listener of each View
            Log.d("RecyclerView", "onClick: " + holder.getAdapterPosition());
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicInfo.size();
    }

    /*
     * ViewHolder
     * */

    public static class MusicViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvId, tvTitle, tvArtist, tvAlbum, tvTime;

        public MusicViewHolder(View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.music_num);
            tvTitle = itemView.findViewById(R.id.music_song);
            tvArtist = itemView.findViewById(R.id.music_singer);
            tvAlbum = itemView.findViewById(R.id.music_album);
            tvTime = itemView.findViewById(R.id.music_duration);
        }

        public void setInfo(MusicInfo musicInfo) {
            tvId.setText(musicInfo.id);
            tvTitle.setText(musicInfo.title);
            tvArtist.setText(musicInfo.artist);
            tvAlbum.setText(musicInfo.album);
            tvTime.setText(musicInfo.duration);
        }
    }
}
