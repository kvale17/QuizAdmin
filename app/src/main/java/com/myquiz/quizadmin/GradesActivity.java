package com.myquiz.quizadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.myquiz.quizadmin.SubjectActivity.selected_sub_index;
import static com.myquiz.quizadmin.SubjectActivity.subList;

public class GradesActivity extends AppCompatActivity {

    private RecyclerView gradesView;
    private Button addGradeBtn;
    public static List<SetModel> gradesIDs = new ArrayList<>();
    private GradesAdapter adapter;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog;
    private Dialog addGradeDialog;
    private EditText dialogGradeName;
    private Button dialogAddBtn;

    public static  int selected_grade_index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades);

        Toolbar toolbar = findViewById(R.id.GradeActivitytoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sets");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingDialog = new Dialog(GradesActivity.this);
        loadingDialog.setContentView(R.layout.loading_bar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        addGradeDialog = new Dialog(GradesActivity.this);
        addGradeDialog.setContentView(R.layout.add_grade_dialog);
        addGradeDialog.setCancelable(true);
        addGradeDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogGradeName = addGradeDialog.findViewById(R.id.ac_grade_name);
        dialogAddBtn = addGradeDialog.findViewById(R.id.ac_grade_btn);

        gradesView = findViewById(R.id.gradesRecyclerView);
        addGradeBtn = findViewById(R.id.btnAddGrade);

        addGradeBtn.setText("ADD NEW SET");

        addGradeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialogGradeName.getText().clear();
                addGradeDialog.show();

            }
        });

        dialogAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(dialogGradeName.getText().toString().isEmpty())
                {

                    dialogGradeName.setError("Enter Set Name");

                }
                else
                    addNewGrade(dialogGradeName.getText().toString());

            }
        });

        firestore = FirebaseFirestore.getInstance();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        gradesView.setLayoutManager(layoutManager);

        loadSets();


    }

    private void loadSets()
    {

        gradesIDs.clear();

        loadingDialog.show();

        firestore.collection("QUIZ").document(SubjectActivity.subList.get(selected_sub_index).getId())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                long noOfGrades = (long)documentSnapshot.get("GRADES");


                if(noOfGrades != 0) {
                    for (int i = 1; i <= noOfGrades; i++) {
                        String gradeName = documentSnapshot.getString("GRADE" + String.valueOf(i) + "_NAME");
                        gradesIDs.add(new SetModel(documentSnapshot.getString("GRADE" + String.valueOf(i) + "_ID"), gradeName));
                    }
                }

                subList.get(selected_sub_index).setGradeCounter(documentSnapshot.getString("COUNTER"));
                subList.get(selected_sub_index).setNoOfGrades(String.valueOf(noOfGrades));

                adapter = new GradesAdapter(gradesIDs);
                gradesView.setAdapter(adapter);

                loadingDialog.dismiss();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(GradesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });







    }
    private void addNewGrade(final String title)
    {
        addGradeDialog.dismiss();
        loadingDialog.show();

        final String curr_sub_id = subList.get(selected_sub_index).getId();
        final String curr_counter = subList.get(selected_sub_index).getGradeCounter();

        Map<String, Object> qData= new ArrayMap<>();
        qData.put("COUNT","0");

        firestore.collection("QUIZ").document(curr_sub_id)
                .collection(curr_counter).document("QUESTIONS_LIST")
                .set(qData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Map<String, Object> subDoc = new ArrayMap<>();
                        subDoc.put("COUNTER", String.valueOf(Integer.valueOf(curr_counter)+1  ));
                        subDoc.put("GRADE" + String.valueOf(gradesIDs.size() + 1) + "_ID", curr_counter);
                        subDoc.put("GRADE" + String.valueOf(gradesIDs.size() + 1) + "_NAME", title);
                        subDoc.put("GRADES", gradesIDs.size()+1);

                        firestore.collection("QUIZ").document(curr_sub_id)
                                .update(subDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(GradesActivity.this, "Grade Added Sucessfully", Toast.LENGTH_SHORT).show();

                                        gradesIDs.add(new SetModel(curr_counter,title));
                                        subList.get(selected_sub_index).setNoOfGrades(String.valueOf(gradesIDs.size()));
                                        subList.get(selected_sub_index).setGradeCounter(String.valueOf(Integer.valueOf(curr_counter)+1));

                                        adapter.notifyItemInserted(gradesIDs.size());
                                        loadingDialog.dismiss();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(GradesActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                                loadingDialog.dismiss();

                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(GradesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();

            }
        });



    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        if(item.getItemId() == android.R.id.home)
        {
            finish();
        }

        return  super.onOptionsItemSelected(item);

    }
}