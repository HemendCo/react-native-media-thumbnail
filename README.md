
# react-native-media-thumbnail
This component gives power you to extract metadata such as **_poster(cover), duration, size, etc.,_** that there are in the **_video, audio, and pictures_** files.
> Of Course, if there is a poster in an audio file you can extract it,
otherwise, the null value is returned.
## what kind of information and data can we extract by this module?

**Video files:**
* Duration
* width and height
* Size
* Thumbnail
* Poster

**Audio files:**
* Duration
* Size
* Thumbnail and Poster if exist

**Pictures:**
* Thumbnail
* Poster
* converting(jpg/png)

## Getting started

`$ npm install react-native-media-thumbnail --save`

### Mostly automatic installation

`$ react-native link react-native-media-thumbnail`

### Manual installation


#### iOS

Currently No Support

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

RNMediaThumbnail.getMedia(
    'FILE_PATH', // <------- uri: {Image, Video, Audio}
    {
      'poster': // <------- Optional
      {
        'export_uri': 'DESTINATION_FILE_PATH', // <------- Allowed: {jpg, png}
      },
      'thumbnail': // <------- Optional
      {
        'export_uri': 'Base64', // <------- Allowed: {Base64, DESTINATION_FILE_PATH}
        'export_type': 'jpg', // <------- Allowed: {jpg, png} - Default: jpg
        'width': 200,
        'height': 200
      }
    },
    (params) => {
      console.log(params);
    }, (e) => {
      console.log('Error: ', e);
    });
```
---

```javascript
import RNMediaThumbnail from 'react-native-media-thumbnail';

RNMediaThumbnail.getMedia(
    'FILE_PATH', // <------- uri: {Image, Video, Audio}
    {
      'poster': // <------- Optional
      {
        'export_uri': 'Base64', // <------- Allowed: {Base64, DESTINATION_FILE_PATH}
        'export_type': 'png', // <------- Allowed: {jpg, png} - Default: jpg
      },
      'thumbnail': // <------- Optional
      {
        'export_uri': 'DESTINATION_FILE_PATH', // <------- Allowed: {jpg, png}
        'width': 200,
        'height': 200
      }
    },
    (params) => {
      console.log(params);
    }, (e) => {
      console.log('Error: ', e);
    });
```
---

## Input
| Param        | Required           | Value  |
| ------------- |:-------------:|:-------------:|
| uri      | Yes | String  |
| poster      | No |   Object   |
| poster.export_uri | Yes: [ poster ] not empty |    String: `Base64` _or_ `DESTINATION_FILE_PATH` |
| poster.export_type | Yes: [ poster.export_uri ] = `Base64` |    String: `jpg` _or_ `png` |
| thumbnail      | No |   Object   |
| thumbnail.export_uri | Yes: [ thumbnail ] not empty |    String: `Base64` _or_ `DESTINATION_FILE_PATH` |
| thumbnail.export_type | Yes: [ thumbnail.export_uri ] = `Base64` |    String: `jpg` _or_ `png` |
| thumbnail.width | Yes: [ thumbnail ] not empty  |    Int    |
| thumbnail.height | Yes: [ thumbnail ] not empty  |    Int    |

## Output
| Param        | Media           | Value  |
| ------------- |:-------------:|:-------------:|
| width      | Image, Video | Int  |
| height      | Image, Video |   Int   |
| size | * |    Double |
| duration | Video, Audio |    Double |
| poster | *  |    Object / null    |
| poster.width | *  |    Int    |
| poster.height | *  |    Int    |
| poster.size | *  |    Double    |
| poster.uri | *  |    String: `Base64` _or_ `DESTINATION_FILE_PATH`    |
| poster.format | *  |    String: `jpg` _or_ `png`    |
| thumbnail | *  |    Object / null    |
| thumbnail.width | *  |    Int    |
| thumbnail.height | *  |    Int    |
| thumbnail.size | *  |    Double    |
| thumbnail.uri | *  |    String: `Base64` _or_ `DESTINATION_FILE_PATH`    |
| thumbnail.format | *  |    String: `jpg` _or_ `png`    |
