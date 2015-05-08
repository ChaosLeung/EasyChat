package org.zhj.easychat.photo;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.zhj.easychat.ArrayRecyclerAdapter;
import org.zhj.easychat.R;
import org.zhj.easychat.app.BaseActionBarActivity;
import org.zhj.easychat.posts.OnItemClickListener;

import java.io.File;

/**
 * @author Chaos
 *         2015/05/07.
 */
public class PhotoActivity extends BaseActionBarActivity {

    private PhotoAdapter adapter;
    private RecyclerView photosView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        getSupportActionBar().setTitle(getString(R.string.select_photo));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        photosView = (RecyclerView) findViewById(R.id.recyclerView);
        photosView.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new PhotoAdapter();
        adapter.setOnItemClickListener(new OnItemClickListener<String>() {
            @Override
            public void onItemClick(String item) {
                Intent intent = new Intent("org.zhj.easychat.ACTION_SELECT_AVATAR_PATH");
                intent.putExtra("path", item);
                sendBroadcast(intent);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        ContentResolver contentResolver = getContentResolver();
//        String[] projection = {MediaStore.Images.Thumbnails.IMAGE_ID, MediaStore.Images.Thumbnails.DATA};
//        Cursor cursor = contentResolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, null, null, null);
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        getColumnData(cursor);
        cursor.close();

        photosView.setAdapter(adapter);
    }

    private void getColumnData(Cursor cursor) {
        if (cursor.moveToFirst()) {
//            int imageIdColumn = cursor.getColumnIndex(MediaStore.Images.Thumbnails.IMAGE_ID);
//            int dataColumn = cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);
            int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            do {
                String imagePath = cursor.getString(dataColumn);
                if (!TextUtils.isEmpty(imagePath)) {
                    adapter.add(imagePath);
                }
            } while (cursor.moveToNext());
        }
    }

    public class PhotoAdapter extends ArrayRecyclerAdapter<String, PhotoAdapter.ViewHolder> {

        private OnItemClickListener<String> onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener<String> listener) {
            this.onItemClickListener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.item_photo, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final String photoPath = get(position);
            Picasso.with(PhotoActivity.this)
                    .load(new File(photoPath))
                    .placeholder(R.drawable.default_avatar_blue)
                    .error(R.drawable.person_image_empty)
                    .resize(120, 120)
                    .centerCrop()
                    .into(holder.photo);

            holder.photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(photoPath);
                }
            });
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private ImageView photo;

            public ViewHolder(View itemView) {
                super(itemView);
                photo = (ImageView) itemView.findViewById(R.id.photo);
            }
        }
    }
}
