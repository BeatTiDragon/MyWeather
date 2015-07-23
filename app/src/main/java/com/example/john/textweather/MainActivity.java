package com.example.john.textweather;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.thinkland.sdk.android.DataCallBack;
import com.thinkland.sdk.android.JuheData;
import com.thinkland.sdk.android.Parameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class MainActivity extends Activity {

    private TextView tv;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String json;
    private String city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        preferences = getSharedPreferences("weaInfo", MODE_PRIVATE);
        editor = preferences.edit();
        editor.apply();
        String temp = preferences.getString("json", "123");
        assert temp != null;
        if (temp.equals("123")) {
            //默认广州
            city = "广州";
            refresh();
        } else {
            city = preferences.getString("city", "广州");
            showWeather(temp);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_refresh) {
            refresh();
        } else if (id == R.id.action_city) {
            final EditText input = new EditText(this);
            input.setHint("请输入国内城市名");
            new AlertDialog.Builder(this).setTitle("城市").setView(input).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    city = input.getText().toString();
                    if (city.equals("")) {
                        Toast.makeText(getApplicationContext(), "请重新查询", Toast.LENGTH_SHORT).show();
                    } else {
                        refresh();
                    }
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).create().show();

        }

        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        /**
         * 请不要添加key参数.
         */
        Parameters params = new Parameters();
        params.add("cityname", city);
        /**
         * 请求的方法 参数: 第一个参数 当前请求的context;
         * 				  第二个参数 接口id;
         * 				  第三个参数 接口请求的url;
         * 				  第四个参数 接口请求的方式;
         * 				  第五个参数 接口请求的参数,键值对com.thinkland.sdk.android.Parameters类型;
         * 				  第六个参数请求的回调方法,com.thinkland.sdk.android.DataCallBack;
         *
         */
        JuheData.executeWithAPI(this, 39, "http://v.juhe.cn/weather/index",
                JuheData.GET, params, new DataCallBack() {
                    /**
                     * 请求成功时调用的方法 statusCode为http状态码,responseString为请求返回数据.
                     */
                    @Override
                    public void onSuccess(int statusCode, String responseString) {
                        // TODO Auto-generated method stub
                        json = responseString;
                        if (json.contains("203902")) {
                            Toast.makeText(getApplicationContext(), "无此城市信息", Toast.LENGTH_SHORT).show();
                            city = preferences.getString("city", "广州");
                        } else if (json.contains("200")) {
                            editor.putLong("time", new Date().getTime());
                            editor.putString("json", json);
                            editor.putString("city", city);
                            editor.commit();
                            showWeather(json);
                        }
                    }

                    /**
                     * 请求完成时调用的方法,无论成功或者失败都会调用.
                     */
                    @Override
                    public void onFinish() {
                        // TODO Auto-generated method stub
                        Toast.makeText(getApplicationContext(), "finish",
                                Toast.LENGTH_SHORT).show();
                    }

                    /**
                     * 请求失败时调用的方法,statusCode为http状态码,throwable为捕获到的异常
                     * statusCode:30002 没有检测到当前网络.
                     * 			  30003 没有进行初始化.
                     * 			  0 未明异常,具体查看Throwable信息.
                     * 			  其他异常请参照http状态码.
                     */
                    @Override
                    public void onFailure(int statusCode,
                                          String responseString, Throwable throwable) {
                        // TODO Auto-generated method stub
                        tv.append(throwable.getMessage() + "\n");
                        if (statusCode == 30002) {
                            Toast.makeText(getApplicationContext(), "请联网",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showWeather(String s) {
        tv.setText("");
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            SimpleDateFormat sdfx = new SimpleDateFormat("yyyyMMdd");
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(new Date(preferences.getLong("time", 0)));

            JSONObject baseJson = new JSONObject(s).getJSONObject("result");
            JSONObject skJson = baseJson.getJSONObject("sk");
            JSONObject todayJson = baseJson.getJSONObject("today");
            JSONObject futureJson = baseJson.getJSONObject("future");
            tv.append("当前城市：" + todayJson.getString("city") + "\n");
            tv.append("当前温度：" + skJson.getString("temp") + "\n");
            tv.append("今日温度：" + todayJson.getString("temperature") + "\n");
            tv.append("今日天气：" + todayJson.getString("weather") + "\n");
            tv.append("今日风情：" + todayJson.getString("wind") + "\n");
            tv.append("当前湿度：" + skJson.getString("humidity") + "\n");
            tv.append("穿衣指数：" + todayJson.getString("dressing_index") + ": " + todayJson.getString("dressing_advice") + "\n");
            tv.append("紫外线强度：" + todayJson.getString("uv_index") + "\n");
            tv.append("舒适度指数：" + todayJson.getString("comfort_index") + "\n");
            tv.append("洗车指数：" + todayJson.getString("wash_index") + "\n");
            tv.append("旅游指数：" + todayJson.getString("travel_index") + "\n");
            tv.append("晨练指数：" + todayJson.getString("exercise_index") + "\n");
            tv.append("干燥指数：" + todayJson.getString("drying_index") + "\n");
            tv.append("更新时间：" + sdf.format(calendar.getTime()) + "\n\n");

            tv.append("未来天气:\n\n");

            for (int i = 0; i < 6; i++) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                tv.append(futureJson.getJSONObject("day_" + sdfx.format(calendar.getTime())).getString("week") + "\n" +
                                futureJson.getJSONObject("day_" + sdfx.format(calendar.getTime())).getString("weather") + "\n" +
                                futureJson.getJSONObject("day_" + sdfx.format(calendar.getTime())).getString("temperature") + "\n" +
                                futureJson.getJSONObject("day_" + sdfx.format(calendar.getTime())).getString("wind") + "\n\n"
                );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        /**
         * 关闭当前页面正在进行中的请求.
         */
        JuheData.cancelRequests(this);
    }
}
