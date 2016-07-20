# WhorlViewProgress

一个加载View_Progress

## 预览

![https://raw.githubusercontent.com/Kyson/WhorlView/master/art/whorl_progress_showcase.gif](https://raw.githubusercontent.com/Kyson/WhorlView/master/art/whorl_progress_showcase.gif)

## 使用

### step1

添加gradle配置

```
dependencies {
    compile 'com.tt:whorlviewlibrary:1.0.3'
}
```

### step2

在xml中添加WhorlView

```xml
    <com.tt.whorlviewlibrary.WhorlView xmlns:app="http://schemas.android.com/apk/res-auto"
        android:id="@+id/whorl2"
        android:layout_width="1dp"
        android:layout_height="1dp"
        android:layout_marginTop="12dp"
        app:whorlview_circle_colors="#F14336_#ffffff_#5677fc_#F44336_#4CAF50"
        app:whorlview_circle_speed="270"
        app:whorlview_parallax="fast"
        app:whorlview_strokeWidth="6"
        app:whorlview_sweepAngle="90">
    </com.tt.whorlviewlibrary.WhorlView>
```

### step3

开始动画

```java
WhorlView whorlView = (WhorlView) this.findViewById(R.id.whorl);
whorlView.start();
```

## XML自定义属性

|属性|类型|说明|默认值|
|---|---|---|---|
|whorlview_circle_colors|string|圆弧颜色|由外向内依次为红绿蓝|
|whorlview_circle_speed|int|转圈速度|270度每秒|
|whorlview_parallax|enum|视差效果|72度每秒|
|whorlview_sweepAngle|float|弧度|90度|
|whorlview_strokeWidth|float|弧宽|5f|

> 1.0.3版本对颜色进行了修改，whorlview_circle_colors属性值应该为<色值>\_<色值>\_<色值>，以\_为分隔符，其中色值为#开头的6位或8位16进制数

## API

提供的api就两个，开始和停止。

`whorlView.start();`

`whorlView.stop();`