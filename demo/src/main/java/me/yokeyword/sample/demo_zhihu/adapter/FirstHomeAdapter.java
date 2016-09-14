package me.yokeyword.sample.demo_zhihu.adapter;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_zhihu.entity.Article;
import me.yokeyword.sample.demo_zhihu.listener.OnItemClickListener;

/**
 * Created by YoKeyword on 16/6/5.
 */
public class FirstHomeAdapter extends RecyclerView.Adapter<FirstHomeAdapter.VH> {
    private List<Article> mItems = new ArrayList<>();
    private LayoutInflater mInflater;

    private OnItemClickListener mClickListener;


    public FirstHomeAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_zhihu_home_first, parent, false);
        final VH holder = new VH(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (mClickListener != null) {
                    mClickListener.onItemClick(position, v, holder);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Article item = mItems.get(position);

        // 把每个图片视图设置不同的Transition名称, 防止在一个视图内有多个相同的名称, 在变换的时候造成混乱
        // Fragment支持多个View进行变换, 使用适配器时, 需要加以区分
        ViewCompat.setTransitionName(holder.img, String.valueOf(position) + "_image");

        holder.img.setImageResource(item.getImgRes());
        holder.tvTitle.setText(item.getTitle());
    }

    public void setDatas(List<Article> items) {
        mItems.clear();
        mItems.addAll(items);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public Article getItem(int position) {
        return mItems.get(position);
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public class VH extends RecyclerView.ViewHolder {
        public TextView tvTitle;
        public ImageView img;

        public VH(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            img = (ImageView) itemView.findViewById(R.id.img);
        }
    }
}
