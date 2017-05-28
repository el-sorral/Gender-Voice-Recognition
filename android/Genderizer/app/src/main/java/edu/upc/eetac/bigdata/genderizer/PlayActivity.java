package edu.upc.eetac.bigdata.genderizer;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlayActivity extends AppCompatActivity {

//    @BindView(R.id.play_button)
//    ImageButton playButton;
//    @BindView(R.id.upload_button)
//    Button uploadButton;
//    private boolean isPlaying = false;
//    private MediaPlayer mediaPlayer;

    @BindView(R.id.image_female)
    ImageView femaleImage;

    @BindView(R.id.image_male)
    ImageView maleImage;

    @BindView(R.id.progressText)
    TextView progressText;

    @BindView(R.id.share_results)
    TextView shareTextView;

    @BindView(R.id.progress)
    ProgressBar progressBar;

    private Gender gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        ButterKnife.bind(this);
        this.upload();
    }

    @NonNull
    private File file() {
        return new File(Environment.getExternalStorageDirectory(), getIntent().getStringExtra("filename"));
    }

    public void upload() {
        new NetworkService().upload(file(), new NetworkResult() {
            @Override
            public void onSuccess(final Gender gender) {

                PlayActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(PlayActivity.this, gender.toString(), Toast.LENGTH_LONG).show();

                        PlayActivity.this.gender = gender;
                        PlayActivity.this.progressBar.setVisibility(View.GONE);
                        PlayActivity.this.progressText.setText("You should be...");
                        PlayActivity.this.shareTextView.setVisibility(View.VISIBLE);

                        switch (gender) {
                            case FEMALE:
                                PlayActivity.this.femaleImage.setVisibility(View.VISIBLE);
                                PlayActivity.this.maleImage.setVisibility(View.GONE);
                                break;
                            case MALE:
                                PlayActivity.this.femaleImage.setVisibility(View.GONE);
                                PlayActivity.this.maleImage.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                });
            }

            @Override
            public void onError() {
                PlayActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PlayActivity.this, "Ups... error", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @OnClick(R.id.share_results)
    public void shareResults() {
        new AlertDialog.Builder(this)
                .setMessage("Are you " + PlayActivity.this.gender.toString().toLowerCase() + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PlayActivity.this.setCorrectGender(true);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PlayActivity.this.setCorrectGender(false);
                    }
                })
                .create().show();
    }

    private void setCorrectGender(boolean isCorrect) {
        Gender g = this.gender;
        if (!isCorrect && (this.gender == Gender.MALE)) {
            g = Gender.FEMALE;
        } else if (!isCorrect && (this.gender == Gender.FEMALE)) {
            g = Gender.MALE;
        }
        this.sendFileNameAndGenderToServer(g);
    }

    private void sendFileNameAndGenderToServer(Gender gender) {
        new NetworkService().sendFilenameAndGender(file(), gender, new NetworkResult() {
            @Override
            public void onSuccess(Gender gender) {
                PlayActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PlayActivity.this.showThanksToast();
                    }
                });
            }

            @Override
            public void onError() {
                PlayActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PlayActivity.this.showThanksToast();
                    }
                });
            }
        });
    }

    private void showThanksToast() {
        Toast.makeText(PlayActivity.this, "Thanks!", Toast.LENGTH_LONG).show();
    }
}
