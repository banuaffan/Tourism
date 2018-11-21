package com.bnadev.tourism.splash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bnadev.tourism.R;
import com.bnadev.tourism.login.LoginActivity;
import com.bnadev.tourism.main.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {

    @BindView(R.id.tvHead)
    TextView tvHead;

    @BindView(R.id.ivLogo)
    ImageView ivLogo;

    @BindView(R.id.tvLoadStatus)
    TextView tvLoadStatus;

    Handler mHandler = new Handler();
    String name;
    String email;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ButterKnife.bind(this);

        SharedPreferences mUser = SplashActivity.this.getSharedPreferences("user",Context.MODE_PRIVATE);

        if(mUser.getString("email",null) != null){
            name = mUser.getString("name", "guest");
            email = mUser.getString("email", "guest@mail.com");
            password = mUser.getString("password", "guest");

            tvLoadStatus.setText("Welcome Back " + name);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                    finish();
                }
            },1000);

        } else {

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                    finish();
                }
            },1000);

        }

    }
}
