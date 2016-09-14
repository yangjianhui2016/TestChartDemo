package com.example.testchartdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;

/**
 * Created by Jeff on 2016/9/13 18:48.
 */
public class LoginActivity extends Activity {

    private EditText userName, passWord;
    private Button logIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_login);
        initView();
    }

    private void initView() {
        userName = (EditText) findViewById(R.id.et_username);
        passWord = (EditText) findViewById(R.id.et_userpassword);
        logIn = (Button) findViewById(R.id.btn_login);

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EMClient.getInstance().login(userName.getText().toString(),passWord.getText().toString(),new EMCallBack() {//回调
                    @Override
                    public void onSuccess() {
                        EMClient.getInstance().groupManager().loadAllGroups();
                        EMClient.getInstance().chatManager().loadAllConversations();
                        Log.d("main", "登录聊天服务器成功！");
                        Intent in=new Intent(LoginActivity.this,TestAct.class);
                        startActivity(in);
                        finish();
                    }

                    @Override
                    public void onProgress(int progress, String status) {
                        Log.d("main", "登录聊天服务器中！"+progress+"------"+status);
                    }

                    @Override
                    public void onError(int code, String message) {
                        Log.d("main", "登录聊天服务器失败！"+message);
                    }
                });
            }
        });
    }
}
