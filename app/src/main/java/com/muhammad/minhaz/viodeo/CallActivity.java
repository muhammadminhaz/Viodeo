package com.muhammad.minhaz.viodeo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallActivity extends AppCompatActivity {

    private TextView nameContact;
    private ImageView profileImage;
    private CircleImageView call, reject;

    private String receiverUserId = "", receiverUserImage = "", receiverUserName = "";
    private String callingID= "",ringingID = "",checker = "",senderUserId = "", senderUserImage = "", senderUserName = "";
    private DatabaseReference userReference;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mediaPlayer = MediaPlayer.create(this, R.raw.ringing);
        nameContact = findViewById(R.id.name_calling);
        profileImage = findViewById(R.id.profile_image_calling);
        call = findViewById(R.id.make_call);
        reject = findViewById(R.id.cut_call);

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                final HashMap<String, Object> callingPickupMap = new HashMap<>();
                callingPickupMap.put("picked", "picked");

                userReference.child(senderUserId).child("Ringing").updateChildren(callingPickupMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isComplete())
                        {
                            Intent intent = new Intent(CallActivity.this, VideoChatActivity.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                checker = "clicked";
                callCancelled();
            }
        });

        getAndSetReceiverProfileInfo();
    }

    private void callCancelled() {
        //Sender side
        userReference.child(senderUserId).child("Calling").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("calling"))
                {
                    callingID = dataSnapshot.child("calling").getValue().toString();

                    userReference.child(callingID)
                            .child("Ringing")
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        userReference.child(senderUserId)
                                                .child("Calling")
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        startActivity(new Intent(CallActivity.this, RegisterActivity.class));
                                                        finish();
                                                    }
                                                });

                                    }
                                }
                            });

                }

                else
                {
                    startActivity(new Intent(CallActivity.this, RegisterActivity.class));
                    finish();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //Receiver side
        userReference.child(senderUserId).child("Ringing").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("ringing"))
                {
                    ringingID = dataSnapshot.child("ringing").getValue().toString();

                    userReference.child(ringingID)
                            .child("Calling")
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        userReference.child(senderUserId)
                                                .child("Ringing")
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        startActivity(new Intent(CallActivity.this, RegisterActivity.class));
                                                        finish();
                                                    }
                                                });

                                    }
                                }
                            });

                }

                else
                {
                    startActivity(new Intent(CallActivity.this, RegisterActivity.class));
                    finish();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getAndSetReceiverProfileInfo() {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(receiverUserId).exists())
                {
                    receiverUserImage = dataSnapshot.child(receiverUserId).child("image").getValue().toString();
                    receiverUserName = dataSnapshot.child(receiverUserId).child("name").getValue().toString();
                    nameContact.setText(receiverUserName);
                    Picasso.get().load(receiverUserImage).placeholder(R.drawable.profile).into(profileImage);
                }
                if (dataSnapshot.child(senderUserId).exists())
                {
                    senderUserImage = dataSnapshot.child(senderUserId).child("image").getValue().toString();
                    senderUserId = dataSnapshot.child(senderUserId).child("name").getValue().toString();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer.start();
        userReference.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!checker.equals("clicked") && !dataSnapshot.hasChild("Calling") && !dataSnapshot.hasChild("Ringing"))
                {

                    final HashMap<String, Object> callingInfo = new HashMap<>();

                    callingInfo.put("calling",receiverUserId);

                    userReference.child(senderUserId).child("Calling").updateChildren(callingInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                final HashMap<String, Object> ringingInfo = new HashMap<>();

                                ringingInfo.put("ringing",senderUserId);

                                userReference.child(receiverUserId).child("Ringing").updateChildren(ringingInfo);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(senderUserId).hasChild("Ringing") && !dataSnapshot.child(senderUserId).hasChild("Calling"))
                {
                    call.setVisibility(View.VISIBLE);
                }

                if (dataSnapshot.child(receiverUserId).child("Ringing").hasChild("picked"))
                {
                    mediaPlayer.stop();
                    Intent intent = new Intent(CallActivity.this, VideoChatActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
