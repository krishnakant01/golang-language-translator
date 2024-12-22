package com.matrix_maeny.languagetranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    Button signup,login;
    TextInputLayout email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Objects.requireNonNull(getSupportActionBar()).hide();

        signup = findViewById(R.id.SignUp);
        login = findViewById(R.id.Login);
        email = findViewById(R.id.emailTextField);
        password = findViewById(R.id.PasswordTextField);

        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String emailText = email.getEditText().getText().toString();
                String passwordText = password.getEditText().getText().toString();

                if(passwordText.isEmpty()){
                    password.setError("Field cannot be empty");
                }
                else if( emailText.isEmpty()){
                    email.setError("Field cannot be empty");
                }
                else{
                    loginUser(emailText,passwordText);
                }

            }
        });

        signup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void loginUser(String emailText, String passwordText){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}