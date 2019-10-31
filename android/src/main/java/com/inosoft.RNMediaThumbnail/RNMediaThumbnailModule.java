
package com.inosoft.RNMediaThumbnail;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import android.os.Build;
import android.net.Uri;
import android.util.Base64;
import android.content.ContentUris;
import android.content.Context;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.Date;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

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

			origin_uri = getRealPathFromURI(origin_uri);
			
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

			origin_uri = getRealPathFromURI(origin_uri);
			
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

			origin_uri = getRealPathFromURI(origin_uri);
			
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
	
	// https://github.com/Wraptime/react-native-get-real-path
	public String getRealPathFromURI(String uriString) {
		Uri uri = Uri.parse(uriString);
		try {
			Context context = this.getReactApplicationContext();
			final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
			if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
				if (isMediaDocument(uri)) {
					// http://www.banbaise.com/archives/745
					final String docId = DocumentsContract.getDocumentId(uri);
					final String[] split = docId.split(":");
					final String type = split[0];

					Uri contentUri = null;
					if ("image".equals(type)) {
						contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
					} else if ("video".equals(type)) {
						contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
					} else if ("audio".equals(type)) {
						contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
					}

					final String selection = "_id=?";
					final String[] selectionArgs = new String[] { split[1] };

					return getDataColumn(context, contentUri, selection, selectionArgs);
				} else if (isDownloadsDocument(uri)) {

					final String id = DocumentsContract.getDocumentId(uri);

					if (id.startsWith("raw:")) {
						return id.replaceFirst("raw:", "");
					} else {
						String[] contentUriPrefixesToTry = new String[] { "content://downloads/public_downloads",
								"content://downloads/my_downloads", "content://downloads/all_downloads" };

						String path = null;
						for (String contentUriPrefix : contentUriPrefixesToTry) {
							Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));
							try {
								path = getDataColumn(context, contentUri, null, null);
								if (path != null) {
									break;
								}
							} catch (Exception e) {
							}
						}

						if (path == null) {
							long millis = System.currentTimeMillis();
							String datetime = new Date().toString();
							datetime = datetime.replace(" ", "");
							datetime = datetime.replace(":", "");
							final String displayName = random() + "_" + datetime + "_" + millis;

							path = writeFile(context, uri, displayName.replace(".", ""));
						}

						return path;
					}
				} else if (isExternalStorageDocument(uri)) {
					final String docId = DocumentsContract.getDocumentId(uri);
					final String[] split = docId.split(":");
					final String type = split[0];

					if ("primary".equalsIgnoreCase(type)) {
						return (Environment.getExternalStorageDirectory() + "/" + split[1]);
					} else {
						String[] proj = { MediaStore.Images.Media.DATA };
						Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
						int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
						cursor.moveToFirst();
						String path = cursor.getString(column_index);
						cursor.close();

						return path;
					}
				}
			} else if ("content".equalsIgnoreCase(uri.getScheme())) {
				return getDataColumn(context, uri, null, null);
			} else if ("file".equalsIgnoreCase(uri.getScheme())) {
				return uri.getPath();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return uri.getPath();
	}

	public static String writeFile(Context context, Uri uri, String displayName) {
		InputStream input = null;
		try {
			input = context.getContentResolver().openInputStream(uri);
			/* save stream to temp file */
			try {
				File file = new File(context.getCacheDir(), displayName);
				OutputStream output = new FileOutputStream(file);
				try {
					byte[] buffer = new byte[4 * 1024]; // or other buffer size
					int read;

					while ((read = input.read(buffer)) != -1) {
						output.write(buffer, 0, read);
					}
					output.flush();

					final String outputPath = file.getAbsolutePath();
					return outputPath;

				} finally {
					output.close();
				}
			} catch (Exception e1a) {
				//
			} finally {
				try {
					input.close();
				} catch (IOException e1b) {
					//
				}
			}
		} catch (FileNotFoundException e2) {
			//
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e3) {
					//
				}
			}
		}

		return null;
	}

	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		// https://github.com/hiddentao/cordova-plugin-filepath/pull/6
		Cursor cursor = null;
		final String[] projection = { MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME };

		try {
			/* get `_data` */
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
				/* bingo! */
				final String filepath = cursor.getString(column_index);
				return filepath;
			}
		} catch (Exception e) {
			if (cursor != null) {
				final int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
				final String displayName = cursor.getString(column_index);

				return writeFile(context, uri, displayName);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}
	
	public static String random() {
		Random generator = new Random();
		StringBuilder randomStringBuilder = new StringBuilder();
		int randomLength = generator.nextInt(10);
		char tempChar;
		for (int i = 0; i < randomLength; i++) {
			tempChar = (char) (generator.nextInt(96) + 32);
			randomStringBuilder.append(tempChar);
		}
		return randomStringBuilder.toString();
	}

	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}
}