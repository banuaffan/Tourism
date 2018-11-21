package com.bnadev.tourism.detail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bnadev.tourism.R;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.tvName)TextView tvName;
    @BindView(R.id.tvAddress)TextView tvAddress;
    @BindView(R.id.ivImage)ImageView ivImage;
    @BindView(R.id.tvDetail)TextView tvDetail;

    String name;
    String address;
    String detail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();

        name = intent.getStringExtra("name");
        address = intent.getStringExtra("address");
        detail = intent.getStringExtra("detail");
        String image = intent.getStringExtra("image");

        tvName.setText(name);
        tvAddress.setText(address);
        Picasso.with(this).load(image).into(ivImage);
        tvDetail.setText(detail);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.direction:

                SharedPreferences mLocation = DetailActivity.this.getSharedPreferences("location", Context.MODE_PRIVATE);
                String latitude = mLocation.getString("latitude", "-6.21462");
                String longitude = mLocation.getString("longitude", "106.84513");

                String uri = "http://maps.google.com/maps?f=d&hl=en&saddr="+latitude+","+longitude+"&daddr="+ address;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
