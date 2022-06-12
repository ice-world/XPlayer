package com.leon.xplayer;

import static com.leon.xplayer.Lib.config.LOGIN_EXTRA_PASSWORD;
import static com.leon.xplayer.Lib.config.LOGIN_EXTRA_USERNAME;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.leon.xplayer.DB.UserConnect;

public class LoginActivity extends AppCompatActivity {

    Button bt_login;
    Button bt_register;

    TextView tv_user;
    TextView tv_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bt_login = findViewById(R.id.loginA_bt_login);
        bt_register = findViewById(R.id.loginA_bt_register);

        tv_user = findViewById(R.id.loginA_et_user);
        tv_pass = findViewById(R.id.LoginA_et_pass);

        bt_login.setOnClickListener(view -> new Thread(() -> {
            String user = tv_user.getText().toString();
            String pass = tv_pass.getText().toString();

            if (user.equals("") || pass.equals("")) {
                Looper.prepare();
                Toast.makeText(this, "输入不能为空！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            boolean res = UserConnect.login(user, pass);
            if (!res) {
                Looper.prepare();
                Toast.makeText(this, "用户名不存在或密码错误！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            } else {
                Intent userInfo = new Intent() {
                    {
                        putExtra(LOGIN_EXTRA_USERNAME, user);
                        putExtra(LOGIN_EXTRA_PASSWORD, pass);
                    }
                };
                setResult(Activity.RESULT_OK, userInfo);
                finish();
            }
        }).start());

        bt_register.setOnClickListener(view -> new Thread(() -> {
            String user = tv_user.getText().toString();
            String pass = tv_pass.getText().toString();
            if (user.equals("") || pass.equals("")) {
                Looper.prepare();
                Toast.makeText(this, "输入不能为空！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            boolean res = UserConnect.register(user, pass);
            if (!res) {
                Looper.prepare();
                Toast.makeText(this, "注册成功！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start());
    }
}