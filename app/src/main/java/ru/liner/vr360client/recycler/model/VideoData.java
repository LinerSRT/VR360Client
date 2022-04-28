package ru.liner.vr360client.recycler.model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.List;

import ru.liner.vr360client.utils.InputStreams;
import ru.liner.vr360client.utils.ListTypeToken;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 22.04.2022, пятница
 **/
public class VideoData {
    @Expose
    @SerializedName("hash")
    public String videoHash;
    @Expose
    @SerializedName("name")
    public String videoName;
    @Expose
    @SerializedName("durationInSeconds")
    public long durationSeconds;
    public long sizeBytes;
    public String videoPath;
    public boolean isPlaying;

    public String downloadLink(){
        return getDownloadLink(this);
    }

    public static String getDownloadLink(VideoData videoData) {
        return String.format("https://vr360.simplex-software.ru/getfilm/?hash=%s", videoData.videoHash);
    }

    @Nullable
    public static List<VideoData> getData(@NonNull Context context) {
        try {
            return new Gson().fromJson(InputStreams.toString(InputStreams.get(context, "video.json", false)), new ListTypeToken<>(VideoData.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "VideoData{" +
                "videoHash='" + videoHash + '\'' +
                ", videoName='" + videoName + '\'' +
                ", durationSeconds=" + durationSeconds +
                ", sizeBytes=" + sizeBytes +
                ", videoPath='" + videoPath + '\'' +
                ", videoURL='" + downloadLink() + '\'' +
                ", isPlaying=" + isPlaying +
                '}';
    }
}
