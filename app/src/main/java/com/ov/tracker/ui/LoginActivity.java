package com.ov.tracker.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.Toaster;
import com.ov.tracker.MainActivity;
import com.ov.tracker.R;
import com.ov.tracker.application.MyApplication;
import com.ov.tracker.constants.CommonConstant;
import com.ov.tracker.entity.http.LoginResult;
import com.ov.tracker.http.LoginInterface;
import com.ov.tracker.http.RetrofitService;
import com.ov.tracker.utils.LogUtil;
import com.ov.tracker.utils.SharedPreferencesUtils;
import com.ov.tracker.utils.permission.PermissionInterceptor;
import com.ov.tracker.utils.permission.PermissionNameConvert;


import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.username)
    TextInputEditText tvUsername;

    @BindView(R.id.password)
    TextInputEditText tvPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        XXPermissions.with(LoginActivity.this)
                .permission(Permission.BLUETOOTH_SCAN)
                .permission(Permission.BLUETOOTH_CONNECT)
                .permission(Permission.BLUETOOTH_ADVERTISE)
                .permission(Permission.ACCESS_FINE_LOCATION)
                .permission(Permission.ACCESS_COARSE_LOCATION)
                .interceptor(new PermissionInterceptor()).request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        if (!allGranted) {
                            return;
                        }
                        Toaster.show(String.format(getString(R.string.demo_obtain_permission_success_hint), PermissionNameConvert.getPermissionString(LoginActivity.this, permissions)));
                    }
                });
        ((View) findViewById(R.id.login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = tvUsername.getText().toString();
                if (TextUtils.isEmpty(username)) {
                    Toaster.show("Email is empty!");
                    return;
                }
                String password = tvPassword.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    Toaster.show("Password is empty!");
                    return;
                }
                SharedPreferencesUtils.setParam(LoginActivity.this, CommonConstant.USER_NAME, username);
                SharedPreferencesUtils.setParam(LoginActivity.this, CommonConstant.PASS_WORD, password);
                login(username, password);
            }
        });

        tvUsername.setText((String) SharedPreferencesUtils.getParam(LoginActivity.this, CommonConstant.USER_NAME, "ovesTestDistributor3@outlook.com"));
        tvPassword.setText((String) SharedPreferencesUtils.getParam(LoginActivity.this, CommonConstant.PASS_WORD, "Oves1234$"));

    }

    private Dialog loginDialog;

    private void showLoginDialog() {
        loginDialog = new Dialog(this);
        loginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        loginDialog.setContentView(R.layout.dialog_loading);
        loginDialog.setCancelable(false);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(loginDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        loginDialog.show();
        loginDialog.getWindow().setAttributes(lp);
    }

    private void login(String username, String password) {
        showLoginDialog();
        String jsonStr = "{    \"operationName\": \"SignInLoginUser\",    \"query\": \"fragment AuthToken on AuthToken { _id accessToken actionScope agentId agentType authenticationInstance { _id name __typename } birthDate createdAt deleteAt deleteStatus email firstName hireDate idString idType lastName name officeAddress { _id city country createdAt deleteAt deleteStatus postcode srpc street unit updatedAt __typename } profile role { _id name __typename } roleName subrole { _id name __typename } type updatedAt __typename}mutation SignInLoginUser($signInCredentials: SignInCredentialsDto!) { signInUser(signInCredentials: $signInCredentials) { ...AuthToken __typename }}\",    \"variables\": {        \"signInCredentials\": {            \"email\": \"" + username + "\",            \"password\": \"" + password + "\"        }    }}";
        LoginInterface anInterface = RetrofitService.getInstance().createInterface(LoginInterface.class);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonStr);
        Call<LoginResult> returnResultCall = anInterface.login(requestBody);
        returnResultCall.enqueue(new Callback<LoginResult>() {
            @Override
            public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {
                try {
                    if (response.isSuccessful()) {
                        LoginResult result = response.body();
                        LoginResult.DataDTO data = result.getData();
                        boolean login = (data != null);
                        if (loginDialog.isShowing()) {
                            loginDialog.dismiss();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (login) {
                                    MyApplication.setUserDataDto(data);
                                    Toaster.show("Login successful.");
                                    try {
                                        TimeUnit.MILLISECONDS.sleep(200);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    LoginActivity.this.finish();
                                } else {
                                    Toaster.show("Email and password do not match.");
                                }
                            }
                        });
                        LogUtil.error(new Gson().toJson(response.body()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<LoginResult> call, Throwable t) {
                t.printStackTrace();
                if (loginDialog.isShowing()) {
                    loginDialog.dismiss();
                }
                Toaster.show(t.getMessage());
            }
        });
    }
}
