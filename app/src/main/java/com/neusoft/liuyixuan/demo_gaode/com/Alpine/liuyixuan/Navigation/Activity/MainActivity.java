package com.neusoft.liuyixuan.demo_gaode.com.Alpine.liuyixuan.Navigation.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;

import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.NaviLatLng;
import com.neusoft.liuyixuan.demo_gaode.R;
import com.neusoft.liuyixuan.demo_gaode.Search_Activity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AMapLocationListener, LocationSource {
    /*****************************************************************************
    地图
     */
    //初始化地图控制器对象
    AMap aMap;
    MapView mMapView = null;
    /**********************************************************************************************************************************************************
    定位
     */
    private TextView mLocationErrText;
    private OnLocationChangedListener mListener;
    //声明mlocationClient对象
    public AMapLocationClient mlocationClient;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);

    /**********************************************************************************************************************************************************
     *时间显示
     */
    private TextView Time;
    private Handler handler;  //异步消息处理器
    private SimpleDateFormat simpleDateFormat;
    private boolean runing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /***********************************************************************************************************************************************************************************************************
        地图
         */
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        map();

        /***********************************************************************************************************************************************************************************************************
         * 定位
         */
        mlocationClient = new AMapLocationClient(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mlocationClient.setLocationListener(this);
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(100000);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        //启动定位
        mlocationClient.startLocation();
        /***********************************************************************************************************************************************************************************************************
         * 时间显示
         */
        Time=findViewById( R.id.Time );
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        Time.setText(simpleDateFormat.format(new Date()));
        //创建消息处理器 接受子线程发送的消息  根据它做出处理，跟新主界面的值
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 1){
                    Time.setText(simpleDateFormat.format(new Date()));
                }
            }
        };
        TimeStart();
        /***********************************************************************************************************************************************************************************************************
         * 页面的跳转
         */
        findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(MainActivity.this, Search_Activity.class);
                startActivity(intent);
            }
        });
    }
    /***********************************************************************************************************************************************************************************************************
     * 方法重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    /***********************************************************************************************************************************************************************************************************
     * 地图
     */
    public void map(){
        if(aMap == null){
            aMap = mMapView.getMap();
            setUpMap();
        }
        mLocationErrText = findViewById(R.id.textView);
        mLocationErrText.setVisibility(View.GONE);
    }

    /***********************************************************************************************************************************************************************************************************
     * 定位
     */
    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        setupLocationStyle();
    }

    /***
     * 指向标
     */
    private void setupLocationStyle(){
        // 自定义系统定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
                fromResource(R.id.imageView));
        // 自定义精度范围的圆形边框颜色
        myLocationStyle.strokeColor(STROKE_COLOR);
        //自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(5);
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(FILL_COLOR);
        // 将自定义的 myLocationStyle 对象添加到地图上
        aMap.setMyLocationStyle(myLocationStyle);
    }
    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                mLocationErrText.setVisibility(View.GONE);
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
                mLocationErrText.setVisibility(View.VISIBLE);
                mLocationErrText.setText(errText);
            }
        }
    }
    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }
    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }
    /***********************************************************************************************************************************************************************************************************
     * 显示时间
     */
    public void TimeStart(){
        //创建线程，定时发送消息
        runing = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (runing){
                    //让线程  发送消息
                    handler.sendEmptyMessage(1);
                    //让线程  睡眠500毫秒
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public void TimeEnd(){
        runing =false;
    }

}