package com.example.vidbregar.bluepodcast.model.data;

import com.google.gson.annotations.SerializedName;

public class Episode {

    @SerializedName("title")
    public String title;
    @SerializedName("pub_date_ms")
    public int publicationDateMiliseconds;
    @SerializedName("description")
    public String description;
    @SerializedName("audio_length")
    public int audioLength;
    @SerializedName("id")
    public String id;
    @SerializedName("audio")
    public String audioUrl;

    public Episode(String title, int publicationDateMiliseconds, String description,
                   int audioLength, String id, String audioUrl) {
        this.title = title;
        this.publicationDateMiliseconds = publicationDateMiliseconds;
        this.description = description;
        this.audioLength = audioLength;
        this.id = id;
        this.audioUrl = audioUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPublicationDateMiliseconds() {
        return publicationDateMiliseconds;
    }

    public void setPublicationDateMiliseconds(int publicationDateMiliseconds) {
        this.publicationDateMiliseconds = publicationDateMiliseconds;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAudioLength() {
        return audioLength;
    }

    public void setAudioLength(int audioLength) {
        this.audioLength = audioLength;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
}
