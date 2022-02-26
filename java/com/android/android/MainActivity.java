package com.iitacsa.shadetreeadvicetool;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener;
import com.google.android.play.core.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    ProgressBar progressbar;
    WebView webview;
    RelativeLayout no_internet_layout;
    private boolean doubleBackToExitPressedOnce;

    private AppUpdateManager mAppUpdateManager;
    private static final int RC_APP_UPDATE = 100;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressbar = findViewById(R.id.progressbar);
        webview = findViewById(R.id.webview);
        no_internet_layout = findViewById(R.id.no_internet_layout);

        WebSettings webSettings = webview.getSettings();
        webview.getSettings().setJavaScriptEnabled(true);
//        Improve WebView performance
        webview.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        //loading cache data
        webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webview.getSettings().setAppCacheEnabled(true);
        webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
//        WebSettings.setUseWideViewPort(true);
        webSettings.setSavePassword(true);
        webSettings.setSaveFormData(true);
        webSettings.setEnableSmoothTransition(true);
//        launch in app itself
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressbar.setVisibility(view.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressbar.setVisibility(view.GONE);
            }
        });

        loadWebPage();


        mAppUpdateManager = AppUpdateManagerFactory.create(this);

        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>()
        {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if(result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && result.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    try {
                        mAppUpdateManager.startUpdateFlowForResult(result,AppUpdateType.FLEXIBLE, MainActivity.this
                                ,RC_APP_UPDATE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Before starting an update, register a listener for updates in onCreate() method
        mAppUpdateManager.registerListener(installStateUpdatedListener);
    }


    //    page loader function
    private void loadWebPage() {

        ConnectivityManager cm = (ConnectivityManager) MainActivity.this
                .getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();


        if (networkInfo!=null && networkInfo.isConnectedOrConnecting()) {


				// Please place your URL below.
            webview.loadUrl("https://.....");

            no_internet_layout.setVerticalGravity(View.GONE);
            webview.setVisibility(View.VISIBLE);
        } else {
            no_internet_layout.setVisibility(View.VISIBLE);
            webview.setVisibility(View.GONE);
            Toast.makeText(this, "You Are Offline", Toast.LENGTH_SHORT).show();
        }
    }



    //      now on back press
    @Override
    public void onBackPressed() {
        if (webview.canGoBack()){
            webview.goBack();
        }
        else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
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


    public void ReconnectWebSite(View view) {
//        loadWebPage();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }








    private InstallStateUpdatedListener installStateUpdatedListener =new InstallStateUpdatedListener()
    {
        @Override
        public void onStateUpdate(InstallState state)
        {
            if(state.installStatus() == InstallStatus.DOWNLOADED)
            {
                showCompletedUpdate();
            }
        }
    };


    @Override
    protected void onStop()
    {
        if(mAppUpdateManager!=null) mAppUpdateManager.unregisterListener(installStateUpdatedListener);
        super.onStop();
    }


    private void showCompletedUpdate()
    {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"New app is ready!",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Install", new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mAppUpdateManager.completeUpdate();
            }
        });
        snackbar.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        /* we can check without requestCode == RC_APP_UPDATE because
    we known exactly there is only requestCode from  startUpdateFlowForResult() */
        if(requestCode == RC_APP_UPDATE && resultCode != RESULT_OK)
        {
            Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
