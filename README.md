
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
  	project(':react-native-media-thumbnail').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-media-thumbnail/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-media-thumbnail')
  	```


## Usage
```javascript
import RNMediaThumbnail from 'react-native-media-thumbnail';

// TODO: What to do with the module?
#### saveVideoThumb

RNMediaThumbnail.saveVideoThumb(
			'VIDEO_PATH.mp4',
			'DESTINATION_PATH.jpg',
			true, // use jpeg = true| use png = false
			(params) => {
				console.log(params);
			}, (e) => {
				console.log('Error: ', e);
			});
```


  
