package com.me.microblog.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.me.microblog.bean.City;
import com.me.microblog.bean.Province;
import com.me.microblog.core.BaseApi;
import com.me.microblog.core.ImageManager;
import com.me.microblog.core.WeiboParser;
import com.me.microblog.http.ForecastCondition;
import com.me.microblog.http.IXmlParser;
import com.me.microblog.http.RssXMLPullHandler;
import com.me.microblog.http.WeatherBean;
import com.me.microblog.R;
import com.me.microblog.WeiboUtils;
import com.me.microblog.util.Constants;
import com.me.microblog.util.WeiboLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 11-12-25
 */
public class WeatherActivity extends Activity {

    EditText location_edit;
    Button button,provinceBtn;
    TextView location, current_temp, current_condition, current_wind_condition, current_humidity;
    TextView first, first_temp, first_condition;
    TextView second, second_temp, second_condition;
    TextView third, third_temp, third_condition;
    TextView fouth, fouth_temp, fouth_condition;
    ImageView current_icon, first_icon, second_icon, third_icon, fouth_icon;

    //////--------
    private SharedPreferences mPreferences;
    private static final int TYPE_NET=0;
    private static final int TYPE_LOCAL=1;
    /**
     * 查询天气的数据类型
     */
    int mWeatherDataType=TYPE_LOCAL;
    ArrayList<Province> mProvinces;
    View.OnClickListener clickListener=new View.OnClickListener() {

        @Override
        public void onClick(View view){
            int id=view.getId();
            if (id==R.id.button) {
                String location_pinying=location_edit.getText().toString();
                String l="Fuzhou";
                if (TextUtils.isEmpty(location_pinying)) {
                    String location=mPreferences.getString(Constants.PREF_LOCATION,null);
                    if (TextUtils.isEmpty(location)) {
                        l=location;
                    }
                } else {
                    l=location_pinying;
                }
                WeiboLog.d("查询的地名： "+l);

                SharedPreferences.Editor editor=mPreferences.edit();
                editor.putString(Constants.PREF_LOCATION,l);
                editor.commit();

                queryWeatherByNet(l,l);
            } else if (id==R.id.city_btn) {
                if (null==mProvinces) {
                    ArrayList<Province> provinces=WeiboParser.parseCitys(WeatherActivity.this);
                    mProvinces=provinces;
                    //WeiboLog.d("provinces:"+provinces);
                }
                showProvinceDialog();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.weather);
        mPreferences=WeatherActivity.this.getSharedPreferences("weather", Context.MODE_PRIVATE);

        location_edit=(EditText) findViewById(R.id.location_edit);
        button=(Button) findViewById(R.id.button);
        provinceBtn=(Button) findViewById(R.id.city_btn);
        button.setOnClickListener(clickListener);
        provinceBtn.setOnClickListener(clickListener);

        location=(TextView) findViewById(R.id.location);
        current_temp=(TextView) findViewById(R.id.current_temp);
        current_condition=(TextView) findViewById(R.id.current_condition);
        current_wind_condition=(TextView) findViewById(R.id.current_wind_condition);
        current_humidity=(TextView) findViewById(R.id.current_humidity);
        current_icon=(ImageView) findViewById(R.id.current_icon);

        //first
        first=(TextView) findViewById(R.id.first);
        first_temp=(TextView) findViewById(R.id.first_temp);
        first_condition=(TextView) findViewById(R.id.first_condition);
        first_icon=(ImageView) findViewById(R.id.first_icon);

        //second   
        second=(TextView) findViewById(R.id.second);
        second_temp=(TextView) findViewById(R.id.second_temp);
        second_condition=(TextView) findViewById(R.id.second_condition);
        second_icon=(ImageView) findViewById(R.id.second_icon);

        //third  
        third=(TextView) findViewById(R.id.third);
        third_temp=(TextView) findViewById(R.id.third_temp);
        third_condition=(TextView) findViewById(R.id.third_condition);
        third_icon=(ImageView) findViewById(R.id.third_icon);

        //fouth  
        fouth=(TextView) findViewById(R.id.fouth);
        fouth_temp=(TextView) findViewById(R.id.fouth_temp);
        fouth_condition=(TextView) findViewById(R.id.fouth_condition);
        fouth_icon=(ImageView) findViewById(R.id.fouth_icon);

        restoreWeather();
    }

    /**
     * 从本地文件还原天气数据
     */
    private void restoreWeather() {
        FetchWeatherTask fetchStatusesTask=new FetchWeatherTask();
        fetchStatusesTask.execute(new Object[]{"", TYPE_LOCAL});

        long time=mPreferences.getLong(Constants.WEA_TIMESTAMP, -1);
        long now=System.currentTimeMillis();
        long delta=now-time-3600000;
        if (time==-1||delta>0) {
            String location=mPreferences.getString(Constants.PREF_LOCATION, null);
            String locationAlias=mPreferences.getString(Constants.WEA_LOCATION_ALIAS, "");
            if (location==null) {
                location=Constants.WEA_DEFAULT_CITY;
            }

            if (TextUtils.isEmpty(locationAlias)) {
                locationAlias=location;
            }

            fetchStatusesTask=new FetchWeatherTask();
            fetchStatusesTask.execute(new Object[]{location, TYPE_NET});
        } else {
            WeiboLog.d("取消本地查询");
        }
    }

    private void queryWeatherByNet(String name, String s){
        location.setText(s);
        FetchWeatherTask weatherTask=new FetchWeatherTask();
        weatherTask.execute(new Object[]{name, TYPE_NET});
    }

    @Override
    protected void onResume() {
        super.onResume();
        WeiboLog.d("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        WeiboLog.d("onPause");
    }

    class FetchWeatherTask extends AsyncTask<Object, Void, Object> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            WeiboLog.d("onPreExecute");
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Object doInBackground(Object... params) {
            try {
                String location=(String) params[0];
                Integer type=(Integer) params[1];

                String filePath=getCacheDir()+"/"+Constants.WEATHER_FILE;
                WeiboLog.d("filePath:"+filePath);
                if (TYPE_LOCAL==type) {
                    File file=new File(filePath);
                    if (file.exists()) {
                        IXmlParser handler=new RssXMLPullHandler();
                        handler.parseRss(new FileInputStream(file));
                        return handler.getWeatherBean();
                    } else {
                        return null;
                    }
                }

                String url=Constants.WEA_URL_STRING+location;
                BufferedReader reader=getByURLConnection(url);
                if (null!=reader) {
                    SharedPreferences.Editor editor=mPreferences.edit();
                    editor.putLong(Constants.WEA_TIMESTAMP, System.currentTimeMillis());
                    editor.commit();

                    saveWeatherToSys(reader, filePath);
                    IXmlParser handler=new RssXMLPullHandler();

                    handler.parseRss(new FileInputStream(filePath));
                    return handler.getWeatherBean();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            setProgressBarIndeterminateVisibility(false);
            WeiboLog.d("onPostExecute");
            if (null==result) {
                WeiboLog.d("获取数据失败。");
                //Toast.makeText(WeatherActivity.this, "获取数据失败", Toast.LENGTH_SHORT).show();
                return;
            }

            WeatherBean weatherBean=(WeatherBean) result;
            if (null==weatherBean) {
                WeiboLog.d("解析数据失败。");
                Toast.makeText(WeatherActivity.this, "解析数据失败", Toast.LENGTH_SHORT).show();
                return;
            }
            WeiboLog.d("weatherBean:"+weatherBean);

            String loc=mPreferences.getString(Constants.PREF_LOCATION, "");
            location.setText(weatherBean.forecast_date+" -- "+loc);
            current_temp.setText(weatherBean.temp_c);
            current_condition.setText(weatherBean.condition);
            current_wind_condition.setText(weatherBean.wind_condition);
            current_humidity.setText(weatherBean.humidity);

            //////
            String current_icon_url=weatherBean.icon;
            FetchWeatherIconTask fetchWeatherIconTask=new FetchWeatherIconTask();
            fetchWeatherIconTask.execute(new Object[]{current_icon, current_icon_url});

            //first
            ArrayList<ForecastCondition> forecastConditions=weatherBean.getForecastConditions();
            if (forecastConditions!=null&&forecastConditions.size()>0) {
                ForecastCondition forecastCondition=forecastConditions.get(0);
                first.setText(forecastCondition.day_of_week);
                first_temp.setText(forecastCondition.high+"/"+forecastCondition.low);
                first_condition.setText(forecastCondition.condition);
                String icon_url=forecastCondition.icon;
                fetchWeatherIconTask=new FetchWeatherIconTask();
                fetchWeatherIconTask.execute(new Object[]{first_icon, icon_url});

                forecastCondition=forecastConditions.get(1);
                second.setText(forecastCondition.day_of_week);
                second_temp.setText(forecastCondition.high+"/"+forecastCondition.low);
                second_condition.setText(forecastCondition.condition);
                icon_url=forecastCondition.icon;
                fetchWeatherIconTask=new FetchWeatherIconTask();
                fetchWeatherIconTask.execute(new Object[]{second_icon, icon_url});

                forecastCondition=forecastConditions.get(2);
                third.setText(forecastCondition.day_of_week);
                third_temp.setText(forecastCondition.high+"/"+forecastCondition.low);
                third_condition.setText(forecastCondition.condition);
                icon_url=forecastCondition.icon;
                fetchWeatherIconTask=new FetchWeatherIconTask();
                fetchWeatherIconTask.execute(new Object[]{third_icon, icon_url});

                forecastCondition=forecastConditions.get(3);
                fouth.setText(forecastCondition.day_of_week);
                fouth_temp.setText(forecastCondition.high+"/"+forecastCondition.low);
                fouth_condition.setText(forecastCondition.condition);
                icon_url=forecastCondition.icon;
                fetchWeatherIconTask=new FetchWeatherIconTask();
                fetchWeatherIconTask.execute(new Object[]{fouth_icon, icon_url});
            }
        }
    }

    class FetchWeatherIconTask extends AsyncTask<Object, Void, Object> {

        @Override
        protected Object doInBackground(Object... params) {
            try {
                ImageView currentIcon=(ImageView) params[0];
                String url=(String) params[1];

                String filePath=getCacheDir()+ WeiboUtils.getWeiboUtil().getMd5(url)+ WeiboUtils.getExt(url);
                WeiboLog.d("filePath:"+filePath);
                File file=new File(filePath);
                if (file.exists()) {
                    Bitmap bitmap=BitmapFactory.decodeFile(filePath);
                    return new Object[]{currentIcon, bitmap};
                } else {
                    String iconUrl=Constants.GOOGLE_WEATHER+url;
                    InputStream inputStream=ImageManager.getImageStream(iconUrl);

                    if (inputStream!=null) {
                        Bitmap bitmap=BitmapFactory.decodeStream(inputStream);
                        FileOutputStream fileOutputStream;
                        fileOutputStream=new FileOutputStream(filePath);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                        return new Object[]{currentIcon, bitmap};
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            WeiboLog.d("onPostExecute");
            if (null==result) {
                WeiboLog.d("获取图片失败。");
                return;
            }

            try {
                ImageView imageView=(ImageView) ((Object[]) result)[0];
                Bitmap bitmap=(Bitmap) ((Object[]) result)[1];
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存天气状况信息到文件
     *
     * @param br
     * @param filePath 存储的文件
     */
    private void saveWeatherToSys(BufferedReader br, String filePath) {
        BufferedWriter writer=null;
        try {
            writer=new BufferedWriter(new FileWriter(filePath));
            char[] buffer=new char[1024];
            int i=0;

            while ((i=br.read(buffer))!=-1) {
                writer.write(buffer, 0, i);
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer!=null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (br!=null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取google的天气数据
     *
     * @param urlString 天气url
     * @return BufferedReader
     */
    public BufferedReader getByURLConnection(String urlString) {
        URL url;
        HttpURLConnection uc;
        BufferedReader is=null;
        try {
            url=new URL(urlString);
            uc=(HttpURLConnection) url.openConnection();
            uc.setRequestProperty("User-Agent", BaseApi.USERAGENT);
            //Map<String, List<String>> headers=uc.getHeaderFields();
            String encode="UTF-8";
            if ("UTF-8".equalsIgnoreCase(encode)) {
                is=new BufferedReader(new InputStreamReader(uc.getInputStream()), 4*1000);
            } else {
                is=new BufferedReader(new InputStreamReader(url.openStream(), encode), 4*1000);
            }
        } catch (MalformedURLException e) {
            //e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return is;
    }

    //-----------------------------
    Button cancelBtn, okBtn;
    Spinner selProvinceSpinner,selCitySpinner;
    Dialog mCityDialog;
    ArrayAdapter mProvinceAdapter;
    View.OnClickListener cityListener=new View.OnClickListener(){
        @Override
        public void onClick(View view){
            int id=view.getId();
            if (id==R.id.cancel) {
                mCityDialog.cancel();
            } else if (id==R.id.ok) {
                mCityDialog.dismiss();
                try {
                    String name=(String) selCitySpinner.getSelectedItem();
                    WeiboLog.d("选中的城市名："+name);
                    String[] names=name.split("-");
                    String pinyin=names[1];

                    if (!TextUtils.isEmpty(pinyin)) {
                        SharedPreferences.Editor editor=mPreferences.edit();
                        editor.putString(Constants.WEA_LOCATION,pinyin);
                        editor.putString(Constants.WEA_LOCATION_ALIAS,names[0]);
                        editor.commit();

                        queryWeatherByNet(pinyin,names[0]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * 显示省份与天气对话框
     */
    private void showProvinceDialog() {
        if (null==mProvinces||mProvinces.size()<1) {
            return;
        }

        if(null==mCityDialog){
            initCityDialog();
        }

        if (null==mProvinceAdapter) {
            mProvinceAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
            mProvinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            for(Province province:mProvinces){
                mProvinceAdapter.add(province.name);
                //WeiboLog.d("添加省份:"+province.name);
            }
            selProvinceSpinner.setAdapter(mProvinceAdapter);
        }

        mCityDialog.show();
    }

    private void initCityDialog() {
        LayoutInflater inflater=LayoutInflater.from(WeatherActivity.this);
        View view=inflater.inflate(R.layout.wea_dialog, null);
        cancelBtn=(Button) view.findViewById(R.id.cancel);
        okBtn=(Button) view.findViewById(R.id.ok);
        selProvinceSpinner=(Spinner) view.findViewById(R.id.sel_province_btn);
        selCitySpinner=(Spinner) view.findViewById(R.id.sel_city_btn);

        cancelBtn.setOnClickListener(cityListener);
        okBtn.setOnClickListener(cityListener);

        selProvinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String name=(String) mProvinceAdapter.getItem(i);
                setCitySpinner(name);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        AlertDialog.Builder builder=new AlertDialog.Builder(WeatherActivity.this)
            .setTitle(R.string.wea_city_title)
            .setView(view);

        final AlertDialog dialog=builder.create();
        mCityDialog=dialog;
    }

    void setCitySpinner(String name) {
        WeiboLog.d("setCitySpinner.选择的省份:"+name);
        for (Province province : mProvinces) {
            if (province.name.equals(name)) {
                ArrayAdapter adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                ArrayList<City> cities=province.cities;

                if (null!=cities) {
                    for (City city : cities) {
                        adapter.add(city.name+"-"+city.pinyin);
                    }

                    selCitySpinner.setAdapter(adapter);
                }
                break;
            }
        }
    }
}
