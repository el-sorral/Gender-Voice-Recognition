package edu.upc.eetac.bigdata.genderizer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import omrecorder.AudioChunk;
import omrecorder.AudioSource;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.Recorder;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.button)
    ImageButton recordButton;
    private Recorder recorder;
    private boolean isRecording = false;
//    @BindView(R.id.chronometer)
//    Chronometer chrono;

    private String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1058);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.filename = String.valueOf(System.currentTimeMillis()) + ".wav";
        this.createRecorder();
    }

    private void createRecorder() {
        this.recorder = OmRecorder.wav(
                new PullTransport.Default(mic(), new PullTransport.OnAudioChunkPulledListener() {
                    @Override
                    public void onAudioChunkPulled(AudioChunk audioChunk) {
                        animateVoice((float) (audioChunk.maxAmplitude() / 200.0));
                    }
                }), file());
    }

    private AudioSource mic() {
        return new AudioSource.Smart(MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                AudioFormat.CHANNEL_IN_MONO, 44100);
    }

    @NonNull
    private File file() {
        Log.v("Genderizer", filename);
        return new File(Environment.getExternalStorageDirectory(), this.filename);
    }

    private void animateVoice(final float maxPeak) {
        this.recordButton.animate().scaleX(1 + maxPeak).scaleY(1 + maxPeak).setDuration(5).start();
    }

    @OnClick(R.id.button)
    public void record() {
        try {
            if (this.isRecording) {
                this.handleStopRecording();
            } else {
                this.handleStartRecording();
            }
            this.isRecording = !this.isRecording;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleStartRecording() {
//        this.chrono.setBase(SystemClock.elapsedRealtime());
//        this.chrono.start();
        this.recorder.startRecording();
        this.recordButton.setImageDrawable(getDrawable(R.drawable.microphone_off));
    }

    private void handleStopRecording() throws IOException {
//        this.chrono.stop();
        this.recorder.stopRecording();
        this.recordButton.setImageDrawable(getDrawable(R.drawable.microphone));
        this.createRecorder();

        Intent intent = new Intent(this, PlayActivity.class);
        intent.putExtra("filename", this.filename);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1058: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    // Handle not granted
                }
                return;
            }
        }
    }
}
