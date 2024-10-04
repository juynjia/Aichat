package com.leojyunjia.aichat;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpRequest {
    public void sendPOST(String url, RequestBody requestBody, MyInterface myInterface){
        OkHttpClient client= new OkHttpClient() //Thread (do sendRequest and wait for response.)
                .newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20,TimeUnit.SECONDS)
                .build();

        Request request= new Request.Builder()
                .url(url)
                .header("Authorization","Bearer "+MainActivity.KEY)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();


        Call call=client.newCall(request);
        //This client will later call back responseCallback with either an HTTP response or a failure exception.
        //call.enqueue( new MyCallback() );  //Step1: Initiates the HTTP request asynchronously. Step2: Creates an object of MyCallback class. Step3: The OkHttp library will use the object of MyCallback class to handle the response when it is ready.
        call.enqueue(
                new Callback(){
                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if(response.isSuccessful()) {
                            String res = response.body().string();
                            Log.d("aichat","res:"+res);
                            String content = parseResponse(res);
                            checkModeration(myInterface, content);
                            //myInterface.onOKcall(content);
                        }else{
                            myInterface.onFailCall(Integer.toString(response.code()));
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                        myInterface.onFailCall(e.getMessage());
                    }
                }
        );
    }

    public String parseResponse(String response){
        try {
            JSONObject jsonObject= new JSONObject(response);
            JSONArray choicesArray= jsonObject.getJSONArray("choices");
            jsonObject = choicesArray.getJSONObject(0);
            String content= jsonObject.getJSONObject("message").getString("content");
            return content;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }

    public void checkModeration(MyInterface myInterface, String content){
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try{
            json.put("input", content);
        }catch (Exception e){
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create( MediaType.parse("application/json"), json.toString());
        Request request = new Request.Builder().url(MainActivity.CHECK_ENDPOINT_URL).header("Authorization","Bearer "+MainActivity.KEY)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        final boolean[] flagged = {false};
        try {
            Call call = client.newCall(request);
            call.enqueue(
                    new Callback(){
                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if(response.isSuccessful()) {
                                String responseData = response.body().string();
                                try {
                                    JSONObject jsonResponse = new JSONObject(responseData);
                                    JSONArray results = jsonResponse.getJSONArray("results");
                                    JSONObject firstResult = results.getJSONObject(0);
                                    flagged[0] = firstResult.getBoolean("flagged");
                                    Log.d("aichat","flag:"+flagged[0]);
                                    myInterface.onOKcall(flagged[0], content);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            e.printStackTrace();
                            myInterface.onFailCall(e.getMessage());
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Log.d("aichat","回傳值:"+flagged[0]);
//        return flagged[0];
    }
}
