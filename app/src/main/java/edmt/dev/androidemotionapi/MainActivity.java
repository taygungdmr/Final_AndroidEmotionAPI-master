package edmt.dev.androidemotionapi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.contract.Scores;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public EmotionServiceClient emotionServiceClient = new EmotionServiceRestClient("cb458268faf3498b80595f610d5f02c3");
    ImageView imageViewFromC;
    TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Bitmap mBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.surprise);

        final ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageBitmap(mBitmap);

        errorText = (TextView)findViewById(R.id.editText);

        Button btnPRocess = (Button)findViewById(R.id.btnEmotion);

        //Convert image to stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        Button btnCamera = (Button)findViewById(R.id.btnCamera);
        imageViewFromC = (ImageView)findViewById(R.id.imageView);

        btnCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view){
                errorText.setText("");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,0);
            }
        });

        btnPRocess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                errorText.setText("");

                AsyncTask<InputStream,String,List<RecognizeResult>> emotionTask= new AsyncTask<InputStream,String,List<RecognizeResult>>()
                {
                    ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
                    @Override
                    protected List<RecognizeResult> doInBackground(InputStream... params) {
                        try{
                            publishProgress("Recognizing....");
                            List<RecognizeResult> result = emotionServiceClient.recognizeImage(params[0]);
                            return result;
                        }
                        catch (Exception ex)
                        {
                            return null;
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        mDialog.show();
                    }

                    @Override
                    protected void onPostExecute(List<RecognizeResult> recognizeResults) {
                        mDialog.dismiss();
                        if(recognizeResults == null){
                            errorText.setText("Emotion API not responding");
                        }
                        else{
                            for(RecognizeResult res : recognizeResults)
                            {
                                String status = getEmo(res);
                                imageView.setImageBitmap(ImageHelper.drawRectOnBitmap(mBitmap,res.faceRectangle,status));
                            }
                        }

                    }

                    @Override
                    protected void onProgressUpdate(String... values) {
                       mDialog.setMessage(values[0]);
                    }
                };

                emotionTask.execute(inputStream);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        imageViewFromC.setImageBitmap(bitmap);
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

        double maxNum = list.get(list.size() - 1);
        if(maxNum == scores.anger)
            return "Anger";
        else if(maxNum == scores.happiness)
            return "Happy";
        else if(maxNum == scores.contempt)
            return "Contemp";
        else if(maxNum == scores.disgust)
            return "Disgust";
        else if(maxNum == scores.fear)
            return "Fear";
        else if(maxNum == scores.neutral)
            return "Neutral";
        else if(maxNum == scores.sadness)
            return "Sadness";
        else if(maxNum == scores.surprise)
            return "Surprise";
        else
            return "Neutral";

    }
}
