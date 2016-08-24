package cn.edu.uestc.acm.cdoj_android.layout.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Map;

import cn.edu.uestc.acm.cdoj_android.Global;
import cn.edu.uestc.acm.cdoj_android.NetContent;
import cn.edu.uestc.acm.cdoj_android.ItemDetailActivity;
import cn.edu.uestc.acm.cdoj_android.R;
import cn.edu.uestc.acm.cdoj_android.Selection;
import cn.edu.uestc.acm.cdoj_android.layout.ListFragmentWithGestureLoad;
import cn.edu.uestc.acm.cdoj_android.layout.PullUpLoadListView;
import cn.edu.uestc.acm.cdoj_android.layout.details.DetailsContainerFragment;
import cn.edu.uestc.acm.cdoj_android.layout.details.DetailsWebViewFragment;
import cn.edu.uestc.acm.cdoj_android.net.NetData;
import cn.edu.uestc.acm.cdoj_android.net.ViewHandler;

/**
 * Created by great on 2016/8/17.
 */
public class ArticleListFragment extends ListFragmentWithGestureLoad {
    SimpleAdapter adapter;
    ArrayList<Map<String, String>> listItems = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;
    PullUpLoadListView listView;
    DetailsWebViewFragment articleDetails;
    boolean isTwoPane;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isTwoPane = ((Selection) Global.currentMainActivity).isTwoPane();
        if (savedInstanceState == null) {
            swipeRefreshLayout = (SwipeRefreshLayout) (getView().findViewById(R.id.listSwipeRefresh));
           /* swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Log.d("上拉刷新", "onRefresh: "+toString());
                    listItems.clear();
                    Global.netContent.getContent(ViewHandler.ARTICLE_LIST, 1);
                }
            });*/
            listView = getListView();
            listView.setOnPullUpLoadListener(new PullUpLoadListView.OnPullUpLoadListener() {
                @Override
                public void onPullUpLoading() {
                    Log.d("上拉加载", "onPullUpLoading: ");
                    Global.netContent.getContent(ViewHandler.ARTICLE_LIST, listItems.size() / 20 + 1);
                }
            });
            Global.netContent.getContent(ViewHandler.ARTICLE_LIST, 1);
            if (isTwoPane) {
                articleDetails = ((Selection) Global.currentMainActivity)
                        .getDetailsContainer()
                        .getDetailsFragment(ViewHandler.ARTICLE_DETAIL);
            }
        }
        Log.d("设置下拉刷新", "onActivityCreated: ");
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("下拉刷新", "onRefresh: "+toString());
                listItems.clear();
                Global.netContent.getContent(ViewHandler.ARTICLE_LIST, 1);
            }
        });
    }

    @Override
    public void addListItem(Map<String, String> listItem) {
        listItems.add(listItem);
    }

    @Override
    public void notifyDataSetChanged() {
        if (adapter == null) {
            adapter = new SimpleAdapter(
                    Global.currentMainActivity, listItems, R.layout.article_list_item,
                    new String[]{"title", "content", "releaseTime", "author"},
                    new int[]{R.id.article_title, R.id.article_content,
                            R.id.article_releaseTime, R.id.article_author});
            setListAdapter(adapter);
        }
        adapter.notifyDataSetChanged();
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        if (listView.isPullUpLoading()) {
            listView.pullUpLoadingComplete();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (!isTwoPane) {
            Context context = l.getContext();
            Intent intent = new Intent(context, ItemDetailActivity.class);
            intent.putExtra("type", ViewHandler.ARTICLE_DETAIL);
            intent.putExtra("id", Integer.parseInt(listItems.get(position).get("id")));
            context.startActivity(intent);
            return;
        }
        Log.d("执行点击", "onListItemClick: ");
        Global.netContent.getContent(ViewHandler.ARTICLE_DETAIL, Integer.parseInt(listItems.get(position).get("id")));
    }
}
