package cn.bavelee.giaotone.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bavelee.donatedialog.DonateToMe;
import cn.bavelee.giaotone.Consts;
import cn.bavelee.giaotone.R;
import cn.bavelee.giaotone.adapter.binder.ClickableTextViewBinder;
import cn.bavelee.giaotone.adapter.binder.FAQEntityViewBinder;
import cn.bavelee.giaotone.adapter.binder.SimpleTextViewBinder;
import cn.bavelee.giaotone.adapter.entity.ClickableTextEntity;
import cn.bavelee.giaotone.adapter.entity.FAQEntity;
import cn.bavelee.giaotone.adapter.entity.SimpleTextEntity;
import cn.bavelee.giaotone.util.IntentUtils;
import cn.bavelee.giaotone.util.PrefsUtils;
import me.drakeet.multitype.MultiTypeAdapter;

public class HelpActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private MultiTypeAdapter mAdapter;
    private List<Object> items = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(FAQEntity.class, new FAQEntityViewBinder());
        mAdapter.register(ClickableTextEntity.class, new ClickableTextViewBinder());
        mAdapter.register(SimpleTextEntity.class, new SimpleTextViewBinder());

        items.add(new ClickableTextEntity(getString(R.string.click_me_for_battery_optimization), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtils.gotoBatteryOptimization(v.getContext());
            }
        }));
        items.add(new ClickableTextEntity(getString(R.string.auto_start), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtils.launchSystemAutoStartManager(v.getContext());
            }
        }));
        items.add(new ClickableTextEntity(getString(R.string.click_me_for_donate_developer), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DonateToMe.show(v.getContext());
            }
        }));
        items.add(new FAQEntity("#. 这个软件挣钱吗？", "\t\t本软件目前免费，将来依然免费，亦不会推出会员功能变相收费。所以我靠什么赚钱？除了仅有的、挂了自己业务的广告位以外，就只剩下各位的捐赠了。软件从最开始到现在一直都是我一个人在开发，开发不易，熬了无数个夜晚，很高兴能给你带来用处，若能在金钱上支持一番，我将更加高兴。感谢所有捐赠过的、没有捐赠过的人。"));
        items.add(new FAQEntity("1. 我退出软件后，就没声音了", "\t\t由于安卓上此功能是通过软件实现的，所以需要保持软件后台运行才能正常使用。请按照第7条帮助进行设置"));
        items.add(new FAQEntity("2. 锁屏后没声音了", "\t\t大部分手机在锁屏后都会清理内存，所以需要为软件设置【白名单】，以及【最近任务】中上锁"));
        items.add(new FAQEntity("3. 导入音频时我找不到我的音频文件", "\t\t鉴于大部分人并不懂得什么是【路径】，所以导入音频显得相当麻烦，可以尝试使用【音频搜索器】，此功能会找到手机上的所有音频文件，你可以选择你想要的音频文件进行导入。若要启用，请在【应用设置】中将音频选择器设置为【内置音频搜索器】"));
        items.add(new FAQEntity("4. 如何导入【QQ】或者【微信】的语音", "\t\t对于本软件而言，我们无法访问【QQ】或者【微信】的语音，你想设置的话请自行录屏并且剪辑为声音，无其它办法。"));
        items.add(new FAQEntity("5. 如何使用【内置音频搜索器】", "\t\t先在【应用设置】中修改【音频选择器】为【内置音频搜索器】，然后再回到主页点击【导入音频】，程序会搜索手机上的所有音频文件，选择合适的点击【导入】即可"));
        items.add(new FAQEntity("6. 我导入的音频没有声音了", "\t\t因为音频文件被你的手机管家当成垃圾文件清理了，暂时无其他解决办法。"));
        items.add(new FAQEntity("7. 软件该如何设置才不会没声音", "\t\t目前，大部分手机只需设置4个(有些手机只有3个)东西，即可保证软件正常运行：\n\t\ta.\t上锁，在【最近列表】中将软件上锁(如果【最近列表】中看不到软件，请在【应用设置】中将【隐藏最近任务】关闭，上锁后可另行开启)\n\t\tb.\t忽略电池优化，在【应用设置】中点击【忽略电池优化】在点击允许即可\n\t\tc.\t自启动，在手机管家中找到【充电提示音】并允许自启动\n\t\td.\t省电策略，如 MIUI 中需要将软件的省电策略设置为无限制"));
        items.add(new FAQEntity("8. 如何开机自启", "\t\t软件开机会自启并运行，需要【自启动】权限，且启动后有会一定时间的延迟，不会一开机就生效(Android 系统机制)。"));
        items.add(new FAQEntity("9. 通知栏一直显示【正在运行】", "\t\tAndroid 8.0 开始应用保持后台运行必须显示一个【正在运行】的通知，这个【正在运行】通知可以在【通知管理】中进行屏蔽。"));
        items.add(new FAQEntity("$. QQ 群为何设置群费", "\t\t收群费的只是主群，由于目前用户体量巨大，我的主要重心放在主群，帮助那些愿意支付群费的人。2￥群费掏不出来？"));
        items.add(new SimpleTextEntity("\n\n\t\t本软件纯属本人(bavelee@foxmail.com)\t\t根据【酷安】社区兴起的充电提示音热度，纯属兴趣制作，不承担任何法律责任。程序自带音频文件皆来自酷友分享，部分音频我进行了二次压缩，本软件仅供娱乐。APP 届时将会完全开源(不是目前)：https://github.com/bavelee/GiaoTone"));
        mAdapter.setItems(items);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                Object item = items.get(position);
                if (item instanceof ClickableTextEntity) {
                    return 1;
                } else return 3;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);

        showFirstTimeTips();
    }

    private void showFirstTimeTips() {
        if (!PrefsUtils.getBoolean(Consts.KEY_IS_SHOWED_TIPS, false)) {
            if (!isFinishing())
                new AlertDialog.Builder(this)
                        .setTitle(R.string.tip_title)
                        .setPositiveButton(R.string.okay, (dialog, which) -> {
                            IntentUtils.requestIgnoreBatteryOptimization(HelpActivity.this);
                            IntentUtils.launchSystemAutoStartManager(HelpActivity.this);
                            dialog.cancel();
                        })
                        .setMessage(R.string.tips)
                        .setCancelable(false)
                        .show();
            PrefsUtils.edit().putBoolean(Consts.KEY_IS_SHOWED_TIPS, true).apply();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
