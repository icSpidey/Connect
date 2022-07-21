package icy.spidey.messenger.connect.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Objects;

import icy.spidey.messenger.connect.Models.Message;
import icy.spidey.messenger.connect.R;
import icy.spidey.messenger.connect.databinding.ItemReceiveBinding;
import icy.spidey.messenger.connect.databinding.ItemSendBinding;

public class MessagesAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Message> messages;

    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;

    String senderRoom;
    String receiverRoom;

    public MessagesAdapter(Context context, ArrayList<Message> messages,String senderRoom, String receiverRoom){
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENT){
            View view = LayoutInflater.from(context).inflate(R.layout.item_send, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (Objects.equals(FirebaseAuth.getInstance().getUid(), message.getSenderId())){
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVE;
        }

    }



    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        int[] reactions = new int[]{
                R.drawable.ic_like,
                R.drawable.ic_heart,
                R.drawable.ic_laugh,
                R.drawable.ic_cry,
                R.drawable.ic_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();
                ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
                    if (pos < 0){
                        return false;
                    }

                    if (holder.getClass() == SentViewHolder.class) {
                        SentViewHolder viewHolder = (SentViewHolder)holder;
                        viewHolder.binding.feeling.setImageResource(reactions[pos]);
                      //  viewHolder.binding.feeling.setVisibility(View.VISIBLE);
                    } else {
                        ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                        viewHolder.binding.feeling.setImageResource(reactions[pos]);
                      //  viewHolder.binding.feeling.setVisibility(View.VISIBLE);
                    }

                    
                  message.setFeeling(pos);

               /*   FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom).child("messages")
                          .child(message.getMessageId()).setValue(message);

                  FirebaseDatabase.getInstance().getReference().child("chats").child(receiverRoom).child("messages")
                            .child(message.getMessageId()).setValue(message);  */



                    return true;
                });


        

        if (holder.getClass() == SentViewHolder.class){
            SentViewHolder viewHolder = (SentViewHolder)holder;

            if (message.getMessage().equals("/*photo*/")) {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.image);
            }

            viewHolder.binding.message.setText(message.getMessage());

        if (message.getFeeling() >= 0){
                viewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

       /*     viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            }); */



        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.binding.message.setText(message.getMessage());

            if (message.getMessage().equals("/*photo*/")) {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.image);
            }

           if (message.getFeeling() >= 0){
                viewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

         /*   viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            }); */



        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder{
        ItemSendBinding binding;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSendBinding.bind(itemView);

        }
    }


    public class ReceiverViewHolder extends  RecyclerView.ViewHolder{

        ItemReceiveBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
        }
    }

}