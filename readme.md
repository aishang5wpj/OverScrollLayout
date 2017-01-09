#前言

上海这两天下雨，刚好上周末又碰上双休，宅在家里没有出去，撸了一个阻尼滑动的自定义ViewGroup，加上今天也不是很忙，就赶紧把它撸完啦。

阻尼滚动貌似是iOS原生支持的效果，每次和PM讨论需求时，一碰到阻尼滑动之类的需求时，就说安卓做不了。以后再碰到类似的需求时，就可以跟PM愉快的装逼啦。

照例，先看效果图。

<img src='app/screenshot/screenshot.gif' height='480px'/>


#项目使用
要使用`DampingScrollViewGroup`给你的项目加上阻尼滚动效果，整个过程只需要两个步骤：

1、在项目的`attrs.xml`文件中添加如下属性：

```
    <declare-styleable name="DampingScrollViewGroup">
        <attr name="dampingFactor" format="float" />
        <attr name="dampingDirection">
            <flag name="left" value="0x0001" />
            <flag name="top" value="0x0010" />
            <flag name="right" value="0x0100" />
            <flag name="bottom" value="0x1000" />
        </attr>
    </declare-styleable>

```
 2、在布局文件中引用`DampingScrollViewGroup`，并分别设置`dampingFactor`和`dampingDirection`的值，他们的含义分别如下：
 
 - `dampingFactor`：`阻尼因子`，值越小表示阻力越大，默认为1（即没有阻尼效果）。
 - `dampingDirection`：`阻尼方向`，顾名思义，支持左、上、右、下这4种阻尼方向。

请务必注意，在xml文件中使用`DampingScrollViewGroup`给你的布局添加阻尼效果时，<b>`DampingScrollViewGroup`的直接子View只能有一个！</b>（可以参考ScrollView。）

举个栗子：

`SimpleActivity.java`

```
public class SimpleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
    }

    public void onClick(View view) {
        Toast.makeText(this, "button click", Toast.LENGTH_SHORT).show();
    }
}
```

`activity_simple.xml`

```
<?xml version="1.0" encoding="utf-8"?>
<com.xiaohongshu.dampingscrolling.DampingScrollViewGroup xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:dampingDirection="left|top|right|bottom"
    app:dampingFactor="0.7">

    <Button
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@android:color/holo_blue_light"
        android:gravity="center"
        android:onClick="onClick"
        android:text="最简单的阻尼滚动"
        android:textColor="@android:color/white" />

</com.xiaohongshu.dampingscrolling.DampingScrollViewGroup>
```

这样，一个非常简单的支持四向阻尼效果的小项目就出来了！看看效果图：

<img src='app/screenshot/demo01.gif' height='480px'/>


#实现过程

 
关于
--

博客：[http://blog.csdn.net/aishang5wpj](http://blog.csdn.net/aishang5wpj)

邮箱：337487365@qq.com

License
--
Copyright 2017 aishang5wpj

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.