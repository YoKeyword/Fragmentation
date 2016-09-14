package me.yokeyword.sample.demo_wechat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_wechat.entity.Msg;
import me.yokeyword.sample.demo_wechat.listener.OnItemClickListener;

/**
 * Created by YoKeyword on 16/6/30.
 */
public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.VH> {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<Msg> mItems = new ArrayList<>();

    private OnItemClickListener mClickListener;

    public MsgAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void addMsg(Msg bean) {
        mItems.add(bean);
        notifyItemInserted(mItems.size() - 1);
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_wechat_msg, parent, false);
        final VH holder = new VH(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onItemClick(holder.getAdapterPosition(), v, holder);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Msg item = mItems.get(position);

        holder.tvMsg.setText(item.message);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    class VH extends RecyclerView.ViewHolder {
        private ImageView imgAvatar;
        private TextView tvMsg;

        public VH(View itemView) {
            super(itemView);
            imgAvatar = (ImageView) itemView.findViewById(R.id.img_avatar);
            tvMsg = (TextView) itemView.findViewById(R.id.tv_msg);
        }
    }
}
