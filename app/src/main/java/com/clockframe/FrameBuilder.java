package com.clockframe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.util.List;


public class FrameBuilder extends Activity {
    private Button bColor;
    private Button bExport;
    private Button bClearColor;
    private Button bClear;
    private ClockFrameView cfv;
    private TextView tvRGB;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_builder);
        bColor = (Button) findViewById(R.id.bColor);
        bExport = (Button) findViewById(R.id.bExport);
        bClearColor = (Button)findViewById(R.id.bClearColor);
        bClear = (Button) findViewById(R.id.bClear);
        cfv = (ClockFrameView) findViewById(R.id.cfv);
        tvRGB = (TextView) findViewById(R.id.tvRGB);

        AlertDialog.Builder builder = new AlertDialog.Builder(FrameBuilder.this);
        final ViewGroup colorpicker = (ViewGroup) getLayoutInflater().inflate(R.layout.dialog_colorpicker, null);
        final ColorPicker cp = (ColorPicker) colorpicker.findViewById(R.id.colorpicker);
        SaturationBar sbColor = (SaturationBar) colorpicker.findViewById(R.id.sbColor);
        ValueBar vbColor = (ValueBar) colorpicker.findViewById(R.id.vbColor);
        cp.addSaturationBar(sbColor);
        cp.addValueBar(vbColor);
        cp.setShowOldCenterColor(false);
        Button bOK = (Button) colorpicker.findViewById(R.id.bOK);
        bOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cfv.setColor(cp.getColor());
                if (dialog != null)
                    dialog.dismiss();
            }
        });
        builder.setView(colorpicker);
        dialog = builder.create();

        bColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dialog!=null)
                dialog.show();
            }
        });
        bExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_EMAIL, "alexis.damiens59@gmail.com");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Frame");
                intent.putExtra(Intent.EXTRA_TEXT, cfv.generateCode());

                startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });
        bClearColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cfv.setColor(0);
            }
        });
        bClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cfv.clear();
            }
        });
        cfv.setOnColorChangedListener(new ClockFrameView.onColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                StringBuilder sb = new StringBuilder();
                sb.append("R : ");
                sb.append(Color.red(color));
                sb.append(" G : ");
                sb.append(Color.green(color));
                sb.append(" B : ");
                sb.append(Color.blue(color));
                tvRGB.setText(sb.toString());
            }
        });
    }
}
