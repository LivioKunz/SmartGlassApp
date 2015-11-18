    package ch.pawi.smartglassapp;

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

        public void run(String inFile, String templateFile, String outFile, int match_method) {

            System.out.println("\nRunning Template Matching");
            try {
                File testi = new File(inFile);
                if(testi.exists()) {
                    FileInputStream fis = new FileInputStream(testi);


                    //Mat img = readInputStreamIntoMat(fis);
                    Mat img = Imgcodecs.imread(inFile);
                    Mat templ = Imgcodecs.imread(templateFile);


                    // / Create the result matrix
                    int result_cols = img.cols() - templ.cols() + 1;
                    int result_rows = img.rows() - templ.rows() + 1;
                    Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

                    // / Do the Matching and Normalize

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

                    // / Show me what you got
                    Imgproc.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(),
                            matchLoc.y + templ.rows()), new Scalar(0, 255, 0));

                    // Save the visualized detection.
                    System.out.println("Writing " + outFile);
                    Imgcodecs.imwrite(outFile, img);
                }
            }
            catch(Exception ex){
                System.out.println("Error: " + ex.getMessage());
            }
        }

        //Test um Bild einzulesen
        private static Mat readInputStreamIntoMat(InputStream inputStream) throws IOException {
            // Read into byte-array
            byte[] temporaryImageInMemory = readStream(inputStream);

            // Decode into mat. Use any IMREAD_ option that describes your image appropriately
            Mat outputImage = Imgcodecs.imdecode(new MatOfByte(temporaryImageInMemory), Imgcodecs.IMREAD_GRAYSCALE);

            return outputImage;
        }

        private static byte[] readStream(InputStream stream) throws IOException {
            // Copy content of the image to byte-array
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = stream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            byte[] temporaryImageInMemory = buffer.toByteArray();
            buffer.close();
            stream.close();
            return temporaryImageInMemory;
        }

    }

