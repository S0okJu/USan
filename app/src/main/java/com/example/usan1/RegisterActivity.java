package com.example.usan1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText EtEmail;
    private EditText EtPwd, Etpwdchk;
    private EditText EtName;
    private Button register, Btnidchk;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        findViewById(R.id.register).setOnClickListener(onClickListener);
        EtEmail = (EditText) findViewById(R.id.et_email);
        EtPwd = (EditText) findViewById(R.id.et_pwd);
        EtName = (EditText) findViewById(R.id.et_name);
        Btnidchk = (Button) findViewById(R.id.btnidchk);
        Etpwdchk = (EditText) findViewById(R.id.et_pwdchk);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.register:
                    SignUp();
                    break;
            }
        }
    };

    private void SignUp() {
        final String email = EtEmail.getText().toString().trim();
        final String pwd = EtPwd.getText().toString().trim();
        final String name = EtName.getText().toString().trim();
        final String pwdchk = Etpwdchk.getText().toString().trim();

        if(email.length()>0 && pwd.length()>0 && pwdchk.length()>0) {
            if(pwd.equals(pwdchk)) {
                mAuth.createUserWithEmailAndPassword(email, pwd)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show();
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    if(task.getException().toString() != null) {
                                        Toast.makeText(RegisterActivity.this, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            }

            else {
                Toast.makeText(RegisterActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(RegisterActivity.this, "이메일과 비밀번호를 확인해 주세요.", Toast.LENGTH_SHORT).show();
        }
    }
}