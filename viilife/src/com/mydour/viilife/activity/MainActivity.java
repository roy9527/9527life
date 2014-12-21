package com.mydour.viilife.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mydour.viilife.R;
import com.mydour.viilife.utils.Config;
import com.mydour.viilife.utils.CrashHandler;

public class MainActivity extends FragmentActivity {

	private WebView wView;

	private WebChromeClient.CustomViewCallback mCustomViewCallback;

	private FragmentManager manager;

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.main_layout);
		manager = getSupportFragmentManager();
		CrashHandler.getInstance().init();
		initView();
		startLoad();
	}

	@SuppressWarnings("deprecation")
	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	private void initView() {
		wView = (WebView) findViewById(R.id.main_web);

		wView.getSettings().setJavaScriptEnabled(true);
		wView.getSettings().setUseWideViewPort(false);
		wView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		wView.getSettings().setBuiltInZoomControls(false);
		wView.getSettings().setCacheMode(
				android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK);
		wView.getSettings().setSupportMultipleWindows(true);
		wView.getSettings().setDomStorageEnabled(true);
		wView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
		String appCachePath = getApplicationContext().getCacheDir()
				.getAbsolutePath();
		wView.getSettings().setAllowContentAccess(true);
		wView.getSettings().setPluginState(PluginState.ON);
		wView.getSettings().setAppCachePath(appCachePath);
		wView.getSettings().setAllowFileAccess(true);
		wView.getSettings().setAppCacheEnabled(true);
		wView.setWebChromeClient(chromeClient);
		wView.setWebViewClient(new WebViewClient() {

			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view,
					String url) {
				return super.shouldInterceptRequest(view, url);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				showLoading(false);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				showLoading(true);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (Config.DEBUG) {
					Log.i("viilife", url);
				}
				// TODO deal with url.
				view.loadUrl(url);
				return true;
			}

		});
	}

	private WebChromeClient chromeClient = new WebChromeClient() {

		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			showDisplayWindow(view, callback);
		}

		@Override
		public void onHideCustomView() {
			hideDisplayWindow();
		}

	};

	private void showDisplayWindow(View view, CustomViewCallback callback) {
		mCustomViewCallback = callback;
		FragmentTransaction ft = manager.beginTransaction();
		Fragment prev = manager.findFragmentByTag("show_display");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);
		VideoFragment vf = new VideoFragment();
		vf.setCustomView(view);
		ft.replace(R.id.show_video, vf, "show_display")
				.commitAllowingStateLoss();
	}

	private void hideDisplayWindow() {
		FragmentTransaction ft = manager.beginTransaction();
		Fragment prev = manager.findFragmentByTag("show_display");
		if (prev != null) {
			ft.remove(prev).commitAllowingStateLoss();
		}
		
		if (manager.getBackStackEntryCount() > 0) {
			manager.popBackStackImmediate();
			return;
		}
		
		if (mCustomViewCallback != null) {
			mCustomViewCallback.onCustomViewHidden();
		}
	}
	
	private void showLoading(boolean show) {
		findViewById(R.id.loading).setVisibility(
				show ? View.VISIBLE : View.GONE);
	}

	private void startLoad() {
		wView.loadUrl(Config.INDEX_URL);
	}

	@Override
	public void onBackPressed() {
		if (manager.getBackStackEntryCount() > 0) {
			manager.popBackStackImmediate();
			return;
		}
		if (wView.canGoBack()) {
			wView.goBack();
			return;
		}
		super.onBackPressed();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			startActivity(new Intent(this, CaptureActivity.class));
		}
		return super.onKeyDown(keyCode, event);
	}

}
