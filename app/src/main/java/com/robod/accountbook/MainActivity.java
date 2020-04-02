package com.robod.accountbook;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.navigation.NavigationView;
import com.robod.accountbook.entity.Record;
import com.robod.accountbook.entity.User;

import org.litepal.LitePal;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Robod Lee
 */
@ContentView(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @ViewInject(R.id.currentDate)
    private TextView currentDate;   //当前选择的日期，精确到月
    @ViewInject(R.id.main_recycler)
    private RecyclerView recyclerView;    //收支记录的RecyclerView
    @ViewInject(R.id.main_drawer)
    private DrawerLayout drawer;    //滑动菜单的实例
    @ViewInject(R.id.nav_view)
    private NavigationView navView;

    private TextView navUserName;       //NavigationView上显示的用户名
    private TextView navPhoneNumber;    //NavigationView上显示的号码
    private Button navClickToLogin;     //NavigationView点击登录

    private static final String TAG = "MainActivity";

    private String currentDateString;  //当前日期的字符串，精确到月
    private List<Record> thisMonthAllRecords;    //记录的List
    private SharedPreferences preferences;
    private int[] mYear;
    private int[] mMonth;
    private int[] mDay;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean alreadyLogin = preferences.getBoolean("already_login", false);
        if (alreadyLogin) {
            String loginUserStr = preferences.getString("login_user", "");
            navClickToLogin.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(loginUserStr)) {
                User loginUser = JSONObject.parseObject(loginUserStr, User.class);
                navPhoneNumber.setText(loginUser.getPhoneNumber());
                navUserName.setText(loginUser.getUsername());
            }
        } else {
            navClickToLogin.setVisibility(View.VISIBLE);
        }
        upgradeMainList();  //当Activity由不可见变为可见时会调用onStart方法，所以创建该活动或者回到该活动时调用upgradeMainList()方法刷新界面
    }

    /**
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void init() {

        LitePal.getDatabase();

        thisMonthAllRecords = new ArrayList<>();

        //滑动菜单
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_setting:
                        startActivity(new Intent(MainActivity.this, SettingActivity.class));
                        break;
                    case R.id.nav_synchronization:
                        startActivity(new Intent(MainActivity.this, SynchornizationActivity.class));
                        break;
                    default:
                }
                return true;
            }
        });

        //初始化当前的日期
        Calendar ca = Calendar.getInstance();
        mYear = new int[]{ca.get(Calendar.YEAR)};
        mMonth = new int[]{ca.get(Calendar.MONTH) + 1};
        mDay = new int[]{ca.get(Calendar.DAY_OF_MONTH)};
        currentDateString = mYear[0] + "年" + mMonth[0] + "月";
        currentDate.setText(currentDateString);

        //初始化NavigationView的头部信息
        View navHeader = navView.inflateHeaderView(R.layout.nav_header);
        navPhoneNumber = navHeader.findViewById(R.id.i_phone_number);
        navUserName = navHeader.findViewById(R.id.i_username);
        navClickToLogin = navHeader.findViewById(R.id.click_to_login);
        navClickToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        preferences = getSharedPreferences("login_info", MODE_PRIVATE);
    }


    @Event(value = {R.id.currentDate, R.id.add_record_fab,R.id.start_navigation,R.id.into_overview})
    private void myClick(View v) {
        switch (v.getId()) {
            case R.id.currentDate:
                DatePickerDialog dlg = new DatePickerDialog(new ContextThemeWrapper(MainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar), null, mYear[0], mMonth[0] - 1, mDay[0]) {
                    @Override
                    protected void onCreate(Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);
                        LinearLayout mSpinners = (LinearLayout) findViewById(getContext().getResources().getIdentifier("android:id/pickers", null, null));
                        if (mSpinners != null) {
                            NumberPicker mMonthSpinner = (NumberPicker) findViewById(getContext().getResources().getIdentifier("android:id/month", null, null));
                            NumberPicker mYearSpinner = (NumberPicker) findViewById(getContext().getResources().getIdentifier("android:id/year", null, null));
                            mSpinners.removeAllViews();
                            if (mMonthSpinner != null) {
                                mSpinners.addView(mMonthSpinner);
                            }
                            if (mYearSpinner != null) {
                                mSpinners.addView(mYearSpinner);
                            }
                        }
                        View dayPickerView = findViewById(getContext().getResources().getIdentifier("android:id/day", null, null));
                        if(dayPickerView != null){
                            dayPickerView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onDateChanged(DatePicker view, int year, int month, int day) {
                        super.onDateChanged(view, year, month, day);
                        mYear[0] = year;
                        mMonth[0] = month + 1;
                        mDay[0] = day;
                        currentDateString = mYear[0] + "年" + mMonth[0] + "月";
                        currentDate.setText(currentDateString);
                        upgradeMainList();
                    }
                };
                dlg.show();
                break;
            //用于添加记录的FloatingActionButton
            case R.id.add_record_fab:
                Intent intent = new Intent(MainActivity.this, AddAndModifyRecordActivity.class);
                startActivity(intent);
                break;
            case R.id.start_navigation:
                drawer.openDrawer(GravityCompat.START);
                break;
            case R.id.into_overview:
                Intent overViewIntent = new Intent(MainActivity.this, OverviewActivity.class);
                overViewIntent.putExtra("year",mYear[0]);
                overViewIntent.putExtra("month",mMonth[0]);
                startActivity(overViewIntent);
                break;
            default:
        }
    }

    /**
     * 用于更新界面信息
     */
    private void upgradeMainList() {
        thisMonthAllRecords.clear();
        thisMonthAllRecords = LitePal.where("dateString like ? and status != ? ", +mYear[0] + "-" +
                ((mMonth[0] < 10) ? ("0" + mMonth[0]) : (mMonth[0])) + "%", "2")
                .find(Record.class);
        if (thisMonthAllRecords != null) {
            Map<String, List<Record>> map = new HashMap<>((int) (thisMonthAllRecords.size() / 0.75) + 1);
            for (Record record : thisMonthAllRecords) {
                List<Record> staList = map.get(record.getDateString());

                if (staList == null) {
                    staList = new ArrayList<>();
                }
                staList.add(record);

                Collections.sort(staList, new Comparator<Record>() {
                    @Override
                    public int compare(Record record1, Record record2) {
                        return (record1.getDateString())
                                .compareTo(record2.getDateString());
                    }
                });
                map.put(record.getDateString(), staList);
            }
            Set<String> set = map.keySet();
            List<List<Record>> thisMonthRecordsByDay = new ArrayList<>();   //按每一天分开的List<List>集合
            for (String s : set) {
                List<Record> list = map.get(s);
                thisMonthRecordsByDay.add(list);
            }
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            RecyclerViewAdapter recyclerAdapter = new RecyclerViewAdapter(thisMonthRecordsByDay);
            recyclerView.setAdapter(recyclerAdapter);
        }
    }

    /**
     * 整个页面的RecyclerView的适配器，每个item是一天的记录(ListView)
     */
    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private List<List<Record>> oneMonthRecordsByDay;
        private AlertDialog popDialog;

        public RecyclerViewAdapter(List<List<Record>> oneMonthRecordsByDay) {
            this.oneMonthRecordsByDay = oneMonthRecordsByDay;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            View view;  //OneDayCard的View
            TextView todayDate;
            TextView todayTotal;
            ListView todayRecordList;

            public ViewHolder(@NonNull View view) {
                super(view);
                this.view = view;
                todayDate = view.findViewById(R.id.today_date);
                todayTotal = view.findViewById(R.id.today_total);
                todayRecordList = view.findViewById(R.id.today_records_list);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_day_card, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            holder.todayRecordList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    final Record selectRecord = oneMonthRecordsByDay.get(holder.getAdapterPosition()).get(position);
                    final View popView = LayoutInflater.from(MainActivity.this).inflate(R.layout.pop_dialog_view, null);
                    popView.findViewById(R.id.pop_modification).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popDialog.dismiss();
                            Intent intent = new Intent(MainActivity.this, AddAndModifyRecordActivity.class);
                            intent.putExtra("uuid", selectRecord.getUuid());
                            startActivity(intent);
                        }
                    });
                    popView.findViewById(R.id.pop_delete).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (selectRecord.getStatus() == 0) {
                                selectRecord.setStatus(2);
                                selectRecord.save();
                            } else {
                                selectRecord.delete();
                            }
                            popDialog.dismiss();
                            upgradeMainList();
                        }
                    });
                    popDialog = new AlertDialog.Builder(MainActivity.this)
                            .setView(popView)
                            .create();
                    popDialog.show();
                    return true;
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            List<Record> oneDayRecords = oneMonthRecordsByDay.get(position);
            double income = 0.0, expenditure = 0.0, money;
            for (Record record : oneDayRecords) {
                money = record.getMoney();
                if (money > 0) {
                    income += money;
                } else {
                    expenditure += money;
                }
            }
            holder.todayTotal.setText("收:" + income + "元  支:" + expenditure + "元");
            holder.todayDate.setText(oneDayRecords.get(0).getDateString().substring(5));
            ListViewAdapter adapter = new ListViewAdapter(oneDayRecords);
            holder.todayRecordList.setAdapter(adapter);
            setListViewHeightBasedOnChildren(holder.todayRecordList);
        }


        @Override
        public int getItemCount() {
            return oneMonthRecordsByDay.size();
        }
    }

    /**
     * 记录每一天的记录卡片里面的list的ListView适配器
     */
    private class ListViewAdapter  extends BaseAdapter {

        private List<Record> todayRecords;

        public ListViewAdapter(List<Record> oneDayRecords) {
            this.todayRecords = oneDayRecords;
        }

        @Override
        public int getCount() {
            return todayRecords.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Record record = todayRecords.get(position);  //每条记录
            View view;  //每条记录的View
            ViewHolder holder;
            if (convertView == null) {
                view = LayoutInflater.from(MainActivity.this).inflate(R.layout.one_record_item, parent, false);
                holder = new ViewHolder();
                holder.recordPoint = view.findViewById(R.id.record_point);
                holder.recordContent = view.findViewById(R.id.record_content);
                holder.recordMoney = view.findViewById(R.id.record_money);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }
            holder.recordContent.setText(record.getContent());
            holder.recordMoney.setText(String.valueOf(record.getMoney()));
            //如果大于等于0，字体改为绿色，默认字体是红色
            if (record.getMoney() >= 0) {
                holder.recordPoint.setTextColor(Color.GREEN);
                holder.recordMoney.setTextColor(Color.GREEN);
                holder.recordMoney.setText("+" + record.getMoney());
            }
            return view;
        }

        class ViewHolder {
            TextView recordPoint;     //每条记录前面的点
            TextView recordContent;   //每条记录的详情
            TextView recordMoney;     //每条记录的金额
        }
    }

    //重新计算ListView的高度
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }

        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));

        listView.setLayoutParams(params);
    }

}
