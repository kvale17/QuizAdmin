package com.myquiz.quizadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import static com.myquiz.quizadmin.GradesActivity.gradesIDs;
import static com.myquiz.quizadmin.GradesActivity.selected_grade_index;
import static com.myquiz.quizadmin.QuestionsActivity.quesList;
import static com.myquiz.quizadmin.SubjectActivity.selected_sub_index;
import static com.myquiz.quizadmin.SubjectActivity.subList;

public class QuestionAddActivity extends AppCompatActivity {

    private EditText question, optionA, optionB, optionC, optionD, answer;
    private Button addQB;
    private String qStr, aStr, bStr, cStr, dStr, ansStr;
    private Dialog loadingDialog;
    private FirebaseFirestore firestore;
    private String action;
    private  int qID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_add);

        Toolbar toolbar = findViewById(R.id.qdetails_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        question= findViewById(R.id.question);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        answer = findViewById(R.id.answer);
        addQB = findViewById(R.id.addQB);


        loadingDialog = new Dialog(QuestionAddActivity.this);
        loadingDialog.setContentView(R.layout.loading_bar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        firestore = FirebaseFirestore.getInstance();

        action = getIntent().getStringExtra("ACTION");

        if(action.compareTo("EDIT") == 0)
        {
            qID = getIntent().getIntExtra("Q_ID",0);
            loadData(qID);
            getSupportActionBar().setTitle("Question " + String.valueOf(qID + 1));
            addQB.setText("Update");
        }
        else
        {
            getSupportActionBar().setTitle("Question " + String.valueOf(quesList.size() + 1));
            addQB.setText("Add");
        }

        addQB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                qStr = question.getText().toString();
                aStr = optionA.getText().toString();
                bStr = optionB.getText().toString();
                cStr = optionC.getText().toString();
                dStr = optionD.getText().toString();
                ansStr = answer.getText().toString();

                if(qStr.isEmpty())
                {
                    question.setError("Enter Question");
                    return;
                }
                if(aStr.isEmpty())
                {
                    optionA.setError("Enter Option A");
                    return;
                }
                if(bStr.isEmpty())
                {
                    optionB.setError("Enter Option B");
                    return;
                }
                if(cStr.isEmpty())
                {
                    optionC.setError("Enter Option C");
                    return;
                }
                if(dStr.isEmpty())
                {
                    optionD.setError("Enter Option D");
                    return;
                }
                if(ansStr.isEmpty())
                {
                    answer.setError("Enter Correct Answer");
                    return;

                }

                if(action.compareTo("EDIT") == 0)
                {

                    editQuestion();

                }
                else
                {

                    addNewQuestion();

                }



            }
        });

    }

    private void addNewQuestion()
    {

        loadingDialog.show();

        Map<String, Object> quesData = new ArrayMap<>();

        quesData.put("QUESTION",qStr);
        quesData.put("A",aStr);
        quesData.put("B",bStr);
        quesData.put("C",cStr);
        quesData.put("D",dStr);
        quesData.put("ANSWER",ansStr);

        final String doc_id = firestore.collection("QUIZ").document(subList.get(selected_sub_index).getId())
                .collection(gradesIDs.get(selected_grade_index).getId()).document().getId();

        firestore.collection("QUIZ").document(subList.get(selected_sub_index).getId())
                .collection(gradesIDs.get(selected_grade_index).getId()).document(doc_id)
                .set(quesData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Map<String, Object> quesDoc = new ArrayMap<>();
                        quesDoc.put("Q" + String.valueOf(quesList.size() + 1) + "_ID", doc_id);
                        quesDoc.put("COUNT",String.valueOf(quesList.size() + 1));

                        firestore.collection("QUIZ").document(subList.get(selected_sub_index).getId())
                                .collection(gradesIDs.get(selected_grade_index).getId()).document("QUESTIONS_LIST")
                                .update(quesDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(QuestionAddActivity.this, "Question Added Sucessfully", Toast.LENGTH_SHORT).show();

                                        quesList.add(new QuestionModel(doc_id, qStr, aStr, bStr, cStr, dStr, Integer.valueOf(ansStr)));

                                        loadingDialog.dismiss();
                                        QuestionAddActivity.this.finish();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(QuestionAddActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                loadingDialog.dismiss();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(QuestionAddActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });

    }

    private void loadData(int id)
    {

        question.setText(quesList.get(id).getQuestion());
        optionA.setText(quesList.get(id).getOptionA());
        optionB.setText(quesList.get(id).getOptionB());
        optionC.setText(quesList.get(id).getOptionC());
        optionD.setText(quesList.get(id).getOptionD());
        answer.setText(String.valueOf(quesList.get(id).getCorrectAns()));

    }

    private void editQuestion()
    {

        loadingDialog.show();

        Map<String, Object> quesData = new ArrayMap<>(0);
        quesData.put("QUESTION", qStr);
        quesData.put("A",aStr);
        quesData.put("B",bStr);
        quesData.put("C",cStr);
        quesData.put("D",dStr);
        quesData.put("ANSWER",ansStr);

        firestore.collection("QUIZ").document(subList.get(selected_sub_index).getId())
                .collection(gradesIDs.get(selected_grade_index).getId()).document(quesList.get(qID).getQuesID())
                .set(quesData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(QuestionAddActivity.this, "Question Updated Sucessfully", Toast.LENGTH_SHORT).show();

                        quesList.get(qID).setQuestion(qStr);
                        quesList.get(qID).setOptionA(aStr);
                        quesList.get(qID).setOptionB(bStr);
                        quesList.get(qID).setOptionC(cStr);
                        quesList.get(qID).setOptionD(dStr);
                        quesList.get(qID).setCorrectAns(Integer.valueOf(ansStr));

                        loadingDialog.dismiss();
                        QuestionAddActivity.this.finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(QuestionAddActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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