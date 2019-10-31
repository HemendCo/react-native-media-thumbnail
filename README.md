
# react-native-media-thumbnail

## Getting started

`$ npm install react-native-media-thumbnail --save`

### Mostly automatic installation

`$ react-native link react-native-media-thumbnail`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-media-thumbnail` and add `RNMediaThumbnail.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNMediaThumbnail.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNMediaThumbnailPackage;` to the imports at the top of the file
  - Add `new RNMediaThumbnailPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
    ```
    include ':react-native-media-thumbnail'
    project(':react-native-media-thumbnail').projectDir = new File(rootProject.projectDir,  '../node_modules/react-native-media-thumbnail/android')
    ```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
    ```
      compile project(':react-native-media-thumbnail')
    ```
4. Add all wanted permissions to your app `android/app/src/main/AndroidManifest.xml` file:
    ```
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    ```

## Usage
```javascript
import RNMediaThumbnail from 'react-native-media-thumbnail';

// Save thumbnail video and get data: {width,height,duration,thumbnail_width,thumbnail_height,thumbnail_uri}

RNMediaThumbnail.saveVideoThumb(
    'VIDEO_PATH.mp4',
    'DESTINATION_PATH.{jpg|png}',
    true, // use jpeg = true| use png = false
    (params) => {
        console.log(params);
    }, (e) => {
        console.log('Error: ', e);
    });
```

```javascript
import RNMediaThumbnail from 'react-native-media-thumbnail';

// Get thumbnail video data: {width,height,duration,thumbnail_width,thumbnail_height,thumbnail_base64}

RNMediaThumbnail.getVideoThumb(
    'VIDEO_PATH.mp4',
    true, // use jpeg = true| use png = false
    (params) => {
        console.log(params);
    }, (e) => {
        console.log('Error: ', e);
    });
```

```javascript
import RNMediaThumbnail from 'react-native-media-thumbnail';

// Save thumbnail image and get data: {width,height,thumbnail_width,thumbnail_height,thumbnail_uri}

RNMediaThumbnail.saveImageThumb(
    'IMAGE_PATH.{jpg|png}',
    'DESTINATION_PATH.{jpg|png}',
    true, // use jpeg = true| use png = false
    (params) => {
        console.log(params);
    }, (e) => {
        console.log('Error: ', e);
    });
```

```javascript
import RNMediaThumbnail from 'react-native-media-thumbnail';

// Get thumbnail image data: {width,height,thumbnail_width,thumbnail_height,thumbnail_base64}

RNMediaThumbnail.getImageThumb(
    'IMAGE_PATH.{jpg|png}',
    true, // use jpeg = true| use png = false
    (params) => {
        console.log(params);
    }, (e) => {
        console.log('Error: ', e);
    });
```

```javascript
import RNMediaThumbnail from 'react-native-media-thumbnail';

// Get audio data: {duration,(if has cover)[width,height,base64,thumbnail_width,thumbnail_height,thumbnail_base64]}

RNMediaThumbnail.getAudioThumb(
    'AUDIO_PATH.mp3',
    true, // use jpeg = true| use png = false
    (params) => {
        console.log(params);
    }, (e) => {
        console.log('Error: ', e);
    });
```
