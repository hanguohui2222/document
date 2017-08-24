package com.amigo.widgetdemol;





import amigoui.app.AmigoActivity;
import amigoui.widget.AmigoSpinner;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class GnSpinerDemo extends AmigoActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_spiner_demo); 
        AmigoSpinner amigoSpinner = (AmigoSpinner)findViewById(R.id.Spinner01);
        
        
        //AmigoSpinner Demo DialogStyle
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
//                this, R.array.planets, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.planets, R.layout.amigo_simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.amigo_simple_spinner_dropdown_item);
        amigoSpinner.setAdapter(adapter);
        amigoSpinner.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        showToast("Spinner1: position=" + position + " id=" + id);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        showToast("Spinner1: unselected");
                    }
                });
      //AmigoSpinner Demo DropDownStyle
//        adapter = ArrayAdapter.createFromResource(this, R.array.planets,
//                android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }
    
    void showToast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}