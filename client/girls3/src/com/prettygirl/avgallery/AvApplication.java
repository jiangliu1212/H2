package com.prettygirl.avgallery;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.StringTokenizer;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.memory.impl.FIFOLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.prettygirl.avgallery.model.AVGirl;
import com.prettygirl.avgallery.util.AlbumCacheFileNameGenerator;
import com.prettygirl.avgallery.util.MUnlimitedDiscCache;
import com.prettygirl.avgallery.util.StorageUtils;

public class AvApplication extends Application {

    private static final String DIR_AV_GIRL_INFO = "langs";

    private static final String KEY_LANG = "KEY_LANGUAGE";

    public static final String VALUE_LANG_SIMPLE = "zh-CN";

    public static final String VALUE_LANG_TW = "zh-TW";

    private static SharedPreferences mSharedPreferences;

    public static String getCurrentLang() {
        return mSharedPreferences.getString(KEY_LANG, VALUE_LANG_SIMPLE);
    }

    public static void setCurrentLang(String value) {
        Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_LANG, value);
        editor.commit();
    }

    private final HashMap<String, ArrayList<AVGirl>> avGirlMap = new HashMap<String, ArrayList<AVGirl>>();

    private static AlbumCacheFileNameGenerator cAlbumCacheFileNameGenerator;

    private void updateBookList() throws IOException {
        String[] names = getAssets().list(DIR_AV_GIRL_INFO);
        ArrayList<AVGirl> girls = null;
        for (String file : names) {
            girls = new ArrayList<AVGirl>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(
                    String.format("%s%s%s%sinfo", DIR_AV_GIRL_INFO, File.separator, file, File.separator))));
            while (true) {
                String s = reader.readLine();
                if (s == null) {
                    break;
                }
                s = s.trim();
                if (s.length() == 0) {
                    continue;
                }
                StringTokenizer tokens = new StringTokenizer(s, "@@@");

                String path = tokens.nextToken().trim();
                String name = tokens.nextToken().trim();
                String sortKey = tokens.nextToken().trim();
                AVGirl girl = new AVGirl(name, Integer.valueOf(sortKey), tokens.nextToken().trim(), path);
                girls.add(girl);
            }
            Collections.sort(girls, new Comparator<AVGirl>() {

                @Override
                public int compare(AVGirl lhs, AVGirl rhs) {
                    return rhs.compareTo(lhs);
                }
            });
            avGirlMap.put(file, girls);
        }
    }

    public ArrayList<AVGirl> getGirlList(String lang) {
        return avGirlMap.get(lang);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        try {
            updateBookList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final File cacheDir = new File(StorageUtils.getImageCacheDir());
        if (cacheDir.exists() == false || cacheDir.isDirectory() == false) {
            cacheDir.mkdir();
        }
        MemoryInfo mi = new MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long halfOfAvailableMegs = mi.availMem / 2;
        int inMemoryCacheSize;
        if (halfOfAvailableMegs > 100 * 1024) {
            inMemoryCacheSize = 10 * 1024 * 1024;
        } else {
            inMemoryCacheSize = Math.min(5 * 1024 * 1024, ((int) halfOfAvailableMegs) - 3 * 1024 * 1024);
        }
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc()
                .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .threadPoolSize(3)
                .threadPriority(Thread.NORM_PRIORITY - 1)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new FIFOLimitedMemoryCache(inMemoryCacheSize))
                .discCache(
                        new MUnlimitedDiscCache(cacheDir,
                                cAlbumCacheFileNameGenerator = new AlbumCacheFileNameGenerator(this)))
                .memoryCache(new FIFOLimitedMemoryCache(inMemoryCacheSize))
                .imageDownloader(new BaseImageDownloader(this)).tasksProcessingOrder(QueueProcessingType.LIFO)
                .defaultDisplayImageOptions(options).build();
        ImageLoader.getInstance().init(config);
    }

    public static String getCacheImageUrl(String url) {
        return cAlbumCacheFileNameGenerator.generate(url);
    }
}