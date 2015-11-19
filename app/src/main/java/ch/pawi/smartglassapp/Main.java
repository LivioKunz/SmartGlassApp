package ch.pawi.smartglassapp;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import java.util.List;

import ch.pawi.smartglassapp.camera.CameraPreview;
import ch.pawi.smartglassapp.camera.PhotoHandler;

import org.opencv.*;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.imgproc.Imgproc;

/**
 * Created by livio on 12.10.2015.
 */
public class Main extends Activity {

    private Camera camera;
    private CameraPreview mPreview;
    private PhotoHandler photoHandler;

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
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this,
                mOpenCVCallBack);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        try {
            camera = Camera.open();

            // ----------- Width Height lesen und einstellen ------------

            Camera.Parameters params = camera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPictureSizes();
            for(Camera.Size size : sizes){

            }
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

    public void onClickTakePicture(View view) {

        takePicture();
    }

    /**
     * Picture Callback beim shutter
     */
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            //new MatchingDemo().run("/sdcard/Pawi_Img/picture.jpg", "/sdcard/Pawi_Img/tabasco_skaliert.png", "/sdcard/Pawi_Img/out_tobasco_camera.png", Imgproc.TM_CCOEFF);
            new MatchingDemo().run("/sdcard/Pawi_Img/picture.jpg", "/sdcard/Pawi_Img/tabasco_bt_skaliert_hintergrund.png", "/sdcard/Pawi_Img/out_tobasco_camera.png", Imgproc.TM_CCOEFF);
        }
    };

    /**
     * Picture Callback f�r raw-Daten
     */
    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

        }
    };

    /**
     * Picture Callback f�r jpeg.
     */
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            photoHandler = new PhotoHandler(getApplicationContext());
            photoHandler.onPictureTaken(data, camera);


        }
    };



    public void onClickTemplateMatch(View view) {
        new MatchingDemo().run("/sdcard/Pawi_Img/schrank_tabasco_1.png", "/sdcard/Pawi_Img/tabasco_skaliert.png", "/sdcard/Pawi_Img/out_tobasco.png", Imgproc.TM_CCOEFF);
    }
}
