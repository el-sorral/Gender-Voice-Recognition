package edu.upc.eetac.bigdata.genderizer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by gerard on 5/23/17.
 */

public class NetworkService {

    private static final String URL = "http://srv.birium.com:5000/";
//    private static final String URL = "http://10.42.0.1:5000/";
    private static final MediaType MEDIA_TYPE_WAV = MediaType.parse("audio/wav");
    private final OkHttpClient client;

    public NetworkService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();
    }

    public void sendFilenameAndGender(File filename, final Gender gender, final NetworkResult callback) {
        RequestBody requestBody = this.getSendFilenameAndGenderBody(filename.getName(), gender);
        Request request = this.buildRequest(requestBody, "verify");
        this.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onSuccess(gender);
            }
        });
    }

    private RequestBody getSendFilenameAndGenderBody(String filename, Gender gender) {
        return new FormBody.Builder()
                .add("filename", filename)
                .add("gender", gender.toString().toLowerCase())
                .build();
    }

    public void upload(final File file, final NetworkResult callback) {
        RequestBody requestBody = this.getUploadRequestBody(file);
        Request request = this.buildRequest(requestBody, "upload");
        this.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String gender = response.body().string().toUpperCase();
                    callback.onSuccess(Gender.valueOf(gender));
                } else {
                    callback.onError();
                }
            }
        });
    }

    private Request buildRequest(RequestBody requestBody, String path) {
        return new Request.Builder()
                .url(NetworkService.URL + path)
                .post(requestBody)
                .build();
    }

    private RequestBody getUploadRequestBody(File file) {
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(MEDIA_TYPE_WAV, file)).build();
    }
}

