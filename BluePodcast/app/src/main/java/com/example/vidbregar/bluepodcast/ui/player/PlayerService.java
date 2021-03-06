package com.example.vidbregar.bluepodcast.ui.player;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import com.example.vidbregar.bluepodcast.model.database.episode.EpisodeEntity;
import com.example.vidbregar.bluepodcast.util.NotificationUtil;
import com.example.vidbregar.bluepodcast.util.SharedPreferencesUtil;
import com.example.vidbregar.bluepodcast.widget.BluePodcastWidget;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import static com.example.vidbregar.bluepodcast.ui.player.PlayerConstants.*;

public class PlayerService extends Service implements AudioManager.OnAudioFocusChangeListener, Player.EventListener {

    public SimpleExoPlayer simpleExoPlayer;
    private String audioUrl;
    private EpisodeEntity episode;
    private String playerStatus;
    private AudioManager audioManager;
    private NotificationUtil notificationUtil;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;
    private SharedPreferencesUtil sharedPreferencesUtil;
    private final IBinder playerBinder = new PlayerBinder();

    public class PlayerBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    private MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPause() {
            super.onPause();
            pause();
        }

        @Override
        public void onStop() {
            super.onStop();
            stop();
            notificationUtil.cancelNotification();
        }

        @Override
        public void onPlay() {
            super.onPlay();
            play();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        notificationUtil = new NotificationUtil(this);

        mediaSession = new MediaSessionCompat(this, getClass().getSimpleName());
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(mediaSessionCallback);

        sharedPreferencesUtil = new SharedPreferencesUtil(getApplicationContext());

        RenderersFactory renderersFactory = new DefaultRenderersFactory(getApplicationContext());
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
        simpleExoPlayer.addListener(this);

        playerStatus = IDLE;
    }

    public void playOrPause(String url) {
        if (audioUrl != null && audioUrl.equals(url)) {
            play();
        } else {
            initializePlayer(url);
        }
    }

    public void setEpisode(EpisodeEntity episode) {
        this.episode = episode;
    }

    public void initializePlayer(String audioUrl) {
        this.audioUrl = audioUrl;

        MediaSource mediaSource = buildMediaSource(Uri.parse(audioUrl));
        simpleExoPlayer.prepare(mediaSource);
        simpleExoPlayer.setPlayWhenReady(true);
    }

    private MediaSource buildMediaSource(Uri audioUrl) {
        final String userAgent = Util.getUserAgent(getApplicationContext(), "Blue Podcast");
        return new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory(userAgent))
                .createMediaSource(audioUrl);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            // Service has been was destroyed -> null intent is returned
            return START_STICKY;
        }

        String action = intent.getAction();

        if (TextUtils.isEmpty(action))
            return START_NOT_STICKY;

        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            stop();
            return START_NOT_STICKY;
        }

        if (action.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();

        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {

            if (playerStatus.equals(STOPPED)) {
                transportControls.stop();
            } else {
                transportControls.pause();
            }

        } else if (action.equalsIgnoreCase(ACTION_STOP)) {
            if (sharedPreferencesUtil.isApplicationAlive()) {
                pause();
            } else {
                stopForeground(true);
                stopSelf();
                widgetNoEpisodePlaying();
            }
            notificationUtil.cancelNotification();
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return playerBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (playerStatus.equals(IDLE)) stopSelf();
        return super.onUnbind(intent);

    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                simpleExoPlayer.setVolume(1f);
                resume();
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                stop();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (isPlaying())
                    pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (isPlaying())
                    simpleExoPlayer.setVolume(0.1f);
                break;
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_BUFFERING:
                playerStatus = LOADING;
                break;
            case Player.STATE_ENDED:
                playerStatus = STOPPED;
                break;
            case Player.STATE_IDLE:
                playerStatus = IDLE;
                widgetNoEpisodePlaying();
                break;
            case Player.STATE_READY:
                playerStatus = playWhenReady ? PLAYING : PAUSED;
                if (playWhenReady) {
                    widgetPlay();
                } else {
                    widgetPause();
                }
                break;
            default:
                playerStatus = IDLE;
                break;
        }

        if (!playerStatus.equals(IDLE))
            notificationUtil.startNotification(playerStatus, episode);
    }

    public String getPlayerStatus() {
        return playerStatus;
    }

    public void play() {
        simpleExoPlayer.setPlayWhenReady(true);
    }

    public void pause() {
        simpleExoPlayer.setPlayWhenReady(false);
        audioManager.abandonAudioFocus(this);
    }

    public void resume() {
        if (audioUrl != null)
            play();
    }

    public void stop() {
        simpleExoPlayer.stop();
        audioManager.abandonAudioFocus(this);
    }

    private void widgetPause() {
        Intent intent = new Intent(this, BluePodcastWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), BluePodcastWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        intent.putExtra(BluePodcastWidget.WIDGET_EPISODE_TITLE_INTENT_EXTRA, episode.getEpisodeTitle());
        intent.putExtra(BluePodcastWidget.WIDGET_THUMBNAIL_URL_INTENT_EXTRA, episode.getThumbnailUrl());
        intent.putExtra(BluePodcastWidget.WIDGET_IS_PLAYING_INTENT_EXTRA, false);
        sendBroadcast(intent);
    }

    private void widgetPlay() {
        Intent intent = new Intent(this, BluePodcastWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), BluePodcastWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        intent.putExtra(BluePodcastWidget.WIDGET_EPISODE_TITLE_INTENT_EXTRA, episode.getEpisodeTitle());
        intent.putExtra(BluePodcastWidget.WIDGET_THUMBNAIL_URL_INTENT_EXTRA, episode.getThumbnailUrl());
        intent.putExtra(BluePodcastWidget.WIDGET_IS_PLAYING_INTENT_EXTRA, true);
        sendBroadcast(intent);
    }

    private void widgetNoEpisodePlaying() {
        Intent intent = new Intent(this, BluePodcastWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), BluePodcastWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        intent.putExtra(BluePodcastWidget.WIDGET_NO_EPISODE_PLAYING_INTENT_EXTRA, "");
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        pause();
        simpleExoPlayer.release();
        simpleExoPlayer.removeListener(this);
        mediaSession.release();
        notificationUtil.cancelNotification();
        super.onDestroy();
    }

    public MediaSessionCompat getMediaSession() {
        return mediaSession;
    }

    public boolean isPlaying() {
        return playerStatus.equals(PLAYING);
    }

    // @formatter:off
    @Override
    public void onPlayerError(ExoPlaybackException error) { }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) { }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) { }

    @Override
    public void onLoadingChanged(boolean isLoading) { }

    @Override
    public void onRepeatModeChanged(int repeatMode) { }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) { }

    @Override
    public void onPositionDiscontinuity(int reason) { }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) { }

    @Override
    public void onSeekProcessed() { }
    // @formatter:on
}