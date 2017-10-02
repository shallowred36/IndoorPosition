package com.davidkuo.indoorposition;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException;
import com.davidkuo.indoorposition.databinding.ActivityLoginBinding;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class LoginActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "LoginPrefsFile";
    private static final int REQUEST_SIGNUP = 0;
    private final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        CognitoAuth.init(getApplicationContext());

        //Restore email and password
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, 0);
        binding.inputEmail.getText().clear();
        binding.inputEmail.getText().append(sp.getString("Email", ""));
        binding.inputPassword.getText().clear();
        binding.inputPassword.getText().append(sp.getString("Password", ""));

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                login();
            }
        });

        binding.linkSignup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void login() {
        if (!validate()) {
            onLoginFailed();
            return;
        }

        binding.btnLogin.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Signing in...");
        progressDialog.show();

        final CognitoUser user = CognitoAuth.returnPool().getUser();
        user.getSessionInBackground(new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice device) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences sp = getSharedPreferences(PREFS_NAME, 0);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("Email", binding.inputEmail.getText().toString());
                        editor.putString("Password", binding.inputPassword.getText().toString());
                        editor.commit();
                        progressDialog.dismiss();
                        binding.btnLogin.setEnabled(true);
                        Intent intent = new Intent(getApplicationContext(), SignedInStart.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    }
                }, 1500);
                Log.d(TAG, "Login successful");
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String UserId) {
                AuthenticationDetails authenticationDetails = new AuthenticationDetails(binding.inputEmail.getText().toString(), binding.inputPassword.getText().toString(), null);
                authenticationContinuation.setAuthenticationDetails(authenticationDetails);
                authenticationContinuation.continueTask();
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {

            }

            @Override
            public void onFailure(Exception exception) {
                final Exception e = exception;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        if (e instanceof UserNotFoundException)
                            Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_LONG).show();
                        else if (e instanceof NotAuthorizedException)
                            Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(LoginActivity.this, e.getMessage().split("\\(")[0], Toast.LENGTH_LONG).show();
                        binding.btnLogin.setEnabled(true);
                    }
                }, 1500);
                Log.e(TAG, e.getMessage());
            }

            @Override
            public void authenticationChallenge(final ChallengeContinuation continuation) {
                if (continuation.getChallengeName().equals("NEW_PASSWORD_REQUIRED")) {
                    progressDialog.dismiss();
                    final AlertDialog.Builder newPasswordDialogBuilder = new AlertDialog.Builder(LoginActivity.this, R.style.DialogTheme);
                    newPasswordDialogBuilder.setCancelable(false);
                    newPasswordDialogBuilder.setMessage("Enter new password");
                    final View newPasswordInput = LayoutInflater.from(LoginActivity.this).inflate(R.layout.dialog_edittext_layout, null);
                    newPasswordDialogBuilder.setView(newPasswordInput);
                    newPasswordDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    final AlertDialog newPasswordDialog = newPasswordDialogBuilder.create();
                    newPasswordDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            Button okButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                            okButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    EditText newPasswordEditText = (EditText) newPasswordInput.findViewById(R.id.input_new_challenge);
                                    String newPassword = newPasswordEditText.getText().toString();
                                    if (newPassword.isEmpty() || newPassword.length() < 4 || newPassword.length() > 10) {
                                        Toast.makeText(LoginActivity.this, "between 4 and 10 alphanumeric characters", Toast.LENGTH_LONG).show();
                                        newPasswordEditText.getText().clear();
                                    } else {
                                        newPasswordDialog.dismiss();
                                        progressDialog.show();
                                        /*Map<String, String> requiredAttrsList = ((NewPasswordContinuation) continuation).getCurrentUserAttributes();
                                        Iterator<Map.Entry<String, String>> requiredAttrs = requiredAttrsList.entrySet().iterator();
                                        while (requiredAttrs.hasNext()) {
                                            Map.Entry<String, String> attr = requiredAttrs.next();
                                            Log.d(TAG, attr.getKey() + " " + attr.getValue());
                                        }*/
                                        ((NewPasswordContinuation) continuation).setUserAttribute("nickname", "admin-created");
                                        ((NewPasswordContinuation) continuation).setPassword(newPassword);
                                        continuation.continueTask();
                                    }
                                }
                            });
                        }
                    });
                    newPasswordDialog.show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) Toast.makeText(LoginActivity.this, "Signup succeeded, please login", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.btnLogin.isEnabled()) super.onBackPressed();
    }

    public void onLoginFailed() {
        Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_LONG).show();
        binding.btnLogin.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = binding.inputEmail.getText().toString();
        String password = binding.inputPassword.getText().toString();

        if (email.isEmpty()) {
            binding.inputEmail.setError("enter a valid nickname/email");
            valid = false;
        } else {
            binding.inputEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            binding.inputPassword.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            binding.inputPassword.setError(null);
        }

        return valid;
    }
}
