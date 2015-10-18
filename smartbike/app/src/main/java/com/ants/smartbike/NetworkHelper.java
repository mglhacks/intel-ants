package com.ants.smartbike;

//import android.app.VoiceInteractor;
//
//import org.json.JSONException;
//import org.json.JSONObject;

import android.app.VoiceInteractor;
import android.content.Context;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by byambajav on 10/18/15.
 */
public class NetworkHelper {

    private static String BASE_URL  = "http://intel-ants-node-red.mybluemix.net/elock/";


    public static void sendGetRequest(String targetMethod, Context context, Map<String, String> params) {
        String url = BASE_URL + targetMethod;
        Uri.Builder b = Uri.parse(url).buildUpon();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            b.appendQueryParameter(entry.getKey(), entry.getValue());
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // the response is already constructed as a JSONObject!
                        System.out.println(response.toString());
//                        try {
//                            response = response.getJSONObject("args");
//                            String site = response.getString("site"),
//                                    network = response.getString("network");
//                            System.out.println("Site: "+site+"\nNetwork: "+network);
//                            System.out.println(response.toString());
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        Volley.newRequestQueue(context).add(jsonRequest);
    }


    public static void sendPostRequest(String targetMethod, Context context, final Map<String, String> data) {
        String url = BASE_URL + targetMethod;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response.toString());
//                        try {
//                            JSONObject jsonResponse = new JSONObject(response).getJSONObject("form");
//                            String site = jsonResponse.getString("site"),
//                                    network = jsonResponse.getString("network");
//                            System.out.println("Site: "+site+"\nNetwork: "+network);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    params.put(entry.getKey(), entry.getValue());
                }

                return params;
            }
        };
        Volley.newRequestQueue(context).add(postRequest);
    }
}
