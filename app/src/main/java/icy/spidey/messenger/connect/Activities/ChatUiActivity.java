package icy.spidey.messenger.connect.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import icy.spidey.messenger.connect.Adapters.MessagesAdapter;
import icy.spidey.messenger.connect.Models.Message;
import icy.spidey.messenger.connect.Models.User;
import icy.spidey.messenger.connect.R;
import icy.spidey.messenger.connect.databinding.ActivityChatUiBinding;

public class ChatUiActivity extends AppCompatActivity {

    ActivityChatUiBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;

    String senderRoom, receiverRoom;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;
    String receiverUid;
    String senderUid;
    String senderPresence;
    String receiverPresence;
    String userName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatUiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        
        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending Image ...");
        dialog.setCancelable(false);

        database.getReference().child("users").child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userName = snapshot.child("name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        messages = new ArrayList<>();
        adapter = new MessagesAdapter(this,messages, senderRoom, receiverRoom);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        String name = getIntent().getStringExtra("name");
        String profile = getIntent().getStringExtra("image");
        String token = getIntent().getStringExtra("token");

        binding.name.setText(name);
        Glide.with(ChatUiActivity.this)
                .load(profile)
                .placeholder(R.drawable.user)
                .into(binding.profileImage);
        binding.imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


         senderUid = FirebaseAuth.getInstance().getUid();
         receiverUid = getIntent().getStringExtra("uid");

         senderPresence = senderUid + receiverUid;
         receiverPresence = receiverUid + senderUid;

         database.getReference().child("privatePresence").child(senderPresence).setValue("Online");

       database.getReference().child("privatePresence").child(receiverPresence).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               if (snapshot.exists()){
                   String status = snapshot.getValue(String.class);
                   if (!status.isEmpty()){
                   binding.status.setText(status);
                   binding.status.setVisibility(View.VISIBLE);
                       if (status.equals("Offline")) {
                           database.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
                               @Override
                               public void onDataChange(@NonNull DataSnapshot snapshot) {
                                   String presence = snapshot.getValue(String.class);
                                   binding.status.setText(presence);
                                   binding.status.setVisibility(View.VISIBLE);
                               }

                               @Override
                               public void onCancelled(@NonNull DatabaseError error) {

                               }
                           });
                       }

                   }

               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;






        database.getReference().child("chats")
                        .child(senderRoom)
                                .child("messages")
                                        .addValueEventListener(new ValueEventListener() {
                                            @SuppressLint("NotifyDataSetChanged")
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                messages.clear();
                                                for (DataSnapshot snapshot1 : snapshot.getChildren()){

                                                    Message message = snapshot1.getValue(Message.class);
                                                    message.setMessageId(snapshot1.getKey());
                                                    messages.add(message);
                                                    binding.recyclerView.smoothScrollToPosition(binding.recyclerView.getAdapter().getItemCount());
                                                }

                                                adapter.notifyDataSetChanged();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

        binding.senBtn.setOnClickListener(v -> {
            if (!binding.msgBox.getText().toString().isEmpty()) {
                String messageTxt = binding.msgBox.getText().toString();

                Date date = new Date();
                Message message = new Message(messageTxt, senderUid, date.getTime());
                binding.msgBox.setText("");
                binding.recyclerView.smoothScrollToPosition(Objects.requireNonNull(binding.recyclerView.getAdapter()).getItemCount());


                String randomKey = database.getReference().push().getKey();

                HashMap<String, Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg", message.getMessage());
                lastMsgObj.put("lastMsgTime", date.getTime());

                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);


                assert randomKey != null;
                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener(unused -> {
                            database.getReference().child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .child(randomKey)
                                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused1) {
                                            sendNotification(userName, message.getMessage(), token);

                                        }
                                    });


                        });
            } else {
                Toast.makeText(this, "Message cant be Empty", Toast.LENGTH_SHORT).show();
            }
        });

        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i,25);

            }
        });

        final Handler handler = new Handler();

        binding.msgBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("privatePresence").child(senderPresence).setValue("Typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping,1000);

            }


            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("privatePresence").child(senderPresence).setValue("Online");
                }
            };
        });


     //   Objects.requireNonNull(getSupportActionBar()).setTitle(name);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    void sendNotification(String name, String message, String token){
        try {


            RequestQueue queue = Volley.newRequestQueue(this);

            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title", name);
            data.put("body", message);

            JSONObject notificationData = new JSONObject();
            notificationData.put("notification", data);
            notificationData.put("to", token);

            JsonObjectRequest request = new JsonObjectRequest(url, notificationData
                    , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                   // Toast.makeText(ChatUiActivity.this, "success", Toast.LENGTH_SHORT).show();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(ChatUiActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    String key  = "Key=AAAAW2pxDew:APA91bF943TqNSu9ozfZx3SJbfes5gm5vj_KwsVAORtycHjJEftseQhWaO2FGpnfLfWdj5LGfKUytsYVt6yTlsxgO9S4Zpv8S1rDM5u6q6yG71lfauDdhswk53TpyGEs23uHd3RisRBt";
                    map.put("Authorization", key);
                    map.put("Content-Type", "application/json");
                    return map;
                }
            };

            queue.add(request);
        } catch (Exception exception){

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == 25){
            if (data != null){
                if (data.getData() != null){
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference  reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        dialog.dismiss();
                                        String filePath = uri.toString();
                                        String messageTxt = binding.msgBox.getText().toString();

                                        Date date = new Date();
                                        Message message = new Message(messageTxt, senderUid, date.getTime() );
                                        message.setMessage("/*photo*/");
                                        message.setImageUrl(filePath);
                                        binding.msgBox.setText("");


                                        String randomKey = database.getReference().push().getKey();

                                        HashMap<String, Object> lastMsgObj = new HashMap<>();
                                        lastMsgObj.put("lastMsg", message.getMessage());
                                        lastMsgObj.put("lastMsgTime", date.getTime());

                                        database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                        database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);


                                        assert randomKey != null;
                                        database.getReference().child("chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message).addOnSuccessListener(unused -> {
                                                    database.getReference().child("chats")
                                                            .child(receiverRoom)
                                                            .child("messages")
                                                            .child(randomKey)
                                                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused1) {

                                                                }
                                                            });


                                                });
                                    }
                                });
                            }
                        }
                    });

                }
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Online");
        database.getReference().child("privatePresence").child(senderPresence).setValue("Online");

    }

    @Override
    protected void onPause() {
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("privatePresence").child(senderPresence).setValue("Offline");
        super.onPause();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}