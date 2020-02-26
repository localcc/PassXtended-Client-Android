package com.localcc.passxtended.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.localcc.passxtended.R;

public class CertificateDialog extends DialogFragment {
    private String issuer, sigalgname, sigalgoid, sigalgparams;
    private DialogInterface.OnClickListener positiveListener, negativeListener;
    public CertificateDialog(String issuer, String sigalgname, String sigalgoid, String sigalgparams,
                             DialogInterface.OnClickListener positiveListener,
                             DialogInterface.OnClickListener negativeListener) {
        this.issuer = issuer;
        this.sigalgname = sigalgname;
        this.sigalgoid = sigalgoid;
        this.sigalgparams = sigalgparams;
        this.positiveListener = positiveListener;
        this.negativeListener = negativeListener;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(this.issuer).append("\n");
        messageBuilder.append(this.sigalgname).append("\n");
        messageBuilder.append(this.sigalgoid).append("\n");
        messageBuilder.append(this.sigalgparams);

        builder.setTitle(R.string.certificate_dialog_title)
                .setMessage(messageBuilder.toString())
                .setNegativeButton(R.string.no, negativeListener)
                .setPositiveButton(R.string.yes, positiveListener);
        return builder.create();
    }
}