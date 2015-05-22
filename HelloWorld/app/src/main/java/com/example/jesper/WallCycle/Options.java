package com.example.jesper.WallCycle;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.amazon.insights.ABTestClient;
import com.amazon.insights.AmazonInsights;
import com.amazon.insights.Event;
import com.amazon.insights.EventClient;
import com.amazon.insights.InsightsCallback;
import com.amazon.insights.InsightsCredentials;
import com.amazon.insights.Variation;
import com.amazon.insights.VariationSet;


public class Options extends Activity {
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    EditText editText1;
    Spinner spinner;

    CheckBox checkBox;
    LinearLayout listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        editText1 = (EditText) findViewById(R.id.editText);
        spinner = (Spinner)findViewById(R.id.spinner1);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        listView = (LinearLayout) findViewById(R.id.linearLayout);

        //Setup
        settings = this.getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = settings.edit();

        final ActionBar actionBar = getActionBar();
        BitmapDrawable background = new BitmapDrawable (BitmapFactory.decodeResource(getResources(), R.drawable.header));
        background.setTileModeX(android.graphics.Shader.TileMode.REPEAT);
        actionBar.setBackgroundDrawable(background);

        Drawable textBkg = editText1.getBackground();
        spinner.setBackgroundDrawable(textBkg);


        editText1.setText(Integer.toString(settings.getInt("TimeDisplayValue", 0)));
        checkBox.setChecked(settings.getBoolean("CheckedStatus", false));

        final String[] items = {"Seconds", "Minutes", "Hours", "Days"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
        spinner.setAdapter(adapter);
        int spinnerPosition = adapter.getPosition(settings.getString("SelectedDropdownTime", "null"));
        spinner.setSelection(spinnerPosition);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.v("trace", "item selected " + items[i]);
                editor.putString("SelectedDropdownTime", items[i]);
                editor.commit();
                saveOptions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.v("trace", "nothing selected");
            }
        });

        editText1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                Log.v("trace", "text changed ");
                try {
                    saveOptions();
                }catch(NumberFormatException e){
                    Log.v("trace", "Invalid Integer");
                }
            }
        });

        checkBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    Log.v("trace", "Checked");
                    checkBox.setChecked(true);
                    editor.putBoolean("CheckedStatus", true);
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.v("trace", "Starting Service");
                            startService(new Intent(Options.this, background.class));
                        }
                    });
                    t.start();
                } else {
                    Log.v("trace", "Unchecked");
                    checkBox.setChecked(false);
                    editor.putBoolean("CheckedStatus", false);

                    Log.v("trace", "In StopService");
                    stopService(new Intent(Options.this, background.class));
                }

                editor.apply();
            }
        });
    }

    protected void onPause() {
        super.onPause();
        // Submit events to the server
    }

    private void saveOptions(){
        int ms = Integer.parseInt(editText1.getText().toString());
        int ms_copy = ms;
        String time = spinner.getSelectedItem().toString();

        if(time == "Seconds") ms *= 1000;
        else if( time == "Minutes") ms = ms * 1000 * 60;
        else if( time == "Hours") ms = ms * 1000 * 60 * 60;
        else if( time == "Days") ms = ms * 1000 * 60 * 60 * 24;

        editor.putInt("Timer", ms);
        editor.putInt("TimeDisplayValue", ms_copy);
        editor.putString("SelectedDropdownTime", time);
        editor.commit();
        Log.v("trace", "saved timer as " + ms/1000 + " seconds");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
