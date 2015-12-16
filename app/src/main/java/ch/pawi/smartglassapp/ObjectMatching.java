    package ch.pawi.smartglassapp;

    import org.opencv.core.Core;
    import org.opencv.core.Core.MinMaxLocResult;
    import org.opencv.core.CvType;
    import org.opencv.core.DMatch;
    import org.opencv.core.Mat;
    import org.opencv.core.MatOfByte;
    import org.opencv.core.MatOfDMatch;
    import org.opencv.core.MatOfKeyPoint;
    import org.opencv.core.Point;
    import org.opencv.core.Scalar;
    import org.opencv.features2d.DescriptorExtractor;
    import org.opencv.features2d.DescriptorMatcher;
    import org.opencv.features2d.FeatureDetector;
    import org.opencv.features2d.Features2d;
    import org.opencv.imgproc.Imgproc;
    import org.opencv.imgcodecs.*; // imread, imwrite, etc
    import java.io.File;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.LinkedList;
    import java.util.List;




    /**
     * Created by Livio Kunz on 14.11.2015.
     */
    public class ObjectMatching {

        private static ObjectMatching instance = null;
        protected ObjectMatching() {
            // Exists only to defeat instantiation.
        }

        public static ObjectMatching getInstance() {
            if(instance == null) {
                instance = new ObjectMatching();
            }
            return instance;
        }

        public  boolean foundObject;

        public boolean start(String inFile, String templateFile, String outFile) throws InterruptedException {
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
                //Minimum Anzahl and gefundenen guten Matches um zu sagen ob Objekt in Bild vorhanden oder nicht
                int MIN_MATCH_COUNT = 5;

                //FeatureDecetor ORB initalisieren
                FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
                DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
                DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

                //Bild und Template in Mat lesen
                Mat img = Imgcodecs.imread(inFile);
                Mat templ = Imgcodecs.imread(templateFile);

                //Templete merkmale im Bild suchen
                Imgproc.cvtColor(templ, templ, Imgproc.COLOR_RGB2GRAY);
                Mat descriptors1 = new Mat();
                MatOfKeyPoint keypoints1 = new MatOfKeyPoint();

                detector.detect(templ, keypoints1);
                descriptor.compute(templ, keypoints1, descriptors1);

                //Aufgenommenes Photo Merkmale suchen
                Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);
                Mat descriptors2 = new Mat();
                MatOfKeyPoint keypoints2 = new MatOfKeyPoint();

                detector.detect(img, keypoints2);
                descriptor.compute(img, keypoints2, descriptors2);

                //Übereinstimmungen suchen und schreiben
                MatOfDMatch matches = new MatOfDMatch();
                //MatOfDMatch filteredMatches = new MatOfDMatch();
                matcher.match(descriptors1, descriptors2, matches);

                //Farben für Punkte
                Scalar RED = new Scalar(255, 0, 0);
                Scalar GREEN = new Scalar(0, 255, 0);

                //Liste für matches erstellen
                //min_dist -> Je kleiner desto genauer muss übereinstimmung sein
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

                //Überprüfen ob matches eine distanz von nicht höher als 1.5 mal die min Distanz haben. Falls Ja Guter Match
                LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
                for (int i = 0; i < matchesList.size(); i++) {
                    if (matchesList.get(i).distance <= (1.27 * min_dist))
                        good_matches.addLast(matchesList.get(i));
                }

                //Gute Matches in Liste laden
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

        //Template Matching verlgeicht ein TEmplate Bild mit dem aufegnommenen Bild und versucht dieses wiederzuerkennen
        //Template Matching wurde verworfen da der Threshold zur bestimmung ob das Objekt auf dem Bild vorhanden ist nicht genügend
        //genau defniert werden konnte.
        private void TemplateMatching(String inFile, String templateFile, String outFile, int match_method){
            System.out.println("\nRunning Template Matching");
            try {
                //Nur zum üprüfen ob inputBild vorhanden ist
                File testifexists = new File(inFile);
                if (testifexists.exists()) {

                    //Template und inputBild in Mat wandeln
                    Mat img = Imgcodecs.imread(inFile);
                    Mat templ = Imgcodecs.imread(templateFile);

                    // Result Mat erstellen (Grösse inputBild)
                    int result_cols = img.cols() - templ.cols() + 1;
                    int result_rows = img.rows() - templ.rows() + 1;
                    Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

                    //Matching ausführen, Normalize wird nicht durchgeführt um Threshold besser setzen zu können
                    Imgproc.matchTemplate(img, templ, result, match_method);
                    //Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

                    // Bester Match mit MinMaxLoc suchen
                    MinMaxLocResult mmr = Core.minMaxLoc(result);

                    //Threshold initaliseren, Threshold ist minVal oder maxVal je nach match Methode
                    double threshold = 0.0;
                    Point matchLoc;
                    if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
                        matchLoc = mmr.minLoc;
                        threshold = mmr.minVal;
                    }else {
                        matchLoc = mmr.maxLoc;
                        threshold = mmr.maxVal;
                    }

                    //Gefundene Bild Partition anzeigen
                    Imgproc.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()), new Scalar(0, 255, 0));

                    //Speichern des Bildes
                    Imgcodecs.imwrite("/sdcard/Pawi_Img/out/" + threshold + ".png",img);

                }
            } catch (Exception ex) {
                System.out.println("Fehler: " + ex.getMessage());
            }
        }
    }

