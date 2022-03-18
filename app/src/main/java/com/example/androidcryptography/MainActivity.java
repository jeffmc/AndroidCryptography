package com.example.androidcryptography;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static EncryptionScheme[] SCHEMES = new EncryptionScheme[]{
            new Scytale(),
            new Caesar(),
            new Vigenere(),
    };

    RadioButton encryptBtn, decryptBtn;
    RadioGroup modeBtnGroup;
    EditText encryptedText, decryptedText;
    MaterialButton actionBtn;
    ImageView directionIcon;

    Spinner schemeSpinner;
    BaseAdapter schemeAdapter;

    final int UPWARD_ICON = R.drawable.ic_baseline_arrow_upward_24, DOWNWARD_ICON = R.drawable.ic_baseline_arrow_downward_24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findUI();
        setMode(true);
    }

    private void findUI() {
        modeBtnGroup = this.findViewById(R.id.modeGroup);
        encryptBtn = this.findViewById(R.id.modeEncryptBtn);
        decryptBtn = this.findViewById(R.id.modeDecryptBtn);

        encryptedText = this.findViewById(R.id.encryptedText);
        decryptedText = this.findViewById(R.id.decryptedText);

        actionBtn = this.findViewById(R.id.actionButton);
        directionIcon = this.findViewById(R.id.directionIcon);

        schemeSpinner = this.findViewById(R.id.schemeSpinner);
        setupSchemeSpinner();

        encryptBtn.setOnClickListener(this);
        decryptBtn.setOnClickListener(this);
        actionBtn.setOnClickListener(this);
    }

    private void setupSchemeSpinner() {
        List<EncryptionScheme> schemes = Arrays.asList(SCHEMES);

        ArrayAdapter<? extends EncryptionScheme> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, schemes);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        schemeSpinner.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        if (view.equals(encryptBtn) || view.equals(decryptBtn)) {
            setMode(view.equals(encryptBtn));
        }
        if (view.equals(actionBtn)) {
            EncryptionScheme sch = ((EncryptionScheme) schemeSpinner.getSelectedItem());
            Toast.makeText(getApplicationContext(), sch.schemeName(), Toast.LENGTH_SHORT).show(); // TODO: Replace with encryption code
        }
    }

    // Set mode (or direction) of the process.
    public void setMode(boolean encryption) {
        if (encryption) {
            actionBtn.setText("Encrypt");
            encryptBtn.setChecked(true);
            directionIcon.setImageResource(DOWNWARD_ICON);
        } else { // Decryption
            encryptBtn.setChecked(false);
            actionBtn.setText("Decrypt");
            directionIcon.setImageResource(UPWARD_ICON);

        }
    }
}