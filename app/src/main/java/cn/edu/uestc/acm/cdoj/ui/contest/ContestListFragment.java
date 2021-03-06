package cn.edu.uestc.acm.cdoj.ui.contest;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Map;

import cn.edu.uestc.acm.cdoj.R;
import cn.edu.uestc.acm.cdoj.net.NetData;
import cn.edu.uestc.acm.cdoj.net.ViewHandler;
import cn.edu.uestc.acm.cdoj.net.data.InfoList;
import cn.edu.uestc.acm.cdoj.net.data.PageInfo;
import cn.edu.uestc.acm.cdoj.net.data.Result;
import cn.edu.uestc.acm.cdoj.tools.TimeFormat;
import cn.edu.uestc.acm.cdoj.ui.ItemDetailActivity;
import cn.edu.uestc.acm.cdoj.ui.LoginActivity;
import cn.edu.uestc.acm.cdoj.ui.modules.Global;
import cn.edu.uestc.acm.cdoj.ui.modules.list.ListViewWithGestureLoad;
import cn.edu.uestc.acm.cdoj.ui.modules.list.MainList;

/**
 * Created by great on 2016/8/17.
 */
public class ContestListFragment extends Fragment implements ViewHandler, MainList{
    private SimpleAdapter mListAdapter;
    private ArrayList<Map<String,Object>> listItems = new ArrayList<>();
    private String key;
    private int clickPosition = -1;
    private int clickContestID = -1;
    private ProgressDialog progressDialog;
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
        mListView.setOnPullUpLoadListener(new ListViewWithGestureLoad.OnPullUpLoadListener(){
            @Override
            public void onPullUpLoading() {
                if (mPageInfo != null && mPageInfo.currentPage < mPageInfo.totalPages) {
                    NetData.getContestList(mPageInfo.currentPage + 1, key, ContestListFragment.this);
                }else {
                    mListView.setPullUpLoadFinish();
                }
            }
        });
        mListView.setProgressContainerVisibility(progressContainerVisibility);
        setupOnListItemClick();
        if (refreshed) refresh();
        mListView.setLayoutParams(container.getLayoutParams());
        return mListView;
    }

    private void setupOnListItemClick() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int contestId = (int) listItems.get(position).get("contestId");
                addInfoOfCurrentClick(position, contestId);
                if (!Global.userManager.isLogin()){
                    reminderUnLogin();
                    return;
                }
                if (listItems.get(position).get("typeName").equals("Private")){
                    reminderEnterPassword();
                    return;
                }
                showAlreadyClick();
                NetData.loginContest(contestId, "123456789", ContestListFragment.this);
            }
        });
    }

    private void addInfoOfCurrentClick(int position, int id) {
        clickPosition = position;
        clickContestID = id;
    }

    private void showAlreadyClick() {
        progressDialog = ProgressDialog
                .show(context, getString(R.string.gettingContest),
                        getString(R.string.linking));
    }

    private void reminderUnLogin() {
        final boolean[] enter = {false};
        AlertDialog.Builder alert = new AlertDialog.Builder(context)
                .setTitle(getString(R.string.notLogin))
                .setMessage(getString(R.string.reminderNotLogin))
                .setPositiveButton(getString(R.string.login), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        if (listItems.get(clickPosition).get("typeName").equals("Public")) {
            alert.setNeutralButton(getString(R.string.enterDirectly), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showDetail();
                }
            });
        }
        alert.show();
    }

    private void reminderEnterPassword() {
        final EditText passwordET = new EditText(context);
        new AlertDialog.Builder(context)
                .setTitle(getString(R.string.pleaseEnterPassword))
                .setView(passwordET)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showAlreadyClick();
                        NetData.loginContest(clickContestID,
                                passwordET.getText().toString(), ContestListFragment.this);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    @Override
    public void show(int which, Result result, long time) {
        switch (which) {
            case ViewHandler.CONTEST_LIST:
                if (refreshed) {
                    listItems.clear();
                    notifyDataSetChanged();
                    refreshed = false;
                }
                if (result.result) {
                    mPageInfo = ((InfoList) result.getContent()).pageInfo;
                    ArrayList<Map<String, Object>> temArrayList = ((InfoList) result.getContent()).getInfoList();
                    for (Map<String, Object> temMap : temArrayList) {
                        temMap.put("contestIdString", "ID:" + temMap.get("contestId"));
                        temMap.put("time", TimeFormat.getFormatDate((long) temMap.get("time")));
                        temMap.put("length", TimeFormat.getFormatTime((int) temMap.get("length")));
                    }
                    listItems.addAll(temArrayList);
                    if (listItems.size() == 0){
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
                break;
            case ViewHandler.LOGIN_CONTEST:
                if (progressDialog != null){
                    progressDialog.cancel();
                }
                if (result.result) {
                    showDetail();
                }else {
                    reminderLoginError();
                }
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (mListAdapter == null) {
            mListView.setAdapter(createAdapter());
        } else{
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
        mListAdapter =  new SimpleAdapter(
                Global.currentMainUIActivity, listItems, R.layout.contest_item_list,
                new String[]{"title", "time", "length", "contestIdString", "status", "typeName"},
                new int[]{R.id.contest_title, R.id.contest_date, R.id.contest_timeLimit,
                        R.id.contest_id, R.id.contest_status, R.id.contest_permission});
        /*mListAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof TextView) {
                    if (!(data instanceof String)) {
                        if (view.getId() == R.id.contest_date) {
                            ((TextView) view).setText(TimeFormat.getFormatDate((long) data));
                        } else if (view.getId() == R.id.contest_timeLimit) {
                            ((TextView) view).setText(TimeFormat.getFormatTime((long) data));
                        } else if (view.getId() == R.id.contest_id) {
                            ((TextView) view).setText(String.valueOf((int) data));
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

    private void showDetail() {
        if (!Global.isTwoPane) {
            showDetailOnActivity();
            return;
        }
        FragmentTransaction mTransaction = mFragmentManager.beginTransaction();
        ContestFragment contest = new ContestFragment()
                .refresh(clickContestID);
        mTransaction.replace(R.id.ui_main_detail, contest);
        mTransaction.commit();
    }

    private void showDetailOnActivity() {
        Intent intent = new Intent(context, ItemDetailActivity.class);
        intent.putExtra("title", (String) listItems.get(clickPosition).get("title"));
        intent.putExtra("type", ViewHandler.CONTEST_DETAIL);
        intent.putExtra("id",clickContestID);
        context.startActivity(intent);
    }

    private void reminderLoginError() {
        AlertDialog.Builder alert = new AlertDialog.Builder(context)
                .setMessage(getString(R.string.reminderLoginError))
                .setNeutralButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        if (listItems.get(clickPosition).get("typeName").equals("Private")) {
            alert.setMessage(getString(R.string.passwordIsWrong));
        } else {
            alert.setMessage(getString(R.string.noPermission));
        }
        alert.show();
    }

    @Override
    public void setRefreshProgressListener(OnRefreshProgressListener listener) {
        hasSetProgressListener = true;
        progressListener = listener;
    }

    @Override
    public void addListItem(Map<String ,Object> listItem) {
        listItems.add(listItem);
    }

    @Override
    public ListViewWithGestureLoad getListView() {
        return mListView;
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

    public ContestListFragment refresh() {
        refreshed = true;
        if (mListView != null) {
            if (progressListener != null) progressListener.start();
            mListView.resetPullUpLoad();
            NetData.getContestList(1, key, this);
        }
        return this;
    }
}
