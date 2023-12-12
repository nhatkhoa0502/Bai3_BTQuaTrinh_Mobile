package com.example.bai3;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private Button btnGoUp, btnSelectAll, btnExit;

    String path = "";

    TextView currentTimeTv,totalTimeTv;
    SeekBar seekBar;
    ImageView pausePlay,nextBtn,previousBtn;
    RelativeLayout controls;
    ArrayList<AudioModel> songsList;
    AudioModel currentSong;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()){
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                    startActivityIfNeeded(intent, 101);
                }catch (Exception ex) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    startActivityIfNeeded(intent, 101);
                }
            }
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        TextView nofiletextView = findViewById(R.id.no_file_text_view);
        controls = findViewById(R.id.controls);

        path = Environment.getExternalStorageDirectory().getPath();
        if (getIntent().getStringExtra("path") != null)
            path = getIntent().getStringExtra("path");
        TextView textView = findViewById(R.id.pathView);
        textView.setText(path);

        btnGoUp = findViewById(R.id.btnGoUp);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        btnExit = findViewById(R.id.btnExit);
        btnGoUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (path.equals("/storage/emulated/0")){
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                String pathParent = getParentFolderPath(path);
                intent.putExtra("path", pathParent);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
        });

        File root =  new File(path);
        File[] filesAndFolders = root.listFiles();

        btnSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<AudioModel> fd = new ArrayList<>();
                for (File t: filesAndFolders){
                    if (isMP3File(t)){
                        fd.add(fromFile(t));
                    }
                }
                songsList = fd;
                if (fd.size() == 0){
                    Toast.makeText(getApplicationContext(), "Không tìm thấy file âm thanh nào!", Toast.LENGTH_SHORT).show();
                } else {
                    MyMediaPlayer.getInstance().reset();
                    MyMediaPlayer.currentIndex = 0;
                    startPlayMusic();
                }
            }
        });

        if (filesAndFolders == null || filesAndFolders.length == 0){
            nofiletextView.setVisibility(View.VISIBLE);
            return;
        }
        nofiletextView.setVisibility(View.INVISIBLE);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyAdapter(getApplicationContext(), filesAndFolders));

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Xử lý khi bài hát kết thúc
                playNextSong();
            }
        });


    }
    private void startPlayMusic(){
        currentTimeTv = findViewById(R.id.current_time);
        totalTimeTv = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        pausePlay = findViewById(R.id.pause_play);
        nextBtn = findViewById(R.id.next);
        previousBtn = findViewById(R.id.previous);

        controls.setVisibility(View.VISIBLE);

        setResourcesWithMusic();

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer!=null){
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    currentTimeTv.setText(convertToMMSS(mediaPlayer.getCurrentPosition()+""));

                    if(mediaPlayer.isPlaying()){
                        pausePlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                    }else{
                        pausePlay.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                    }

                }
                new Handler().postDelayed(this,100);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer!=null && fromUser){
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }


    private String getParentFolderPath(String currentPath) {
        File currentFolder = new File(currentPath);

        // Kiểm tra xem đường dẫn hiện tại có tồn tại không
        if (currentFolder.exists()) {
            File parentFolder = currentFolder.getParentFile();

            // Kiểm tra xem thư mục cha có tồn tại không
            if (parentFolder != null && parentFolder.exists()) {
                return parentFolder.getAbsolutePath();
            } else {
                Toast.makeText(this, "Parent folder does not exist.", Toast.LENGTH_SHORT).show();
                return null; // Trả về null nếu có lỗi
            }
        } else {
            Toast.makeText(this, "Current folder does not exist.", Toast.LENGTH_SHORT).show();
            return null; // Trả về null nếu có lỗi
        }
    }
    public boolean isMP3File(File file) {
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf(".");

        // Kiểm tra xem file có phần mở rộng không
        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
            String fileExtension = fileName.substring(lastDotIndex + 1).toLowerCase();

            // Kiểm tra xem phần mở rộng có phải là "mp3" không
            if (fileExtension.equals("mp3")) {
                return true;
            }
        }

        return false;
    }
    public AudioModel fromFile(File file) {
        // Lấy thông tin từ File và tạo đối tượng AudioModel
        String path = file.getAbsolutePath();
        String title = file.getName();
        String duration = getAudioDuration(file); // Lấy độ dài thực của file âm thanh

        return new AudioModel(path, title, duration);
    }

    private String getAudioDuration(File file) {
        String durationStr = "";
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(file.getAbsolutePath());
            durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return durationStr;
    }


    void setResourcesWithMusic(){
        currentSong = songsList.get(MyMediaPlayer.currentIndex);

        totalTimeTv.setText(convertToMMSS(currentSong.getDuration()));

        pausePlay.setOnClickListener(v-> pausePlay());
        nextBtn.setOnClickListener(v-> playNextSong());
        previousBtn.setOnClickListener(v-> playPreviousSong());

        playMusic();
    }


    private void playMusic(){

        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setProgress(0);
            seekBar.setMax(mediaPlayer.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void playNextSong(){

        if(MyMediaPlayer.currentIndex== songsList.size()-1)
            return;
        MyMediaPlayer.currentIndex +=1;
        mediaPlayer.reset();
        setResourcesWithMusic();

    }

    private void playPreviousSong(){
        if(MyMediaPlayer.currentIndex== 0)
            return;
        MyMediaPlayer.currentIndex -=1;
        mediaPlayer.reset();
        setResourcesWithMusic();
    }

    private void pausePlay(){
        if(mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else
            mediaPlayer.start();
    }


    public static String convertToMMSS(String duration){
        Long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }
}
