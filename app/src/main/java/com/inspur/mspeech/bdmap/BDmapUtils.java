package com.inspur.mspeech.bdmap;

import android.content.Context;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import java.util.List;
import io.reactivex.rxjava3.annotations.NonNull;

/**
 * @author : zhangqinggong
 * date    : 2023/11/20 9:06
 * desc    : 百度地图操作类
 */
public class BDmapUtils {
    public static final int COCE_ERROR = 201;
    public static final int COCE_NOT_THIS_CITY = 202;
    public static final int COCE_NO_RESULT = 203;
    /**
     * 初始化 application调用
     * @param context
     */
    public static void init(Context context){
        //百度地图
        SDKInitializer.setAgreePrivacy(context,true);

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.initialize(context);

        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);

//        locationc.setAgreePrivacy(true);

    }


    /**
     * 获取详细地址
     *
     */
    public static void getAddress(String cityName,String location,@NonNull IAddressListener iAddressListener){
        //创建POI检索实例
        PoiSearch poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                // 处理地点搜索结果
                if (poiResult == null || poiResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    // 搜索失败
                    if (iAddressListener != null){
                        iAddressListener.fail(COCE_ERROR,"查询错误");
                    }
                } else {
                    // 获取搜索结果
                    List<PoiInfo> poiInfos = poiResult.getAllPoi();
                    if (poiInfos != null && poiInfos.size() > 0) {
                        // 获取第一个地点的详细地址
                        String address = poiInfos.get(0).address;
                        String city = poiInfos.get(0).city;

                        if (city.contains(cityName)){
                            if (iAddressListener != null){
                                iAddressListener.success(address);
                            }
                        }else {
                            //返回结果不是输入城市
                            if (iAddressListener != null){
                                iAddressListener.fail(COCE_NOT_THIS_CITY,"该城市没有此地点");
                            }
                        }

                    }else {
                        if (iAddressListener != null){
                            iAddressListener.fail(COCE_NO_RESULT,"没有查询到改地点");
                        }
                    }
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }

            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        });

        poiSearch.searchInCity(new PoiCitySearchOption()
                .cityLimit(false)
                .city(cityName) //必填
                .keyword(location) //必填
                .pageNum(0));

    }

    public interface IAddressListener{
        void success(String address);
        void fail(int code,String msg);
    }

}
