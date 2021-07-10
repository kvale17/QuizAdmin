package com.myquiz.quizadmin;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.util.ArrayMap;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public class SubjectActivity extends AppCompatActivity {

    private RecyclerView cat_recycler_view;
    public static List<SubjectModel> subList = new ArrayList<>();
    public static  int selected_sub_index = 0;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog, addSubDialog;
    private Button addSub;
    private EditText dialogSubName;
    private Button dialogAddBtn;
    private SubjectAdapter adapter;
    private ImageView subIcon;
    private Uri imageUri;
    public  static Uri ImageUri;
    public static Uri lastUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    boolean chosePic;




    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Subjects");





        cat_recycler_view = findViewById(R.id.catRecyclerView);
        addSub = findViewById(R.id.btnAddSubject);



        chosePic=false;

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        loadingDialog = new Dialog(SubjectActivity.this);
        loadingDialog.setContentView(R.layout.loading_bar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        addSubDialog = new Dialog(SubjectActivity.this);
        addSubDialog.setContentView(R.layout.add_subject_dialog);
        addSubDialog.setCancelable(true);
        addSubDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        subIcon = addSubDialog.findViewById(R.id.subIcon);
        dialogSubName = addSubDialog.findViewById(R.id.ac_sub_name);
        dialogAddBtn = addSubDialog.findViewById(R.id.ac_sub_btn);


        firestore = FirebaseFirestore.getInstance();

        subIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });

        addSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialogSubName.getText().clear();
               addSubDialog.show();

            }
        });



        dialogAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(dialogSubName.getText().toString().isEmpty())
                {

                    dialogSubName.setError("Enter Subject Name");

                }
                else
                addNewSubject(dialogSubName.getText().toString());

            }
        });


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        cat_recycler_view.setLayoutManager(layoutManager);

        loadData();


    }
    private void loadData() {

        loadingDialog.show();
        subList.clear();


        firestore.collection("QUIZ").document("Subjects").get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {


                        long count = (long) documentSnapshot.get("COUNT");

                        for (int i = 1; i <= count; i++) {
                            String subName = documentSnapshot.getString("SUB" + String.valueOf(i) + "_NAME");
                            String subID = documentSnapshot.getString("SUB" + String.valueOf(i) + "_ID");

                            subList.add(new SubjectModel(subID,subName,"0", "1"));
                        }

                        adapter = new SubjectAdapter(subList);
                        cat_recycler_view.setAdapter(adapter);


                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dismiss();
                            }
                        },2000);

                    }


                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SubjectActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();

            }
        });

    }


    private void choosePicture()
    {


        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data!=null && data.getData()!=null)
        {

            imageUri = data.getData();
            subIcon.setImageURI(imageUri);
            chosePic = true;
        }

        if(requestCode == 2 && resultCode == RESULT_OK && data!=null && data.getData()!=null)
        {

            imageUri = data.getData();
            chosePic = true;

            Toast.makeText(this, "Image Selected", Toast.LENGTH_SHORT).show();




        }
    }

    public void uploadPicture(String title)
    {

        StorageReference riversRef = storageReference.child("IMAGES").child(title + ".png");

        if(chosePic = true) {
            final String randomKey = UUID.randomUUID().toString();



            riversRef.putFile(imageUri)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(SubjectActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
       else
        {
            riversRef.putFile(lastUri)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(SubjectActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
        chosePic = false;
    }

    private void addNewSubject(final String title)
    {
        addSubDialog.dismiss();
        loadingDialog.show();

        final Map<String,Object> subData =new ArrayMap<>();
        subData.put("NAME", title);
        subData.put("GRADES", 0);
        subData.put("COUNTER","1");

        if(chosePic == true)
        {
            uploadPicture(title);
            chosePic = false;
            subIcon.setImageDrawable(getDrawable(R.drawable.empty_icon));

        }


        final String doc_id = firestore.collection("QUIZ").document().getId();

        firestore.collection("QUIZ").document(doc_id)
                .set(subData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Map<String,Object> subDoc = new ArrayMap<>();
                        subDoc.put("SUB" + String.valueOf(subList.size() + 1) + "_NAME", title);
                        subDoc.put("SUB" + String.valueOf(subList.size() + 1) + "_ID", doc_id);
                        subDoc.put("COUNT", subList.size() + 1);

                        firestore.collection("QUIZ").document("Subjects")
                                .update(subDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(SubjectActivity.this, "Subject Added Sucessfully", Toast.LENGTH_SHORT).show();

                                        subList.add(new SubjectModel(doc_id, title, "0","1"));

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                adapter.notifyItemInserted(subList.size());
                                                loadingDialog.dismiss();
                                            }
                                        },8000);


                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(SubjectActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                loadingDialog.dismiss();

                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(SubjectActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home)
        {
            SubjectActivity.this.finish();
        }

        return super.onOptionsItemSelected(item);
    }
}