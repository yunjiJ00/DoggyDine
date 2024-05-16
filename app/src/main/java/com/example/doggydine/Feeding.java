package com.example.doggydine;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Feeding extends AppCompatActivity {
    private String barcode_num;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Food> arrayList;
    private ArrayList<Food> originList;
    private FirebaseDatabase database;
    private Spinner mspinner;
    private DatabaseReference databaseReference;
    private LottieAnimationView mLt_empty;
    private TextView mTv_emty;

    private ImageView mImageview;
    Button btn_scan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feeding);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        recyclerView =findViewById(R.id.d_f_recyclerView);
        mImageview = findViewById(R.id.Iv_go_shopping);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>();
        originList = new ArrayList<>();
        mLt_empty = findViewById(R.id.Lt_empty);
        mLt_empty.setVisibility(View.INVISIBLE);
        mTv_emty = findViewById(R.id.tv_empty);
        mTv_emty.setVisibility(View.INVISIBLE);
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("DoggyDine").child("Food");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                arrayList.clear();
                originList.clear();


                for (DataSnapshot snapshot : datasnapshot.getChildren()){
                    Food food = new Food();
                    food.setProfile(snapshot.child("profile").getValue(String.class));
                    food.setName(snapshot.child("name").getValue(String.class));
                    food.setScore(snapshot.child("score").getValue(String.class));
                    food.setPrice(snapshot.child("price").getValue(String.class));
                    food.setSales_Volume(snapshot.child("sales_Volume").getValue(String.class));

                    Map<String, Boolean> materialMap = new HashMap<>();
                    DataSnapshot materialSnapshot = snapshot.child("material");
                    for (DataSnapshot materialChild : materialSnapshot.getChildren()) {
                        Boolean isTrue = materialChild.getValue(Boolean.class);
                        if (isTrue) {
                            materialMap.put(materialChild.getKey(), isTrue);
                        }
                    }
                    food.setMaterial(materialMap);

                    arrayList.add(food);
                    originList.add(food);
                    // 데이터 잘가져오는지 확인하기위해서 로그 확인용부분 나중에 없애도 됨!
                    Log.d("FirebaseData", food.toString());

                }
                adapter.notifyDataSetChanged();


            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
        adapter = new CustomAdapter(arrayList,this);
        recyclerView.setAdapter(adapter);


        mImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Feeding.this, FoodCompare.class);
                startActivity(intent);
                finish();
            }
        });


        Button recommend_btn = (Button) findViewById(R.id.recommend_btn);
        recommend_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Feeding.this, Recommend.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_stay); // 애니메이션 설정
            }
        });


        // Spinner에서 선택된 정렬 방법을 감지하는 리스너 구현
        mspinner = findViewById(R.id.spinner);
        mspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // 원본 상태 선택
                    arrayList.clear();
                    arrayList.addAll(originList);
                    adapter.notifyDataSetChanged();
                }

                else if (position == 1) { // 가격 순 선택
                    Collections.sort(arrayList, new Comparator<Food>() {
                        @Override
                        public int compare(Food food1, Food food2) {
                            // 가격이 null이면 두 음식은 동등하다고 간주
                            if (food1.getPrice() == null && food2.getPrice() == null) {
                                return 0;
                            } else if (food1.getPrice() == null) {
                                return 1;
                            } else if (food2.getPrice() == null) {
                                return -1;
                            }
                            // 문자열을 Double로 내림차순
                            return Double.compare(Double.parseDouble(food1.getPrice()), Double.parseDouble(food2.getPrice()));
                        }
                    });
                } else if (position == 2) { // 평점 순 선택
                    Collections.sort(arrayList, new Comparator<Food>() {
                        @Override
                        public int compare(Food food1, Food food2) {
                            // 평점이 같으면 이름 순으로 정렬
                            if (food1.getScore().equals(food2.getScore())) {
                                return food1.getName().compareTo(food2.getName());
                            }
                            // 평점을 Double로 오름차순
                            return Double.compare(Double.parseDouble(food2.getScore()), Double.parseDouble(food1.getScore()));
                        }
                    });
                } else if (position == 3) { // 판매량 순 선택
                    Collections.sort(arrayList, new Comparator<Food>() {
                        @Override
                        public int compare(Food food1, Food food2) {
                            // sale_Volume이 null이면 두 음식은 동등하다고 간주
                            if (food1.getSales_Volume() == null && food2.getSales_Volume() == null) {
                                return food1.getName().compareTo(food2.getName());
                            } else if (food1.getSales_Volume() == null) {
                                return 1;
                            } else if (food2.getSales_Volume() == null) {
                                return -1;
                            }
                            // 판매량이 같으면 이름 순으로 정렬
                            if (food1.getSales_Volume().equals(food2.getSales_Volume())) {
                                return food1.getName().compareTo(food2.getName());
                            }
                            // 문자열을 Integer로 오름차순
                            return Integer.compare(Integer.parseInt(food2.getSales_Volume()), Integer.parseInt(food1.getSales_Volume()));
                        }
                    });
                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 아무것도 선택되지 않았을 때의 처리
            }
        });

        btn_scan = findViewById(R.id.barcode_btn);
        btn_scan.setOnClickListener(v->{
            scanCode();
        });

        Intent intent = getIntent();
        //테스트
        if (intent != null && intent.getBooleanExtra("fromRecommend", false)) {
            ArrayList<String> selectedIngredients = intent.getStringArrayListExtra("selectedIngredients");
            if (selectedIngredients != null) {
                Log.d("SelectedIngredients", selectedIngredients.toString());
            }
        }

        //추천 intent 처리용 //
        // 선택된 재료가 모두 포함되지 않은 경우를 처리
        if (intent != null && intent.getBooleanExtra("fromRecommend", false)) {
            ArrayList<String> selectedIngredients = intent.getStringArrayListExtra("selectedIngredients");
            if (selectedIngredients != null) {
                // Firebase에서 조건을 충족하지 않는 데이터를 가져와서 RecyclerView에 표시
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        arrayList.clear();
                        boolean dataFound = false; // 데이터가 있는지 여부를 나타내는 플래그
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Food food = snapshot.getValue(Food.class);

                            // 모든 선택된 재료가 false인지 확인
                            boolean allIngredientsFalse = true;
                            for (String ingredient : selectedIngredients) {
                                if (food.getMaterial().containsKey(ingredient) && food.getMaterial().get(ingredient)) {
                                    allIngredientsFalse = false;
                                    break;
                                }
                            }
                            // 모든 재료가 false이면 RecyclerView에 추가
                            if (allIngredientsFalse) {
                                arrayList.add(food);
                                dataFound = true; // 데이터가 발견되었음을 표시
                            }
                        }
                        // 데이터가 없으면
                        if (!dataFound || arrayList.isEmpty()) {
                            // 비어 있는 상태를 표시
                            TextView tvRanking = findViewById(R.id.tv_d_f_ranking);
                            tvRanking.setText("맞춤 상품 리스트");
                            mLt_empty.setVisibility(View.VISIBLE);
                            mTv_emty.setVisibility(View.VISIBLE);
                            mLt_empty.setAnimation(R.raw.dog_sleep); // .json 파일을 로드
                            mLt_empty.loop(true);
                            mLt_empty.playAnimation();
                            btn_scan.setText("돌아가기");

                            btn_scan.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    tvRanking.setText("상품 순위 : ");
                                    Intent intent = new Intent(Feeding.this, Feeding.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            });

                        } else {
                            // 데이터가 있는 경우 RecyclerView 갱신
                            TextView tvRanking = findViewById(R.id.tv_d_f_ranking);
                            tvRanking.setText("맞춤 상품 리스트");
                            btn_scan.setText("돌아가기");
                            btn_scan.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    tvRanking.setText("상품 순위 : ");
                                    Intent intent = new Intent(Feeding.this, Feeding.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            });
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // 쿼리 취소 시 처리할 내용
                    }
                });
            }

    } else if (intent != null && intent.getBooleanExtra("FromBarcode", false)){
            //여기에 바코드 intent 처리
            Log.d("Barcode", "Receives Success");
        }
    }


    private void scanCode(){
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->{
        if(result.getContents() != null){
            // 바코드 결과를 변수에 저장
            barcode_num = result.getContents();
            //Test용
            Log.d("Barcode", "Scanned Barcode Number: " + barcode_num);

            Intent intent = new Intent();
            intent.putExtra("FromBarcode",true);
            intent.putExtra("Barcode_num",barcode_num);


            AlertDialog.Builder builder = new AlertDialog.Builder(Feeding.this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    //일단여기에 test용으로 달아두자
                    startActivity(intent);
                }
            }).show();
        }
    });
}