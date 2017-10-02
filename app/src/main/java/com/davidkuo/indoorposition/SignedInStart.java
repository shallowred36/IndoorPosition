package com.davidkuo.indoorposition;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.davidkuo.indoorposition.databinding.ActivitySignedinStartBinding;

/**
 * Created by shallow_red36 on 2017/02/09.
 */

public class SignedInStart extends AppCompatActivity {
    private ActivitySignedinStartBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_signedin_start);

        binding.user.setText(CognitoAuth.returnPool().getCurrentUser().getUserId());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
