package ch.pawi.smartglassapp;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import ch.pawi.smartglassapp.camera.CameraPreview;
import ch.pawi.smartglassapp.camera.PhotoHandler;
import ch.pawi.smartglassapp.communication.TCPServer;
import ch.pawi.smartglassapp.detection.ObjectMatching;
import foodfinder.hslu.ch.foodfinderapp.entity.Product;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

/**
 * Created by livio on 12.10.2015.
 */
public class Main extends Activity {

    private Camera camera;
    private CameraPreview mPreview;
    private PhotoHandler photoHandler;
    private boolean objectFound;
    private TextView txtView;

    private Product product;

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

        txtView = (TextView) findViewById(R.id.textView);
        txtView.setTextColor(Color.RED);

        tcpIPConnection();
        txtView.setText("Online");
        txtView.setTextColor(Color.GREEN);

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
    //ToDo: 30 sek lang iterierung machen

    public void onClickTakePicture(View view) throws InterruptedException {
        ObjectMatching match = ObjectMatching.getInstance();
        boolean objectFound = false;
        //while(!objectFound){
            takePicture();
            objectFound = match.start("/sdcard/Pawi_Img/picture.png", "/sdcard/Pawi_Img/tabasco.png", "/sdcard/Pawi_Img/orb");
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



    //Server instanzieren und starten -> Warten auf Client verbindung
    public void tcpIPConnection(){
        new Thread(new Runnable() {
            public void run() {
                product = null;
                //Port 8080, IP egal da Server und nicht Client
              server = TCPServer.getInstance();
                server.setPort(8080);
                server.run();

                    if(server.getInput() != null){
                        if(server.getInput().isConnected()){

                            product = server.receive();
                            if (product != null) {
                                timer.run();
                            }
                    }
                }
            }
        }).start();
    }

    //Timer 30 Sekunden
    //Wenn Objekt gefunden wird abbruch ansonsten nach 30 sek abbruch
    private Runnable timer  = new Runnable(){
        public void run(){
            ObjectMatching match = ObjectMatching.getInstance();

            //Zeit wielange nach Objekt gesucht werden soll
            for(int i=0; i<=5; i++){
                try{
                takePicture();
                    if(match.start("/sdcard/Pawi_Img/picture.png", "/sdcard/Pawi_Img/" + product.getName() + ".png", "/sdcard/Pawi_Img/orb")){
                        //Antwort an Server senden
                        server = TCPServer.getInstance();
                        server.send(true);
                        objectFound = true;
                        break;
                    }
                    SystemClock.sleep(1000);
                } catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
            if(!objectFound) {
                server.send(false);
                objectFound = false;
            }

        }
    };
}
