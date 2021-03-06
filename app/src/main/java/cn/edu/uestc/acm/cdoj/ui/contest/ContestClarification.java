package cn.edu.uestc.acm.cdoj.ui.contest;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
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
import cn.edu.uestc.acm.cdoj.tools.TimeFormat;
import cn.edu.uestc.acm.cdoj.ui.modules.Global;
import cn.edu.uestc.acm.cdoj.ui.modules.list.ListViewWithGestureLoad;

/**
 * Created by great on 2016/8/25.
 */
public class ContestClarification extends Fragment implements ViewHandler{

    private SimpleAdapter mListAdapter;
    private int contestID = -1;
    private ArrayList<Map<String, Object>> listItems = new ArrayList<>();
    private ProgressDialog mProgressDialog;
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
                if (mPageInfo != null && mPageInfo.currentPage < mPageInfo.totalPages) {
                    NetData.getContestComment(contestID, mPageInfo.currentPage + 1, ContestClarification.this);
                } else {
                    mListView.setPullUpLoadFinish();
                }
            }
        });
        setupOnListItemClick();
        if (refreshed) refresh();
        mListView.setLayoutParams(container.getLayoutParams());
        return mListView;
    }

    private ListAdapter setupAdapter() {
        mListAdapter = new SimpleAdapter(
                Global.currentMainUIActivity, listItems, R.layout.contest_clarification_item_list,
                new String[]{"header", "ownerName", "time", "content"},
                new int[]{R.id.contestClarification_header, R.id.contestClarification_user,
                        R.id.contestClarification_submitDate, R.id.contestClarification_content}){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                v.setTag(position);
                return v;
            }
        };
        mListAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView) {
                    if (data instanceof Integer) {
                        ((ImageView) view).setImageResource((int) data);
                        return true;
                    } else if (data instanceof Bitmap) {
                        ((ImageView) view).setImageBitmap((Bitmap) data);
                        return true;
                    }
                } else if (view instanceof TextView) {
                    ((TextView) view).setText(data.toString());
                    return true;
                }
                return false;
            }
        });
        return mListAdapter;
    }

    private void setupOnListItemClick() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mProgressDialog = ProgressDialog
                        .show(context, getString(R.string.getting), getString(R.string.linking));
                NetData.getArticleDetail((int) listItems.get(position).get("articleId"), new ContestClarificationAlert(context, mProgressDialog));
            }
        });
    }

    @Override
    public void show(int which, Result result, long time) {
        switch (which) {
            case ViewHandler.CONTEST_COMMENT:
                if (refreshed) {
                    listItems.clear();
                    notifyDataSetChanged();
                    refreshed = false;
                }
                if (result.result){
                    mPageInfo = ((InfoList) result.getContent()).pageInfo;
                    ArrayList<Map<String, Object>> listItemsReceived = ((InfoList) result.getContent()).getInfoList();
                    setupReceivedData(listItemsReceived);
                    if (listItems.size() == 0) {
                        mListView.setDataIsNull();
                        notifyDataSetChanged();
                        return;
                    }
                    if (mPageInfo.currentPage == mPageInfo.totalItems) {
                        mListView.setPullUpLoadFinish();
                    }
                }else {
                    mListView.setPullUpLoadFailure();
                }
                notifyDataSetChanged();
                return;
            case ViewHandler.AVATAR:
                int position = (int) result.getExtra();
                if (position < listItems.size())
                    listItems.get(position).put("header", result.getContent());
                View view = mListView.findItemViewWithTag(position);
                if (view != null) {
                    ImageView imageView = (ImageView) view.findViewById(R.id.contestClarification_header);
                    if (imageView != null) imageView.setImageBitmap((Bitmap) result.getContent());
                }
        }
    }

    private void setupReceivedData(ArrayList<Map<String, Object>> listItemsReceived) {
        for (int i =0; i < listItemsReceived.size(); ++i) {
            Map<String, Object> temMap = listItemsReceived.get(i);
            temMap.put("content", ((String) temMap.get("content")).replaceAll("!\\[title].*\\)", "[图片]"));
            temMap.put("time", TimeFormat.getFormatDate((long) temMap.get("time")));
            temMap.put("header", R.drawable.logo);
            listItems.add(temMap);
            Global.userManager.getAvatar((String) temMap.get("ownerEmail"), listItems.size() - 1, this);
        }
    }

    public void notifyDataSetChanged() {
        if (mListAdapter == null) {
            mListView.setAdapter(setupAdapter());
        }else {
            mListAdapter.notifyDataSetChanged();
        }
        mListAdapter.notifyDataSetChanged();
        if (mListView.isPullUpLoading()) {
            mListView.setPullUpLoading(false);
        }
        if (mListView.isRefreshing()) {
            mListView.setRefreshing(false);
        }
    }

    private void refresh() {
        if (contestID != -1) refresh(contestID);
    }

    public ContestClarification refresh(int contestID) {
        if (contestID < 1) return this;
        refreshed = true;
        this.contestID = contestID;
        if (mListView != null) {
            mListView.resetPullUpLoad();
            NetData.getContestComment(contestID, 1, this);
        }
        return this;
    }
}
