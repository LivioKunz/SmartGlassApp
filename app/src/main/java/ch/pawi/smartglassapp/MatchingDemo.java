    package ch.pawi.smartglassapp;

    import android.widget.Toast;

    import org.opencv.core.Core;
    import org.opencv.core.Core.MinMaxLocResult;
    import org.opencv.core.CvType;
    import org.opencv.core.Mat;
    import org.opencv.core.MatOfByte;
    import org.opencv.core.Point;
    import org.opencv.core.Scalar;
    import org.opencv.imgproc.Imgproc;

    import org.opencv.imgcodecs.*; // imread, imwrite, etc
    import org.opencv.videoio.*;   // VideoCapture

    import java.io.ByteArrayOutputStream;
    import java.io.File;
    import java.io.FileInputStream;
    import java.io.IOException;
    import java.io.InputStream;


    /**
     * Created by livio on 14.11.2015.
     */
    public class MatchingDemo {

        //ToDo: Better matching, if not found give back false or similar

        public void run(String inFile, String templateFile, String outFile, int match_method) {

            System.out.println("\nRunning Template Matching");
            try {
                File testi = new File(inFile);
                if(testi.exists()) {
                    FileInputStream fis = new FileInputStream(testi);


                    //Mat img = readInputStreamIntoMat(fis);
                    Mat img = Imgcodecs.imread(inFile);
                    Mat templ = Imgcodecs.imread(templateFile);


                    // Create the result matrix
                    int result_cols = img.cols() - templ.cols() + 1;
                    int result_rows = img.rows() - templ.rows() + 1;
                    Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

                    // Do the Matching and Normalize
                    Imgproc.matchTemplate(img, templ, result, match_method);
                    Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

                    // / Localizing the best match with minMaxLoc
                    MinMaxLocResult mmr = Core.minMaxLoc(result);

                    Point matchLoc;
                    if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
                        matchLoc = mmr.minLoc;
                    } else {
                        matchLoc = mmr.maxLoc;
                    }

                    // Show me what you got
                    Imgproc.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(),
                            matchLoc.y + templ.rows()), new Scalar(0, 255, 0));

                    // Save the visualized detection.
                    //Toast.makeText(this, "Output: " + outFile, Toast.LENGTH_SHORT).show();
                    System.out.println("Writing " + outFile);
                    Imgcodecs.imwrite(outFile, img);
                }
            }
            catch(Exception ex){
                System.out.println("Error: " + ex.getMessage());
            }
        }

    }

