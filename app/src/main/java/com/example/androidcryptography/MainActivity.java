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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

// Jeff McMillan 4/7/22
// MainActivity contains all UI logic and user-triggered event handling for the application.
// All encryption activities are dispatched to respective EncryptionScheme subclasses.
// This app allows you to change direction between encryption/decryption (with radio buttions or press of icon)
// It also utilizes a spinner UI element to select which scheme you would like to employ
// There is a section of the UI to specify scheme parameters that will
// dynamically change according to the scheme's needs.

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener, SeekBar.OnSeekBarChangeListener {

    private static EncryptionScheme PLAINTEXT_SCHEME = new Plaintext(), // Scheme constants
            SCYTALE_SCHEME = new Scytale(),
            CAESAR_SCHEME = new Caesar(),
            NSAES_SCHEME = new NSAES(),
            BASE64URL_SCHEME = new Base64URL(),
            VIGENERE_SCHEME = new Vigenere();

    private static EncryptionScheme[] SCHEMES = new EncryptionScheme[]{ // first is default
            PLAINTEXT_SCHEME,
            SCYTALE_SCHEME,
            CAESAR_SCHEME,
            VIGENERE_SCHEME,
            NSAES_SCHEME,
            BASE64URL_SCHEME,
    };

    // radio buttons for selecting mode
    RadioButton encryptBtn, decryptBtn;
    RadioGroup modeBtnGroup;
    // text elements
    EditText encryptedText, decryptedText;
    // shows direction of encryption, can be pressed to flip
    ImageView directionIcon;

    // select scheme
    Spinner schemeSpinner;

    // scheme parameters
    // integer
    View numInputParent;
    SeekBar numInput;
    TextView numLabel, numValueLabel;
    // string
    EditText keyInput;
    View keyInputParent;
    TextView paramLabel;
    // seed
    TextView seedInput;
    View seedInputParent;

    // direction of encryption (true=encryption, false=decryption)
    boolean direction;

    // Icon resource pointers
    final int UPWARD_ICON = R.drawable.ic_baseline_arrow_upward_24, DOWNWARD_ICON = R.drawable.ic_baseline_arrow_downward_24;

    // Entry point
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findUI(); // Setup UI elements
        setDirection(true); // Direction will be encryption and scheme will be 0th index in SCHEMES array
    }

    private void findUI() { // Find views and setup listeners
        // radio btns
        modeBtnGroup = this.findViewById(R.id.modeGroup);
        encryptBtn = this.findViewById(R.id.modeEncryptBtn);
        decryptBtn = this.findViewById(R.id.modeDecryptBtn);
        encryptBtn.setOnClickListener(this);
        decryptBtn.setOnClickListener(this);
        direction = encryptBtn.isChecked();

        // plaintext ciphertext
        encryptedText = this.findViewById(R.id.encryptedText);
        decryptedText = this.findViewById(R.id.decryptedText);
        setupTextWatchers();

        // direction icon
        directionIcon = this.findViewById(R.id.directionIcon);
        directionIcon.setOnClickListener(this);

        // scheme selection
        schemeSpinner = this.findViewById(R.id.schemeSpinner);
        setupSchemeSpinner();
        schemeSpinner.setOnItemSelectedListener(this);

        // parameters
        paramLabel = this.findViewById(R.id.parametersLabel);
        numInput = this.findViewById(R.id.intInput);
        numInput.setOnSeekBarChangeListener(this);
        numValueLabel = this.findViewById(R.id.numValueLabel);
        numLabel = this.findViewById(R.id.numInputLabel);
        numInputParent = this.findViewById(R.id.numInputParent);
        keyInput = this.findViewById(R.id.keyString);
        keyInputParent = this.findViewById(R.id.keyInputParent);
        seedInput = this.findViewById(R.id.longNumberInput);
        seedInputParent = this.findViewById(R.id.longInputParent);
        setupKeySeedWatcher();
    }

    private void setupSchemeSpinner() { // setup the data to fill spinner w/ SCHEMES array
        List<EncryptionScheme> schemes = Arrays.asList(SCHEMES);

        ArrayAdapter<? extends EncryptionScheme> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, schemes);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        schemeSpinner.setAdapter(adapter);
    }

    private void setupTextWatchers() { // Setup textwatchers to only update in correct direction, prevent infinite event loopback
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
    private void setupKeySeedWatcher() { // Textwatcher for key in parameters to actively update cipher/plaintext
        keyInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void afterTextChanged(Editable editable) {
                EncryptionScheme sch = getScheme();
                if (sch instanceof Vigenere) {
                    Vigenere vig = (Vigenere) sch;
                    vig.key = editable.toString();
                    updateText();
                }
            }
        });
        seedInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void afterTextChanged(Editable editable) {
                EncryptionScheme sch = getScheme();
                if (sch instanceof NSAES) {
                    NSAES nsaes = (NSAES) sch;
                    long newSeed;
                    try {
                        newSeed = Long.valueOf(editable.toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        newSeed = 0;
                        Toast.makeText(getApplicationContext(), "Enter a valid seed (0 - " + Long.MAX_VALUE + ")", Toast.LENGTH_SHORT).show();
                        seedInput.setText(Long.toString(newSeed));
                    }
                    nsaes.seed(newSeed);
                    updateText();
                }
            }
        });
    }

    public EncryptionScheme getScheme() { // Returns the currently selected scheme
        return ((EncryptionScheme) schemeSpinner.getSelectedItem());
    }

    // Set direction of the process, change UI appearance to reflect mode and update text.
    public void setDirection(boolean encryption) {
        direction = encryption;
        encryptBtn.setChecked(encryption);
        decryptBtn.setChecked(!encryption);
        encryptedText.setEnabled(!encryption);
        decryptedText.setEnabled(encryption);
        directionIcon.setImageResource(encryption ? DOWNWARD_ICON : UPWARD_ICON);
        updateText();
    }

    public void updateText() { // Update cipher or plain depending upon direction
        if (direction) { // To prevent event loopback
            String encrypted = getScheme().encrypt(decryptedText.getText().toString());
            encryptedText.setText(encrypted);
        } else {
            String decrypted = getScheme().decrypt(encryptedText.getText().toString());
            decryptedText.setText(decrypted);
        }
    }

    public void updateScheme() { // Updates algorithm inputs for specified scheme and updates text as well.
        EncryptionScheme newScheme = getScheme();
        Toast.makeText(getApplicationContext(), newScheme.schemeName(), Toast.LENGTH_SHORT).show();
        if (newScheme instanceof Plaintext) {
            updateParametersLayout();
        } else if (newScheme instanceof Scytale) {
            Scytale scy = (Scytale) newScheme;
            setIntLabel(scy.rows);
            updateParametersLayout(true,Scytale.UI_MAX_ROWS - 1, scy.rows, "Rows:",
                    false, "");
        } else if (newScheme instanceof Caesar) {
            Caesar cae = (Caesar) newScheme;
            setIntLabel(cae.shift);
            updateParametersLayout(true, Caesar.UI_MAX_SHIFT, cae.shift, "Shift:",
                    false,"");
        } else if (newScheme instanceof Vigenere) {
            Vigenere vig = (Vigenere) newScheme;
            updateParametersLayout(false,1,0, "",
                    true,vig.key);
        } else if (newScheme instanceof NSAES) {
            NSAES nsaes = (NSAES) newScheme;
            updateParametersLayout(true,NSAES.MAX_BLOCK_SIZE-NSAES.MIN_BLOCK_SIZE,0, "Block Size:", // 2-8 block length
                    false,"",
                    true, nsaes.seed());
        }
        updateText();
    }

    private void updateParametersLayout() { // Default to none
        updateParametersLayout(false,1,0,"",
                false,"");
    }

    // Update all UI elements with provided arguments, and modify visibility depending on activity
    private void updateParametersLayout(boolean numEnabled, int numMax, int numVal, String numStr, boolean textEnabled, String txt) {
        updateParametersLayout(numEnabled, numMax, numVal, numStr, textEnabled, txt, false, 0);
    }
    // Update all UI elements with provided arguments, and modify visibility depending on activity
    private void updateParametersLayout(boolean numEnabled, int numMax, int numVal, String numStr, boolean textEnabled, String txt, boolean seedEnabled, long seed) {
        numInput.setMax(numMax);
        numInput.setProgress(numVal);
        numInput.setEnabled(numEnabled);
        numLabel.setText(numStr);
        numInputParent.setVisibility(numEnabled ? View.VISIBLE : View.INVISIBLE);
        keyInput.setText(txt);
        keyInput.setEnabled(textEnabled);
        keyInputParent.setVisibility(textEnabled ? View.VISIBLE : View.INVISIBLE);
        paramLabel.setVisibility(numEnabled||textEnabled||seedEnabled ? View.VISIBLE : View.INVISIBLE);
        seedInput.setText(Long.toString(seed));
        seedInputParent.setVisibility(seedEnabled ? View.VISIBLE : View.INVISIBLE);
    }

    // String to int conversion for setting int value label.
    private void setIntLabel(int val) {
        numValueLabel.setText(Integer.toString(val));
    }

    // Direction Listener
    @Override
    public void onClick(View view) {
        if (view.equals(encryptBtn) || view.equals(decryptBtn)) {
            setDirection(view.equals(encryptBtn));
        }
        if (view.equals(directionIcon)) {
            setDirection(!direction); // Flip direction on arrow click
        }
    }

    // Spinner Listener for Scheme Selection
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.equals(schemeSpinner)) {
            updateScheme();
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) { // SHOULD NEVER HAPPEN!
        if (adapterView.equals(schemeSpinner)) {
            Toast.makeText(getApplicationContext(), "No Encryption", Toast.LENGTH_SHORT).show();
        }
    }

    // Seekbar Listener for Integer input
    @Override
    public void onProgressChanged(SeekBar seekBar, int newValue, boolean fromUser) {
        if (seekBar == numInput) {
//            if (!fromUser) return; // If not user origin, return.
            EncryptionScheme sch = getScheme();
            if (sch instanceof Scytale) {
                Scytale scy = (Scytale) sch;
                scy.rows = newValue + 1;
                setIntLabel(scy.rows);
            } else if (sch instanceof Caesar) { // TODO: Implement for all schemes, and text input too.
                Caesar cae = (Caesar) sch;
                cae.shift = newValue;
                setIntLabel(cae.shift);
            } else if (sch instanceof NSAES) {
                NSAES nsaes = (NSAES) sch;
                nsaes.blockSize(newValue + NSAES.MIN_BLOCK_SIZE);
                setIntLabel(nsaes.blockSize());
            }
            updateText(); // Update text to reflect change
        }
    }
    // Unused
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) { }
}