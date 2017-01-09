package com.xiaohongshu.dampingscrolling;

import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private TextView mTextView;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(10);
        mViewPager.setAdapter(new MyPagerAdapter(createViewList()));

        mTextView = (TextView) findViewById(R.id.textview);
        mTextView.setText(createText());

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(new MyRecyclerViewAdapter(createList()));
    }

    private List<String> createList() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("RecyclerView. \t\t");
        }
        return list;
    }

    private String createText() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            stringBuilder.append("HorizontalScrollView. \t\t");
        }
        return stringBuilder.toString();
    }

    private List<View> createViewList() {
        List<View> list = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            View view = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, null);
            view.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText("ViewPager. \t\t");
            textView.setTextColor(Color.WHITE);
            list.add(view);
        }
        return list;
    }

    public void onClick(View view) {
        Toast.makeText(this, "button click", Toast.LENGTH_SHORT).show();
    }

    /**
     * Created by wupengjian on 16/12/14.
     */
    static class MyPagerAdapter extends PagerAdapter {

        private List<View> mViewList;

        public MyPagerAdapter(List<View> list) {
            mViewList = list;
        }

        public Object getItem(int position) {
            return mViewList.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return mViewList.indexOf(object);
        }

        @Override
        public int getCount() {
            return mViewList == null ? 0 : mViewList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mViewList.get(position), 0);
            return mViewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViewList.get(position));
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    /**
     * Created by wupengjian on 16/10/20.
     */
    public static class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyHolder> {

        private List<String> mData;

        public MyRecyclerViewAdapter(List<String> list) {
            mData = list;
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, null);
            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(MyHolder holder, int position) {
            holder.tv.setText(mData.get(position));
            holder.tv.setTextColor(Color.WHITE);
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        class MyHolder extends RecyclerView.ViewHolder {

            TextView tv;

            public MyHolder(View itemView) {
                super(itemView);
                tv = (TextView) itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
