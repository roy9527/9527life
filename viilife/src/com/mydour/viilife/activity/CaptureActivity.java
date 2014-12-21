package com.mydour.viilife.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.mydour.viilife.R;

public class CaptureActivity extends FragmentActivity {

	// private ImageView imageView;
	private WebView webView;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.capture_layout);
		init();
	}

	@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
	private void init() {
		// imageView = (ImageView) findViewById(R.id.capture_img);
		webView = (WebView) findViewById(R.id.web_capture);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.loadUrl("file:///android_asset/capture.html");
		webView.addJavascriptInterface(this, "vii");
	}

	@JavascriptInterface
	public void onCapture() {
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(
				Environment.getExternalStorageDirectory(), "camera.jpg")));
		startActivityForResult(intent, 9527);
	}

	@JavascriptInterface
	public void onFetchPic() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		startActivityForResult(intent, 9527);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 9527 && resultCode == Activity.RESULT_OK) {
			Bitmap myBitmap = null;
			String filePath = "";
			if (data == null) {
				filePath = new File(Environment.getExternalStorageDirectory(),
						"camera.jpg").getAbsolutePath();
				myBitmap = justifyPic(filePath, true);
			} else {
				Uri uri = data.getData();
				if (uri != null) {
					try {
						String[] proj = { MediaStore.Images.Media.DATA };

						ContentResolver resolver = getContentResolver();

						Cursor cursor = resolver.query(uri, proj, null, null,
								null);

						int column_index = cursor
								.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

						cursor.moveToFirst();

						filePath = cursor.getString(column_index);
						cursor.close();
						myBitmap = justifyPic(filePath, false);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}

			if (myBitmap != null) {
				// imageView.setImageBitmap(myBitmap);
				webView.loadUrl("javascript:uploadPic('" + filePath + "')");
				myBitmap.recycle();
			}
		}
	}

	private Bitmap justifyPic(String filePath, boolean compress) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		options.inSampleSize = calculateInSampleSize(options, 960, 1600);

		options.inJustDecodeBounds = false;
		Bitmap bm = BitmapFactory.decodeFile(filePath, options);
		if (compress) {
			FileOutputStream fos = null;
			try {

				File f = new File(filePath);
				if (f.exists()) {
					f.delete();
				}
				f.createNewFile();
				fos = new FileOutputStream(filePath);
				bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (fos != null)
						fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		int ro = readPictureDegree(filePath);
		bm = rotateBitmap(bm, ro);
		return bm;
	}

	private int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
		}

		return inSampleSize;
	}

	private int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	private Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
		if (bitmap == null)
			return null;

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix mtx = new Matrix();
		mtx.postRotate(rotate);
		return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
	}
}
