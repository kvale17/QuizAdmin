package com.myquiz.quizadmin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;
import java.util.Map;

import static com.myquiz.quizadmin.GradesActivity.gradesIDs;
import static com.myquiz.quizadmin.GradesActivity.selected_grade_index;
import static com.myquiz.quizadmin.SubjectActivity.selected_sub_index;
import static com.myquiz.quizadmin.SubjectActivity.subList;

public class GradesAdapter extends RecyclerView.Adapter<GradesAdapter.ViewHolder> {

    private List<SetModel> gradeIDs;

    public GradesAdapter(List<SetModel> gradeIDs) {
        this.gradeIDs = gradeIDs;
    }

    @NonNull
    @Override
    public GradesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grade_item_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GradesAdapter.ViewHolder holder, int position) {

        String gradeID = gradeIDs.get(position).getId();
        String title = gradeIDs.get(position).getName();

        holder.setData(position, gradeID, this, title);;
    }

    @Override
    public int getItemCount() {
        return gradeIDs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView gradeName;
        private ImageView deleteGradeBtn;
        private Dialog loadingDialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            gradeName = itemView.findViewById(R.id.gradeName);
            deleteGradeBtn = itemView.findViewById(R.id.gradeDelete);

            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_bar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);



        }



        private void setData(final int pos, final String gradeID, final GradesAdapter adapter, String title)
        {
            gradeName.setText(title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    selected_grade_index = pos;

                    Intent intent = new Intent(itemView.getContext(), QuestionsActivity.class);
                    itemView.getContext().startActivity(intent);

                }
            });

            deleteGradeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Grade")
                            .setMessage("Do you want to delete this Set?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    deleteGrade(pos, gradeID, itemView.getContext(), adapter);

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

        private void deleteGrade(final int pos, final String gradeID, final Context context, final GradesAdapter adapter)
        {
            loadingDialog.show();

            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("QUIZ").document(subList.get(selected_sub_index).getId())
                    .collection(gradeID).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            WriteBatch batch = firestore.batch();

                            for(QueryDocumentSnapshot doc : queryDocumentSnapshots)
                            {
                                batch.delete(doc.getReference());
                            }

                            batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Map<String, Object> subDoc = new ArrayMap<>();
                                    int index = 1;

                                    for(int i = 0; i<gradeIDs.size(); i++)
                                    {
                                        if(i != pos)
                                        {
                                            subDoc.put("GRADE" + String.valueOf(index) + "_ID", gradeIDs.get(i).getId());
                                            subDoc.put("GRADE" + String.valueOf(index) + "_NAME", gradeIDs.get(i).getName());
                                            index++;
                                        }
                                        else
                                        {
                                           DocumentReference firestore1 = firestore.collection("QUIZ").document(subList.get(selected_sub_index).getId());

                                        }


                                    }

                                    subDoc.put("GRADES", index -1);
                                    subDoc.put("COUNTER", String.valueOf(index));

                                    final String curr_counter2 = String.valueOf(Integer.valueOf(subList.get(selected_sub_index).getGradeCounter()) -1) ;
                                    subList.get(selected_sub_index).setGradeCounter(curr_counter2);

                                    firestore.collection("QUIZ").document(subList.get(selected_sub_index).getId())
                                            .update(subDoc)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    Toast.makeText(context, "Set Deleted Sucessfully", Toast.LENGTH_SHORT).show();

                                                    gradesIDs.remove(pos);

                                                    subList.get(selected_sub_index).setNoOfGrades(String.valueOf(gradesIDs.size()));

                                                    adapter.notifyDataSetChanged();

                                                    loadingDialog.dismiss();


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
