package icy.spidey.messenger.connect.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

import icy.spidey.messenger.connect.Models.User;
import icy.spidey.messenger.connect.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {




    ActivityProfileBinding binding;
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;
    Uri selectedImage;
    ProgressDialog dialog;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getSupportActionBar().hide();



         auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();



        dialog = new ProgressDialog(this);
        dialog.setMessage("Setting Up Your Profile ...");
        dialog.setCancelable(false);




       binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i,45);

            }
        });

       binding.continueBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               String name = binding.nameBox.getText().toString();

               if (name.isEmpty()){
                   binding.nameBox.setError("Please Type a Username");
                   return;
               }

               dialog.show();

               if (selectedImage != null){
                   StorageReference reference = storage.getReference().child("Profiles").child(Objects.requireNonNull(auth.getUid()));
                   reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                           if (task.isSuccessful()){
                               reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                   @Override
                                   public void onSuccess(Uri uri) {
                                     String imageUrl = uri.toString();
                                     String uid = auth.getUid();
                                     String phone = auth.getCurrentUser().getPhoneNumber();
                                     String name = binding.nameBox.getText().toString();

                                     User user = new User(uid, name, phone, imageUrl);

                                     database.getReference()
                                             .child("users")
                                             .child(auth.getUid())
                                             .setValue(user)
                                             .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                 @Override
                                                 public void onSuccess(Void unused) {
                                                     dialog.dismiss();
                                                     Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
                                                        startActivity(intent);
                                                        finish();


                                                 }
                                             });
                                   }
                               });
                           }
                       }
                   });
               } else {
                   String uid = auth.getUid();
                   String phone = auth.getCurrentUser().getPhoneNumber();
                   User user = new User(uid, name, phone, "NO IMAGE");

                   database.getReference()
                           .child("users")
                           .child(auth.getUid())
                           .setValue(user)
                           .addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void unused) {
                                   dialog.dismiss();
                                   Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                   startActivity(intent);
                                   finish();


                               }
                           });
               }


           }
       });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        assert data != null;
        if (data.getData()!=null) {
            binding.imageView.setImageURI(data.getData());
            selectedImage = data.getData();
        }
    }
}