package cn.edu.uestc.acm.cdoj_android;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import cn.edu.uestc.acm.cdoj_android.layout.DetailsContainerFragment;
import cn.edu.uestc.acm.cdoj_android.layout.MyListFragment;
import cn.edu.uestc.acm.cdoj_android.net.NetData_1;

public class MainActivity extends AppCompatActivity implements Selection {

    private TabLayout tab_bottom;
    private DetailsContainerFragment detailsContainer_Fragment;
    private MyListFragment[] list_Fragment;
    private FragmentManager fragmentManager;
    private Information information;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fragmentManager = getFragmentManager();
        list_Fragment = new MyListFragment[3];
        if (savedInstanceState == null){
            initViews();
        }else {
            findBackFragment();
        }
    }

    private void findBackFragment() {
        detailsContainer_Fragment = (DetailsContainerFragment)fragmentManager.findFragmentByTag("detailsContainer_Fragment");
        detailsContainer_Fragment.addSelection(this);
        for (int i = 0; i != 3; ++i) {
            list_Fragment[i] = (MyListFragment)fragmentManager.findFragmentByTag("list_Fragment"+i);
        }
    }

    private void initViews(){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        detailsContainer_Fragment = new DetailsContainerFragment();
        detailsContainer_Fragment.addSelection(this);
        transaction.add(R.id.details_container,detailsContainer_Fragment,"detailsContainer_Fragment");
        for (int i = 0; i != 3; ++i) {
            list_Fragment[i] = (new MyListFragment()).createAdapter(this);
            transaction.add(R.id.list_main,list_Fragment[i],"list_Fragment"+i);
        }
        information = new Information(this,list_Fragment,detailsContainer_Fragment);
        NetData_1.getArticleList(1, information);
        NetData_1.getProblemList(1, information);
        NetData_1.getContestList(1, information);
        transaction.commit();
        setDefaultFragment();
    }

    @Override
    public void initTab(ViewPager detailsContainer_ViewPager) {
        tab_bottom = (TabLayout)findViewById(R.id.tabLayout_bottom);
        tab_bottom.setupWithViewPager(detailsContainer_ViewPager);
        tab_bottom.getTabAt(0).setIcon(R.drawable.ic_action_home);
        tab_bottom.getTabAt(1).setIcon(R.drawable.ic_action_tiles_large);
        tab_bottom.getTabAt(2).setIcon(R.drawable.ic_action_achievement);
//        tab_bottom.getTabAt(3).setIcon(R.drawable.ic_action_user);
    }
    private void setDefaultFragment() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.show(list_Fragment[0]);
        transaction.commit();
    }

    @Override
    public void setSelectionList(final int position) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideListFragment();
        switch (position){
            case 0:
                transaction.show(list_Fragment[position]);
            case 1:
                transaction.show(list_Fragment[position]);
                break;
            case 2:
                transaction.show(list_Fragment[position]);
                break;
        }
        transaction.commit();
    }

    private void hideListFragment(){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (int i = 0; i != 3; ++i) {
            transaction.hide(list_Fragment[i]);
        }
        transaction.commit();
    }
}