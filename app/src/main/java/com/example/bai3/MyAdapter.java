package com.example.bai3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{
    Context context;

    boolean temp = true;
    File[] filesAndFolders;
    public MyAdapter(Context context, File[] filesAndFolders){
        this.context = context;
        this.filesAndFolders = filesAndFolders;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        File selectionFile = filesAndFolders[position];

        ArrayList<AudioModel> fd = new ArrayList<>();
        for (File t: filesAndFolders){
            if (isMP3File(t)){
                fd.add(fromFile(t));
            }
        }

        holder.textView.setText(selectionFile.getName());

        if (selectionFile.isDirectory()){
            holder.imageView.setImageResource(R.drawable.baseline_folder_24);
        } else {
            holder.imageView.setImageResource(R.drawable.baseline_insert_drive_file_24);
        }

        if (isMP3File(selectionFile)) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(selectionFile.getPath());

            // Lấy ảnh album từ metadata
            byte[] albumArt = retriever.getEmbeddedPicture();
            Bitmap bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
            holder.imageView.setImageBitmap(bitmap);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isMP3File(selectionFile)){
//                    Toast.makeText(context.getApplicationContext(), "mp3!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context,MusicPlayerActivity.class);
                    MyMediaPlayer.getInstance().reset();
//                    MyMediaPlayer.currentIndex = position;
                    for (int i =0 ; i< fd.size(); i++) {

                        if (Objects.equals(fd.get(i).getTitle(), selectionFile.getName())) {
                            if (temp)
                                MyMediaPlayer.currentIndex = i;
                            else
                                MyMediaPlayer.currentIndex = --i;
                            break;
                        }
                    }
                    temp = false;
                    intent.putExtra("LIST",fd);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    return;
                } else {
//                    Toast.makeText(context.getApplicationContext(), "not mp3!", Toast.LENGTH_SHORT).show();
                }

                if (selectionFile.isDirectory()){
                    Intent intent = new Intent(context, MainActivity.class);
                    String path = selectionFile.getAbsolutePath();
                    intent.putExtra("path", path);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });

//        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//
//                PopupMenu popupMenu = new PopupMenu(context, view);
//                popupMenu.getMenu().add("DELETE");
//                popupMenu.getMenu().add("MOVE");
//                popupMenu.getMenu().add("RENAME");
//
//                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem menuItem) {
//                        if (menuItem.getTitle().equals("DELETE")){
//                            boolean delete = selectionFile.delete();
//                            if (delete){
//                                Toast.makeText(context.getApplicationContext(), "DELETE", Toast.LENGTH_SHORT).show();
//                                view.setVisibility(View.GONE);
//                            }
//                        }
//                        if (menuItem.getTitle().equals("MOVE")){
//
//                            Toast.makeText(context.getApplicationContext(), "MOVED", Toast.LENGTH_SHORT).show();
//                        }
//                        if (menuItem.getTitle().equals("RENAME")){
//
//                            Toast.makeText(context.getApplicationContext(), "RENAMED", Toast.LENGTH_SHORT).show();
//                        }
//                        return true;
//                    }
//                });
//                popupMenu.show();
//
//                return true;
//            }
//        });
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

    @Override
    public int getItemCount() {
        return filesAndFolders.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;
        public ViewHolder(View itemView){
            super(itemView);
            textView = itemView.findViewById(R.id.file_view);
            imageView = itemView.findViewById(R.id.icon_view);


        }
    }
}
