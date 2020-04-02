package com.robod.accountbook;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.robod.accountbook.entity.Record;
import com.robod.accountbook.util.Constant;

import org.litepal.LitePal;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robod Lee
 */
@ContentView(R.layout.activity_overview)
public class OverviewActivity extends AppCompatActivity {

    @ViewInject(R.id.ov_currentDate)
    private Button currentDate;
    @ViewInject(R.id.month_income)
    private TextView monthIncomeTv;
    @ViewInject(R.id.month_expenditure)
    private TextView monthExpenditureTv;
    @ViewInject(R.id.month_surplus)
    private TextView monthSurplusTv;
    @ViewInject(R.id.average_expenditure)
    private TextView averageExpenditureTv;

    private double monthIncome;     //月收入
    private double monthExpenditure;    //月支出

    private String currentDateString;
    private int mYear;
    private int mMonth;
    private List<Record> thisMonthAllRecords;

    private static final String TAG = "OverviewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        mYear = getIntent().getIntExtra("year",2020);
        mMonth = getIntent().getIntExtra("month",1);
        currentDateString = mYear + "年" + mMonth + "月";
        currentDate.setText(currentDateString);
        thisMonthAllRecords = new ArrayList<>();
        upgradeInformation();
    }

    @Event({R.id.back_to_main,R.id.ov_currentDate})
    private void myClick(View view) {
        switch (view.getId()) {
            case R.id.back_to_main:
                finish();
                break;
            case R.id.ov_currentDate:
                DatePickerDialog dlg = new DatePickerDialog(new ContextThemeWrapper(OverviewActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar), null, mYear, mMonth - 1, 1) {
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
                        mYear = year;
                        mMonth = month + 1;
                        currentDateString = mYear + "年" + mMonth + "月";
                        currentDate.setText(currentDateString);
                        upgradeInformation();
                    }
                };
                dlg.show();
                break;
            default:
        }
    }

    private void upgradeInformation() {
        thisMonthAllRecords.clear();
        thisMonthAllRecords = LitePal.where("dateString like ? and status != ? ", +mYear + "-" +
                ((mMonth < 10) ? ("0" + mMonth) : (mMonth)) + "%", "2")
                .find(Record.class);
        if (thisMonthAllRecords!=null && thisMonthAllRecords.size()!=0) {
            for (Record record : thisMonthAllRecords) {
                double money = record.getMoney();
                if (money>0) {
                    monthIncome += money;
                } else if (money<0) {
                    monthExpenditure += money;
                }
            }
            monthIncomeTv.setText(String.valueOf(monthIncome));
            monthExpenditureTv.setText(String.valueOf(monthExpenditure));
            monthSurplusTv.setText(String.valueOf(monthIncome + monthExpenditure));
            if (mYear%4==0 && mYear%100!=0 && mYear%400==0 && monthExpenditure!=0){
                averageExpenditureTv.setText(String.valueOf(monthExpenditure/ Constant.DAY_OF_MONTH_LEAP_YEAR[mMonth]));
            } else if (monthExpenditure!=0){
                averageExpenditureTv.setText(Constant.DECIMAL_FORMAT.format(monthExpenditure/ Constant.DAY_OF_MONTH_COMMON_YEAR[mMonth]));
            }
        }
    }
}
