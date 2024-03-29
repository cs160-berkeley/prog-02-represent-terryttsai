package com.cs160.joleary.catnip;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.json.JSONArray;

import io.fabric.sdk.android.Fabric;

public class RepMain extends Activity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "mHS7vcmYV5kVJU83fkrvDL7fG";
    private static final String TWITTER_SECRET = "LY0GCQTGypTXaXgRzFvN70uHVYthUCzoidtcv9756q3pwtLAXZ";


    private Button mFeedBtn;
    private ArrayAdapter<String> mAdapter;
    private ListView mListView;

    private SensorManager mSensorManager;

    private ShakeEventListener mSensorListener;

    private GoogleApiClient mApiClient;
    private static final String START_ACTIVITY = "/start_activity";
    private static final String WEAR_MESSAGE_PATH = "/message";

    //private static String TAG = LocationActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_rep_main3);
        final Context context = this;

        mListView = (ListView) findViewById(R.id.list);

        mAdapter = new ArrayAdapter<String>( this, R.layout.list_item );
        mListView.setAdapter(mAdapter);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initGoogleApiClient();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        initGoogleApiClient();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();

        mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() {

            public void onShake() {
                Toast.makeText(RepMain.this, "Random location!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(context, Reps.class);
                intent.putExtra("zipNum", "21221");
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
//        mApiClient.connect();
//
        if (mApiClient != null && !(mApiClient.isConnected() || mApiClient.isConnecting()))
            mApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if( mApiClient != null && !( mApiClient.isConnected() || mApiClient.isConnecting() ) )
            mApiClient.connect();
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onStart() {
        mApiClient.connect(); //added
        super.onStart();
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        Log.d("T", "in RepMain, got: " + messageEvent.getPath());
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (messageEvent.getPath().equalsIgnoreCase(WEAR_MESSAGE_PATH)) {
                    //mAdapter.add(new String(messageEvent.getData())); //comment out?
                    //mAdapter.notifyDataSetChanged(); //comment out?
                    String zip = new String(messageEvent.getData());

                    Toast.makeText(RepMain.this, "Received zip: " + zip, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, Reps.class);
                    intent.putExtra("zipNum", zip);
                    startActivity(intent);
                }
            }
        });
    }

//    private void sendMessage( final String path, final String text ) {
//        new Thread( new Runnable() {
//            @Override
//            public void run() {
//                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
//                for(Node node : nodes.getNodes()) {
//                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
//                            mApiClient, node.getId(), path, text.getBytes() ).await();
//                }
//
//            }
//        }).start();
//    }

    @Override
    public void onConnected( Bundle bundle ) {
        Wearable.MessageApi.addListener( mApiClient, this );
        //sendMessage(START_ACTIVITY, "");
    }

    @Override
    protected void onStop() {
        if ( mApiClient != null ) {
            Wearable.MessageApi.removeListener( mApiClient, this );
            if ( mApiClient.isConnected() ) {
                mApiClient.disconnect();
            }
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if( mApiClient != null )
            mApiClient.unregisterConnectionCallbacks( this );
        super.onDestroy();
        //mApiClient.disconnect(); //added
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

}
