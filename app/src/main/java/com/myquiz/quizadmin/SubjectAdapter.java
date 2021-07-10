package com.myquiz.quizadmin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

import static com.myquiz.quizadmin.SubjectActivity.ImageUri;
import static com.myquiz.quizadmin.SubjectActivity.lastUri;
import static com.myquiz.quizadmin.SubjectActivity.subList;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.ViewHolder> {

    private List<SubjectModel> sub_list;

    private StorageReference storageRef;
    private Context context;



    public SubjectAdapter(List<SubjectModel> sub_list)
    {
        this.sub_list = sub_list;
    }



    @NonNull
    @Override
    public SubjectAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sub_item_layout,viewGroup,false);

        context = viewGroup.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectAdapter.ViewHolder viewHolder, int position) {

        String title = sub_list.get(position).getName();

        viewHolder.setData(title, position, this);

    }

    @Override
    public int getItemCount()
    {
        return sub_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView subName;
        private ImageView delete;
        private ImageView icon;
        public  ImageView editIcon;
        private Dialog loadingDialog;
        private Dialog editDialog;


        private EditText tv_edit_SubName;
        private Button updateSubBtn;





        public ViewHolder(@NonNull View itemView){
            super(itemView);

            subName = itemView.findViewById(R.id.subName);
            delete = itemView.findViewById(R.id.subDelete);
            icon = itemView.findViewById(R.id.subIcon);
            storageRef = FirebaseStorage.getInstance().getReference();



            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_bar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


            editDialog = new Dialog(itemView.getContext());
            editDialog.setContentView(R.layout.edit_subject_dialog);
            editDialog.setCancelable(true);
            editDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


            tv_edit_SubName = editDialog.findViewById(R.id.ec_sub_name);
            updateSubBtn = editDialog.findViewById(R.id.ec_add_btn);
            editIcon = editDialog.findViewById(R.id.ec_subIcon);



        }


        private void setData(final String title, final int position, final SubjectAdapter adapter)
        {
            subName.setText(title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SubjectActivity.selected_sub_index = position;

                    Intent intent = new Intent(itemView.getContext(), GradesActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });

            storageRef.child("IMAGES").child(title + ".png")
                    .getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(final Uri uri) {

                            GlideApp.with(context).load(uri).into(icon);

                            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {


                                    editDialog.show();
                                    tv_edit_SubName.setText(title);


                                    return false;
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });



            editIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    ((Activity)context).startActivityForResult(intent, 2);



                }
            });




            updateSubBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {


                    if(tv_edit_SubName.getText().toString().isEmpty())
                    {
                        tv_edit_SubName.setError("Enter Subject Name");
                    }


                    StorageReference reference1 = storageRef.child("IMAGES").child(sub_list.get(position).getName()+".png");

                    storageRef.child("IMAGES").child(sub_list.get(position).getName()+".png")
                            .getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    lastUri = uri;

                                    if(context instanceof SubjectActivity)
                                        ((SubjectActivity)context).uploadPicture(tv_edit_SubName.getText().toString());


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });


                    updateSubject(tv_edit_SubName.getText().toString(), position, itemView.getContext(), adapter);
                    reference1.delete();

                }
            });



            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Subject")
                            .setMessage("Do you want to delete this subject?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    deleteSubject(position, itemView.getContext(), adapter );

                                }
                            })
                            .setNegativeButton("Cancel",null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                    dialog.getButton(dialog.BUTTON_POSITIVE).setBackgroundColor(Color.RED);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setBackgroundColor(Color.RED);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0,0,50,0);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setLayoutParams(params);

                }
            });

        }



        private void deleteSubject(final int id, final Context context, final SubjectAdapter adapter)
        {
            loadingDialog.show();

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            StorageReference image = storageRef.child("IMAGES").child(subList.get(id).getName() + ".png");
            image.delete();


            Map<String, Object> subDoc = new ArrayMap<>();
            int index = 1;

            for(int i=0; i< sub_list.size(); i++) {
                if (i != id) {
                    subDoc.put("SUB" + index + "_ID", sub_list.get(i).getId());
                    subDoc.put("SUB" + index + "_NAME", sub_list.get(i).getName());
                    index++;
                } else {
                    firestore.collection("QUIZ").document(sub_list.get(i).getId()).delete();

                }
            }


                subDoc.put("COUNT", index -1);


                firestore.collection("QUIZ").document("Subjects")
                        .set(subDoc)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(context, "Subject Deleted Sucessfully", Toast.LENGTH_SHORT).show();

                                subList.remove(id);
                                adapter.notifyDataSetChanged();

                                loadingDialog.dismiss();



                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();

                    }
                });

            }




        public void updateSubject(final String subNewName, final int pos, final Context context, final SubjectAdapter adapter)
        {

            editDialog.dismiss();

            loadingDialog.show();

            Map<String, Object> subData = new ArrayMap<>();
            subData.put("NAME",subNewName);

            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("QUIZ").document(sub_list.get(pos).getId())
                    .update(subData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Map<String, Object> subDoc = new ArrayMap<>();
                            subDoc.put("SUB" + String.valueOf(pos + 1) + "_NAME", subNewName);

                            firestore.collection("QUIZ").document("Subjects")
                                    .update(subDoc)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Toast.makeText(context, "Subject Changed Sucessfully", Toast.LENGTH_SHORT).show();


                                            subList.get(pos).setName(subNewName);

                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    adapter.notifyDataSetChanged();
                                                    loadingDialog.dismiss();
                                                }
                                            },8000);

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    loadingDialog.dismiss();

                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();

                }
            });

        }





    }

}
