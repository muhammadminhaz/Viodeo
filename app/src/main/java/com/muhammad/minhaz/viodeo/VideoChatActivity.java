package com.muhammad.minhaz.viodeo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

    private static String API_KEY = "46668462";
    private static String SESSION_ID = "2_MX40NjY2ODQ2Mn5-MTU4NjU2ODQxMzU5N35aT0xWUlhaUlI0VWlJMmdnY0J5Y213TXd-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00NjY2ODQ2MiZzaWc9MWQ1NmM5Y2RmNTZjMzBhMjQ5NzM2OWUzMGM4OTljMjk2NDRiYzM0ZTpzZXNzaW9uX2lkPTJfTVg0ME5qWTJPRFEyTW41LU1UVTROalUyT0RReE16VTVOMzVhVDB4V1VsaGFVbEkwVldsSk1tZG5ZMEo1WTIxM1RYZC1mZyZjcmVhdGVfdGltZT0xNTg2NTY4NDg2Jm5vbmNlPTAuMTgyNTAwMDYyNTUzMDk1MjUmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTU4NjU5MDA4NSZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;
    private FrameLayout publisherViewController, subscriberViewController;
    private Session session;
    private Publisher publisher;
    private Subscriber subscriber;


    private ImageView closeVideoChat;
    private DatabaseReference userReference;
    private String userID = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        closeVideoChat = findViewById(R.id.close_video_chat);
        closeVideoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(userID).hasChild("Ringing"))
                        {
                            userReference.child(userID).child("Ringing").removeValue();
                            if (publisher != null)
                            {
                                publisher.destroy();
                            }
                            if (subscriber != null)
                            {
                                subscriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this, RegisterActivity.class));
                            finish();
                        }

                        if (dataSnapshot.child(userID).hasChild("Calling"))
                        {
                            userReference.child(userID).child("Calling").removeValue();
                            if (publisher != null)
                            {
                                publisher.destroy();
                            }
                            if (subscriber != null)
                            {
                                subscriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this, RegisterActivity.class));
                            finish();
                        }
                        else
                        {
                            if (publisher != null)
                            {
                                publisher.destroy();
                            }
                            if (subscriber != null)
                            {
                                subscriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this, RegisterActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoChatActivity.this);
    }
    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions()
    {
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms))
        {
            publisherViewController = findViewById(R.id.publisher_container);
            subscriberViewController = findViewById(R.id.subscriber_container);

            session = new Session.Builder(this, API_KEY, SESSION_ID).build();
            session.setSessionListener(VideoChatActivity.this);
            session.connect(TOKEN);
        }
        else
        {
            EasyPermissions.requestPermissions(this, "Permissions aren't granted", RC_VIDEO_APP_PERM);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session) {
        publisher = new Publisher.Builder(this).build();
        publisher.setPublisherListener(VideoChatActivity.this);

        publisherViewController.addView(publisher.getView());

        if (publisher.getView() instanceof GLSurfaceView)
        {
            ((GLSurfaceView) publisher.getView()).setZOrderOnTop(true);
        }
        session.publish(publisher);
    }

    @Override
    public void onDisconnected(Session session) {

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        if (subscriber == null)
        {
            subscriber = new Subscriber.Builder(this, stream).build();
            session.subscribe(subscriber);
            subscriberViewController.addView(subscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        if (subscriber != null)
        {
            subscriber = null;
            subscriberViewController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
