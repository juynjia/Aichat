package com.leojyunjia.aichat;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

//取消掉
public class MyCallback implements Callback { //implement okhttp3's Callback interface.

    @Override
    public void onFailure(Call call, IOException e){
        e.printStackTrace();
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if(response.isSuccessful()) {
            String res = response.body().string();
            System.out.println("Response: " + res);
        }else{
            System.out.println("Request failed with status code: " + response.code());
        }
    }
}
