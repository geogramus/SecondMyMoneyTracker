package com.loftscholl.mymoneytrackertwo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.loftscholl.mymoneytrackertwo.api.AuthResult;

import java.io.IOException;

public class AuthActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 999;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        findViewById(R.id.auth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(Auth.GoogleSignInApi.getSignInIntent(googleApiClient), RC_SIGN_IN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess() && result.getSignInAccount() != null) {
                final GoogleSignInAccount account = result.getSignInAccount();
                getSupportLoaderManager().restartLoader(0, null, new LoaderManager.LoaderCallbacks<AuthResult>() {
                    @Override
                    public Loader<AuthResult> onCreateLoader(int id, Bundle args) {
                        return new AsyncTaskLoader<AuthResult>(AuthActivity.this) {
                            @Override
                            public AuthResult loadInBackground() {
                                try {
                                    return ((LSApp) getApplicationContext()).api().auth(account.getId()).execute().body();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        };
                    }

                    @Override
                    public void onLoadFinished(Loader<AuthResult> loader, AuthResult result) {
                        if (result != null && result.isSuccess()) {
                            ((LSApp) getApplicationContext()).setAuthToken(result.authToken);
                            finish();
                        } else
                            showError();
                    }

                    @Override
                    public void onLoaderReset(Loader<AuthResult> loader) {
                    }
                }).forceLoad();
            } else
                showError();
        }
    }


    private void showError() {
        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
    }

}

