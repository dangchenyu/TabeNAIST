package org.tensorflow.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Size;
import android.util.TypedValue;
import android.view.Surface;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;

import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.tracking.MultiBoxTracker;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class testActivity extends Activity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private File currentImageFile = null;
    private static final int TF_OD_API_INPUT_SIZE = 300;
    protected int previewWidth = 0;
    protected int previewHeight = 0;
    private static final boolean MAINTAIN_ASPECT = false;
    private String dish = "";
    private String currentImagePath = null;
    private Classifier detector;
    private ImageView mimageView;
    private String recommend;
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.1f;
    private Boolean salad = false;

    private static final String TF_OD_API_MODEL_FILE =
            "file:///android_asset/frozen_inference_graph.pb";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/dishes.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }

    public void Detect(View view) {
        Intent intent = new Intent(this, DetectorActivity.class);
        startActivity(intent);
    }

    public void Takephoto(View view) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File imageFile = null;

            try {
                imageFile = getImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (imageFile != null) {
                Uri imageUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", imageFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    private File getImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "jpg_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);
        currentImagePath = imageFile.getAbsolutePath();
        return imageFile;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath);

            Integer price = 0;
            Integer kcal = 0;
            previewHeight = bitmap.getHeight();
            previewWidth = bitmap.getWidth();
            Matrix frameToCropTransform = ImageUtils.getTransformationMatrix(
                    previewWidth, previewHeight, 300, 300, 0, MAINTAIN_ASPECT);
            Matrix cropToFrameTransform = new Matrix();
            frameToCropTransform.invert(cropToFrameTransform);
            Bitmap croppedBitmap = Bitmap.createBitmap(300, 300, Config.ARGB_8888);
            final Canvas canvas = new Canvas(croppedBitmap);
            canvas.drawBitmap(bitmap, frameToCropTransform, null);

            try {
                detector = TensorFlowObjectDetectionAPIModel.create(
                        getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
            Bitmap cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
            final Canvas canvasnew = new Canvas(cropCopyBitmap);
            final Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.0f);


            final List<Classifier.Recognition> mappedRecognitions =
                    new LinkedList<Classifier.Recognition>();

            for (final Classifier.Recognition result : results) {
                final RectF location = result.getLocation();
                if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {

                    cropToFrameTransform.mapRect(location);
                    result.setLocation(location);
                    mappedRecognitions.add(result);
                    String str = result.getTitle();
                    String pattern = "[^0-9]";

                    Pattern r = Pattern.compile(pattern);
                    Matcher m = r.matcher(str);
                    if (m.find()) {
                        String[] dis = m.replaceAll(" ").split("\\s+");
                        String[] dishes = str.split(",");
                        String z = dishes[0];
                        if (z.equals("Salad")) {

                            salad = true;
                        }

                        dish += z;
                        dish += " ";
                        int x = Integer.parseInt(dis[1]);
                        price += x;
                        int y = Integer.parseInt(dis[2]);
                        kcal += y;
                        if (kcal < 600 && !salad) {
                            recommend = "Dieting? \n How about take one \nSalad?";
                        } else if (kcal < 600 && salad) {
                            recommend = "Dieting? Come On!";
                            salad=false;
                        } else if (kcal < 800 && kcal > 500) {
                            recommend = "Looks Good!";
                        } else if (kcal > 800) {
                            recommend = "Watch out your \nWeight!";
                        }
                    } else {
                        System.out.println("NO MATCH");
                    }


                }
            }
            String p = price.toString();

            String k = kcal.toString();
            Intent intent = new Intent(this, DisplayActivity.class);
            intent.putExtra("Recommend", recommend);
            recommend = "";
            intent.putExtra("Dish", dish);
            dish = "";
            intent.putExtra("Price", p);
            intent.putExtra("Kcal", k);


            intent.putExtra("bitmap", cropCopyBitmap);

            startActivity(intent);
        }


    }
//            Bundle extras=data.getExtras();
//            Bitmap imageBitmap=(Bitmap)extras.get("data");
//            Matrix frameToCropTransform = ImageUtils.getTransformationMatrix(
//                    120, 160,
//                    300, 300,
//                    0, MAINTAIN_ASPECT);
//            Bitmap croppedBitmap = Bitmap.createBitmap(300, 300, Config.ARGB_8888);
//            final Canvas canvas = new Canvas(croppedBitmap);
//            canvas.drawBitmap(imageBitmap, frameToCropTransform, null);

}












