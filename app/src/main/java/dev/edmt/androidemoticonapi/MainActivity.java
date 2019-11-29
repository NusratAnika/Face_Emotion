package dev.edmt.androidemoticonapi;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.contract.Scores;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public EmotionServiceClient emotionServiceClient = new EmotionServiceRestClient(" ef95aaba4f8f4bcb92a5e22b628eb766");

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Bitmap mBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.cry);
        final ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageBitmap(mBitmap);

        Button btnPRocess = (Button)findViewById(R.id.btnEmotion);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        btnPRocess.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                @SuppressLint("StaticFieldLeak") AsyncTask<InputStream, String, List<RecognizeResult>> emotionTask= new AsyncTask<InputStream, String, List<RecognizeResult>> ()
                {
                    ProgressDialog mDialog = new ProgressDialog (MainActivity.this);
                    @Override
                    protected List<RecognizeResult> doInBackground(InputStream... params) {
                        try {
                            publishProgress("Recognizing ...");
                            List<RecognizeResult> result =emotionServiceClient.recognizeImage(params[0]);
                            return result;
                        }
                        catch (Exception e)
                        {
                            return null;
                        }
                    }
                    @Override
                    protected void onPreExecute(){
                        mDialog.show();
                    }

                    @Override
                    protected void onPostExecute(List<RecognizeResult> recognizeResults){
                        mDialog.dismiss();
                        for (RecognizeResult res : recognizeResults)
                        {
                            String status = getEmo(res);
                            imageView.setImageBitmap(ImageHelper.drawRectOnBitmap(mBitmap,res.faceRectangle,status));

                        }
                    }
                    @Override
                    protected void onProgressUpdate (String... values){
                        mDialog.setMessage(values[0]);
                    }

                };
                emotionTask.execute(inputStream);
            }

        });


    }

    private String getEmo(RecognizeResult res) {
        List<Double> list = new ArrayList<>();
        Scores scores = res.scores;

        list.add(scores.anger);
        list.add(scores.happiness);
        list.add(scores.contempt);
        list.add(scores.disgust);
        list.add(scores.fear);
        list.add(scores.neutral);
        list.add(scores.sadness);
        list.add(scores.surprise);


        Collections.sort(list);

        double maxNum = list.get(list.size()-1);

        if (maxNum == scores.anger)
            return "Anger";
        else if (maxNum == scores.happiness)
            return "Happy";
        else if (maxNum == scores.contempt)
            return "Contempt";
        else if (maxNum == scores.disgust)
            return "Disgust";
        else if (maxNum == scores.sadness)
            return "Sad";
        else if (maxNum == scores.surprise)
            return "Surprise";
        else if (maxNum == scores.fear)
            return "Fear";
        else
            return "Neutral";

    }
}
