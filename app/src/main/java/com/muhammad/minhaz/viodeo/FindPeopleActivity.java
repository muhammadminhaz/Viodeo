package com.muhammad.minhaz.viodeo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FindPeopleActivity extends AppCompatActivity {

    private RecyclerView findFriendList;
    private EditText search;
    private String str = "";
    private DatabaseReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);

        userReference = FirebaseDatabase.getInstance().getReference().child("Users");

        search = findViewById(R.id.search_user);
        findFriendList = findViewById(R.id.find_friends_list);
        findFriendList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (search.getText().toString().equals("")){
                    Toast.makeText(FindPeopleActivity.this, "Empty",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    str = s.toString();
                    onStart();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = null;

        if (str.equals("")){
            options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(userReference, Contacts.class).build();
        }
        else
        {
            options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(userReference.orderByChild("name").startAt(str).endAt(str + "\uf8ff"), Contacts.class).build();

        }

        FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder findFriendsViewHolder, final int i, @NonNull final Contacts contacts) {
                findFriendsViewHolder.usernameText.setText(contacts.getName());
                Picasso.get().load(contacts.getImage()).into(findFriendsViewHolder.profileImageView);

                findFriendsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_profile = getRef(i).getKey();
                        Intent intent = new Intent(FindPeopleActivity.this, ProfileActivity.class);
                        intent.putExtra("visit_profile", visit_profile);
                        intent.putExtra("profile_image", contacts.getImage());
                        intent.putExtra("profile_name", contacts.getName());
                        startActivity(intent);
                    }
                });


            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_ui, parent, false);
                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                return viewHolder;
            }
        };
        findFriendList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {

        TextView usernameText;
        Button videoCall;
        ImageView profileImageView;
        RelativeLayout cardView;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            usernameText = itemView.findViewById(R.id.name_contact);
            videoCall = itemView.findViewById(R.id.call_button);

            profileImageView = itemView.findViewById(R.id.image_contact);
            cardView = itemView.findViewById(R.id.card_view1);

            videoCall.setVisibility(View.GONE);
        }
    }
}
