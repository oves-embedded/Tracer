package com.ov.tracker.ui.home;

import static com.ov.tracker.enums.EventBusTagEnum.BLE_FIND;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.gson.Gson;
import com.hjq.bar.OnTitleBarListener;
import com.hjq.bar.TitleBar;
import com.ov.tracker.MainActivity;
import com.ov.tracker.R;

import com.ov.tracker.callback.EventCallBack;
import com.ov.tracker.entity.BleDeviceInfo;
import com.ov.tracker.entity.EventBusMsg;
import com.ov.tracker.ui.BleDetailActivity;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeFragment extends Fragment implements EventCallBack {

    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.titleBar)
    TitleBar titleBar;
    private BleAdapter bleAdapter;
    private List<BleDeviceInfo> list;
    private LayoutInflater inflater;
    private MainActivity activity2;
    private Unbinder unbinder;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        activity2 = (MainActivity) getActivity();
        ButterKnife.bind(activity2);
        this.inflater = inflater;
        View rootView = inflater.inflate(R.layout.fragment_home, null);
        unbinder = ButterKnife.bind(this, rootView);
        list = new CopyOnWriteArrayList<>();
        activity2.addCallBack(this);
        refreshLayout.setEnableLoadMore(false);
        bleAdapter = new BleAdapter(R.layout.item_ble_list);
        bleAdapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.SlideInRight);
        //默认的AnimationType类型：AlphaIn, ScaleIn, SlideInBottom, SlideInLeft, SlideInRight
        bleAdapter.setNewInstance(list);
        recyclerView.setAdapter(bleAdapter);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                activity2.startScan();
            }
        });

        titleBar.setOnTitleBarListener(new OnTitleBarListener() {
            @Override
            public void onRightClick(TitleBar titleBar) {
                OnTitleBarListener.super.onRightClick(titleBar);
                showFilterDialog();
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    private Boolean sortByRssi = false;
    private String keyWord = null;

    public void showFilterDialog() {
        List<String> productNameList = null;
        if (list != null && list.size() > 0) {
            for (BleDeviceInfo info : list) {
                if (productNameList == null) productNameList = new ArrayList<>();
                if (!productNameList.contains(info.getProductName())) {
                    productNameList.add(info.getProductName());
                }
            }
        }
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_filter);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        EditText etKeyword = dialog.findViewById(R.id.et_Keyword);
        SwitchCompat orderBy = dialog.findViewById(R.id.orderBy);
        if (!TextUtils.isEmpty(keyWord)) {
            etKeyword.setText(keyWord);
        }
        orderBy.setChecked(sortByRssi);
        dialog.findViewById(R.id.btn_close_filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.btn_apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyWord = etKeyword.getText().toString();
                sortByRssi = orderBy.isChecked();
//                refreshLayout.autoRefresh();
                refreshAdapter();
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }


    @Override
    public void eventBusListener(EventBusMsg msg) {
        if (msg.getTagEnum() == BLE_FIND) {
            list.clear();
            List<BleDeviceInfo> newList = (List<BleDeviceInfo>) msg.getT();
            if (newList != null && newList.size() > 0) {
                if (refreshLayout.isRefreshing()) {
                    refreshLayout.finishRefresh();
                }
                list.addAll(newList);
            }
            refreshAdapter();
        }
    }

    public void refreshAdapter() {
        if (list != null && list.size() > 0) {
            if (sortByRssi) {
                Collections.sort(list, new Comparator<BleDeviceInfo>() {
                    @Override
                    public int compare(BleDeviceInfo o1, BleDeviceInfo o2) {
                        return o2.getRssi() - o1.getRssi();
                    }
                });
            }

            if (keyWord != null && keyWord.length() > 0) {
                List<BleDeviceInfo> infoList = new CopyOnWriteArrayList<>();
                for (BleDeviceInfo info : list) {
                    String s = info.getFullName().toUpperCase();
                    String s1 = keyWord.toUpperCase();
                    if (s.contains(s1)) {
                        infoList.add(info);
                    }
                }
                list.clear();
                list.addAll(infoList);
            }
        }
//        bleAdapter.setNewInstance(list);
        bleAdapter.notifyDataSetChanged();
    }


    class BleAdapter extends BaseQuickAdapter<BleDeviceInfo, BaseViewHolder> {

        public BleAdapter(int layoutResId) {
            super(layoutResId);
        }

        @Override
        protected void convert(BaseViewHolder baseViewHolder, BleDeviceInfo info) {
            String fullName = info.getFullName();


            if (!TextUtils.isEmpty(fullName)) {
                baseViewHolder.setText(R.id.tv_bleName, fullName);
            }
            String productName = info.getProductName() == null ? "" : info.getProductName().toLowerCase();
            productName = productName.replace("-", "_");
            int drawableId = getActivity().getResources().getIdentifier("icon_" + productName, "mipmap", getActivity().getPackageName());
            baseViewHolder.setImageResource(R.id.iv_devIcon, drawableId);

            String address = info.getAddress();
            if (!TextUtils.isEmpty(address)) {
                baseViewHolder.setText(R.id.tv_mac, address);
            }
            int dbm = info.getRssi();
            baseViewHolder.setText(R.id.tv_dbm, dbm + "dBm ≈ " + getDistance(dbm) + "M");
            baseViewHolder.findView(R.id.ll_item).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity2, BleDetailActivity.class);
                    intent.putExtra("data", new Gson().toJson(info));
                    startActivity(intent);
                }
            });
        }

        //A和n的值，需要根据实际环境进行检测得出
        private final double A_Value = 70;
        /**
         * A - 发射端和接收端相隔1米时的信号强度
         */
        private final double n_Value = 2.5;

        /**
         * n - 环境衰减因子
         */
        public String getDistance(int rssi) {
            int iRssi = Math.abs(rssi);
            double power = (iRssi - A_Value) / (10 * n_Value);
            return String.format("%.1f", Math.pow(10, power));
        }
    }

    /**
     * onDestroy中进行解绑操作
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}