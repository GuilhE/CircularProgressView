# CircularProgressView:
![header](sample1.png)

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/6cb896704b2648288ec8512a84277c5c)](https://www.codacy.com/app/GuilhE/android-circular-progress-view?utm_source=github.com&utm_medium=referral&utm_content=GuilhE/android-circular-progress-view&utm_campaign=badger)
[![Build Status](https://travis-ci.org/GuilhE/android-circular-progress-view.svg?branch=master)](https://travis-ci.org/GuilhE/android-circular-progress-view) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-CircularProgressView-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/6152) [![Preview-Appetize.io](https://img.shields.io/badge/Preview-Appetize.io-brightgreen.svg?style=flat.svg)](https://appetize.io/app/yqf57p0pmtf2gfv1tqq4fk87qm)

A fancy CircularProgressView.

#### Version 1.x
  - **November, 2017**  - Progress thumb and animation callback
  - **September, 2017** - CircularProgressView


## Getting started

Include it into your project, for example, as a Gradle dependency:

```groovy
implementation 'com.github.guilhe:circular-progress-view:${LATEST_VERSION}'
```
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.guilhe/circular-progress-view/badge.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22circular-progress-view%22)  [ ![Download](https://api.bintray.com/packages/gdelgado/android/circular-progress-view/images/download.svg) ](https://bintray.com/gdelgado/android/circular-progress-view/_latestVersion)  

## Sample usage

Check out the __sample__ module where you can find a few examples of how to create it by `xml` or `java`.

Attributes accepted in xml:
```xml
<declare-styleable name="CircularProgressView">
    <attr name="max" format="integer"/>
    <attr name="shadow" format="boolean"/>
    <attr name="progressThumb" format="boolean"/>
    <attr name="startingAngle" format="integer"/>
    <attr name="progress" format="integer"/>
    <attr name="progressBarThickness" format="dimension"/>
    <attr name="progressBarColor" format="color"/>
    <attr name="backgroundColor" format="color"/>
    <attr name="backgroundAlphaEnabled" format="boolean"/>
</declare-styleable>
```
Example:
```xml
<com.github.guilhe.circularprogressview.CircularProgressView
                    android:layout_width="100dp"
                    android:layout_height="100dp"
                    app:progress="60"
                    app:progressBarThickness="10dp"
                    app:progressBarColor="@android:color/holo_purple"/>
 ```

To customize this `View` by code, these are the available methods to do so:
```java
    public void setSize(int size) {}
    
    public void setStartingAngle(int angle) {}
    
    public int getStartingAngle() {}
    
    public void setMax(int max) {}
    
    public int getMax() {}
    
    public void setColor(int color) {}
    
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setColorResource(@ColorRes int resId) {}
    
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setColor(Color color) {}
    
    public void setProgressColor(int color) {}
    
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setProgressColorResource(@ColorRes int resId) {}
    
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setProgressColor(Color color) {}
    
    public int getProgressColor() {}
    
    public void setBackgroundColor(int color) {}
    
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setShadowColorResource(@ColorRes int resId) {}
    
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setBackgroundColor(Color color) {}

    public void setBackgroundAlphaEnabled(boolean alphaEnabled){}
    
    public int getBackgroundColor() {}
    
    public void setShadowEnabled(boolean enable) {}
    
    public boolean isShadowEnabled() {}
    
    public void setProgressThumbEnabled(boolean enable) {}
    
    public boolean isProgressThumbEnabled() {}
    
    public void setProgressStrokeThickness(float thickness) {}
    
    public float getProgressStrokeThickness() {}
    
    public void setProgress(float progress) {}
    
    public void setProgress(float progress, boolean animate) {}
    
    public void setProgress(float progress, boolean animate, long duration) {}
           
    public float getProgress() {}
    
    public void resetProgress() {}
    
    public void resetProgress(boolean animate) {}
    
    public void resetProgress(boolean animate, long duration) {}
    
    public void setAnimationInterpolator(TimeInterpolator interpolator) {}
    
    public void setProgressAnimationCallback(OnProgressChangeAnimationCallback callback) {}
```

For more details checkout the __sample app__, _javadocs_ or the code itself.

## Sample
<img src="sample.gif" alt="Sample" width="30%"/>

<a href='https://play.google.com/store/apps/details?id=com.github.guilhe.cicularprogressview.sample&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img width="30%" alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'/></a>

## Binaries

Binaries and dependency information for Gradle, Maven, Ivy and others can be found at [https://search.maven.org](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22circular-progress-view%22).

<a href='https://bintray.com/gdelgado/android/circular-progress-view?source=watch' alt='Get automatic notifications about new "circular-progress-view" versions'><img src='https://www.bintray.com/docs/images/bintray_badge_bw.png'></a>

## Dependencies
- [com.android.support:support-annotations](https://developer.android.com/topic/libraries/support-library/packages.html#annotations)

## Bugs and Feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/GuilhE/android-circular-progress-view/issues).

 
## LICENSE

Copyright (c) 2017-present, CircularProgressView Contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
