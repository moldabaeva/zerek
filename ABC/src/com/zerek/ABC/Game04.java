package com.zerek.ABC;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageView;

/**
 * Created with IntelliJ IDEA.
 * User: mode
 * Date: 6/28/13
 * Time: 4:00 PM
 */


public class Game04 extends FragmentActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {
    private ViewPager pager;
    private Gallery gallery;

    private static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";

    private GalleryLogic galleryLogic;

    private boolean bDo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MainActivity.prepWindow(this, true, 0);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.var_04);

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                moveTo(true, position, true);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // Navigation toolbar
        gallery = GalleryLogic.getGallery(this);
        gallery.setOnItemSelectedListener(this);
        gallery.setOnItemClickListener(this);


        PagerAdapter pagerAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {  //FragmentPagerAdapter
            @Override
            public int getCount() {
                return GalleryLogic.galleryAdapter.getCount();
            }

            @Override
            public Fragment getItem(int pageNumber) {
                Game04_Frag result = new Game04_Frag();

                Bundle arguments = new Bundle();
                arguments.putInt(ARGUMENT_PAGE_NUMBER, pageNumber);
                result.setArguments(arguments);

                return result;
            }
        };
        pager.setAdapter(pagerAdapter);
        pagerAdapter.notifyDataSetChanged();

        bDo = true;
        galleryLogic = new GalleryLogic(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        galleryLogic.Resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        galleryLogic.Pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        unbindDrawables(pager);
        System.gc();
        Runtime.getRuntime().gc();
    }

//    private void unbindDrawables(View view) {
//        if (view == null)
//            return;
//        if (view.getBackground() != null) {
//            view.getBackground().setCallback(null);
//        }
//        if (view instanceof ViewGroup) {
//            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
//                unbindDrawables(((ViewGroup) view).getChildAt(i));
//            }
//            ((ViewGroup) view).removeAllViews();
//        }
//    }

    private void moveTo(boolean bGallery, int position, boolean bPlay) {
        if (bDo) {
            bDo = false;

            if (bGallery) {
                if (position != gallery.getSelectedItemPosition())
                    gallery.setSelection(position, true);
            } else if (position != pager.getCurrentItem()) {
                pager.setCurrentItem(position, true);
            }

            MainActivity.cur.soundPlay(position, bPlay);
        }
        bDo = true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (adapterView != null && view != null)
            galleryLogic.sliderRun(false);
        moveTo(false, position, id != GalleryLogic.C_EVEN);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
//       moveTo(false, position, false);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    class Game04_Frag extends Fragment {
        private int pageNumber;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
        }

        @Override
        public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
            View view = layoutInflater.inflate(R.layout.var_04_frag, null);

            ImageView imageView = (ImageView) view.findViewById(R.id.image1);
            imageView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    MainActivity.cur.soundPlay(pageNumber, true);
                }
            });

            GalleryLogic.galleryAdapter.getGalleryItem(pageNumber).setBitmap(imageView);

            return view;
        }
    }
}