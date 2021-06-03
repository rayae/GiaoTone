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
import cn.bavelee.giaotone.adapter.entity.ClickableTextEntity;
import me.drakeet.multitype.ItemViewBinder;

public class ClickableTextViewBinder extends ItemViewBinder<ClickableTextEntity, ClickableTextViewBinder.ClickableTextHolder> {

    @NonNull
    @Override
    protected ClickableTextHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new ClickableTextHolder(inflater.inflate(R.layout.layout_clickable_text, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull ClickableTextHolder holder, @NonNull ClickableTextEntity item) {
        holder.tv.setText(item.getText());
        holder.tv.setOnClickListener(item.getListener());
    }

    public static class ClickableTextHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv)
        AppCompatTextView tv;

        public ClickableTextHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
