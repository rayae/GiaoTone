package cn.bavelee.giaotone.adapter.binder;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bavelee.giaotone.R;
import cn.bavelee.giaotone.adapter.entity.SimpleTextEntity;
import me.drakeet.multitype.ItemViewBinder;

public class SimpleTextViewBinder extends ItemViewBinder<SimpleTextEntity, SimpleTextViewBinder.HeaderHolder> {

    @NonNull
    @Override
    protected HeaderHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new HeaderHolder(inflater.inflate(R.layout.layout_header, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull HeaderHolder holder, @NonNull SimpleTextEntity item) {
        holder.tv.setGravity(item.isCenter() ? Gravity.CENTER : Gravity.START);
        holder.tv.setText(item.getTitle());
        holder.itemView.setOnLongClickListener(item.getListener());
    }

    public static class HeaderHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv)
        AppCompatTextView tv;

        HeaderHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
