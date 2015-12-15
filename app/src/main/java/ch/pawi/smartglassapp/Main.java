package ch.pawi.smartglassapp;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import java.io.ObjectInputStream;
import java.util.List;

import ch.pawi.smartglassapp.camera.CameraPreview;
import ch.pawi.smartglassapp.camera.PhotoHandler;
import foodfinder.hslu.ch.foodfinderapp.entity.Product;

import org.opencv.*;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Created by livio on 12.10.2015.
 */
public class Main extends Activity {

    private Camera camera;
    private CameraPreview mPreview;
    private PhotoHandler photoHandler;
    private boolean connected;

    TCPServer server;

    private void main(String[] args){
        takePicture();
    }

    //Init OpenCV
    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }


    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            Log.i("loading libs", "OpenCV loading status " + status);
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("loading libs", "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    //System.loadLibrary("native_sample");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mOpenCVCallBack);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        tcpIPConnection();

        try {
            camera = Camera.open();

            // ----------- Width Height lesen und einstellen ------------

            Camera.Parameters params = camera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPictureSizes();
            for(Camera.Size size : sizes){}
            params.setPictureSize(640,  480);
            camera.setParameters(params);

            // ---------- Ende ------------------
            camera.setDisplayOrientation(0);
            mPreview = new CameraPreview(this, camera);

            // set Preview for Camera
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);



        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void takePicture(){
        try {
            // if (ConfigFileReaded == true) {
            camera.takePicture(shutterCallback, rawCallback, jpegCallback);
            camera.startPreview();

            // }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
    private void waitForProduct(){
        Product p = new Product();
        //p = server.recieve();

        takePicture();
        //Matchingdemo aufrufen mit bild von erhaltenem proudkt

    }
*/
    public void onClickTakePicture(View view) throws InterruptedException {
        MatchingDemo match = MatchingDemo.getInstance();
        boolean objectFound = false;
        //while(!objectFound){
            takePicture();
            objectFound = match.run("/sdcard/Pawi_Img/picture.png", "/sdcard/Pawi_Img/tabasco.png", "/sdcard/Pawi_Img/orb");
        //}
    }

    /**
     * Picture Callback beim shutter
     */
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() { }
    };

    /**
     * Picture Callback f�r raw-Daten
     */
    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

        }
    };

    /**
     * Picture Callback für jpeg.
     */
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            photoHandler = new PhotoHandler(getApplicationContext());
            photoHandler.onPictureTaken(data, camera);
        }
    };



    public void tcpIPConnection(){
        new Thread(new Runnable() {
            public void run() {
              server = new TCPServer(8080);
              server.run();
            }
        }).start();
    }
}
