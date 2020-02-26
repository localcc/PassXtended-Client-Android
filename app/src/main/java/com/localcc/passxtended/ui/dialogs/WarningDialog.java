package com.localcc.passxtended.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class WarningDialog extends DialogFragment {
    private int title, button_text;
    public WarningDialog(int title, int button_text) {
        this.title = title;
        this.button_text = button_text;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(title).setNeutralButton(button_text, null);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}