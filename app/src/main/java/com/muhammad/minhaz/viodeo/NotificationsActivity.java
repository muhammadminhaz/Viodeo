package com.muhammad.minhaz.viodeo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView notificationsList;
    private DatabaseReference friendReqReference, contactsReference, usersReference;
    private FirebaseAuth auth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        friendReqReference = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts");
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");


        notificationsList = findViewById(R.id.notification_list);
        notificationsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(friendReqReference.child(currentUserId),Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, NotificationsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacts, NotificationsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final NotificationsViewHolder notificationsViewHolder, int i, @NonNull Contacts contacts) {
                notificationsViewHolder.accept.setVisibility(View.VISIBLE);
                notificationsViewHolder.decline.setVisibility(View.VISIBLE);

                final String listUserId = getRef(i).getKey();

                DatabaseReference requestTypeReference = getRef(i).child("request_type").getRef();
                requestTypeReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            String type = dataSnapshot.getValue().toString();
                            if (type.equals("received")){
                                notificationsViewHolder.cardView.setVisibility(View.VISIBLE);
                                usersReference.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("image")){
                                            final String imageStr = dataSnapshot.child("image").getValue().toString();
                                            Picasso.get().load(imageStr).into(notificationsViewHolder.profileImageView);
                                        }

                                        final String nameStr = dataSnapshot.child("name").getValue().toString();
                                        notificationsViewHolder.usernameText.setText(nameStr);

                                        notificationsViewHolder.accept.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                contactsReference.child(currentUserId).child(listUserId).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            contactsReference.child(listUserId).child(currentUserId).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful())
                                                                    {
                                                                        friendReqReference.child(currentUserId).child(listUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful())
                                                                                {
                                                                                    friendReqReference.child(listUserId).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful())
                                                                                            {
                                                                                                Toast.makeText(NotificationsActivity.this, "New Contact Saved", Toast.LENGTH_SHORT).show();

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
                                        });

                                        notificationsViewHolder.decline.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                friendReqReference.child(currentUserId).child(listUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            friendReqReference.child(currentUserId).child(listUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful())
                                                                    {
                                                                        Toast.makeText(NotificationsActivity.this, "Friend Request Cancelled", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });

                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            else {
                                notificationsViewHolder.cardView.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friend_ui, parent, false);
                NotificationsActivity.NotificationsViewHolder viewHolder = new NotificationsActivity.NotificationsViewHolder(view);
                return viewHolder;
            }
        };
        notificationsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class NotificationsViewHolder extends RecyclerView.ViewHolder {

        TextView usernameText;
        Button accept, decline;
        ImageView profileImageView;
        RelativeLayout cardView;

        public NotificationsViewHolder(@NonNull View itemView) {
            super(itemView);

            usernameText = itemView.findViewById(R.id.name_notification);
            accept = itemView.findViewById(R.id.request_accept_button);
            decline = itemView.findViewById(R.id.request_decline_button);
            profileImageView = itemView.findViewById(R.id.image_notification);
            cardView = itemView.findViewById(R.id.card_view);
        }
    }

    private void acceptFriendRequest() {
    }

    private void cancelFriendRequest() {
    }
}
