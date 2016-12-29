package vn.edu.uit.floodpoint.Activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.race604.drawable.wave.WaveDrawable;
import vn.edu.uit.floodpoint.Model.FloodPoint;
import vn.edu.uit.floodpoint.Model.ListPoint;
import vn.edu.uit.floodpoint.R;


public class SignInActivity extends AppCompatActivity {

    private SignInButton signInButton;
    private ImageView imgLogo;
    public FirebaseAuth mAuth;
    private int RC_SIGN_IN = 123;
    GoogleApiClient mGoogleApiClient;
    public DatabaseReference floodPointRef = FirebaseDatabase.getInstance().getReference("floodpoint");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.GOOGLE_SIGN_IN_KEY1))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(SignInActivity.this, "Cannot connect to Google!", Toast.LENGTH_SHORT).show();
                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mapViewsID();
        addEventListener();
        signIn();
    }

    private void loadData() {
        /*floodPointRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    ListPoint.getInstance().updatePoint(data.getKey(),data.getValue(FloodPoint.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
        floodPointRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ListPoint.getInstance().updatePoint(dataSnapshot.getKey(),dataSnapshot.getValue(FloodPoint.class));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                ListPoint.getInstance().updatePoint(s,dataSnapshot.getValue(FloodPoint.class));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                ListPoint.getInstance().removePoint(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Intent intent = new Intent(SignInActivity.this, MapsActivity.class);
        startActivity(intent);
        finish();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            signInButton.setVisibility(View.VISIBLE);
                        } else {
                            loadData();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(this, "Cannot login with Google", Toast.LENGTH_SHORT).show();
                signInButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void mapViewsID() {
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        imgLogo= (ImageView) findViewById(R.id.imgLogo);
        WaveDrawable mWaveDrawable = new WaveDrawable(getResources().getDrawable(R.drawable.logo));
        mWaveDrawable.setIndeterminate(true);
        mWaveDrawable.setWaveAmplitude(3);
        mWaveDrawable.setLevel(2);
        mWaveDrawable.setWaveLength(10);
        imgLogo.setImageDrawable(mWaveDrawable);
    }

    private void addEventListener() {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }
}
