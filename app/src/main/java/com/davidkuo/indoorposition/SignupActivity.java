package com.davidkuo.indoorposition;

import android.app.ProgressDialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.davidkuo.indoorposition.databinding.ActivitySignupBinding;


/**
 * Created by shallow_red36 on 2017/02/05.
 */

public class SignupActivity extends AppCompatActivity {
    private final String TAG = "SignupActivity";
    private ActivitySignupBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup);

        binding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });
    }

    private void signup() {
        if (!validate()) {
            onSignupFailed();
            return;
        }

        binding.btnSignup.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Signing up...");
        progressDialog.show();

        CognitoUserAttributes userAttributes = new CognitoUserAttributes();
        String userId = binding.signupEmail.getText().toString();
        String nickname = binding.signupNickname.getText().toString();
        String password = binding.signupPassword.getText().toString();
        userAttributes.addAttribute("email", userId);
        userAttributes.addAttribute("nickname", nickname);
        CognitoAuth.returnPool().signUpInBackground(nickname, password, userAttributes, null, new SignUpHandler() {
            @Override
            public void onSuccess(CognitoUser user, boolean signUpConfirmationState, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        //Toast.makeText(SignupActivity.this, "Signup succeeded, please login", Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK, null);
                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    }
                }, 1500);
            }

            @Override
            public void onFailure(Exception exception) {
                final String[] strings = exception.getMessage().split("\\(");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(SignupActivity.this, strings[0], Toast.LENGTH_LONG).show();
                        binding.btnSignup.setEnabled(true);
                    }
                }, 1500);
                Log.e(TAG, exception.toString());
            }
        });
    }

    public void onSignupFailed() {
        Toast.makeText(SignupActivity.this, "Signup failed", Toast.LENGTH_LONG).show();
        binding.btnSignup.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = binding.signupEmail.getText().toString();
        String nickname = binding.signupNickname.getText().toString();
        String password = binding.signupPassword.getText().toString();
        String repassword = binding.signupRepassword.getText().toString();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.signupEmail.setError("enter a valid email address");
            valid = false;
        } else {
            binding.signupEmail.setError(null);
        }

        if (nickname.isEmpty() || nickname.length() < 3) {
            binding.signupNickname.setError("at least 3 characters");
            valid = false;
        } else {
            binding.signupNickname.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            binding.signupPassword.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            binding.signupPassword.setError(null);
            if (!password.equals(repassword)) {
                binding.signupRepassword.setError("passwords do not match");
                valid = false;
            } else {
                binding.signupRepassword.setError(null);
            }
        }

        return valid;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }
}
