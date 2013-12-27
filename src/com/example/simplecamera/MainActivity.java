package com.example.simplecamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
	protected Camera mCamera = null;
	private CameraPreview mPreview;
	protected String TAG = "main activity";
	public static final int MEDIA_TYPE_IMAGE = 1;
	protected Boolean preview_active;
	protected int cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		setPictureButton();
		setCameraSwapButton();
	}

	@Override
	protected void onResume() {
		super.onResume();
		cameraInit(cameraID);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
	}
	
	public void cameraInit(int camID) {
		
		mCamera = getCameraInstance(camID);
		cameraID = camID;
		
		// Create our Preview view and set it as the content of our
		// activity.
		mPreview = new CameraPreview(this, mCamera);
		preview_active = true;
		Log.i(TAG, "preview initilized");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void setPictureButton() {
		// Add a listener to the Capture button
		Button captureButton = (Button) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (preview_active) {
					// get an image from the camera
					mCamera.takePicture(null, null, mPicture);
					preview_active = false;
				} else {
					mCamera.stopPreview();
					mCamera.startPreview();
					preview_active = true;
				}
			}
		});
	}

	public void setCameraSwapButton() {
		// Add a listener to the Capture button
		Button captureButton = (Button) findViewById(R.id.button_swap);
		captureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
				preview.removeView(mPreview);

				if (cameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
					// switch to front facing camera
					cameraInit(Camera.CameraInfo.CAMERA_FACING_FRONT);
				} else {
					// switch to back facing camera
					cameraInit(Camera.CameraInfo.CAMERA_FACING_BACK);
				}
				preview.addView(mPreview);
			}
		});
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(int camID) {
		Camera c = null;
		try {
			c = Camera.open(camID); // attempt to get a Camera instance
			Log.i("camera util", "camera opened");
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			Log.e("camera init", "camera unavailable");
			return null;
		}

		return c; // returns null if camera is unavailable
	}

	@Override
	protected void onPause() {
		super.onPause();
		// surfaceDestroyed in CameraPreview is automatically called here
	}


	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
			Log.i("camera util", "camera released");
		}
	}

	// This part is where the picture is actually taken
	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			Log.i("TAG", "picture taken");

			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				Log.d(TAG,
						"Error creating media file, check storage permissions");
				return;
			}

			if (data.length == 0) {
				Log.d(TAG, "No picture data recived");
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();

				if (!pictureFile.exists()) {
					Log.d("TAG", "File not saved correctly");
				}

			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}
		}

	};

	// This part is for storing the photo
	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"SimpleCameraApp");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		String filepath = mediaStorageDir.getPath() + File.separator + "IMG_"
				+ timeStamp + ".jpg";
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(filepath);
			Log.d("photo save", "Saved in: " + filepath);

		} else {
			return null;
		}

		return mediaFile;
	}

}
