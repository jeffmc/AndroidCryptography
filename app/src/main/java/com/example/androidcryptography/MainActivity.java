package com.example.androidcryptography;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    static EncryptionScheme PLAINTEXT_SCHEME = new Plaintext(),
            SCYTALE_SCHEME = new Scytale(),
            CAESAR_SCHEME = new Caesar(),
            VIGENERE_SCHEME = new Vigenere();

    static EncryptionScheme[] SCHEMES = new EncryptionScheme[]{
            PLAINTEXT_SCHEME,
            SCYTALE_SCHEME,
            CAESAR_SCHEME,
            VIGENERE_SCHEME,
    };

    RadioButton encryptBtn, decryptBtn;
    RadioGroup modeBtnGroup;
    EditText encryptedText, decryptedText;
    ImageView directionIcon;

    Spinner schemeSpinner;

    boolean direction;

    final int UPWARD_ICON = R.drawable.ic_baseline_arrow_upward_24, DOWNWARD_ICON = R.drawable.ic_baseline_arrow_downward_24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findUI();
        setDirection(true);
    }

    private void findUI() {
        modeBtnGroup = this.findViewById(R.id.modeGroup);
        encryptBtn = this.findViewById(R.id.modeEncryptBtn);
        direction = encryptBtn.isChecked();
        decryptBtn = this.findViewById(R.id.modeDecryptBtn);

        encryptedText = this.findViewById(R.id.encryptedText);
        decryptedText = this.findViewById(R.id.decryptedText);
        setupTextWatchers();

//        actionBtn = this.findViewById(R.id.actionButton);
        directionIcon = this.findViewById(R.id.directionIcon);

        schemeSpinner = this.findViewById(R.id.schemeSpinner);
        setupSchemeSpinner();
        schemeSpinner.setOnItemSelectedListener(this);

        encryptBtn.setOnClickListener(this);
        decryptBtn.setOnClickListener(this);
    }

    private void setupSchemeSpinner() {
        List<EncryptionScheme> schemes = Arrays.asList(SCHEMES);

        ArrayAdapter<? extends EncryptionScheme> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, schemes);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        schemeSpinner.setAdapter(adapter);
    }

    private void setupTextWatchers() {
        encryptedText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void afterTextChanged(Editable editable) {
                if (!direction) updateText(); // Only update in response to user triggered events
            }
        });
        decryptedText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void afterTextChanged(Editable editable) {
                if (direction) updateText(); // Only update in response to user triggered events
            }
        });
    }

    public EncryptionScheme getScheme() {
        return ((EncryptionScheme) schemeSpinner.getSelectedItem());
    }

    // Set direction of the process.
    public void setDirection(boolean encryption) {
        direction = encryption;
        encryptBtn.setChecked(encryption);
        decryptBtn.setChecked(!encryption);
        encryptedText.setEnabled(!encryption);
        decryptedText.setEnabled(encryption);
        directionIcon.setImageResource(encryption ? DOWNWARD_ICON : UPWARD_ICON);
        updateText();
    }

    public void updateText() {
        if (direction) {
            String encrypted = getScheme().encrypt(decryptedText.getText().toString());
//            if (!encryptedText.getText().toString().equals(encrypted)) { // To not trigger looping textchanged events
            encryptedText.setText(encrypted);
//            }
        } else {
            String decrypted = getScheme().decrypt(encryptedText.getText().toString());
//            if (!decryptedText.getText().toString().equals(decrypted)) { // To not trigger looping textchanged events
            decryptedText.setText(decrypted);
//            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.equals(encryptBtn) || view.equals(decryptBtn)) {
            setDirection(view.equals(encryptBtn));
        }
    }

    // Spinner Listener
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.equals(schemeSpinner)) {
            Toast.makeText(getApplicationContext(), getScheme().schemeName(), Toast.LENGTH_SHORT).show();
            updateText();
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        if (adapterView.equals(schemeSpinner)) {
            Toast.makeText(getApplicationContext(), "No Encryption", Toast.LENGTH_SHORT).show();
        }
    }

}