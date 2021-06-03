package cn.bavelee.giaotone.adapter.binder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bavelee.giaotone.R;
import cn.bavelee.giaotone.adapter.entity.FAQEntity;
import me.drakeet.multitype.ItemViewBinder;

public class FAQEntityViewBinder extends ItemViewBinder<FAQEntity, FAQEntityViewBinder.FAQHolder> {

    @NonNull
    @Override
    protected FAQHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new FAQHolder(inflater.inflate(R.layout.layout_faq, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull FAQHolder holder, @NonNull FAQEntity item) {
        holder.tvTitle.setText(item.getTitle());
        holder.tvContent.setText(item.getContent());
    }

    public static class FAQHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvTitle)
        AppCompatTextView tvTitle;
        @BindView(R.id.tvContent)
        AppCompatTextView tvContent;


        FAQHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
