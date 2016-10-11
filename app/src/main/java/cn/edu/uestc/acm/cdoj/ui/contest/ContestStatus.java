package cn.edu.uestc.acm.cdoj.ui.contest;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.edu.uestc.acm.cdoj.net.data.PageInfo;
import cn.edu.uestc.acm.cdoj.ui.modules.Global;
import cn.edu.uestc.acm.cdoj.R;
import cn.edu.uestc.acm.cdoj.ui.modules.list.ListViewWithGestureLoad;
import cn.edu.uestc.acm.cdoj.net.NetData;
import cn.edu.uestc.acm.cdoj.net.ViewHandler;
import cn.edu.uestc.acm.cdoj.net.data.InfoList;
import cn.edu.uestc.acm.cdoj.net.data.Status;

/**
 * Created by great on 2016/8/25.
 */
public class ContestStatus extends Fragment implements ViewHandler{
    private SimpleAdapter mListAdapter;
    private ArrayList<Map<String, Object>> listItems = new ArrayList<>();
    private int contestID = -1;
    private int[] problemIDs;
    private ListViewWithGestureLoad mListView;
    private PageInfo mPageInfo;
    private Context context;
    private boolean refreshed;

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
                if (mPageInfo != null && mPageInfo.currentPage != mPageInfo.totalPages) {
                    NetData.getStatusList(-1, null, contestID, mPageInfo.currentPage + 1, ContestStatus.this);
                } else {
                    mListView.setPullUpLoadFinish();
                }
            }
        });
        if (refreshed) refresh();
        mListView.setLayoutParams(container.getLayoutParams());
        return mListView;
    }

    public void addListItem(Map<String, Object> listItem) {
        listItems.add(listItem);
    }

    public void notifyDataSetChanged() {
        if (mListAdapter == null) {
            mListView.setAdapter(setupAdapter());
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

    private ListAdapter setupAdapter() {
        mListAdapter = new SimpleAdapter(
                Global.currentMainUIActivity, listItems, R.layout.contest_status_item_list,
                new String[]{"result", "language", "user", "cost", "probOrder", "submitDate"},
                new int[]{R.id.contestStatus_result, R.id.contestStatus_language,
                        R.id.contestStatus_user, R.id.contestStatus_cost,
                        R.id.contestStatus_probOrder, R.id.contestStatus_submitDate});
        return mListAdapter;
    }

    public void setProblemIDs(int[] problemIDs) {
        this.problemIDs = problemIDs;
    }

    @Override
    public void show(int which, Object data, long time) {
        if (refreshed) {
            listItems.clear();
            notifyDataSetChanged();
            refreshed = false;
        }
        if (((InfoList) data).result) {
            mPageInfo = ((InfoList) data).pageInfo;
            ArrayList<Status> infoList_status = ((InfoList) data).getInfoList();
            if (infoList_status.size() == 0) {
                mListView.setDataIsNull();
                notifyDataSetChanged();
                return;
            }
            for (Status tem : infoList_status) {
                Map<String, Object> listItem = new HashMap<>();
                listItem.put("result", tem.returnType);
                listItem.put("submitDate", tem.timeString);
                listItem.put("language", tem.language+"/"+tem.length+"B");
                listItem.put("cost", "cost:"+tem.timeCost+"ms/"+tem.memoryCost+"KB");
                listItem.put("user", tem.nickName);
                if (problemIDs != null) {
                    int i = 0;
                    while (i != problemIDs.length && tem.problemId != problemIDs[i]) ++i;
                    if (i == problemIDs.length) {
                        listItem.put("probOrder", "?");
                    } else {
                        listItem.put("probOrder", String.valueOf((char)('A' + i)));
                    }
                }
                addListItem(listItem);
            }
            if (mPageInfo.currentPage == mPageInfo.totalItems) {
                mListView.setPullUpLoadFinish();
            }
        } else {
            mListView.setPullUpLoadFailure();
        }
        notifyDataSetChanged();
    }

    private void refresh() {
        if (contestID != -1) refresh(contestID);
    }

    public ContestStatus refresh(int contestID)  {
        if (contestID < 1) return this;
        this.contestID = contestID;
        refreshed = true;
        if (mListView != null) {
            mListView.resetPullUpLoad();
            NetData.getStatusList(-1, null, contestID, 1, this);
        }
        return this;
    }
}
