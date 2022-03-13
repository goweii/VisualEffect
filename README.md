# VisualEffect

[![Android CI](https://github.com/goweii/VisualEffect/actions/workflows/android.yml/badge.svg?branch=master)](https://github.com/goweii/VisualEffect/actions/workflows/android.yml)

Used to achieve visual effects in Android, such as Gaussian blur, mosaic, watermark, etc.

# Preview

| ![](https://s1.ax1x.com/2022/03/13/bb1vZj.gif) | ![](https://s1.ax1x.com/2022/03/13/bb3piq.gif) | ![](https://s1.ax1x.com/2022/03/13/bb1zon.gif) | ![](https://s1.ax1x.com/2022/03/13/bb3CWV.gif) |
| ---- | ---- | ---- | ---- |

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
