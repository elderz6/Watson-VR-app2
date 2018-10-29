package com.watsonrecognition2.anselmojnior.watsonrecognition2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibm.watson.developer_cloud.android.library.camera.CameraHelper;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassResult;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImage;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifierResult;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyOptions;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private CameraHelper helper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helper = new CameraHelper(this);
    }


    public void takePicture(View view) {
        helper.dispatchTakePictureIntent();
    }


    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CameraHelper.REQUEST_IMAGE_CAPTURE) {
            final Bitmap photo = helper.getBitmap(resultCode);
            final File photoFile = helper.getFile(resultCode);
            final ImageView preview = findViewById(R.id.preview);
            preview.setImageBitmap(photo);


            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {

                    IamOptions options = new IamOptions.Builder()
                            .apiKey("Watson api key here")
                            .build();

                    VisualRecognition service = new VisualRecognition("2018-03-19", options);

                    InputStream imagesStream = null;
                    try {
                        imagesStream = new FileInputStream(photoFile);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView detectedObjects =
                                        findViewById(R.id.detected_objects);
                                detectedObjects.setText("");
                            }
                        });
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try{
                    ClassifyOptions classifyOptions = new ClassifyOptions.Builder()
                            .imagesFile(imagesStream)
                            .imagesFilename(photoFile.getAbsolutePath())
                            .threshold((float) 0.6)
                            .classifierIds(Arrays.asList("default"))
                            .owners(Arrays.asList("me"))
                            .build();
                    final ClassifiedImages result = service.classify(classifyOptions).execute();
                    ClassifiedImage classification = result.getImages().get(0);
                    ClassifierResult classifier = classification.getClassifiers().get(0);
                    final List fRes = classifier.getClasses();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i=0; i<fRes.size(); i++){
                                    ClassResult classe = (ClassResult)fRes.get(i);
                                    String cName = classe.getClassName();
                                    String cScore = classe.getScore().toString();
                                    String formatRes = "Class: "+cName+"  Score: "+cScore+"\n";
                                    System.out.println(formatRes);
                                    TextView detectedObjects =
                                            findViewById(R.id.detected_objects);
                                    detectedObjects.setText(detectedObjects.getText() + formatRes);
                                }
                            }
                        });
                    }catch (final Exception e){
                        System.out.println(e);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView detectedObjects =
                                        findViewById(R.id.detected_objects);
                                detectedObjects.setText(e.getMessage());

                            }
                        });
                    }
                }
            });

        }

    }
}

