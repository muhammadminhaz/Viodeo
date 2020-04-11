package com.muhammad.minhaz.viodeo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private Button saveButton;
    private TextInputEditText usernameText, bioText;
    private ImageView profileImage;
    private Uri imageUri;
    private StorageReference userProfileImageReference;
    private String downloadUrl;
    private DatabaseReference userReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userProfileImageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        saveButton = findViewById(R.id.save_settings);
        usernameText = findViewById(R.id.username);
        bioText = findViewById(R.id.bio);
        profileImage = findViewById(R.id.settings_profile_image);
        progressDialog = new ProgressDialog(this);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });
        retrieveUserInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data!=null)
        {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private void saveUserData(){
        final String getUsername = usernameText.getText().toString();
        final String getBio = bioText.getText().toString();

        if (imageUri==null){
            userReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("image")){
                        saveInfoOnly();
                    }
                    else {
                        Toast.makeText(SettingsActivity.this, "Please select an image first", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else if (getUsername.equals("")){
            Toast.makeText(this, "Username is mandatory", Toast.LENGTH_SHORT).show();
        }
        else if (getBio.equals("")){
            Toast.makeText(this, "About you is mandatory", Toast.LENGTH_SHORT).show();
        }
        else {

            progressDialog.setTitle("Account Settings");
            progressDialog.setMessage("Please wait");
            progressDialog.show();

            final StorageReference filePath = userProfileImageReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            final UploadTask uploadTask = filePath.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    downloadUrl = filePath.getDownloadUrl().toString();
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        downloadUrl = task.getResult().toString();
                        HashMap<String, Object> profileMap = new HashMap<>();
                        profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        profileMap.put("name", getUsername);
                        profileMap.put("bio", getBio);
                        profileMap.put("image", downloadUrl);

                        userReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Intent intent = new Intent(SettingsActivity.this, ContactsActivity.class);
                                    startActivity(intent);
                                    finish();
                                    progressDialog.dismiss();
                                    Toast.makeText(SettingsActivity.this,"Profile Updated", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
                    }
                }
            });


        }
    }

    private void saveInfoOnly() {
        final String getUsername = usernameText.getText().toString();
        final String getBio = bioText.getText().toString();



        if (getUsername.equals(""))
        {
            Toast.makeText(this, "Username is mandatory", Toast.LENGTH_SHORT).show();

        }
        else if (getBio.equals(""))
        {
            Toast.makeText(this, "About you is mandatory", Toast.LENGTH_SHORT).show();

        }
        else {
            progressDialog.setTitle("Account Settings");
            progressDialog.setMessage("Please wait");
            progressDialog.show();
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
            profileMap.put("name", getUsername);
            profileMap.put("bio", getBio);


            userReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Intent intent = new Intent(SettingsActivity.this, ContactsActivity.class);
                        startActivity(intent);
                        finish();
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this,"Profile Updated", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

    }
    private void retrieveUserInfo(){
        userReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String imageDb = dataSnapshot.child("image").getValue().toString();
                    String nameDb = dataSnapshot.child("name").getValue().toString();
                    String bioDb = dataSnapshot.child("bio").getValue().toString();

                    usernameText.setText(nameDb);
                    bioText.setText(bioDb);
                    Picasso.get().load(imageDb).placeholder(R.drawable.profile).into(profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
