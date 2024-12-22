package com.matrix_maeny.languagetranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    Button signup, login;
    TextInputLayout email, password, fullName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Objects.requireNonNull(getSupportActionBar()).hide();

        signup = findViewById(R.id.SignUp);
        login = findViewById(R.id.Login);
        email = findViewById(R.id.emailTextField);
        password = findViewById(R.id.PasswordTextField);
        fullName = findViewById(R.id.Description);

        signup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Validate and store data in firebase

                String fullNameText = fullName.getEditText().getText().toString();
                String emailText = email.getEditText().getText().toString().trim();
                String passwordText = password.getEditText().getText().toString().trim();

                registerUser(fullNameText,emailText,passwordText);


            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        
    }

    private void registerUser(String fullNameText, String emailText, String passwordText) {

        if(validateName(fullNameText) && validateEmail(emailText) && validatePassword(passwordText)){
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Toast.makeText(SignUpActivity.this,"Sign Up Failed",Toast.LENGTH_SHORT).show();
        }

    }

    private boolean validateName(String fullNameText) {
        if(fullNameText.isEmpty()){
            fullName.setError("Required");
            return false;
        }
        else{
            fullName.setError(null);
            fullName.setErrorEnabled(false);
            return true;
        }

    }

    private boolean validateEmail(String emailText) {
        if(emailText.isEmpty()){
            email.setError("Required");
            return false;
        }
        else{
            email.setError(null);
            email.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword(String passwordText) {
        if(passwordText.isEmpty()){
            password.setError("Required");
            return false;
        }
        else if(passwordText.length()<6){
            password.setError("Password is too short");
            return false;
        }
        else{
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }
}