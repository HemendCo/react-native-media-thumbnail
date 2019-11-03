
package com.inosoft.RNMediaThumbnail;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.ReadableMap;

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
import android.webkit.MimeTypeMap;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.Date;
import java.util.Arrays;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;

public class RNMediaThumbnailModule extends ReactContextBaseJavaModule {

	private final ReactApplicationContext reactContext;
	private final GRP grp;
	
	static final int TYPE_IMAGE = 1;
	static final int TYPE_VIDEO = 2;
	static final int TYPE_AUDIO = 3;
	
	static final String[] EXPORT_TYPES = {"jpg", "png"};
	
	static final String POSTER_EXPORT_TYPE = "png";
	static final String THUMBNAIL_EXPORT_TYPE = "jpg";

	public RNMediaThumbnailModule(ReactApplicationContext reactContext) {
		super(reactContext);
		this.reactContext = reactContext;
		this.grp = new GRP(reactContext);
	}

	@Override
	public String getName() {
		return "RNMediaThumbnail";
	}
	
	@ReactMethod
	public void getMedia(String uri, ReadableMap options, Callback successCallback, Callback errorCallback) throws Exception {
		try {
			uri = this.grp.getRealPathFromURI(uri);
			
			if(uri == null) {
				throw new Exception("'uri' is not valid");
			}
			
			WritableMap fd = getFileData(uri);
			
			if (fd.getInt("mediaType") == 0) {
				throw new Exception("Media type is not support. allowed types: 'Image', 'Video', 'Audio'");
			}
			
			WritableMap params = getData(uri, fd.getInt("mediaType"), options);
			
			successCallback.invoke(params);
		} catch (Exception e) {
			errorCallback.invoke(e.getMessage());
		}
	}
	
	private WritableMap getData(String uri, Integer mediaType, ReadableMap options) throws Exception {
		WritableMap params = new WritableNativeMap();
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		
		ReadableMap poster = options.hasKey("poster") ? options.getMap("poster") : null;
		ReadableMap thumbnail = options.hasKey("thumbnail") ? options.getMap("thumbnail") : null;
		WritableMap posterWrite = null;
		WritableMap thumbnailWrite = null;
		String poster_export_type = null;
		String thumbnail_export_type = null;
		CompressFormat poster_cformat = null;
		CompressFormat thumbnail_cformat = null;
		String ext = null;
		Bitmap poster_bmp = null;
		boolean checkBuildVersion = false;
		Integer width = 0;
		Integer height = 0;
		
		if(poster != null) {
			poster_export_type = poster.hasKey("export_type") ? poster.getString("export_type").toLowerCase() : POSTER_EXPORT_TYPE;
			
			if(!poster.hasKey("export_uri") || poster.isNull("export_uri")) {
				throw new Exception("Poster 'export_uri' is not valid. 'Base64' or '{DESTINATION_PATH}'");
			}
			
			if(!isValidExportType(poster_export_type)) {
				throw new Exception("Poster 'export_type' is not valid. allowed types: 'jpg', 'png'");
			}
			
			if(!poster.getString("export_uri").equals("Base64")) {
				ext = getFileExtension(poster.getString("export_uri"));
				
				if(!ext.equals("jpg") && !ext.equals("png")) {
					throw new Exception("Poster extension 'export_uri' is not valid. allowed extensions: 'jpg', 'png'");
				}
				
				if(!poster.hasKey("export_type")) {
					poster_export_type = ext.equals("jpg") ? "jpg" : "png";
				}
				
				if((poster_export_type.equals("jpg") && !ext.equals("jpg")) || (poster_export_type.equals("png") && !ext.equals("png"))) {
					throw new Exception("Poster 'export_type' does not match 'export_uri' extension");
				}
			}
			
			poster_cformat = getCompressFormat(poster_export_type);
		}
		
		if(thumbnail != null) {
			thumbnail_export_type = thumbnail.hasKey("export_type") ? thumbnail.getString("export_type").toLowerCase() : THUMBNAIL_EXPORT_TYPE;
			
			if(!thumbnail.hasKey("export_uri") || thumbnail.isNull("export_uri")) {
				throw new Exception("Thumbnail 'export_uri' is not valid. 'Base64' or '{DESTINATION_PATH}'");
			}
			
			if(!thumbnail.hasKey("width") || thumbnail.getInt("width") <= 0) {
				throw new Exception("Thumbnail 'width' is not valid.");
			}
			
			if(!thumbnail.hasKey("height") || thumbnail.getInt("height") <= 0) {
				throw new Exception("Thumbnail 'height' is not valid.");
			}
			
			if(!isValidExportType(thumbnail_export_type)) {
				throw new Exception("Thumbnail 'export_type' is not valid. allowed types: 'jpg', 'png'");
			}
			
			if(!thumbnail.getString("export_uri").equals("Base64")) {
				ext = getFileExtension(thumbnail.getString("export_uri"));
				
				if(!ext.equals("jpg") && !ext.equals("png")) {
					throw new Exception("Thumbnail extension 'export_uri' is not valid. allowed extensions: 'jpg', 'png'");
				}
				
				if(!thumbnail.hasKey("export_type")) {
					thumbnail_export_type = ext.equals("jpg") ? "jpg" : "png";
				}
				
				if((thumbnail_export_type.equals("jpg") && !ext.equals("jpg")) || (thumbnail_export_type.equals("png") && !ext.equals("png"))) {
					throw new Exception("Thumbnail 'export_type' does not match 'export_uri' extension");
				}
			}
			
			thumbnail_cformat = getCompressFormat(thumbnail_export_type);
		}
		
		if(mediaType == TYPE_IMAGE) {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
			poster_bmp = BitmapFactory.decodeFile(uri, opt);
			
			width = opt.outWidth;
			height = opt.outHeight;
			
			params.putString("media_type", "Image");
			params.putInt("width", width);
			params.putInt("height", height);
			params.putDouble("size", new Double(byteSizeOf(poster_bmp)));
		} else if(mediaType == TYPE_VIDEO) {
			if (checkBuildVersion && Build.VERSION.SDK_INT >= 14) {
				retriever.setDataSource(uri, new HashMap<String, String>());
			} else {
				retriever.setDataSource(uri);
			}

			poster_bmp = retriever.getFrameAtTime(1000000); //unit in microsecond: 1000000 mic = 1 sec

			long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
			width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
			height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
			
			params.putString("media_type", "Video");
			params.putDouble("duration", duration);
			params.putInt("width", width);
			params.putInt("height", height);
			params.putDouble("size", new File(uri).length());
		} else if(mediaType == TYPE_AUDIO) {
			if (checkBuildVersion && Build.VERSION.SDK_INT >= 14) {
				retriever.setDataSource(uri, new HashMap<String, String>());
			} else {
				retriever.setDataSource(uri);
			}
			
			poster_bmp = extractAlbumArt(retriever);
			
			long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
			width = poster_bmp.getWidth();
			height = poster_bmp.getHeight();
			
			params.putString("media_type", "Audio");
			params.putDouble("duration", duration);
			params.putDouble("size", new File(uri).length());
		}
		
		if(poster_bmp != null) {
			if(poster != null) {
				WritableMap posterExpMap = export(poster_bmp, poster.getString("export_uri"), poster_cformat);
				
				posterWrite = new WritableNativeMap();
	
				posterWrite.putInt("width", width);
				posterWrite.putInt("height", height);
				posterWrite.putDouble("size", posterExpMap.getDouble("size"));
				posterWrite.putString("uri", posterExpMap.getString("uri"));
				posterWrite.putString("format", poster_export_type);
			}
			
			if(thumbnail != null) {
				thumbnailWrite = new WritableNativeMap();
				
				int dwidth = thumbnail.getInt("width");
				int dheight = thumbnail.getInt("height");
				
				if(width < dwidth) {
					dwidth = width;
					dheight = width;
				}
	
				Map ratio = resizeKeepingRatio(width, height, dwidth, dheight);
				
				//Bitmap thumbnail_bmp = Bitmap.createScaledBitmap(poster_bmp, (int) ratio.get("width"), (int) ratio.get("height"), false);
				Bitmap thumbnail_bmp = ThumbnailUtils.extractThumbnail(poster_bmp, (int) ratio.get("width"), (int) ratio.get("height"));
	
				WritableMap thumbnailExpMap = export(thumbnail_bmp, thumbnail.getString("export_uri"), thumbnail_cformat);
				
				thumbnailWrite.putInt("width", thumbnail_bmp.getWidth());
				thumbnailWrite.putInt("height", thumbnail_bmp.getHeight());
				thumbnailWrite.putDouble("size", thumbnailExpMap.getDouble("size"));
				thumbnailWrite.putString("uri", thumbnailExpMap.getString("uri"));
				thumbnailWrite.putString("format", thumbnail_export_type);
			}
		}
		
		params.putMap("poster", posterWrite);
		params.putMap("thumbnail", thumbnailWrite);
		
		return params;
	}
	
	private static String getFileExtension(String path) {
		return path.substring(path.lastIndexOf(".") + 1, path.length());
	}
	
	private static Boolean isValidExportType(String type) {
		if(Arrays.asList(EXPORT_TYPES).contains(type)) {
			return true;
		}
		
		return false;
	}

	private static WritableMap getFileData(String uri) {
		WritableMap fd = new WritableNativeMap();
		
		String extension = MimeTypeMap.getFileExtensionFromUrl(uri);
		fd.putString("ext", extension);
		
		int mediaType = 0;
		if (extension != null) {
			String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

			if (type != null) {
				if (type.startsWith("image")) {
					mediaType = TYPE_IMAGE;
				} else if (type.startsWith("video")) {
					mediaType = TYPE_VIDEO;
				} else if (type.startsWith("audio")) {
					mediaType = TYPE_AUDIO;
				}
			}
		}
		
		fd.putInt("mediaType", mediaType);
		
		return fd;
	}

	private CompressFormat getCompressFormat(String export_type) {
		CompressFormat cFormat = export_type.equals("jpg") ? CompressFormat.JPEG : CompressFormat.PNG;
		
		return cFormat;
	}
	
	private WritableMap export(Bitmap bmp, String export_uri, CompressFormat cFormat) throws Exception {
		WritableMap export = new WritableNativeMap();
		
		try {
			if(export_uri.equals("Base64")) {
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				bmp.compress(cFormat, 90, byteArrayOutputStream);
				byte[] byteArray = byteArrayOutputStream.toByteArray();
				export.putDouble("size", new Double(byteArrayOutputStream.size()));
				export.putString("uri", Base64.encodeToString(byteArray, Base64.DEFAULT));
			} else {
				File file = new File(export_uri);
				OutputStream out = new FileOutputStream(file);
				bmp.compress(cFormat, 90, out);
				export.putDouble("size", file.length());
				export.putString("uri", export_uri);
			}
		} catch (FileNotFoundException e) {
			throw new Exception(e.getMessage());
		}
		
		return export;
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

	public static int byteSizeOf(Bitmap data) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
			return data.getRowBytes() * data.getHeight();
		} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			return data.getByteCount();
		} else {
			return data.getAllocationByteCount();
		}
	}
	
	// https://github.com/Wraptime/react-native-get-real-path
	static public final class GRP {

		private final ReactApplicationContext reactContext;
		
		public GRP(ReactApplicationContext reactContext) {
			this.reactContext = reactContext;
		}
		
		public String getRealPathFromURI(String uriString) {
			Uri uri = Uri.parse(uriString);
			try {
				Context context = this.reactContext;
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
}