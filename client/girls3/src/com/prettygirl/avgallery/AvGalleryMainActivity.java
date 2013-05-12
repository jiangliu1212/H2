package com.prettygirl.avgallery;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prettygirl.app.base.BaseActivity;
import com.prettygirl.avgallery.dialog.ExitDialog;
import com.prettygirl.avgallery.model.AVGirl;
import com.prettygirl.avgallery1.R;

public class AvGalleryMainActivity extends BaseActivity {

    public static final String EXT_KEY_GIRL = "ext_key_girl";

    private String mLang;

    private ArrayList<AVGirl> mGirls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.av_gallery_main_activity);

        mLang = AvApplication.getCurrentLang();
        mGirls = ((AvApplication) getApplicationContext()).getGirlList(mLang);

        GridView mGridView = (GridView) findViewById(R.id.av_gallery_grid_view);
        //        mGridView.setFastScrollEnabled(true);
        mGridView.setAdapter(new AVAdapter());
        mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(parent.getContext(), AvGalleryDetailActivity.class);
                intent.putExtra(EXT_KEY_GIRL, mGirls.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result = super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ExitDialog d = new ExitDialog(this);
            d.show();
        }
        return result;
    }

    class AVAdapter extends BaseAdapter implements SectionIndexer {

        private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        @Override
        public int getCount() {
            if (mGirls == null) {
                return 0;
            }
            return mGirls.size();
        }

        @Override
        public Object getItem(int position) {
            if (mGirls == null) {
                return null;
            }
            return mGirls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Context context = parent.getContext();
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.av_gallery_main_grid_item, null);
            }
            Object obj = convertView.getTag();
            ViewHolder cViewHolder = null;
            if (obj == null || !(obj instanceof ViewHolder)) {
                cViewHolder = new ViewHolder(convertView);
                convertView.setTag(cViewHolder);
            } else {
                cViewHolder = (ViewHolder) obj;
            }
            AVGirl girl = mGirls.get(position);
            ImageLoader.getInstance().cancelDisplayTask(cViewHolder.imageView);
            ImageLoader.getInstance().displayImage("assets://icons/" + girl.id + ".jpg", cViewHolder.imageView);
            cViewHolder.mNameView.setText(girl.name);
            cViewHolder.mSubView.setText(girl.subTilte);
            return convertView;
        }

        class ViewHolder {
            ImageView imageView;
            TextView mNameView;
            TextView mSubView;

            ViewHolder(View view) {
                this.imageView = (ImageView) view.findViewById(R.id.av_gallery_av_image);
                this.mNameView = (TextView) view.findViewById(R.id.av_gallery_av_name);
                this.mSubView = (TextView) view.findViewById(R.id.av_gallery_av_sub_title);
            }
        }

        @Override
        public Object[] getSections() {
            String[] sections = new String[mSections.length()];
            for (int i = 0; i < mSections.length(); i++)
                sections[i] = String.valueOf(mSections.charAt(i));
            return sections;
        }

        @Override
        public int getPositionForSection(int section) {
            return 0;
        }

        @Override
        public int getSectionForPosition(int position) {
            return 0;
        }

    }

}