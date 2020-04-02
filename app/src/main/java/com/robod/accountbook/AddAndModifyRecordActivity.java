package com.robod.accountbook;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.robod.accountbook.entity.Category;
import com.robod.accountbook.entity.Record;
import com.robod.accountbook.util.CategoriesUtils;
import com.robod.accountbook.util.ToastUtil;
import com.robod.accountbook.util.UuidUtil;

import org.litepal.LitePal;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Robod Lee
 * 用于添加和修改Record
 */
@ContentView(R.layout.activity_add_modify_record)
public class AddAndModifyRecordActivity extends AppCompatActivity{

    @ViewInject(R.id.add_record_viewpager)
    private ViewPager viewPager;
    @ViewInject(R.id.add_record_category_name)
    private TextView categoryName;  //显示当前选择的分类的名称
    @ViewInject(R.id.add_record_content)
    private EditText contentEdit;     //备注
    @ViewInject(R.id.add_record_date)
    private TextView dateText;      //日期
    @ViewInject(R.id.add_record_money)
    private EditText moneyEdit;     //金额

    private static final String TAG = "Add_ModifyRecordAct";
    private boolean isIncome = false;       //true是收入，false是支出
    private List<RecyclerView> pagerList;        //ViewPager每一页View的集合
    private String dateString;    //日期的字符串，1999-04-05
    private boolean toModify;   //判断当前的操作是否去修改Record，true则修改，false则添加
    private Record toModifyRecord;  //待修改的数据
    private int year;
    private int month;
    private int day;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);

        try {
            toModifyRecord = LitePal.where("uuid = ? ",getIntent().getStringExtra("uuid")).find(Record.class).get(0);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        toModify = toModifyRecord!=null;
        if (toModify) {
            isIncome = toModifyRecord.getMoney()>0;
        }

        initWidget();   //初始化界面上的控件

        initData(); //初始化所需数据
    }

    /**
     * 初始化界面上的控件
     */
    private void initWidget() {
        //ViewPager
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            //当页面的滑动状态改变时该方法会被触发，页面的滑动状态有3个：“0”表示什么都不做，“1”表示开始滑动，“2”表示结束滑动
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == 2) {
                    if (viewPager.getCurrentItem()==0) {
                        isIncome = false;
                    } else {
                        isIncome = true;
                    }
                    changeMoneyEditColor();
                }
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initData() {
        //初始化收入分类列表
        List<Category> incomeList;          //收入分类列表
        incomeList = new ArrayList<>();
        for (int i = 0; i< CategoriesUtils.incomeCategoryName.length ; i++) {
            Category category = new Category();
            category.setImageId(CategoriesUtils.incomeCategoryIcon[i]);
            category.setName(CategoriesUtils.incomeCategoryName[i]);
            incomeList.add(category);
        }

        //初始化支出分类列表
        List<Category> expenditureList;     //支出分类列表
        expenditureList = new ArrayList<>();
        for (int j = 0; j< CategoriesUtils.expenditureCategoryName.length ; j++) {
            Category category = new Category();
            category.setImageId(CategoriesUtils.expenditureCategoryIcon[j]);
            category.setName(CategoriesUtils.expenditureCategoryName[j]);
            expenditureList.add(category);
        }

        //初始化ViewPager每一页的View的集合
        //支出页
        pagerList = new ArrayList<>();
        RecyclerView recyclerView1 = new RecyclerView(AddAndModifyRecordActivity.this);
        StaggeredGridLayoutManager layoutManager1 = new
                StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        recyclerView1.setLayoutManager(layoutManager1);
        recyclerView1.setAdapter(new CategoryAdapter(expenditureList));
        pagerList.add(recyclerView1);
        //收入页
        RecyclerView recyclerView2 = new RecyclerView(AddAndModifyRecordActivity.this);
        StaggeredGridLayoutManager layoutManager2 = new
                StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyclerView2.setLayoutManager(layoutManager2);
        recyclerView2.setAdapter(new CategoryAdapter(incomeList));
        pagerList.add(recyclerView2);
        //设置适配器
        viewPager.setAdapter(new ViewPagerAdapter());

        //设置当前的日期
        LocalDateTime nowDate = LocalDateTime.now();
        dateString = nowDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        year = nowDate.getYear();
        month = nowDate.getMonthValue();
        day = nowDate.getDayOfMonth();
        dateText.setText(dateString);

        //如果是修改数据，则将数据填充到页面上
        if (toModify) {
            categoryName.setText(toModifyRecord.getCategory());
            contentEdit.setText(toModifyRecord.getContent());
            moneyEdit.setText(String.valueOf(Math.abs(toModifyRecord.getMoney())));
            dateString = toModifyRecord.getDateString();
            dateText.setText(dateString);
        }
    }

    @Event(value = {R.id.expenditure,R.id.income,R.id.add_record_date,R.id.save_record,R.id.back_to_main_})
    private void myClick(View v) throws ParseException {
        switch (v.getId()) {
            case R.id.expenditure:
                viewPager.setCurrentItem(0);
                isIncome = false;
                changeMoneyEditColor();
                break;
            case R.id.income:
                viewPager.setCurrentItem(1);
                isIncome = true;
                changeMoneyEditColor();
                break;
            case R.id.add_record_date:
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddAndModifyRecordActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int mYear, int mMonth, int dayOfMonth) {
                                year = mYear;
                                month = mMonth+1;
                                day = dayOfMonth;
                                dateString = year+"-" + (month<10 ? ("0"+month+"-") : (month+"-")) + (day<10 ? ("0"+day) : (day));
                                dateText.setText(dateString);
                            }
                        },
                        year, month-1, day);
                datePickerDialog.show();
                break;
            case R.id.save_record:
                Date date = new Date(year-1900,month-1,day);
                boolean saveSuccess;
                String categoryNameStr = categoryName.getText().toString();
                String contentStr = contentEdit.getText().toString();
                if (toModify) {
                    Log.d(TAG, toModifyRecord.toString());
                    toModifyRecord.setCategory(categoryName.getText().toString());
                    toModifyRecord.setContent(contentEdit.getText().toString());
                    double money = Double.parseDouble(moneyEdit.getText().toString());
                    toModifyRecord.setMoney(isIncome?(money):(money*-1));
                    toModifyRecord.setStatus(((toModifyRecord.getStatus()==0)?(3):(1)));
                    toModifyRecord.setDate(date);
                    toModifyRecord.setDateString(dateString);
                    saveSuccess = toModifyRecord.save();
                    Log.d(TAG, toModifyRecord.toString());
                } else {
                    Record record = new Record();
                    record.setCategory((!TextUtils.isEmpty(categoryNameStr)?(categoryNameStr):("无")));
                    record.setContent((!TextUtils.isEmpty(contentStr)?(contentStr):("无")));
                    double money = Double.parseDouble(moneyEdit.getText().toString());
                    record.setMoney(isIncome?(money):(money*-1));
                    record.setStatus(1);    //1代表未同步到服务器
                    record.setDate(date);
                    record.setUuid(UuidUtil.getUuid());
                    record.setDateString(dateString);
                    saveSuccess = record.save();
                }
                if (saveSuccess) {
                    ToastUtil.Pop("保存成功");
                    finish();
                } else {
                    ToastUtil.Pop("保存失败");
                }
                break;
            case R.id.back_to_main_:
                finish();
                break;
            default:
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:     //Toolbar上的返回按钮，finish当前Activity
                finish();
                break;
            default:
        }
        return true;
    }

    /**
     * 切换收入支出列表时，改变字体颜色,并清空categoryName
     */
    private void changeMoneyEditColor() {
        moneyEdit.setText("");
        categoryName.setText("");
        if (isIncome) {
            moneyEdit.setTextColor(Color.GREEN);
        } else {
            moneyEdit.setTextColor(Color.RED);
        }
    }

    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

        private List<Category> categories;

        class ViewHolder extends RecyclerView.ViewHolder {
            View categoryView;
            ImageView categoryImage;
            TextView categoryName;

            public ViewHolder(View view) {
                super(view);
                categoryView = view;
                categoryImage = view.findViewById(R.id.add_record_icon);
                categoryName = view.findViewById(R.id.add_record_name);
            }
        }

        public CategoryAdapter(List<Category> categories) {
            this.categories = categories;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.add_record_category_item, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            holder.categoryView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    categoryName.setText(holder.categoryName.getText());
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Category category = categories.get(position);
            holder.categoryImage.setImageResource(category.getImageId());
            holder.categoryName.setText(category.getName());
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }
    }

    private class ViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return pagerList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            RecyclerView recyclerView = pagerList.get(position);
            container.addView(recyclerView);
            return recyclerView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View)object);
        }
    }
}
