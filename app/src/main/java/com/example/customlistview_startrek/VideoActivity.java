package com.example.customlistview_startrek;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class VideoActivity extends AppCompatActivity {

    VideoView videoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video);

        videoView=(VideoView)findViewById(R.id.videoView);
        //specify the location of media file
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.wrath_of_khan));

        // create an object of media controller
        MediaController mediaController = new MediaController(this);
        // set media controller object for a video view
        videoView.setMediaController(mediaController);
        videoView.start();
    }

    public void goBack(View view){
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
    }
}
