package com.daasuu.exoplayerfilter;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.daasuu.epf.EPlayerView;
import com.daasuu.epf.filter.GlFilter;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EPlayerView ePlayerView;
    private ExoPlayer player;
    private Button button;
    private SeekBar seekBar;
    private PlayerTimer playerTimer;
    private Button rotateButton;

    private int rotate = 0;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
        registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                MediaItem item = MediaItem.fromUri(uri);
                setMediaItem(item);
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpSimpleExoPlayer();
        if (player.getMediaItemCount() == 0) {
            setMediaItem();
        }
        setUoGlPlayerView();
        setUpTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
        if (playerTimer != null) {
            playerTimer.stop();
            playerTimer.removeMessages(0);
        }
    }

    private void setUpViews() {
        // play pause
        button = (Button) findViewById(R.id.btn);
        button.setOnClickListener(v -> {
            if (player == null) return;

            if (button.getText().toString().equals(MainActivity.this.getString(R.string.pause))) {
                player.setPlayWhenReady(false);
                button.setText(R.string.play);
            } else {
                player.setPlayWhenReady(true);
                button.setText(R.string.pause);
            }
        });

        Button loadVideoButton = (Button) findViewById(R.id.btn_select_video);
        loadVideoButton.setOnClickListener(v -> {
            pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                    .build()
            );
        });

        // rotate
        rotateButton = (Button) findViewById(R.id.btn_rotate);
        rotateButton.setText("Rotated 0°");
        rotateButton.setOnClickListener(v -> {
            this.rotate += 90;
            if (this.rotate >= 360) {
                this.rotate = 0;
            }
            rotateButton.setText(String.format(Locale.getDefault(), "Rotated %d°", this.rotate));
            this.ePlayerView.setRotation(this.rotate);
        });

        // seek
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (player == null) return;

                if (!fromUser) {
                    // We're not interested in programmatically generated changes to
                    // the progress bar's position.
                    return;
                }

                player.seekTo(progress * 1000L);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // do nothing
            }
        });

        // list
        ListView listView = (ListView) findViewById(R.id.list);
        final List<FilterType> filterTypes = FilterType.createFilterList();
        listView.setAdapter(new FilterAdapter(this, R.layout.row_text, filterTypes));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            GlFilter filter = FilterType.createGlFilter(filterTypes.get(position), getApplicationContext());
            ePlayerView.setGlFilter(filter);
        });
    }

    private void setUpSimpleExoPlayer() {
        if (player != null) return;

        // SimpleExoPlayer
        player = new ExoPlayer.Builder(this).build();
    }

    private void setMediaItem(MediaItem mediaItem) {
        if (player == null) {
            setUpSimpleExoPlayer();
        }

        player.clearMediaItems();
        player.addMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    private void setMediaItem() {
        setMediaItem(MediaItem.fromUri(Constant.STREAM_URL_MP4_VOD_SHORT));
    }

    private void setUoGlPlayerView() {
        ePlayerView = new EPlayerView(this);
        ePlayerView.setExoPlayer(player);
        ePlayerView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((MovieWrapperView) findViewById(R.id.layout_movie_wrapper)).addView(ePlayerView);
        ePlayerView.onResume();
    }

    private void setUpTimer() {
        playerTimer = new PlayerTimer();
        playerTimer.setCallback(timeMillis -> {
            long position = player.getCurrentPosition();
            long duration = player.getDuration();

            if (duration <= 0) return;

            seekBar.setMax((int) duration / 1000);
            seekBar.setProgress((int) position / 1000);
        });
        playerTimer.start();
    }

    private void releasePlayer() {
        ePlayerView.onPause();
        ((MovieWrapperView) findViewById(R.id.layout_movie_wrapper)).removeAllViews();
        ePlayerView = null;
        player.stop();
        player.release();
        player = null;
    }
}