    package ch.pawi.smartglassapp;

    import android.graphics.Bitmap;
    import android.os.Environment;
    import android.widget.ImageView;
    import android.widget.Toast;

    import org.opencv.android.Utils;
    import org.opencv.core.Core;
    import org.opencv.core.Core.MinMaxLocResult;
    import org.opencv.core.CvType;
    import org.opencv.core.DMatch;
    import org.opencv.core.Mat;
    import org.opencv.core.MatOfByte;
    import org.opencv.core.MatOfDMatch;
    import org.opencv.core.MatOfKeyPoint;
    import org.opencv.core.MatOfPoint;
    import org.opencv.core.MatOfPoint2f;
    import org.opencv.core.Point;
    import org.opencv.core.Scalar;
    import org.opencv.features2d.DescriptorExtractor;
    import org.opencv.features2d.DescriptorMatcher;
    import org.opencv.features2d.FeatureDetector;
    import org.opencv.features2d.Features2d;
    import org.opencv.imgproc.Imgproc;

    import org.opencv.imgcodecs.*; // imread, imwrite, etc
    import org.opencv.videoio.*;   // VideoCapture

    import java.io.ByteArrayOutputStream;
    import java.io.Console;
    import java.io.File;
    import java.io.FileInputStream;
    import java.io.IOException;
    import java.io.InputStream;
    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.Comparator;
    import java.util.Date;
    import java.util.LinkedList;
    import java.util.List;




    /**
     * Created by livio on 14.11.2015.
     */
    public class MatchingDemo {

        private static MatchingDemo instance = null;
        protected MatchingDemo() {
            // Exists only to defeat instantiation.
        }

        public static MatchingDemo getInstance() {
            if(instance == null) {
                instance = new MatchingDemo();
            }
            return instance;
        }

        public  boolean foundObject;

        public boolean run(String inFile, String templateFile, String outFile) throws InterruptedException {
            foundObject=false;


                //ToDo: Only one Object is to search
                //ORB(inFile, "/sdcard/Pawi_Img/zucker.png", outFile);
                //ORB(inFile, "/sdcard/Pawi_Img/ravioli.png", outFile);
                ORB(inFile, "/sdcard/Pawi_Img/tabasco.png", outFile);

            System.out.println("-------------------------------------------------");
            System.out.println("Matching finished");
            System.out.println("-------------------------------------------------");
            return foundObject;
        }

        private void ORB(String inFile, String templateFile, String outFile){

            try {
                int MIN_MATCH_COUNT = 15;

                FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
                DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
                DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

                Mat img = Imgcodecs.imread(inFile);
                Mat templ = Imgcodecs.imread(templateFile);

                // template
                Imgproc.cvtColor(templ, templ, Imgproc.COLOR_RGB2GRAY);
                Mat descriptors1 = new Mat();
                MatOfKeyPoint keypoints1 = new MatOfKeyPoint();

                detector.detect(templ, keypoints1);
                descriptor.compute(templ, keypoints1, descriptors1);

                //Aufgenommenes Photo
                Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);
                Mat descriptors2 = new Mat();
                MatOfKeyPoint keypoints2 = new MatOfKeyPoint();

                detector.detect(img, keypoints2);
                descriptor.compute(img, keypoints2, descriptors2);

                MatOfDMatch matches = new MatOfDMatch();
                MatOfDMatch filteredMatches = new MatOfDMatch();
                matcher.match(descriptors1, descriptors2, matches);

                // Linking
                Scalar RED = new Scalar(255, 0, 0);
                Scalar GREEN = new Scalar(0, 255, 0);

                List<DMatch> matchesList = matches.toList();
                Double max_dist = 0.0;
                Double min_dist = 100.0;

                //Max und Min Distanz zwischen Keypoints berechnen
                for (int i = 0; i < matchesList.size(); i++) {
                    Double dist = (double) matchesList.get(i).distance;
                    if (dist < min_dist)
                        min_dist = dist;
                    if (dist > max_dist)
                        max_dist = dist;
                }

                System.out.println("Min_dist: " + min_dist);
                System.out.println("Max_dist: " + max_dist);

                //Überprüfen ob mateches eine distanz von nicht höher als 1.5 mal die min Distanz haben. Falls Ja guter Match
                LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
                for (int i = 0; i < matchesList.size(); i++) {
                    if (matchesList.get(i).distance <= (1.25 * min_dist))
                        good_matches.addLast(matchesList.get(i));
                }

                // Printing
                MatOfDMatch goodMatches = new MatOfDMatch();
                goodMatches.fromList(good_matches);

                System.out.println(matches.size() + " " + goodMatches.size());

                Mat outputImg = new Mat();
                MatOfByte drawnMatches = new MatOfByte();
                Features2d.drawMatches(templ, keypoints1, img, keypoints2, goodMatches, outputImg, GREEN, RED, drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);

                ArrayList<Point> obj_corners = new ArrayList<>(4);

                //Wenn genügend viele Matches gefunden wurden wird Resultat ausgegeben
                if (good_matches.size() >= MIN_MATCH_COUNT) {
                    try {
                        Date d = new Date();
                        Imgcodecs.imwrite("/sdcard/Pawi_Img/orb/" + d.getTime() + ".png", outputImg);
                        foundObject = true;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }



            }catch(Exception ex){
                ex.printStackTrace();
            }
        }

        private void TemplateMatching(String inFile, String templateFile, String outFile, int match_method){
            System.out.println("\nRunning Template Matching");
            try {
                File testi = new File(inFile);
                if (testi.exists()) {
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
                    //Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

                    // / Localizing the best match with minMaxLoc

                    MinMaxLocResult mmr = Core.minMaxLoc(result);

                    double threshold = 0.0;
                    Point matchLoc;
                    if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
                        matchLoc = mmr.minLoc;
                        threshold = mmr.minVal;
                    }else {
                        matchLoc = mmr.maxLoc;
                        threshold = mmr.maxVal;
                    }

                    // Show me what you got
                    Imgproc.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()), new Scalar(0, 255, 0));

                    // Save the visualized detection.
                    //Toast.makeText(this, "Output: " + outFile, Toast.LENGTH_SHORT).show();

                    Imgcodecs.imwrite("/sdcard/Pawi_Img/out/" + threshold + ".png",img);
                    File file = new File("/sdcard/Pawi_Img/out/", threshold +".png");
                    if(file.exists()){
                        System.out.print("works");
                    }
                    System.out.println("Writing " + outFile);
                    //Imgcodecs.imwrite(outFile, img);

                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

