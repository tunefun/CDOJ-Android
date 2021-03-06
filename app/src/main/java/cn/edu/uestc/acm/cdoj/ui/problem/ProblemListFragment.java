package cn.edu.uestc.acm.cdoj.ui.problem;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import cn.edu.uestc.acm.cdoj.R;
import cn.edu.uestc.acm.cdoj.net.NetData;
import cn.edu.uestc.acm.cdoj.net.ViewHandler;
import cn.edu.uestc.acm.cdoj.net.data.InfoList;
import cn.edu.uestc.acm.cdoj.net.data.PageInfo;
import cn.edu.uestc.acm.cdoj.net.data.Result;
import cn.edu.uestc.acm.cdoj.ui.ItemDetailActivity;
import cn.edu.uestc.acm.cdoj.ui.modules.Global;
import cn.edu.uestc.acm.cdoj.ui.modules.list.ListViewWithGestureLoad;
import cn.edu.uestc.acm.cdoj.ui.modules.list.MainList;

/**
 * Created by great on 2016/8/17.
 */
public class ProblemListFragment extends Fragment implements ViewHandler, MainList {
    private SimpleAdapter mListAdapter;
    private ArrayList<Map<String, Object>> listItems = new ArrayList<>();
    private String key;
    private FragmentManager mFragmentManager;
    private ListViewWithGestureLoad mListView;
    private MainList.OnRefreshProgressListener progressListener;
    private PageInfo mPageInfo;
    private Context context;
    private boolean refreshed;
    private boolean hasSetProgressListener;
    private int progressContainerVisibility = View.VISIBLE;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getFragmentManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mListView = new ListViewWithGestureLoad(context);
        mListView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        mListView.setOnPullUpLoadListener(new ListViewWithGestureLoad.OnPullUpLoadListener() {
            @Override
            public void onPullUpLoading() {
                if (mPageInfo != null && mPageInfo.currentPage < mPageInfo.totalPages) {
                    NetData.getProblemList(mPageInfo.currentPage + 1, key, 0, ProblemListFragment.this);
                } else {
                    mListView.setPullUpLoadFinish();
                }
            }
        });
        mListView.setProgressContainerVisibility(progressContainerVisibility);
        configOnListItemClick();
        if (refreshed) refresh();
        mListView.setLayoutParams(container.getLayoutParams());
        return mListView;
    }

    private void configOnListItemClick() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!Global.isTwoPane) {
                    showDetailOnActivity(position);
                    return;
                }
                FragmentTransaction mTransaction = mFragmentManager.beginTransaction();
                ProblemFragment problem = new ProblemFragment().refresh((int) listItems.get(position).get("id"));
                problem.setTitle((String) listItems.get(position).get("title"));
                mTransaction.replace(R.id.ui_main_detail, problem);
                mTransaction.commit();
            }
        });
    }

    private void showDetailOnActivity(int position) {
        Context context = mListView.getContext();
        Intent intent = new Intent(context, ItemDetailActivity.class);
        intent.putExtra("title", (String) listItems.get(position).get("title"));
        intent.putExtra("type", ViewHandler.PROBLEM_DETAIL);
        intent.putExtra("id", (int) listItems.get(position).get("problemId"));
        context.startActivity(intent);
    }

    @Override
    public void show(int which, Result result, long time) {
        if (refreshed) {
            listItems.clear();
            notifyDataSetChanged();
            refreshed = false;
        }
        if (result.result) {
            mPageInfo = ((InfoList) result.getContent()).pageInfo;
            ArrayList<Map<String, Object>> temArrayList = ((InfoList) result.getContent()).getInfoList();
            for (Map<String, Object> temMap : temArrayList) {
                temMap.put("problemIdString", "ID:" + temMap.get("problemId"));
                temMap.put("solved", "Solved:" + temMap.get("solved"));
                temMap.put("tried", "Tried:" + temMap.get("tried"));
            }
            listItems.addAll(temArrayList);
            if (listItems.size() == 0) {
                mListView.setDataIsNull();
                notifyDataSetChanged();
                return;
            }
            if (hasSetProgressListener && mPageInfo.currentPage == 1) {
                progressListener.end();
            }
            if (mPageInfo.currentPage == mPageInfo.totalItems) {
                mListView.setPullUpLoadFinish();
            }
        } else {
            mListView.setPullUpLoadFailure();
        }
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        if (mListAdapter == null){
            mListView.setAdapter(createAdapter());
        }else {
            mListAdapter.notifyDataSetChanged();
        }
        if (mListView.isPullUpLoading()) {
            mListView.setPullUpLoading(false);
        }
        if (mListView.isRefreshing()) {
            mListView.setRefreshing(false);
        }
    }

    private ListAdapter createAdapter() {
        mListAdapter = new SimpleAdapter(
                Global.currentMainUIActivity, listItems, R.layout.problem_item_list,
                new String[]{"title", "source", "problemIdString", "solved", "tried"},
                new int[]{R.id.problem_title, R.id.problem_source, R.id.problem_id, R.id.problem_solved, R.id.problem_tried});
        /*mListAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof TextView) {
                    if (!(data instanceof String)) {
                        if (view.getId() == R.id.problem_id) {
                            ((TextView) view).setText("ID:" + String.valueOf((int) data));
                        } else if (view.getId() == R.id.problem_solved) {
                            ((TextView) view).setText("Solved:" + String.valueOf((int) data));
                        } else if (view.getId() == R.id.problem_tried) {
                            ((TextView) view).setText("Tried:" + String.valueOf((int) data));
                        }
                    } else {
                        ((TextView) view).setText((String) data);
                    }
                    return true;
                }
                return false;
            }
        });*/
        return mListAdapter;
    }

    @Override
    public void addListItem(Map<String, Object> listItem) {
        listItems.add(listItem);
    }

    @Override
    public ListViewWithGestureLoad getListView() {
        return mListView;
    }

    @Override
    public void setRefreshProgressListener(OnRefreshProgressListener listener) {
        hasSetProgressListener = true;
        progressListener = listener;
    }

    @Override
    public void setProgressContainerVisibility(int visibility) {
        progressContainerVisibility = visibility;
        if (mListView != null) {
            mListView.setProgressContainerVisibility(visibility);
        }
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ProblemListFragment refresh() {
        refreshed = true;
        if (mListView != null){
            if (progressListener != null) progressListener.start();
            mListView.resetPullUpLoad();
            listItems.clear();
            NetData.getProblemList(1, key, 0, this);
        }
        return this;
    }
}
