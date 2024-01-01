package com.ov.tracker.ui.home;

import static com.ov.tracker.enums.EventBusTagEnum.BLE_FIND;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.gson.Gson;
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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeFragment extends Fragment implements EventCallBack {

    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private BleAdapter bleAdapter;
    private List<BleDeviceInfo>list;
    private LayoutInflater inflater;

    private MainActivity activity2;

    private Unbinder unbinder;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        activity2= (MainActivity) getActivity();
        ButterKnife.bind(activity2);
        this.inflater=inflater;
        View rootView=inflater.inflate(R.layout.fragment_home,null);
        unbinder=ButterKnife.bind(this, rootView);
        list=new ArrayList<>();
        activity2.addCallBack(this);
        refreshLayout.setEnableLoadMore(false);
        bleAdapter=new BleAdapter(R.layout.item_ble_list);
        recyclerView.setAdapter(bleAdapter);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                activity2.startScan();
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }



    @Override
    public void eventBusListener(EventBusMsg msg) {
        if(msg.getTagEnum()==BLE_FIND){
            List<BleDeviceInfo>list= (List<BleDeviceInfo>) msg.getT();
            bleAdapter.setNewInstance(list);
            if(list!=null&&list.size()>0){
                if(refreshLayout.isRefreshing()){
                    refreshLayout.finishRefresh();
                }
            }
        }
    }


    class BleAdapter extends BaseQuickAdapter<BleDeviceInfo, BaseViewHolder> {

        public BleAdapter(int layoutResId) {
            super(layoutResId);
        }

        @Override
        protected void convert(BaseViewHolder baseViewHolder, BleDeviceInfo info) {
            String fullName = info.getFullName();
            if(!TextUtils.isEmpty(fullName)){
                baseViewHolder.setText(R.id.tv_bleName,fullName);
            }
            String productName = info.getProductName()==null?"":info.getProductName().toLowerCase();
            productName=productName.replace("-","_");
            int drawableId = getActivity().getResources().getIdentifier("icon_" + productName, "mipmap", getActivity().getPackageName());
            baseViewHolder.setImageResource(R.id.iv_devIcon,drawableId);

            String address = info.getAddress();
            if(!TextUtils.isEmpty(address)){
                baseViewHolder.setText(R.id.tv_mac,address);
            }

            int dbm=info.getRssi();
            baseViewHolder.setText(R.id.tv_dbm,dbm+"dBm ≈ "+getDistance(dbm)+"M");

            baseViewHolder.findView(R.id.ll_item).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(activity2, BleDetailActivity.class);
                    intent.putExtra("data",new Gson().toJson(info));
                    startActivity(intent);
                }
            });
        }

        //A和n的值，需要根据实际环境进行检测得出
        private  final double A_Value=70;/**A - 发射端和接收端相隔1米时的信号强度*/
        private  final double n_Value=2.5;/** n - 环境衰减因子*/
        public String getDistance(int rssi){
            int iRssi = Math.abs(rssi);
            double power = (iRssi-A_Value)/(10*n_Value);
            return String.format("%.1f",Math.pow(10,power));
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