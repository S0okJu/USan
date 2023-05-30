package com.example.usan_comb1.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.usan_comb1.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// 하단바, 플로팅 버튼
public class MainActivity extends AppCompatActivity {

    // 하단바 구현을 위한 변수 선언
    BottomNavigationView bottomNavigationView;
    Menu menu;

    // 앱 종료를 위한 시간 변수 설정
    private final long finishtimeed = 1000;
    private long presstime = 0;

    // 플로팅 버튼 변수 선언
    FloatingActionButton fab;
    private Button myButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 하단 바 //
        bottomNavigationView=findViewById(R.id.navigation);
        menu=bottomNavigationView.getMenu();

        bottomNavigationView.setOnNavigationItemSelectedListener(new ItemSelectedListener());
        bottomNavigationView.setSelectedItemId(R.id.home);  //선택된 아이템 지정
        // 하단 바 //

//         //테스트 버튼 //
//        myButton = findViewById(R.id.testbtn);
//
//        myButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
//                startActivity(intent);
//            }
//        });

        // 플로팅 버튼 //
//        fab = findViewById(R.id.fab);
//
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
//                startActivity(intent);
//            }
//        });
        // 플로팅 버튼 //
    }// onCreate()..

    // 하단 바 클릭 시 아이콘 변경
    class ItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener{
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch(menuItem.getItemId())
            {
                case R.id.home:
                    replaceFragment(new com.example.usan_comb1.fragment.HomeFragment());
                    menuItem.setIcon(R.drawable.select_ic_home);    // 선택한 이미지 변경
                    menu.findItem(R.id.chat).setIcon(R.drawable.unselect_ic_chat);
                    menu.findItem(R.id.user).setIcon(R.drawable.unselect_ic_user);
                    break;

                case R.id.chat:
                    replaceFragment(new com.example.usan_comb1.fragment.ChatFragment());
                    menuItem.setIcon(R.drawable.select_ic_chat);    // 선택한 이미지 변경
                    menu.findItem(R.id.home).setIcon(R.drawable.unselect_ic_home);
                    menu.findItem(R.id.user).setIcon(R.drawable.unselect_ic_user);
                    break;

                case R.id.user:
                    replaceFragment(new com.example.usan_comb1.fragment.UserFragment());
                    menuItem.setIcon(R.drawable.select_ic_user);    // 선택한 이미지 변경
                    menu.findItem(R.id.home).setIcon(R.drawable.unselect_ic_home);
                    menu.findItem(R.id.chat).setIcon(R.drawable.unselect_ic_chat);
                    break;
            }// switch()..
            return true;
        }
    }// ItemSelectedListener class..

    // 하단 바 아이콘 클릭 시 Fragment 변경
    private void replaceFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    // 뒤로가기 버튼 누를 시 Toast 메시지 출력 / 연속으로 누를 시 앱 종료
    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - presstime;

        if (0 <= intervalTime && finishtimeed >= intervalTime)
        {
            finish();
        }
        else
        {
            presstime = tempTime;
            Toast.makeText(getApplicationContext(), "한 번 더 누르시면 앱이 종료됩니다", Toast.LENGTH_SHORT).show();
        }
    }


}// MainActivity class..