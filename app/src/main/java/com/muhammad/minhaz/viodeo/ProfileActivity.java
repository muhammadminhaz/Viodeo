package com.muhammad.minhaz.viodeo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    String receiverUserID = "", receiverUserImage = "", receiverUserName = "";
    private ImageView bgProfileImage;
    private TextView nameProfile;
    private Button acceptButton, rejectButton;
    private FirebaseAuth auth;
    private String senderUserId;
    private String currentState = "new";
    private DatabaseReference friendRequestReference, contactsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts");

        auth = FirebaseAuth.getInstance();
        senderUserId = auth.getCurrentUser().getUid();

        receiverUserID = getIntent().getExtras().get("visit_profile").toString();
        receiverUserImage = getIntent().getExtras().get("profile_image").toString();
        receiverUserName = getIntent().getExtras().get("profile_name").toString();

        bgProfileImage = findViewById(R.id.bg_profile);
        nameProfile = findViewById(R.id.name_profile);
        acceptButton = findViewById(R.id.add_friend);
        rejectButton = findViewById(R.id.decline_friend);

        Picasso.get().load(receiverUserImage).into(bgProfileImage);
        nameProfile.setText(receiverUserName);

        manageClickEvents();
    }

    private void manageClickEvents() {

        friendRequestReference.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverUserID)){
                    String requestType = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                    if (requestType.equals("sent")){
                        currentState = "request_sent";
                        acceptButton.setText("Cancel Friend Request");
                    }
                    else if (requestType.equals("received")){
                        currentState = "request_received";
                        acceptButton.setText("Accept Friend Request");

                        rejectButton.setVisibility(View.VISIBLE);
                        rejectButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelFriendRequest();
                            }
                        });
                    }
                }
                else
                {
                    contactsReference.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiverUserID))
                            {
                                currentState = "friends";
                                acceptButton.setText("Delete Contact");
                            }
                            else {
                                currentState = "new";
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (senderUserId.equals(receiverUserID)){
            acceptButton.setVisibility(View.GONE);
        }
        else
        {
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentState.equals("new")){
                        sendFriendRequest();
                    }
                    if (currentState.equals("request_sent")){
                        cancelFriendRequest();
                    }
                    if (currentState.equals("request_received")){
                        acceptFriendRequest();
                    }
                    if (currentState.equals("request_sent")){
                        cancelFriendRequest();
                    }
                }
            });
        }
    }

    private void acceptFriendRequest() {
        contactsReference.child(senderUserId).child(receiverUserID).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    contactsReference.child(receiverUserID).child(senderUserId).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                friendRequestReference.child(senderUserId).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                        {
                                            friendRequestReference.child(receiverUserID).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                    {
                                                        currentState = "friends";
                                                        acceptButton.setText("Delete Contact");
                                                        rejectButton.setVisibility(View.GONE);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });

                            }
                        }
                    });
                }
            }
        });
    }

    private void cancelFriendRequest() {
        friendRequestReference.child(senderUserId).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    friendRequestReference.child(receiverUserID).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                currentState = "new";
                                acceptButton.setText("Add Friend");
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendFriendRequest() {
        friendRequestReference.child(senderUserId).child(receiverUserID).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                friendRequestReference.child(receiverUserID).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestReference.child(receiverUserID).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        currentState = "request_sent";
                                        acceptButton.setText("Cancel Friend Request");
                                        Toast.makeText(ProfileActivity.this, "Friend request sent", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}
