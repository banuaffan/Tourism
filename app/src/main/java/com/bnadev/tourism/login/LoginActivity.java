package com.bnadev.tourism.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bnadev.tourism.main.MainActivity;
import com.bnadev.tourism.R;
import com.bnadev.tourism.model.User;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    @BindView(R.id.ivLogo)ImageView ivLogo;
    @BindView(R.id.etName)EditText etName;
    @BindView(R.id.etEmail)EditText etEmail;
    @BindView(R.id.etPassword)EditText etPassword;
    @BindView(R.id.btSignIn)Button btSignIn;
    @BindView(R.id.btSignUp)Button btSignUp;
    @BindView(R.id.btSignInGoogle)SignInButton btSignInGoogle;

    private ProgressDialog mProgressDialog;

    boolean doubleBackToExitPressedOnce = false;
    boolean clickSignUp = false;
    boolean clickSignIn = true;

    private static final int RC_SIGN_IN = 007;
    private GoogleApiClient mGoogleApiClient;

    Handler mHandler = new Handler();

    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        database = FirebaseDatabase.getInstance().getReference();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        btSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                etName.setVisibility(View.GONE);
                ConstraintLayout.LayoutParams param = (ConstraintLayout.LayoutParams)etEmail.getLayoutParams();
                param.setMargins(24,16,24,4);
                etEmail.setLayoutParams(param);

                signIn();
            }
        });

        btSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                etName.setVisibility(View.VISIBLE);
                ConstraintLayout.LayoutParams param = (ConstraintLayout.LayoutParams)etEmail.getLayoutParams();
                param.setMargins(24,100,24,4);
                etEmail.setLayoutParams(param);

                signUp();
            }
        });

        btSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInGoogle();
            }
        });


    }

    private void submitUser(User user) {

        database.child("user").push().setValue(user).addOnSuccessListener(this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                etName.setText("");
                etEmail.setText("");
                etPassword.setText("");
                Toast.makeText(getApplicationContext(),"Sign Up Success! Logged In!",Toast.LENGTH_SHORT).show();

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(LoginActivity.this,MainActivity.class));
                        finish();
                    }
                },1000);

            }
        });
    }

    private void signUp(){

        if(!TextUtils.isEmpty(etName.getText().toString())&&!TextUtils.isEmpty(etEmail.getText().toString())&&!TextUtils.isEmpty(etPassword.getText().toString())){

            SharedPreferences mUser = LoginActivity.this.getSharedPreferences("user",Context.MODE_PRIVATE);
            SharedPreferences.Editor mEditor = mUser.edit();
            mEditor.putString("name",etName.getText().toString());
            mEditor.putString("email",etEmail.getText().toString());
            mEditor.putString("password",etPassword.getText().toString());
            mEditor.apply();

            submitUser(new User(etName.getText().toString(),etEmail.getText().toString(),etPassword.getText().toString()));

        }else {
            Toast.makeText(getApplicationContext(),"Everything Must Filled!",Toast.LENGTH_SHORT).show();
        }
    }

    private void signIn(){
        if(!TextUtils.isEmpty(etEmail.getText().toString())&&!TextUtils.isEmpty(etPassword.getText().toString())){

            String email = etEmail.getText().toString().trim();
            final String password = etPassword.getText().toString().trim();

            Query query = database.child("user").orderByChild("email").equalTo(email);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){

                        for(DataSnapshot userDs : dataSnapshot.getChildren()){

                            User user = userDs.getValue(User.class);

                            if (user.getPassword().equals(password)){
                                Toast.makeText(getApplicationContext(),"Sign In Succes!",Toast.LENGTH_SHORT).show();

                                SharedPreferences mUser = LoginActivity.this.getSharedPreferences("user",Context.MODE_PRIVATE);
                                SharedPreferences.Editor mEditor = mUser.edit();
                                mEditor.putString("name",user.getName());
                                mEditor.putString("email",user.getEmail());
                                mEditor.putString("password",user.getPassword());
                                mEditor.apply();

                                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                                finish();
                            }else {
                                Toast.makeText(getApplicationContext(),"Wrong Password!",Toast.LENGTH_SHORT).show();
                            }
                        }

                    }else {
                        Toast.makeText(getApplicationContext(),"Email Not Found!",Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }else {
            Toast.makeText(getApplicationContext(),"Everything Must Filled!",Toast.LENGTH_SHORT).show();
        }
    }

    private void signInGoogle(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    private void handleResult(GoogleSignInResult result){
        if (result.isSuccess()){

            GoogleSignInAccount hasil = result.getSignInAccount();

            String name = hasil.getDisplayName();
            String email = hasil.getEmail();
            final String url;
            if (hasil.getPhotoUrl() == null) {
                url = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png";
            } else {
                url = hasil.getPhotoUrl().toString();
            }
            String photoUrl = url;
            String password = hasil.getDisplayName() + hasil.getEmail();

            SharedPreferences mUser = LoginActivity.this.getSharedPreferences("user",Context.MODE_PRIVATE);
            SharedPreferences.Editor mEditor = mUser.edit();
            mEditor.putString("name",name);
            mEditor.putString("email",email);
            mEditor.putString("photo",photoUrl);
            mEditor.putString("password",password);
            mEditor.apply();

            Toast.makeText(this, "Success Sign In Using Google!", Toast.LENGTH_SHORT).show();

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    finish();
                }
            },1000);

        }else {

        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Sign In Google ","onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}
