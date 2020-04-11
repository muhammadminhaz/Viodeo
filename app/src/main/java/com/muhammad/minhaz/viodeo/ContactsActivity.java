package com.muhammad.minhaz.viodeo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ContactsActivity extends AppCompatActivity {

    BottomNavigationView navView;
    RecyclerView contactList;
    ImageView findPeople;
    private DatabaseReference contactsReference,usersReference;
    private FirebaseAuth auth;
    private String currentUserId, calledBy = "", userName = "", profileImage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        contactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts");
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        findPeople = findViewById(R.id.find_people);
        contactList = findViewById(R.id.contacts_list);
        contactList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        findPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactsActivity.this, FindPeopleActivity.class);
                startActivity(intent);
            }
        });

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId())
                    {
                        case R.id.navigation_home:
                            Intent homeIntent = new Intent(ContactsActivity.this, ContactsActivity.class);
                            startActivity(homeIntent);
                            break;
                        case R.id.navigation_settings:
                            Intent settingsIntent = new Intent(ContactsActivity.this, SettingsActivity.class);
                            startActivity(settingsIntent);
                            break;
                        case R.id.navigation_notifications:
                            Intent notificationsIntent = new Intent(ContactsActivity.this, NotificationsActivity.class);
                            startActivity(notificationsIntent);
                            break;
                        case R.id.navigation_logout:
                            FirebaseAuth.getInstance().signOut();
                            Intent logoutIntent = new Intent(ContactsActivity.this, RegisterActivity.class);
                            startActivity(logoutIntent);
                            finish();
                            break;
                    }

                    return true;
                }
            };

    @Override
    protected void onStart() {
        super.onStart();

        checkForReceivingCall();
        validateUser();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(contactsReference.child(currentUserId), Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder contactsViewHolder, int i, @NonNull Contacts contacts) {
                final String listUserId = getRef(i).getKey();

                usersReference.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            userName = dataSnapshot.child("name").getValue().toString();
                            profileImage = dataSnapshot.child("image").getValue().toString();

                            contactsViewHolder.usernameText.setText(userName);
                            Picasso.get().load(profileImage).into(contactsViewHolder.profileImageView);
                        }

                        contactsViewHolder.call.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ContactsActivity.this, CallActivity.class);
                                intent.putExtra("visit_user_id", listUserId);
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_ui, parent, false);
                ContactsActivity.ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };
        contactList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void checkForReceivingCall() {
        usersReference.child(currentUserId).child("Ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("ringing")){
                    calledBy = dataSnapshot.child("ringing").getValue().toString();
                    Intent intent = new Intent(ContactsActivity.this, CallActivity.class);
                    intent.putExtra("visit_user_id", calledBy);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void validateUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(currentUserId);
        reference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists())
                {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        TextView usernameText;
        Button call;
        ImageView profileImageView;


        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            usernameText = itemView.findViewById(R.id.name_contact);
            call = itemView.findViewById(R.id.call_button);
            profileImageView = itemView.findViewById(R.id.image_contact);

        }
    }

}
