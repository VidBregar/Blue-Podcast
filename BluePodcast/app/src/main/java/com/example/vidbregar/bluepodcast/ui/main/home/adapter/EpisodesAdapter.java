package com.example.vidbregar.bluepodcast.ui.main.home.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vidbregar.bluepodcast.R;
import com.example.vidbregar.bluepodcast.model.data.Episode;
import com.example.vidbregar.bluepodcast.ui.main.home.listener.EpisodeClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EpisodesAdapter extends RecyclerView.Adapter<EpisodesAdapter.ViewHolder> {

    private List<Episode> episodes;
    private EpisodeClickListener episodeClickListener;

    public EpisodesAdapter(EpisodeClickListener episodeClickListener) {
        this.episodeClickListener = episodeClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.episode_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Episode episode = episodes.get(position);
        viewHolder.episodeTitleTextView.setText(episode.getTitle());
        viewHolder.episodeDurationTextView.setText(secondsToMinutes(episode.getAudioLength()));
    }

    private String secondsToMinutes(int seconds) {
        int minutes = Math.round(seconds / 60);
        return String.valueOf(minutes) + "m";
    }

    @Override
    public int getItemCount() {
        if (episodes == null) return 0;
        else return episodes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.podcast_episode_title)
        TextView episodeTitleTextView;
        @BindView(R.id.podcast_episode_duration)
        TextView episodeDurationTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            episodeClickListener.onEpisodeClickListener(episodes.get(getAdapterPosition()));
        }
    }

    public void swapEpisodes(List<Episode> episodes) {
        this.episodes = episodes;
        notifyDataSetChanged();
    }
}
