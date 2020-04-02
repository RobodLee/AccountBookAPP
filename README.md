# AccountBookAPP

## 前言

我平时喜欢用账本记一记我的日常消费然后做个总结，一直用本子记不是很方便，从网上下载的APP又太臃肿，很多功能都用不到，就想着能不能自己开发一个APP。刚好最近学完了SpringBoot，我之前也学习过Android，再加上疫情期间一直在家也有时间，就花了一段时间开发完了。本来以为没什么难度，但还是遇到了一些比较棘手的问题，所以就打算写篇文章总结一下。先给大家看一下我的界面：

> ps：我不会说的太详细，我只写一下大体的思路以及我所遇到的问题，详细的代码有兴趣的小伙伴请自行阅读。


![账本APP](https://raw.githubusercontent.com/RobodLee/image_store/master/Android/%E8%B4%A6%E6%9C%ACAPP%E5%BC%80%E5%8F%91/%E8%B4%A6%E6%9C%ACAPP.gif)

---

## 前期准备

既然是做账本，那么肯定免不了要用到数据库，我用的Android自带的SQLite数据库，为了简化数据库的操作，我使用了LitePal框架；还有一点，我不想在我的代码里写一大堆findViewById,这样会显得代码非常的乱，所以我还使用了xUtils框架去简化我的代码；数据有了，自然要存储到服务器中，免不了要用到HTTP，这里我选择的是大名鼎鼎的OkHttp框架；既然要传输数据，那么我选择了json格式去传输数据，解析json我使用了阿里的FastJson框架，因为我觉得FastJson用起来还是比较顺手的；为了省去一堆的getter和setter方法，我还使用了非常好用的Lombok插件。

---

## 一、添加以及修改记录的功能实现

![添加记录界面](https://github.com/RobodLee/image_store/raw/master/Android/%E8%B4%A6%E6%9C%ACAPP%E5%BC%80%E5%8F%91/%E6%B7%BB%E5%8A%A0%E8%AE%B0%E5%BD%95%E7%95%8C%E9%9D%A2.jpg "添加记录界面")

最先实现的功能应该是如何添加记录了，然后再去考虑怎么将数据展示出来。我在这个Activity里面集成了添加以及修改数据的功能，我用了一个boolean值“toModify”去判断当前操作功能，是添加数据的话点击保存就会添加记录，否则就会修改数据，首先肯定要有个Record类,每个字段的作用都在注释里了。

我一开始用的不是Date而是LocalDateTime，然后我从数据库根据时间查询数据的时候一直查询不到数据，本来我以为是查询部分写的不对，结果到处数据库一看，date根本就没有值。我又以为是我在添加数据的时候写的有问题，然后想了一下，其它字段都没问题，怎么唯独date有问题，然后我换成了Date就可以正常保存了。这里要提一点，就是SQLite的主键必须是int型，我本来id是Stirng型的，值就是uuid字符串，结果不行，我就改成了int，然后再加一个String类型的uuid字段去做数据的唯一标识符。
```java
public class Record extends LitePalSupport implements Serializable {

    private int id;             //主键
    private String category;    //分类的名称
    private String content;     //备注
    private double money;       //金额,大于等于0代表收入，用绿色表示;小于0代表支出，用红色表示，等于0用灰色表示
    //状态，0:已同步到服务器，1:未同步到服务器，
    // 2:之前同步到服务器现在本地删除，同步时让服务器删除这条记录,3.之前同步到服务器现在在本地修改，同步时让服务器修改这条记录
    private int status;
    private Date date;          //记录的日期
    private String dateString;  //日期的字符串2020-04-01
    private String uuid;        //每条记录的唯一标识符

}
```

我先来分析一下我的界面构成：

首先界面上有一些分类的图标，有支出和收入两页，我是用ViewPager去实现两页的切换，展示分类图标我使用了RecyclerView，下面有一个选择时间的控件，用的是DatePickerDialog，其它的就是一些TextView，Button什么的。

再来介绍一下功能：点击分类图标的时候，下面对应的就会出现分类的名称，然后输入金额（正数），点击右上角的保存按钮时就会去判断当前分类是收入还是支出，如果是支出的话就将金额*(-1)，这样就变成了负数，保存成功就会finish掉当前活动回到主页面，失败的话会弹出一个Toast。
```java
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
```

这个toModifyRecord就是需要修改的数据，是在主页面中点击修改然后传入一个uuid，再根据uuid查出来的：

```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        ············

        try {
            toModifyRecord = LitePal.where("uuid = ? ",getIntent().getStringExtra("uuid")).find(Record.class).get(0);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        toModify = toModifyRecord!=null;
        
        ············

    }
```

看这行代码的朋友们可能有点疑惑，我为什么不直接传个对象过来而是拿着uuid再去数据库中查一遍，这不是有点多此一举吗，在《第一行代码》中提到过这样一段话：

> 首先，最简单的一种更新方式就是对已存储的对象重新设值，然后重新调用save() 方法即可。那么这里我们就要了解一个概念，什么是已存储的对象？
对于LitePal来说，对象是否已存储就是根据调用model.isSaved() 方法的结果来判断的，返回true 就表示已存储，返回false 就表示未存储。那么接下来的问题就是，什么情况下会返回true ，什么情况下会返回false呢？实际上只有在两种情况下model.isSaved() 方法才会返回true ，一种情况是已经调用过model.save() 方法去添加数据了，此时model 会被认为是已存储的对象。另一种情况是model 对象是通过LitePal提供的查询API查出来的，由于是从数据库中查到的对象，因此也会被认为是已存储的对象。

意思就是我如果直接传一个对象的实例过来，那么这个对象就是数据库中的对象的拷贝，并不是数据库中的对象的引用，我如果用对象的拷贝去执行save()方法是不会对数据库中的数据产生影响，那就不对了，所以我需要拿着uuid再去从数据库中把数据查询出来，再去调用save()方法才有用。如果没查到就说明我没有传uuid过来，那么就是去使用添加数据的功能。

## 二、数据的展示

![主界面](https://github.com/RobodLee/image_store/raw/master/Android/%E8%B4%A6%E6%9C%ACAPP%E5%BC%80%E5%8F%91/%E4%B8%BB%E7%95%8C%E9%9D%A2.jpg)

说完了数据的保存再来说一下数据的展示，这是整个APP比较核心的功能。标题栏上有一个显示当前月份的TextView，点击的话会出现一个图示的日期选择控件。至于数据的展示呢，我使用了RecyclerView嵌套ListView去实现的，RecyclerView的每一个子项是每一天记录的集合，每一天的数据是用一个ListView去展示的。我在做这个界面的时候遇到了一个问题，就是一直只有一条数据显示，我本来以为是我数据没添加上，结果导出来一看是有数据的。我就一直在调试代码，怎么都没看出问题，然后往上一滑才发现原来是一条数据占满了屏幕。原来是RecyclerView嵌套ListView的时候ListView不知道自己的高度，需要重新测量高度，我在网上找到了解决方案：

```java
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
```

怎么从数据库中查询出数据然后展示出来废了我好一番劲，好在问题完美地解决了。现在就来说一下我的具体实现过程吧，先把核心的代码贴出来：

```
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
```

首先从数据库中查询出符合条件的数据，封装成一个List集合，然后对List集合进行排序，排完序后把List集合封装成List<List<Record>> thisMonthRecordsByDay,每个子项是一天的记录的集合。然后就创建了一个自定义的RecyclerViewAdapter的实例，在RecyclerViewAdapter中绑定数据的时候再去创建ListViewAdapter的实例进行每一天的数据的展示。具体代码我就不贴了，看我的源码就知道了。

现在就是每条记录的点击事件了，我是在RecyclerViewAdapter中的onCreateViewHolder()方法中进行事件绑定的。

```
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
```

![记录长按界面](https://raw.githubusercontent.com/RobodLee/image_store/master/Android/%E8%B4%A6%E6%9C%ACAPP%E5%BC%80%E5%8F%91/%E8%AE%B0%E5%BD%95%E9%95%BF%E6%8C%89%E7%95%8C%E9%9D%A2.jpg)

长按记录的时候会弹出两个选项，一个是删除，一个是修改，点击修改的话会进入到刚才添加记录的Activity中进行数据的修改；点击删除的话首先会去判断status的值是不是0，0代表该条记录之前已经同步到服务器中，现在改成2，代表下次同步的时候把该条记录在服务器中删除后再在本地删除，如果不是直接删了就好。


## 三、数据同步功能

![同步界面](https://raw.githubusercontent.com/RobodLee/image_store/master/Android/%E8%B4%A6%E6%9C%ACAPP%E5%BC%80%E5%8F%91/%E5%90%8C%E6%AD%A5%E7%95%8C%E9%9D%A2.jpg)

现在就来说最后一个功能了。如果数据一直保存在本地的话，那么数据迟早有一天会丢失的，所以应该有个同步功能将数据保存在服务器里，这样数据就不会丢失了。服务器端的开发我会放在下一篇文章里，现在先来看看客户端的具体实现。界面很简单，只有两个按钮，一个是同步到云端，一个是下载到本地，来看一下具体的实现：



### 1.上传到云端功能：

```java
            final List<Record> toUpgradeRecords = LitePal.where("status > ?", "0").find(Record.class);
            HttpUtil.uploadRecords(ip+Constant.UPLOAD_RECORDS, phoneNumber, toUpgradeRecords, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    showToast("服务器异常");
                    finish();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    ResultInfo resultInfo = JSONObject.parseObject(responseBody, ResultInfo.class);
                    if (resultInfo.isFlag()) {
                        for (Record record : toUpgradeRecords) {
                            if (record.getStatus() != 2) {
                                record.setStatus(0);
                                record.save();
                            } else {
                                record.delete();
                            }
                        }
                        showToast("同步成功");
                        finish();
                    } else {
                        showToast("同步失败");
                        finish();
                    }
                }
            });

```

首先查询出未同步到云端的记录集合，然后调用HttpUtil.uploadRecords()方法去传输数据，来看一下HttpUtil.uploadRecords()方法干了什么吧：

```java
    public static void uploadRecords(String address, String phoneNumber , List<Record> toUpgradeRecords , okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("phoneNumber",phoneNumber)
                .add("recordsJson" , JSON.toJSONString(toUpgradeRecords))
                .build();
        Request request = new Request.Builder()
                .url(address)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }
```
就是把List集合用FastJson转换成Json字符串，然后和phoneNumber一起发送给服务器，用的是POST请求，然后就会回到前面的回调当中去处理结果，如果是同步成功的话，就去判断每条记录的status值，该干啥干啥，失败的话就给个提示然后finish掉当前的Activity。

### 2.下载到本地功能实现：

看完了上传功能再来看一下下载功能：

```java
            String address = ip+Constant.DOWNLOAD_RECORDS + phoneNumber;
            HttpUtil.sendOkHttpGetRequest(address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    showToast("服务器异常");
                    finish();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    ResultInfo resultInfo = JSONObject.parseObject(responseBody, ResultInfo.class);
                    Log.d(TAG, "onResponse: "+resultInfo.getData().toString());
                    List<Record> recordsDownloadFormServer = JSONArray.parseArray(resultInfo.getData().toString(),Record.class);
                    if (resultInfo.isFlag()) {
                        for (Record record : recordsDownloadFormServer) {
                            try {
                                LitePal.where("uuid = ? ", record.getUuid()).find(Record.class).get(0);
                            } catch (Exception e) {
                                e.printStackTrace();
                                record.setDateString(ConvertUtils.dateToString(record.getDate()));
                                record.save();
                            }
                        }
                        showToast("下载成功");
                    }
                    finish();
                }
            });
```
首先使用HttpUtil.sendOkHttpGetRequest()方法将请求发送到服务器，用的是get请求，服务器会根据传过去的phoneNumber将该用户的所有记录都传回来，传过来的记录集合也是Json，所以先用FastJson解析成List。然后循环遍历List判断该条记录在本地有没有，有的话就跳过，没有的话就保存到SQLite中。

## 总结

到此为止，整个账本APP的核心功能和一些我所遇到的问题以及解决方法都已经写完了，其它的一些比较简单的功能我就不再赘述。写的不是很好，大家如果不满意的话直接关掉就好，谢谢！
