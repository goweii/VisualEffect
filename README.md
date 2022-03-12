# VisualEffect

Used to achieve visual effects in Android, such as Gaussian blur, mosaic, watermark, etc.

# Preview

|  |  |  |  |
| ---- | ---- | ---- | ---- |
| ![](https://raw.githubusercontent.com/goweii/VisualEffect/master/app/preview/20220312_125541.gif) | ![](https://raw.githubusercontent.com/goweii/VisualEffect/master/app/preview/20220312_125614.gif) | ![](https://raw.githubusercontent.com/goweii/VisualEffect/master/app/preview/20220312_125630.gif) | ![](https://raw.githubusercontent.com/goweii/VisualEffect/master/app/preview/20220312_125700.gif) |

# How to Use

To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
```
  
Step 2. Add the dependency

[![](https://jitpack.io/v/goweii/VisualEffect.svg)](https://jitpack.io/#goweii/VisualEffect)

```
dependencies {
  implementation 'com.github.goweii.VisualEffect:visualeffect-blur:$version'
}
```
