package com.gionee.hotspottransmission.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.callback.IReceivedImagesDataChangListener;
import com.gionee.hotspottransmission.utils.LogUtil;
import com.gionee.hotspottransmission.manager.ReceivedImageSourceManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import java.util.List;
import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;

public class ImageShowActivity extends AmigoActivity implements IReceivedImagesDataChangListener {
    private ViewPager mPager;
    private ImageAdapter mPagerAdapter;
    private AmigoActionBar mActionBar;
    private String message = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initLoader();
        setDataChanagerListener();
        showImages();
        initActionBar();
    }
    private void initActionBar() {
        mActionBar = getAmigoActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setNavigationMode(amigoui.app.AmigoActionBar.DISPLAY_HOME_AS_UP);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setTitle(getImageName());
        mActionBar.show();
    }

    private void initView(){
        setContentView(R.layout.activity_image_show);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ImageAdapter(this);
        mPager.setOffscreenPageLimit(0);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    private void initLoader(){
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration
                .createDefault(this);
        ImageLoader.getInstance().init(configuration);
    }

    @TargetApi(Build.VERSION_CODES.DONUT)
    private void showImages(){
        mPager.setAdapter(mPagerAdapter);
        int currentItem = ReceivedImageSourceManager.getInstance(this).getCurrentItem();
        mPager.setCurrentItem(currentItem);
    }

    private void updateTitle(){
        mActionBar.setTitle(getImageName());
    }

    private void setDataChanagerListener() {
        ReceivedImageSourceManager.getInstance(this).setListener(this);
    }

    @Override
    public void onDataChanage() {
        if (mPagerAdapter != null) {
            LogUtil.i("刷新图片显示的title");
            mPagerAdapter.notifyDataSetChanged();
            mActionBar.setTitle(getImageName());
        }
    }

    @TargetApi(Build.VERSION_CODES.DONUT)
    private String getImageName() {
        int index = mPager.getCurrentItem();
        List<String> imageNames = ReceivedImageSourceManager.getInstance(this).getmImagesNames();
        if (index < imageNames.size()) {
            LogUtil.i("图片显示index = " + index + " , title = " + imageNames.get(index));
            return imageNames.get(index);
        } else {
            return "";
        }
    }


    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int i, float v, int i1) {
            updateTitle();
        }

        @Override
        public void onPageSelected(int i) {

        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    }

    private class ImageAdapter extends PagerAdapter {

        private LayoutInflater inflater;
        private DisplayImageOptions options;
        private List<String> mDatas;
        
        ImageAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            options = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.ic_empty)
                    .showImageOnFail(R.drawable.ic_error)
                    .resetViewBeforeLoading(true)
                    .cacheInMemory(true)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .considerExifParams(true)
                    .displayer(new FadeInBitmapDisplayer(300))
                    .build();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            if (ReceivedImageSourceManager.getInstance(ImageShowActivity.this).getReceivedImagePathes() != null) {
                return ReceivedImageSourceManager.getInstance(ImageShowActivity.this).getReceivedImagePathes().size();
            }
            return 0;
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            LogUtil.i("imageShow....instantiateItem...position:" + position);
            View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
            assert imageLayout != null;
            ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
            final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
            mDatas = ReceivedImageSourceManager.getInstance(ImageShowActivity.this).getReceivedImagePathes();
            String imagePathUri = null;
            if (position < mDatas.size()) {
                imagePathUri = mDatas.get(position);
            }
            if(imagePathUri == null){
                return null;
            }
            ImageLoader.getInstance().displayImage(imagePathUri, imageView, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    spinner.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    switch (failReason.getType()) {
                        case IO_ERROR:
                            message = "Input/Output error";
                            break;
                        case DECODING_ERROR:
                            message = "Image can't be decoded";
                            break;
                        case NETWORK_DENIED:
                            message = "Downloads are denied";
                            break;
                        case OUT_OF_MEMORY:
                            message = "Out Of Memory error";
                            break;
                        case UNKNOWN:
                            message = "Unknown error";
                            break;
                    }
                    spinner.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    spinner.setVisibility(View.GONE);
                }
            });

            view.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }
}
