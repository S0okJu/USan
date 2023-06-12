package com.example.usan_comb1.activity.sign;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.request.RegisterData;
import com.example.usan_comb1.response.RegisterResponse;
import com.example.usan_comb1.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 회원가입
public class RegisterActivity extends AppCompatActivity {
    private EditText EtPwd, EtName, EtEmail;
    private Button Btnreg;
    private ProductService service;
    private PreferenceManager preferenceManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        EtPwd = findViewById(R.id.et_pwd);
        EtName = findViewById(R.id.et_name);
        Btnreg = findViewById(R.id.register);
        EtEmail = findViewById(R.id.et_email);

        service = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        preferenceManager = new PreferenceManager(getApplicationContext());

        Btnreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });
    }

    // 이메일 형식 확인
    private boolean isEmailValid(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }


    private void attemptRegister() {
        EtPwd.setError(null);
        EtName.setError(null);
        EtEmail.setError(null);

        String password = EtPwd.getText().toString();
        String nickname = EtName.getText().toString();
        String email = EtEmail.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // 이름의 유효성 검사
        if (nickname.isEmpty()) {
            EtName.setError("이름을 입력해 주세요.");
            focusView = EtName;
            cancel = true;
        }

        if (email.isEmpty()) {
            EtEmail.setError("이메일을 입력해 주세요.");
            focusView = EtEmail;
            cancel = true;
        }  else if (!isEmailValid(email)) {
            EtEmail.setError("유효한 이메일을 입력해 주세요.");
            focusView = EtEmail;
            cancel = true;
        }

        // 패스워드의 유효성 검사
        if (password.isEmpty()) {
            EtPwd.setError("비밀번호를 입력해주세요.");
            focusView = EtPwd;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            EtPwd.setError("6자 이상의 비밀번호를 입력해 주세요.");
            focusView = EtPwd;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            startRegister(new RegisterData(nickname, password, email));
        }
    }

    private void startRegister(RegisterData data) {
        service.userRegister(data).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                RegisterResponse result = response.body();
                if (result != null && response.isSuccessful()) {

                    setFirebase(data);

                    int status_code = result.getStatus_code(); // status_code 가져오기
                    if (status_code == 409) {
                        Toast.makeText(RegisterActivity.this, "동일한 이름이 이미 존재합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "우산에 오신 것을 환영합니다!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setFirebase(RegisterData data){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put("email",data.getEmail());
        user.put("username",data.getNickname());
        database.collection("users").
                add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("FIRE","Add user");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FIRE", "Fail to add");
                    }
                });

    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }
}