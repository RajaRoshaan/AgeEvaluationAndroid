package com.example.mobile;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile.ml.BoneAgeModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    Button btnCaptureImage;
    TextView tv;
    TextView tv3;
    Button btnSelect;
    Button btnClear;
    Button btnPredict;
    ImageView imageDisplay;
    private ProgressBar p;
    private Bitmap img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnCaptureImage = (Button) findViewById(R.id.camera);
        btnSelect = (Button) findViewById(R.id.select);
        btnClear = (Button) findViewById(R.id.clear);
        btnPredict = (Button) findViewById(R.id.predict);
        imageDisplay = (ImageView) findViewById(R.id.boneimg);
        tv = (TextView) findViewById(R.id.tv3);
        tv3=(TextView) findViewById(R.id.tvd);



        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(intent, 0);
                } catch (ActivityNotFoundException e) {
                    Log.d("Error", e.toString());
                }


            }


        });
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 100);
            }
        });

        btnPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                       img = Bitmap.createScaledBitmap(img, 256, 256, true);

                       try {
                           BoneAgeModel model = BoneAgeModel.newInstance(getApplicationContext());

                           // Creates inputs for reference.
                           TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 3}, DataType.FLOAT32);

                           TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                           tensorImage.load(img);
                           ByteBuffer byteBuffer = tensorImage.getBuffer();

                           Log.d("shape", byteBuffer.toString());
                           Log.d("shape", inputFeature0.getBuffer().toString());

                           //Problem code
                           inputFeature0.loadBuffer(byteBuffer);

                           // Runs model inference and gets result.
                           BoneAgeModel.Outputs outputs = model.process(inputFeature0);
                           TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                           // Releases model resources if no longer used.
                           model.close();

                           System.out.println(outputFeature0);

                           //tv.setText("Age  "+ outputFeature0.getFloatArray()[0]);
                           Log.d("Predicted value", String.valueOf((outputFeature0.getFloatArray()[0]) * (-1)));
//                    Log.d("Predicted value", String.valueOf(outputFeature0.getFloatArray()[1]));
                           tv.setText("Age In Years" + outputFeature0.getFloatArray()[0] * (-1));
                           /*tv.setText(String.valueOf((outputFeature0.getFloatArray()[0])*(-1)));*/


                       } catch (IOException e) {
                           // TODO Handle the exception
                       }
                   }


        });
        if (savedInstanceState != null) {
            tv.setText(savedInstanceState.getString("data"));
        } else {
            Log.d("TAG1", "we have error");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            imageDisplay.setImageURI(data.getData());

            Uri uri = data.getData();
            try {
                img = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(requestCode==0){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageDisplay.setImageBitmap(imageBitmap);

            img = ARGBBitmap(imageBitmap);

        }
    }

    private Bitmap ARGBBitmap(Bitmap img) {
        return img.copy(Bitmap.Config.ARGB_8888,true);
    }

}