package com.example.vidbregar.bluepodcast.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.example.vidbregar.bluepodcast.BuildConfig;
import com.example.vidbregar.bluepodcast.model.data.Channel;
import com.example.vidbregar.bluepodcast.model.data.PodcastGenre;
import com.example.vidbregar.bluepodcast.model.network.PodcastService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PodcastViewModel extends ViewModel {

    private static final int COMEDY_GENRE_ID = 133;
    private static final int BUSINESS_GENRE_ID = 93;
    private static final int HEALTH_GENRE_ID = 88;

    private PodcastService podcastService;
    private MutableLiveData<List<Channel>> bestPodcastsLiveData;
    private MutableLiveData<List<Channel>> comedyPodcastsLiveData;
    private MutableLiveData<List<Channel>> businessPodcastsLiveData;
    private MutableLiveData<List<Channel>> healthPodcastsLiveData;

    public PodcastViewModel(PodcastService podcastService) {
        this.podcastService = podcastService;
    }

    public MutableLiveData<List<Channel>> getBestPodcastsLiveData() {
        if (bestPodcastsLiveData == null) {
            bestPodcastsLiveData = new MutableLiveData<>();
            getBestPodcasts();
        }
        return bestPodcastsLiveData;
    }

    public MutableLiveData<List<Channel>> getComedyPodcastsLiveData() {
        if (comedyPodcastsLiveData == null) {
            comedyPodcastsLiveData = new MutableLiveData<>();
            getComedyPodcasts();
        }
        return comedyPodcastsLiveData;
    }

    public MutableLiveData<List<Channel>> getBusinessPodcastsLiveData() {
        if (businessPodcastsLiveData == null) {
            businessPodcastsLiveData = new MutableLiveData<>();
            getBusinessPodcasts();
        }
        return businessPodcastsLiveData;
    }

    public MutableLiveData<List<Channel>> getHealthPodcastsLiveData() {
        if (healthPodcastsLiveData == null) {
            healthPodcastsLiveData = new MutableLiveData<>();
            getHealthPodcasts();
        }
        return healthPodcastsLiveData;
    }

    private void getBestPodcasts() {
        Call<PodcastGenre> bestPodcastsCall = podcastService.getBestPodcasts(BuildConfig.API_KEY);
        bestPodcastsCall.enqueue(new Callback<PodcastGenre>() {
            @Override
            public void onResponse(Call<PodcastGenre> call, Response<PodcastGenre> response) {
                if (response.isSuccessful()) {
                    Log.e("SUCCESS", response.body().getName());
                    bestPodcastsLiveData.setValue(response.body().getChannels());
                }
            }

            @Override
            public void onFailure(Call<PodcastGenre> call, Throwable t) {
                Log.e("ERROR", t.getMessage());
            }
        });
    }

    private void getComedyPodcasts() {
        Call<PodcastGenre> comedyPodcastsCall = podcastService.getGenrePodcasts(BuildConfig.API_KEY, COMEDY_GENRE_ID);
        comedyPodcastsCall.enqueue(new Callback<PodcastGenre>() {
            @Override
            public void onResponse(Call<PodcastGenre> call, Response<PodcastGenre> response) {
                if (response.isSuccessful()) {
                    Log.e("SUCCESS", response.body().getName());
                    comedyPodcastsLiveData.setValue(response.body().getChannels());
                }
            }

            @Override
            public void onFailure(Call<PodcastGenre> call, Throwable t) {
                Log.e("ERROR", t.getMessage());
            }
        });
    }

    private void getBusinessPodcasts() {
        Call<PodcastGenre> comedyPodcastsCall = podcastService.getGenrePodcasts(BuildConfig.API_KEY, BUSINESS_GENRE_ID);
        comedyPodcastsCall.enqueue(new Callback<PodcastGenre>() {
            @Override
            public void onResponse(Call<PodcastGenre> call, Response<PodcastGenre> response) {
                if (response.isSuccessful()) {
                    Log.e("SUCCESS", response.body().getName());
                    businessPodcastsLiveData.setValue(response.body().getChannels());
                }
            }

            @Override
            public void onFailure(Call<PodcastGenre> call, Throwable t) {
                Log.e("ERROR", t.getMessage());
            }
        });
    }

    private void getHealthPodcasts() {
        Call<PodcastGenre> comedyPodcastsCall = podcastService.getGenrePodcasts(BuildConfig.API_KEY, HEALTH_GENRE_ID);
        comedyPodcastsCall.enqueue(new Callback<PodcastGenre>() {
            @Override
            public void onResponse(Call<PodcastGenre> call, Response<PodcastGenre> response) {
                if (response.isSuccessful()) {
                    Log.e("SUCCESS", response.body().getName());
                    healthPodcastsLiveData.setValue(response.body().getChannels());
                }
            }

            @Override
            public void onFailure(Call<PodcastGenre> call, Throwable t) {
                Log.e("ERROR", t.getMessage());
            }
        });
    }
}
