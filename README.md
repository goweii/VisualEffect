# VisualEffect

Used to achieve visual effects in Android, such as Gaussian blur, mosaic, watermark, etc.

# Preview

|  |  |  |  |
| ---- | ---- | ---- | ---- |
| ![](https://raw.githubusercontent.com/goweii/VisualEffect/master/simple/preview/20220312_125541.gif) | ![](https://raw.githubusercontent.com/goweii/VisualEffect/master/simple/preview/20220312_125614.gif) | ![](https://raw.githubusercontent.com/goweii/VisualEffect/master/simple/preview/20220312_125630.gif) | ![](https://raw.githubusercontent.com/goweii/VisualEffect/master/simple/preview/20220312_125700.gif) |

# How to Use

To get a Git project into your build:

Step 1. Add the JitPack repository

```
maven { url 'https://jitpack.io' }
```
  
Step 2. Add the dependency

[![](https://jitpack.io/v/goweii/VisualEffect.svg)](https://jitpack.io/#goweii/VisualEffect)

```
// Core library (must be imported)
implementation "com.github.goweii.VisualEffect:visualeffect-core:$version"
// Effect Library (imported on demand)
implementation "com.github.goweii.VisualEffect:visualeffect-blur:$version"
implementation "com.github.goweii.VisualEffect:visualeffect-mosaic:$version"
implementation "com.github.goweii.VisualEffect:visualeffect-watermask:$version"
// View Library (import on demand)
implementation "com.github.goweii.VisualEffect:visualeffect-view:$version"
```
