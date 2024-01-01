package com.ov.tracker.ui;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson2.JSON;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hjq.bar.OnTitleBarListener;
import com.hjq.bar.TitleBar;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.Toaster;
import com.ov.tracker.R;
import com.ov.tracker.constants.DataConvert;
import com.ov.tracker.constants.ReturnResult;
import com.ov.tracker.entity.BleDeviceInfo;
import com.ov.tracker.entity.CharacteristicDomain;
import com.ov.tracker.entity.DescriptorDomain;
import com.ov.tracker.entity.EventBusMsg;
import com.ov.tracker.entity.ServicesPropertiesDomain;
import com.ov.tracker.enums.ServiceNameEnum;
import com.ov.tracker.service.BleService;
import com.ov.tracker.utils.BleDeviceUtil;
import com.ov.tracker.utils.LogUtil;
import com.ov.tracker.utils.permission.PermissionInterceptor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BleDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private BleDeviceInfo info;
    private BleService bleService;
    private BleDeviceUtil bleDeviceUtil;
    @BindView(R.id.titleBar)
    TitleBar titleBar;
    @BindView(R.id.iv_device)
    ImageView iv_device;
    @BindView(R.id.tv_group)
    LinearLayout tv_group;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.tv_att)
    TextView tv_att;
    @BindView(R.id.tv_sts)
    TextView tv_sts;
    @BindView(R.id.tv_dia)
    TextView tv_dia;
    @BindView(R.id.tv_dta)
    TextView tv_dta;
    @BindView(R.id.tv_cmd)
    TextView tv_cmd;

    private ServiceNameEnum serviceNameEnum = ServiceNameEnum.ATT_SERVICE_NAME;
    private String serviceUUID;

    private BleAttrAdapter bleAttrAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_detail);
        ButterKnife.bind(this);
        info = JSON.parseObject(getIntent().getStringExtra("data"), BleDeviceInfo.class);
        initService();

        tv_att.setOnClickListener(this);
        tv_cmd.setOnClickListener(this);
        tv_dia.setOnClickListener(this);
        tv_dta.setOnClickListener(this);
        tv_sts.setOnClickListener(this);

        bleAttrAdapter = new BleAttrAdapter(R.layout.item_ble_attr);
        recyclerView.setAdapter(bleAttrAdapter);

        titleBar.setOnTitleBarListener(new OnTitleBarListener() {
            @Override
            public void onLeftClick(TitleBar titleBar) {
                BleDetailActivity.this.finish();
            }

            @Override
            public void onRightClick(TitleBar titleBar) {
            }
        });

        if (!TextUtils.isEmpty(info.getFullName())) {
            String[] s = info.getFullName().split(" ");
            if (s.length > 1) {
                String replace = s[1].replace("-", "_");
                int drawableId = getResources().getIdentifier("ov_" + replace.toLowerCase(), "mipmap", getPackageName());
                iv_device.setImageResource(drawableId);
            }
        }
    }

    @Override
    public void onClick(View v) {
        tv_att.setBackgroundResource(R.drawable.shape_item_normal);
        tv_sts.setBackgroundResource(R.drawable.shape_item_normal);
        tv_dta.setBackgroundResource(R.drawable.shape_item_normal);
        tv_cmd.setBackgroundResource(R.drawable.shape_item_normal);
        tv_dia.setBackgroundResource(R.drawable.shape_item_normal);
        switch (v.getId()) {
            case R.id.tv_att:
                serviceNameEnum = ServiceNameEnum.ATT_SERVICE_NAME;
                tv_att.setBackgroundResource(R.drawable.shape_item_selector);
                break;
            case R.id.tv_sts:
                serviceNameEnum = ServiceNameEnum.STS_SERVICE_NAME;
                tv_sts.setBackgroundResource(R.drawable.shape_item_selector);
                break;
            case R.id.tv_dta:
                serviceNameEnum = ServiceNameEnum.DTA_SERVICE_NAME;
                tv_dta.setBackgroundResource(R.drawable.shape_item_selector);
                break;
            case R.id.tv_cmd:
                serviceNameEnum = ServiceNameEnum.CMD_SERVICE_NAME;
                tv_cmd.setBackgroundResource(R.drawable.shape_item_selector);
                break;
            case R.id.tv_dia:
                serviceNameEnum = ServiceNameEnum.DIA_SERVICE_NAME;
                tv_dia.setBackgroundResource(R.drawable.shape_item_selector);
                break;
        }
        refreshRecyclerView();
    }


    public void initService() {
        Intent intent = new Intent(BleDetailActivity.this, BleService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                bleService = ((BleService.BleServiceBinder) service).getService();
                XXPermissions.with(BleDetailActivity.this).permission(Permission.BLUETOOTH_SCAN).permission(Permission.BLUETOOTH_CONNECT).permission(Permission.BLUETOOTH_ADVERTISE).permission(Permission.ACCESS_FINE_LOCATION).permission(Permission.ACCESS_COARSE_LOCATION).interceptor(new PermissionInterceptor()).request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        if (!allGranted) {
                            return;
                        }
                        mHandler.sendEmptyMessageDelayed(1, 200);
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        }, BIND_AUTO_CREATE);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (info != null) {
                        try {
                            showDialog();
                            if (popWindow.isShowing()) {
                                tv_content.setText("Bluetooth connecting...");
                                tv_title.setText("LOADING");
                                loading_progress.setVisibility(View.INVISIBLE);
                            }
                            connectBle();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 2:
                    Toaster.showLong(msg.obj.toString());
                    break;
                case 3:
                    if (loading_progress != null && popWindow.isShowing()) {
                        loading_progress.setVisibility(View.VISIBLE);
                        loading_progress.setMax(bleDeviceUtil.getMaxCharacteristicCount());
                        loading_progress.setProgress(currentProgress);

                        ReturnResult<CharacteristicDomain> result = (ReturnResult<CharacteristicDomain>) msg.obj;
                        if (result.ok()) {
                            tv_content.setText(result.getData().getName() + ":" + result.getData().getDesc());
                        }else{
                            tv_content.setText("error:"+result.getExceptionMsg());
                        }

                    }
                    break;
                case 4:
                    if (popWindow.isShowing()) {
                        popWindow.dismiss();
                    }
                    break;
            }
        }
    };

    private void refreshRecyclerView() {
        if (bleDeviceUtil != null) {
            Map<String, ServicesPropertiesDomain> serviceDataDtoMap = bleDeviceUtil.getServiceDataDtoMap();
            if (serviceDataDtoMap != null) {
                Collection<ServicesPropertiesDomain> values = serviceDataDtoMap.values();
                for (ServicesPropertiesDomain domain : values) {
                    if (domain.getUuid().startsWith(serviceNameEnum.getPrefixCode())) {
                        ArrayList<CharacteristicDomain> characteristicDomains = new ArrayList<>(domain.getCharacterMap().values());
                            Collections.sort(characteristicDomains, new Comparator<CharacteristicDomain>() {
                                @Override
                                public int compare(CharacteristicDomain o1, CharacteristicDomain o2) {
                                    if(o1!=null&&o1.getName()!=null&&o2!=null&&o2.getName()!=null){
                                        return o1.getName().compareTo(o2.getName());
                                    }
                                    return 0;
                                }
                            });

                        bleAttrAdapter.setNewInstance(characteristicDomains);
                        serviceUUID=domain.getUuid();
                        return;
                    }
                }
            }
        }
        bleAttrAdapter.setNewInstance(new ArrayList<>());
    }


    private int currentProgress = 0;

    private void connectBle() {
        new Thread() {
            @Override
            public void run() {
                try {
                    bleDeviceUtil = bleService.connectBle(info.getAddress());
                    if (bleDeviceUtil == null) {
                        Message message = new Message();
                        message.what = 2;
                        message.obj = "Device connect error!";
                        mHandler.sendMessage(message);
                    } else {
                        Map<String, ServicesPropertiesDomain> serviceDataDtoMap = bleDeviceUtil.getServiceDataDtoMap();
                        bleDeviceUtil.setMtu(64);
                        Collection<ServicesPropertiesDomain> values = serviceDataDtoMap.values();
                        for (ServicesPropertiesDomain servicesPropertiesDomain : values) {
                            Map<String, CharacteristicDomain> characterMap = servicesPropertiesDomain.getCharacterMap();
                            Collection<CharacteristicDomain> chValues = characterMap.values();
                            String serviceUUID = servicesPropertiesDomain.getUuid();
                            for (CharacteristicDomain characteristicDomain : chValues) {
                                String chUUID = characteristicDomain.getUuid();
                                Map<String, DescriptorDomain> descMap = characteristicDomain.getDescMap();
                                Collection<DescriptorDomain> descValues = descMap.values();
                                for (DescriptorDomain descriptorDomain : descValues) {
                                    bleDeviceUtil.readDescriptor(serviceUUID, chUUID, descriptorDomain.getUuid());
                                }
                                ReturnResult<CharacteristicDomain> result = bleDeviceUtil.readCharacteristic(serviceUUID, chUUID);
                                LogUtil.debug("===>" + JSON.toJSONString(result));
                                currentProgress++;

                                Message message = new Message();
                                message.what = 3;
                                message.obj = result;
                                mHandler.sendMessage(message);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mHandler.sendEmptyMessage(4);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshRecyclerView();
                        }
                    });

                }
            }
        }.start();
    }


    private TextView tv_content;
    private TextView tv_title;
    private ProgressBar loading_progress;
    private PopupWindow popWindow;

    private void showDialog() {
        View view = LayoutInflater.from(BleDetailActivity.this).inflate(R.layout.dialog_show_progress, null);
        popWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popWindow.setAnimationStyle(android.R.style.Animation_Dialog);//设置动画
        popWindow.setFocusable(false); // 设置不允许在外点击消失
        popWindow.setOutsideTouchable(false);
        // 设置背景，这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popWindow.setBackgroundDrawable(new BitmapDrawable());
        //软键盘不会挡着popupwindow
        popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        //设置SelectPicPopupWindow弹出窗体的背景
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        tv_title = view.findViewById(R.id.tv_title);
        tv_title.setText("Loading...");
        tv_content = view.findViewById(R.id.tv_content);
        tv_content.setText("Loading...");
        loading_progress = view.findViewById(R.id.loading_progress);

        //监听菜单的关闭事件
        popWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });
        //设置菜单显示的位置
        popWindow.showAtLocation(iv_device.getRootView(), Gravity.CENTER, 0, 0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void subscriber(EventBusMsg msg) {

    }


    class BleAttrAdapter extends BaseQuickAdapter<CharacteristicDomain, BaseViewHolder> {

        public BleAttrAdapter(int layoutResId) {
            super(layoutResId);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder baseViewHolder, CharacteristicDomain bleDeviceInfo) {
            if (bleDeviceInfo != null) {
                try {
                    String name = bleDeviceInfo.getName();
                    Object realVal = DataConvert.convert2Obj(bleDeviceInfo.getValues(), bleDeviceInfo.getValType());
                    String uni = (TextUtils.isEmpty(name) ? "-" : name) + ":" + (realVal == null ? "<NULL>" : realVal.toString());
                    baseViewHolder.setText(R.id.tv_name, uni);
                    baseViewHolder.setText(R.id.tv_desc, TextUtils.isEmpty(bleDeviceInfo.getDesc()) ? "<NULL>" : bleDeviceInfo.getDesc());

                    if(serviceNameEnum==ServiceNameEnum.CMD_SERVICE_NAME){
                        baseViewHolder.findView(R.id.write).setVisibility(View.VISIBLE);
                        baseViewHolder.findView(R.id.write).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showWriteDialog(bleDeviceInfo);

                            }
                        });
                    }else{
                        baseViewHolder.findView(R.id.write).setVisibility(View.INVISIBLE);
                    }
                    baseViewHolder.findView(R.id.read).setVisibility(View.VISIBLE);
                    baseViewHolder.findView(R.id.read).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ReturnResult<CharacteristicDomain> characteristicDomainReturnResult = bleDeviceUtil.readCharacteristic(serviceUUID, bleDeviceInfo.getUuid());
                            if(characteristicDomainReturnResult.ok()){
                                Toaster.show("Refresh 【"+bleDeviceInfo.getName()+"】 success.");
                            }else{
                                Toaster.show("Refresh 【"+bleDeviceInfo.getName()+"】 fail:"+characteristicDomainReturnResult.getExceptionMsg());
                            }
                            refreshRecyclerView();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }



    private void showWriteDialog(CharacteristicDomain domain) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_write_attr);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        final EditText et_post = (EditText) dialog.findViewById(R.id.et_post);

        int valType = domain.getValType();
        if (valType == 0 || valType == 1 || valType == 2) {
            et_post.setHint("Please enter a number");
            et_post.setInputType(InputType.TYPE_CLASS_NUMBER);
            et_post.setMaxEms(5);
        } else if (valType == 3) {
            et_post.setHint("Please enter a number");
            et_post.setInputType(InputType.TYPE_CLASS_NUMBER);
            et_post.setMaxEms(10);
        } else if (valType == 5) {
            et_post.setHint("Please enter characters.");
            et_post.setMaxEms(20);
        }
        TextView tv_domainName = dialog.findViewById(R.id.domainName);
        TextView tv_domainDesc = dialog.findViewById(R.id.domainDesc);
        tv_domainName.setText(domain.getName() == null ? "" : domain.getName());
        tv_domainDesc.setText(domain.getDesc() == null ? "" : domain.getDesc());
        ((AppCompatButton) dialog.findViewById(R.id.bt_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialog.cancel();
            }
        });
        ((AppCompatButton) dialog.findViewById(R.id.bt_paste)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = clipboardManager.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    // 处理剪贴板内容
                    CharSequence text = clipData.getItemAt(0).getText();
                    et_post.setText(text);
                }
            }
        });
        ((AppCompatButton) dialog.findViewById(R.id.bt_submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputStr = et_post.getText().toString().trim();
                try {
                    byte[] bytes = DataConvert.convert2Arr(inputStr, domain.getValType());
                    if (bytes != null && bleDeviceUtil != null) {
                        bleDeviceUtil.writeCharacteristic(serviceUUID, domain.getUuid(), bytes);
                        bleDeviceUtil.readCharacteristic(serviceUUID,domain.getUuid());
                        refreshRecyclerView();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    dialog.dismiss();
                    dialog.cancel();
                }
            }
        });
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleDeviceUtil != null) {
            bleDeviceUtil.destroy();
        }
        bleDeviceUtil = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


}
