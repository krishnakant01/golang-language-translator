package com.matrix_maeny.languagetranslator;

import static android.Manifest.permission.CAMERA;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.github.dhaval2404.imagepicker.ImagePicker;
import com.github.dhaval2404.imagepicker.ImagePickerActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.matrix_maeny.languagetranslator.databinding.ActivityMainBinding;

import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final String[] fromLanguages = {"From", "English", "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan"
            , "Czech","Chinese", "Danish", "German", "Greek", "Hindi", "Italian", "Japanese", "Kannada", "Korean", "Marathi", "Persian","Portuguese", "Russian"
            ,"Romanian", "Spanish", "Telugu","Tamil","Turkish","Thai", "Urdu","Ukrainian","Vietnamese", "Welsh"};

    private final String[] toLanguages = {"To", "English", "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan"
            , "Czech","Chinese", "Danish", "German", "Greek", "Hindi", "Italian", "Japanese", "Kannada", "Korean", "Marathi", "Persian","Portuguese", "Russian"
            ,"Romanian", "Spanish", "Telugu","Tamil","Turkish","Thai", "Urdu","Ukrainian","Vietnamese", "Welsh"};

    private final static int MIC_REQUEST_CODE = 1;
    int languageCode = 0, fromLanguageCode = 0, toLanguageCode = 0;
    private String sourceText = "";
    private static final int REQUEST_IMAGE_CODE = 2;

    private TextInputLayout sourceTextLayout;

    Uri imageUri;
    TextRecognizer textRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sourceTextLayout = findViewById(R.id.textInputLayout);
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        binding.fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageCode = getLanguageCode(fromLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter fromSpinnerAdapter = new ArrayAdapter(MainActivity.this, R.layout.spinner_item, fromLanguages);
        fromSpinnerAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        binding.fromSpinner.setAdapter(fromSpinnerAdapter);

        binding.toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageCode = getLanguageCode(toLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter toSpinnerAdapter = new ArrayAdapter(MainActivity.this, R.layout.spinner_item, toLanguages);
        toSpinnerAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        binding.toSpinner.setAdapter(toSpinnerAdapter);

        binding.translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSourceText() && checkFromLanguage() && checkToLanguage()) {
                    binding.sourceText.setText(sourceText);
                    binding.sourceText.setSelection(sourceText.length());
                    translateText();
                }
            }
        });

        binding.inputMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to convert into text");

                try {
                    startActivityForResult(intent, MIC_REQUEST_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.inputImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkCameraPermission()){
                    Log.d("message", "Camera permission granted");
                    ImagePicker.with(MainActivity.this)
                            .crop()	    			//Crop image(Optional), Check Customization for more option
                            .compress(1024)			//Final image size will be less than 1 MB(Optional)
                            .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                            .start(REQUEST_IMAGE_CODE);
                }else{
                    Log.d("message", "Camera Permission not granted");
                    requestCameraPermission();
                }
            }
        });
    }

    @Contract(pure = true)
    private int getLanguageCode(@NonNull String language) {

        int lCode = 0;

        switch (language) {
            case "English":
                lCode = FirebaseTranslateLanguage.EN;
                break;
            case "Afrikaans":
                lCode = FirebaseTranslateLanguage.AF;
                break;
            case "Arabic":
                lCode = FirebaseTranslateLanguage.AR;
                break;
            case "Belarusian":
                lCode = FirebaseTranslateLanguage.BE;
                break;
            case "Bulgarian":
                lCode = FirebaseTranslateLanguage.BG;
                break;
            case "Bengali":
                lCode = FirebaseTranslateLanguage.BN;
                break;
            case "Catalan":
                lCode = FirebaseTranslateLanguage.CA;
                break;
            case "Czech":
                lCode = FirebaseTranslateLanguage.CS;
                break;
            case "Danish":
                lCode = FirebaseTranslateLanguage.DA;
                break;
            case "German":
                lCode = FirebaseTranslateLanguage.DE;
                break;
            case "Greek":
                lCode = FirebaseTranslateLanguage.EL;
                break;
            case "Welsh":
                lCode = FirebaseTranslateLanguage.CY;
                break;
            case "Hindi":
                lCode = FirebaseTranslateLanguage.HI;
                break;
            case "Urdu":
                lCode = FirebaseTranslateLanguage.UR;
                break;
            case "Telugu":
                lCode = FirebaseTranslateLanguage.TE;
                break;
            case "Spanish":
                lCode = FirebaseTranslateLanguage.ES;
                break;
            case "Persian":
                lCode = FirebaseTranslateLanguage.FA;
                break;
            case "Italian":
                lCode = FirebaseTranslateLanguage.IT;
                break;
            case "Japanese":
                lCode = FirebaseTranslateLanguage.JA;
                break;
            case "Kannada":
                lCode = FirebaseTranslateLanguage.KN;
                break;
            case "Korean":
                lCode = FirebaseTranslateLanguage.KO;
                break;
            case "Marathi":
                lCode = FirebaseTranslateLanguage.MR;
                break;
            case "Russian":
                lCode = FirebaseTranslateLanguage.RU;
                break;
            case "Romanian":
                lCode = FirebaseTranslateLanguage.RO;
                break;
            case "Portuguese":
                lCode = FirebaseTranslateLanguage.PT;
                break;
            case "Tamil":
                lCode = FirebaseTranslateLanguage.TA;
                break;
            case "Thai":
                lCode = FirebaseTranslateLanguage.TH;
                break;
            case "Turkish":
                lCode = FirebaseTranslateLanguage.TR;
                break;
            case "Ukrainian":
                lCode = FirebaseTranslateLanguage.UK;
                break;
            case "Vietnamese":
                lCode = FirebaseTranslateLanguage.VI;
                break;
            case "Chinese":
                lCode = FirebaseTranslateLanguage.ZH;
                break;



        }

        return lCode;
    }

    private boolean checkCameraPermission(){
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission(){
        int PERMISSION_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PERMISSION_CODE);
    }

    private boolean checkSourceText() {
        sourceText = "";

        try {
            sourceText = Objects.requireNonNull(binding.sourceText.getText()).toString().trim();
            if (!sourceText.equals("")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(MainActivity.this, "Please enter text", Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean checkFromLanguage() {

        if (fromLanguageCode == 0) {
            Toast.makeText(this, "Please select languages", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean checkToLanguage() {

        if (toLanguageCode == 0) {
            Toast.makeText(this, "Please select languages", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @SuppressLint("SetTextI18n")
    private void translateText() {
        binding.resultText.setText("Downloading Language...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode).build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                binding.resultText.setText("Translating...");
                translator.translate(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        binding.resultText.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "fail to translate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "fail to download Model: " + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if(cameraPermission){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MIC_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> list = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                binding.sourceText.setText(list.get(0));
            }

        }

       if(requestCode == REQUEST_IMAGE_CODE && resultCode == RESULT_OK){
            if(data!=null){
                imageUri = data.getData();
                Log.i("message", "Data is not null, Image Selected");
                Toast.makeText(this, "Image Selected", Toast.LENGTH_SHORT).show();
                recognizeText();
            }else{
                Log.i("message", "Data is null, Image Not Selected");
                Toast.makeText(this, "Image not selected", Toast.LENGTH_SHORT).show();
            }
       }
    }

    private void recognizeText(){
        if(imageUri!=null){
            try {
                Log.i("message", "Inside recognizeText function");
                InputImage inputImage = InputImage.fromFilePath(MainActivity.this,imageUri);

                Task<Text> result =textRecognizer.process(inputImage).addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text text) {
                        String recognizeText = text.getText();
                        sourceTextLayout.getEditText().setText(recognizeText);
                        Log.i("message", "Text Recognized Success");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // do something
        startActivity(new Intent(MainActivity.this,AboutActivity.class));
        return super.onOptionsItemSelected(item);
    }
}