package com.neusoft.liuyixuan.demo_gaode.com.Alpine.liuyixuan.Navigation.Activity;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.LocationSource;

interface AMapLocationListener {
    void activate(LocationSource.OnLocationChangedListener onLocationChangedListener);
    void deactivate();
    //定位回调  在回调方法中调用“mListener.onLocationChanged(amapLocation);”可以在地图上显示系统小蓝点。
    void onLocationChanged(AMapLocation aMapLocation);
}
