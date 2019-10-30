
package com.inosoft.RNMediaThumbnail;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import android.os.Build;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.util.Base64;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import java.util.Map;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import java.io.File;
import java.io.FileOutputStream;

public class RNMediaThumbnailModule extends ReactContextBaseJavaModule {

	public static final int THUMBNAIL_WIDTH = 600;
	public static final int THUMBNAIL_HEIGHT = 600;
	private final ReactApplicationContext reactContext;

	public RNMediaThumbnailModule(ReactApplicationContext reactContext) {
		super(reactContext);
		this.reactContext = reactContext;
	}

	@Override
	public String getName() {
		return "RNMediaThumbnail";
	}
	
	@ReactMethod
	public void saveImageThumb(String origin_uri, String destination_uri, boolean convertToJPEG, Callback successCallback, Callback errorCallback) {
		try {
			WritableMap params = convertImage(origin_uri, destination_uri, convertToJPEG);
			
			successCallback.invoke(params);
		} catch (Exception e) {
			errorCallback.invoke(e.getMessage());
		}
	}

	@ReactMethod
	public void getImageThumb(String origin_uri, boolean convertToJPEG, Callback successCallback, Callback errorCallback) {
		try {
			WritableMap params = convertImage(origin_uri, "Base64", convertToJPEG);
			
			successCallback.invoke(params);
		} catch (Exception e) {
			errorCallback.invoke(e.getMessage());
		}
	}

	@ReactMethod
	public void saveVideoThumb(String origin_uri, String destination_uri, boolean convertToJPEG, Callback successCallback, Callback errorCallback) {
		try {
			WritableMap params = convertVideo(origin_uri, destination_uri, convertToJPEG);
			
			successCallback.invoke(params);
		} catch (Exception e) {
			errorCallback.invoke(e.getMessage());
		}
	}

	@ReactMethod
	public void getVideoThumb(String origin_uri, boolean convertToJPEG, Callback successCallback, Callback errorCallback) {
		try {
			WritableMap params = convertVideo(origin_uri, "Base64", convertToJPEG);
			
			successCallback.invoke(params);
		} catch (Exception e) {
			errorCallback.invoke(e.getMessage());
		}
	}

//	@ReactMethod
//	public void saveAudioThumb(String origin_uri, String destination_uri, boolean convertToJPEG, Callback successCallback, Callback errorCallback) {
//		try {
//			WritableMap params = convertAudio(origin_uri, destination_uri, convertToJPEG);
//			
//			successCallback.invoke(params);
//		} catch (Exception e) {
//			errorCallback.invoke(e.getMessage());
//		}
//	}

	@ReactMethod
	public void getAudioThumb(String origin_uri, boolean convertToJPEG, Callback successCallback, Callback errorCallback) {
		try {
			WritableMap params = convertAudio(origin_uri, "Base64", convertToJPEG);
			
			successCallback.invoke(params);
		} catch (Exception e) {
			errorCallback.invoke(e.getMessage());
		}
	}
	
	private CompressFormat getCompressFormat(boolean convertToJPEG) {
		CompressFormat cFormat = convertToJPEG ? CompressFormat.JPEG : CompressFormat.PNG;
		
		return cFormat;
	}
	
	private WritableMap convertImage(String origin_uri, String destination_uri, boolean convertToJPEG) {
		WritableMap params = new WritableNativeMap();
		
		try {
			CompressFormat cFormat = getCompressFormat(convertToJPEG);
			
			// Decode image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap original = BitmapFactory.decodeFile(origin_uri, options);

			int width = options.outWidth;
			int height = options.outHeight;
			
			int dwidth = THUMBNAIL_WIDTH;
			int dheight = THUMBNAIL_HEIGHT;
			
			if(width < THUMBNAIL_WIDTH) {
				dwidth = width;
				dheight = width;
			}

			Map ratio = resizeKeepingRatio(width, height, dwidth, dheight);
			
			//Bitmap thumb = Bitmap.createScaledBitmap(original, (int) ratio.get("width"), (int) ratio.get("height"), false);
			Bitmap thumb = ThumbnailUtils.extractThumbnail(original, (int) ratio.get("width"), (int) ratio.get("height"));

			WritableMap thumbMap = new WritableNativeMap();
			thumbMap.putInt("width", thumb.getWidth());
			thumbMap.putInt("height", thumb.getHeight());
			
			if(destination_uri == "Base64") {
				thumbMap.putString("base64", bitmapToBase64(thumb, cFormat));
			} else {
				File file = new File(destination_uri);
				thumb.compress(cFormat, 90, new FileOutputStream(file));
				thumbMap.putString("uri", destination_uri);
			}
			
			params.putMap("thumbnail", thumbMap);
			params.putInt("width", width);
			params.putInt("height", height);
		} catch (Exception e) {
			e.printStackTrace();
			params.putString("error", e.getMessage());
		}
		
		return params;
	}
	
	private WritableMap convertVideo(String origin_uri, String destination_uri, boolean convertToJPEG) {
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();

		WritableMap params = new WritableNativeMap();
		
		try {
			CompressFormat cFormat = getCompressFormat(convertToJPEG);
			
			boolean checkBuildVersion = false;
			if (checkBuildVersion && Build.VERSION.SDK_INT >= 14) {
				retriever.setDataSource(origin_uri, new HashMap<String, String>());
			} else {
				retriever.setDataSource(origin_uri);
			}

			Bitmap original = retriever.getFrameAtTime(1000000); //unit in microsecond: 1000000 mic = 1 sec

			long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
			int width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
			int height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
			
			int dwidth = THUMBNAIL_WIDTH;
			int dheight = THUMBNAIL_HEIGHT;
			
			if(width < THUMBNAIL_WIDTH) {
				dwidth = width;
				dheight = width;
			}

			Map ratio = resizeKeepingRatio(width, height, dwidth, dheight);
			
			//Bitmap thumb = Bitmap.createScaledBitmap(original, (int) ratio.get("width"), (int) ratio.get("height"), false);
			Bitmap thumb = ThumbnailUtils.extractThumbnail(original, (int) ratio.get("width"), (int) ratio.get("height"));

			WritableMap thumbMap = new WritableNativeMap();
			thumbMap.putInt("width", thumb.getWidth());
			thumbMap.putInt("height", thumb.getHeight());
			
			if(destination_uri == "Base64") {
				thumbMap.putString("base64", bitmapToBase64(thumb, cFormat));
			} else {
				File file = new File(destination_uri);
				thumb.compress(cFormat, 90, new FileOutputStream(file));
				thumbMap.putString("uri", destination_uri);
			}
			
			params.putMap("thumbnail", thumbMap);
			params.putInt("width", width);
			params.putInt("height", height);
			params.putDouble("duration", duration);
		} catch (Exception e) {
			e.printStackTrace();
			params.putString("error", e.getMessage());
		} finally {
			try {
				retriever.release();
			} catch (Exception e) {
				e.printStackTrace();
				params.putString("error", e.getMessage());
			}
		}
		
		return params;
	}
	
	private WritableMap convertAudio(String origin_uri, String destination_uri, boolean convertToJPEG) {
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();

		WritableMap params = new WritableNativeMap();
		
		try {
			CompressFormat cFormat = getCompressFormat(convertToJPEG);
			
			boolean checkBuildVersion = false;
			if (checkBuildVersion && Build.VERSION.SDK_INT >= 14) {
				retriever.setDataSource(origin_uri, new HashMap<String, String>());
			} else {
				retriever.setDataSource(origin_uri);
			}
			
			long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
			
			Bitmap original = extractAlbumArt(retriever);
			
			if(original != null) {
				params.putInt("width", original.getWidth());
				params.putInt("height", original.getHeight());
				
				if(destination_uri == "Base64") {
					params.putString("base64", bitmapToBase64(original, cFormat));
				} else {
					File file = new File(destination_uri);
					original.compress(Bitmap.CompressFormat.PNG, 90, new FileOutputStream(file));
					params.putString("uri", destination_uri);
				}
				
				int width = original.getWidth();
				int height = original.getHeight();
				
				int dwidth = THUMBNAIL_WIDTH;
				int dheight = THUMBNAIL_HEIGHT;
				
				if(width < THUMBNAIL_WIDTH) {
					dwidth = width;
					dheight = width;
				}
	
				Map ratio = resizeKeepingRatio(width, height, dwidth, dheight);
				
				//Bitmap thumb = Bitmap.createScaledBitmap(original, (int) ratio.get("width"), (int) ratio.get("height"), false);
				Bitmap thumb = ThumbnailUtils.extractThumbnail(original, (int) ratio.get("width"), (int) ratio.get("height"));
	
				WritableMap thumbMap = new WritableNativeMap();
				thumbMap.putInt("width", thumb.getWidth());
				thumbMap.putInt("height", thumb.getHeight());
				
				if(destination_uri == "Base64") {
					thumbMap.putString("base64", bitmapToBase64(thumb, cFormat));
				} else {
					File file = new File(destination_uri);
					thumb.compress(cFormat, 90, new FileOutputStream(file));
					thumbMap.putString("uri", destination_uri);
				}
				
				params.putMap("thumbnail", thumbMap);
			}
			
			params.putDouble("duration", duration);
		} catch (Exception e) {
			e.printStackTrace();
			params.putString("error", e.getMessage());
		} finally {
			try {
				retriever.release();
			} catch (Exception e) {
				e.printStackTrace();
				params.putString("error", e.getMessage());
			}
		}
		
		return params;
	}
	
	private Bitmap extractAlbumArt(MediaMetadataRetriever audioMetadataRetriever) {
		byte[] audioAlbumArtBytes = audioMetadataRetriever.getEmbeddedPicture();
		if (audioAlbumArtBytes == null) {
			return null;
		}
		
		return BitmapFactory.decodeByteArray(audioAlbumArtBytes, 0, audioAlbumArtBytes.length);
	}

	public Map<String, Integer> resizeKeepingRatio(int width, int height, int destWidth, int destHeight) {
		Map<String, Integer> RatioMap = new HashMap<String, Integer>();

		Float ratioW = (float) width / destWidth;
		Float ratioH = (float) height / destHeight;
		if (ratioW <= 1 && ratioH <= 1) {
			Float ratio = (1 / ((ratioW > ratioH) ? ratioW : ratioH));
			width *= ratio;
			height *= ratio;
		} else if (ratioW > 1 && ratioH <= 1) {
			Float ratio = (1 / ratioW);
			width *= ratio;
			height *= ratio;
		} else if (ratioW <= 1 && ratioH > 1) {
			Float ratio = (1 / ratioH);
			width *= ratio;
			height *= ratio;
		} else if (ratioW >= 1 && ratioH >= 1) {
			Float ratio = (1 / ((ratioW > ratioH) ? ratioW : ratioH));
			width *= ratio;
			height *= ratio;
		}

		RatioMap.put("width", width);
		RatioMap.put("height", height);

		return RatioMap;
	}

	private String bitmapToBase64(Bitmap bitmap, CompressFormat cFormat) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		bitmap.compress(cFormat, 90, byteArrayOutputStream);
		byte[] byteArray = byteArrayOutputStream.toByteArray();
		return Base64.encodeToString(byteArray, Base64.DEFAULT);
	}
}