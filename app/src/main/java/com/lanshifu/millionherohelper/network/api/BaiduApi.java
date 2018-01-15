package com.lanshifu.millionherohelper.network.api;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;

/**
 * Created by lanshifu on 2018/1/14.
 */

public interface BaiduApi {

    String Agents[] = new String[]{
            "Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:57.0) Gecko/20100101 Firefox/57.0",
            "Mozilla/5.0 (X11; Fedora; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.108 Safari/537.36"
    };

    @Headers({
            "Host: www.baidu.com",
            "User-Agent: Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:57.0) Gecko/20100101 Firefox/57.0"
    })
    @GET("/s")
    Observable<ResponseBody> get(
            @Header("Host:www.baidu.com")
            @Path("wd") String wd);
}
