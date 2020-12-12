package com.example.navigation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Locale;

public class select_destination extends AppCompatActivity {
    ArrayList<Current_beacon> beacon = new ArrayList<Current_beacon>();
    ArrayList<String> destination = new ArrayList<String>();//목적지 목록 배열_listview adapter 연결용
    int count =0;

    TextToSpeech tts;//음성출력 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_destination);

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);//음성인식 버튼
        fab.setOnClickListener(new FABClickListener());

        Intent intent = getIntent();//receive the beacon_name(UUID)
        final String beacon_uuid = intent.getStringExtra("beacon_uuid");//통신 중인 비콘 UUID 변수

        tts = new TextToSpeech(this, new android.speech.tts.TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status){
                if(status != android.speech.tts.TextToSpeech.ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        if( beacon_uuid != null){
            ListView listView = (ListView) findViewById(R.id.listView);//listview instance
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, destination){
                @Override
                public View getView(int position, View convertView, ViewGroup parent){
                    View view = super.getView(position, convertView, parent);
                    TextView tv = (TextView) view.findViewById(android.R.id.text1);
                    tv.setTextColor(Color.WHITE);
                    tv.setGravity(Gravity.CENTER);
                    tv.setTextSize(40);
                    tv.setPadding(0,40,0,40);
                    return view;
                }
            };
            listView.setAdapter(adapter);

            //update desination lists
            FirebaseFirestore db = FirebaseFirestore.getInstance();//get the firestore access
            db.collection("Beacon").document(beacon_uuid).collection("Route")//search the db info
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                for (QueryDocumentSnapshot document : task.getResult()){
                                    //Log.d("beacon"," => " + document.getData());//print all possible path
                                    beacon.add(document.toObject(Current_beacon.class));//db datamodel
                                    destination.add(beacon.get(count).getDest_name());//db info add the arraylist
                                    Log.d("beacon", "dest_name = " + beacon.get(count).getDest_name());
                                    Log.d("beacon", "inter_path = " + beacon.get(count).getInter_path());
                                    Log.d("beacon", "navigation = " + beacon.get(count).getNavigation());
                                    count++;//total number of path
                                    adapter.notifyDataSetChanged();//update the adapter
                                }
                            }
                            else Log.d("beacon","Error getting documnets: " + task.getException());

                            //String speak = beacon[0].getVoice("UUID2");
                            //tts.setPitch(1.5f);//tone
                            //tts.setSpeechRate(1.0f);//speed
                            //tts.speak(speak,TextToSpeech.QUEUE_FLUSH,null);//speech
                        }
                    });


            listView.setOnItemClickListener(//Click the des array_event
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                            //String item = String.valueOf(parent.getItemAtPosition(i));//클릭한 위치의 des values
                            //Toast.makeText(select_destination.this, item, Toast.LENGTH_SHORT).show();
                            String destination = String.valueOf(parent.getItemAtPosition(i));
                            int obj = 1;
                            for(int j=0; j<beacon.size();j++){
                                if(beacon.get(j).getDest_name() == destination)
                                    obj = j;
                            }
                            Intent intent = new Intent(select_destination.this,load_navigation.class);//목적지 선택 시 arrival_info로 이동
                            //intent.putExtra("doc_route",destination);//목적지
                            intent.putExtra("beacon_obj",beacon.get(obj));
                            startActivity(intent);
                        }
                    }

            );
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class FABClickListener implements  View.OnClickListener{//음성인식 floating button
        @Override
        public void onClick(View view) {//floating button 클릭 시 음성인식 코드
            Toast.makeText(getApplicationContext(), "음성인식해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

}
