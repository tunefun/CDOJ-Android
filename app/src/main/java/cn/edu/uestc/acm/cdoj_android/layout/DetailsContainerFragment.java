package cn.edu.uestc.acm.cdoj_android.layout;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewFragment;

import cn.edu.uestc.acm.cdoj_android.R;
import cn.edu.uestc.acm.cdoj_android.Selection;

/**
 * Created by great on 2016/8/15.
 */
public class DetailsContainerFragment extends Fragment {
    WebViewFragment[] details_Fragment;
    ViewPager detailsContainer_ViewPager;
    Selection selection;
    WebView webView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        details_Fragment = new WebViewFragment[3];
        for (int i = 0; i != 3; ++i) {
            details_Fragment[i] = new WebViewFragment();
        }
        webView = details_Fragment[0].getWebView();
        return inflater.inflate(R.layout.details_container_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        detailsContainer_ViewPager = (ViewPager)getView();
        detailsContainer_ViewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position){
                    case 0:
                        return details_Fragment[0];
                    case 1:
                        return details_Fragment[1];
                    case 2:
                        return details_Fragment[2];
                    default:
                        return null;
                }
            }
            @Override
            public int getCount() {
                return 3;
            }
        });
        detailsContainer_ViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0: selection.setSelectionList(0);break;
                    case 1: selection.setSelectionList(1);break;
                    case 2: selection.setSelectionList(2);break;
//                    default: setSelection(3);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        Log.d(getTag(), "onViewCreated: ");
        selection.initTab(detailsContainer_ViewPager);
    }

    public void addSelection(Selection selection) {
        Log.d("addSelection", "addSelection: ");
        this.selection = selection;
    }
}