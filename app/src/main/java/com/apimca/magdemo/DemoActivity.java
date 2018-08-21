package com.apimca.magdemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ca.mas.core.service.MssoIntents;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASAuthenticationListener;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASDevice;
import com.ca.mas.foundation.MASOtpAuthenticationHandler;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;
import com.ca.mas.ui.MASLoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DemoActivity extends AppCompatActivity {

    // Object declaration
    private final String TAG = MainActivity.class.getSimpleName();
    private Button btnStartMAG;
    private Button btnUserAuth;
    private Button btnCallAPI;
    private Button btnLogout;
    private Button btnDeregister;
    private TextView txtLog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        btnStartMAG = (Button) findViewById(R.id.btnStartMAG);
        btnUserAuth = (Button) findViewById(R.id.btnUserAuth);
        btnCallAPI = (Button) findViewById(R.id.btnCallAPI);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnDeregister = (Button) findViewById(R.id.btnDeregister);
        txtLog = (TextView) findViewById(R.id.txtLog);

        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
    }

    //
    // Function to start MAG
    //
    public void startMAG(View v) {
        MAS.debug();
        MAS.start(this, true);
        showMessage("The MAG SDK has been successfully started!",1);
    }

    //
    // Function to show the user authentication page
    //
    public void userAuth(View v) {

            //Setup the MASAuthenticationListener for a user authentication request.
            MAS.setAuthenticationListener(new MASAuthenticationListener() {

                @Override
                public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {
                    Intent loginIntent = new Intent(context, MASLoginActivity.class);
                    loginIntent.putExtra(MssoIntents.EXTRA_AUTH_PROVIDERS, providers);
                    loginIntent.putExtra(MssoIntents.EXTRA_REQUEST_ID, requestId);
                    startActivity(loginIntent);
                }

                @Override
                public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {

                }
            });

        //Will trigger the MASAuthenticationListener when the user's token has expired.
            MASUser.login(new MASCallback<MASUser>() {
                @Override
                public void onSuccess(MASUser result) {
                    //User login successfully
                    showMessage("User "+MASUser.getCurrentUser().getUserName()+" successfully authenticated",1);
                }

                @Override
                public void onError(Throwable e) {
                    //Handle the error
                    showMessage("Error authenticating the user: "+e.getLocalizedMessage(),1);
                }
            });

    }

    //
    // Function to make the API Call
    //
    public void callAPI(View v) {

        //Endpoint (API) to be called
        String path = "/protected/resource/products";

        Uri.Builder uriBuilder = new Uri.Builder().encodedPath(path);
        uriBuilder.appendQueryParameter("operation", "listProducts");
        uriBuilder.appendQueryParameter("pName2", "pValue2");

        MASRequest.MASRequestBuilder requestBuilder = new MASRequest.MASRequestBuilder(uriBuilder.build());
        requestBuilder.header("hName1", "hValue1");
        requestBuilder.header("hName2", "hValue2");
        MASRequest request = requestBuilder.get().build();

        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }

            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                try {
                    List<String> objects = parseProductListJson(result.getBody().getContent());
                    String objectString = "";
                    int size = objects.size();
                    for (int i = 0; i < size; i++) {
                        objectString += objects.get(i);
                        if (i != size - 1) {
                            objectString += "\n";
                        }
                    }

                    txtLog.setText(objectString);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage());
            }
        });

    }

    //
    // User Logout
    //
    public void doLogout(View v) {
        MASUser.getCurrentUser().logout(new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                showMessage("User successfully logged out",1);
            }

            @Override
            public void onError(Throwable e) {
                showMessage("Error during user logout: " + e.getLocalizedMessage(),1);
            }
        });

    }


    //
    // Device deregistration
    //
    public void deviceDereg(View v) {
        MASDevice.getCurrentDevice().deregister(new MASCallback<Void>() {
            @Override
            public void onSuccess(Void object) {
                //The device is successfully de-registered.
            }

            @Override
            public void onError(Throwable e) {
                //Handle the error
            }
        });
    }

    public void showMessage(final String message, final int toastLength) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DemoActivity.this, message, toastLength).show();
            }
        });
    }

    //
    // Parse JSON
    //
    private static List<String> parseProductListJson(JSONObject json) throws JSONException {
        try {
            List<String> objects = new ArrayList<>();
            JSONArray items = json.getJSONArray("products");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = (JSONObject) items.get(i);
                Integer id = (Integer) item.get("id");
                String name = (String) item.get("name");
                String price = (String) item.get("price");
                objects.add(id + ": " + name + ", $" + price);
            }
            return objects;
        } catch (ClassCastException e) {
            throw (JSONException) new JSONException("Response JSON was not in the expected format").initCause(e);
        }
    }

}
