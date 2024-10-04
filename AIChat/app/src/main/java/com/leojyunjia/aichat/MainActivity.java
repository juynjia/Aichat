package com.leojyunjia.aichat;

import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// we have to design  how to react user's behavior. Write here!!!
public class MainActivity extends AppCompatActivity implements MyInterface {
    protected static final String KEY="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";
    //"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";

    protected static final              String URL="https://api.openai.com/v1/chat/completions";
    public static final String CHECK_ENDPOINT_URL = "https://api.openai.com/v1/moderations";

    private EditText input;
    private Button sendButton, reportButton;
    private TextView inputContent,Answer;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // why??  becasue it( AppCompatActivity or Activity) has all the most basic fuction or attribute of Android system!!!
        setContentView(R.layout.activity_main);// render our designed user' interface.

        db = FirebaseFirestore.getInstance();
        input = findViewById(R.id.edittext_Input); //link the widget in user's interface to object here. so we have to create the object to control it!!!
        inputContent=findViewById(R.id.inputContent);

        Answer=findViewById(R.id.textView_Answer);
        sendButton=findViewById(R.id.send);//link the button widget in user's interface to object(sendButton) here
        sendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String userInput=input.getText().toString(); // by this object, we get string in the user's interface.
                if(userInput.isEmpty()) return;
                inputContent.setText(userInput);
                HttpRequest httpRequest = new HttpRequest();
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body(userInput));
                httpRequest.sendPOST(URL, requestBody, MainActivity.this);
            }
        });

        reportButton=findViewById(R.id.report);//link the button widget in user's interface to object(sendButton) here
        reportButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                reportContent(Answer.getText().toString());
            }
        });


    }

    private String body(String input){  // to create a json string
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("model", "gpt-4o-mini");
            JSONArray messageArray = new JSONArray();
            messageArray.put( new JSONObject().put("role", "system").put("content", "You are a helpful assistant") );
            messageArray.put( new JSONObject().put("role","user").put("content",input));
            jsonObject.put("messages", messageArray);

            return jsonObject.toString();
        }catch (JSONException e){
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onOKcall(boolean flag, String content){
        //Answer.setText(content); // forbidden ! because other thread CAN NOT directly do UI render task.!!
        if(flag){
            runOnUiThread(new Runnable() { //  runOnUiThread: do ALL UI render.becasue ALL UI render task have to be done by UI thread.
                @Override
                public void run() {
                    Answer.setText("Inappropriate content, not displayed");
                }
            });
        }else{
            runOnUiThread(new Runnable() { //  runOnUiThread: do ALL UI render.becasue ALL UI render task have to be done by UI thread.
                @Override
                public void run() {
                    Answer.setText(content);
                }
            });
        }
    }

    @Override
    public void onFailCall(String error){
        //Answer.setText(error);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Answer.setText(error);
            }
        });
    }



    // 方法來提交檢舉回報
    public void reportContent(String content) {
        // 準備報告的資料
        Map<String, Object> report = new HashMap<>();
        report.put("content", content);
        report.put("timestamp", System.currentTimeMillis());
        // 將報告添加到 Firestore "reports" 集合
        db.collection("reports")
                .add(report)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(MainActivity.this, "檢舉已提交", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "檢舉提交失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}