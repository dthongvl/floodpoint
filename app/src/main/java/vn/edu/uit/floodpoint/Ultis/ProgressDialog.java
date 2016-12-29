package vn.edu.uit.floodpoint.Ultis;

import android.app.Activity;

/**
 * Created by dthongvl on 11/13/16.
 */
public class ProgressDialog {
    private android.app.ProgressDialog progressDialog;
    private Activity activity;
    private String message;
    private static ProgressDialog ourInstance = new ProgressDialog();

    public static ProgressDialog getInstance(Activity activity, String message) {
        ourInstance.activity = activity;
        ourInstance.message = message;
        return ourInstance;
    }

    private ProgressDialog() {
    }

    public void showProgressDialog() {
        progressDialog = new android.app.ProgressDialog(ourInstance.activity);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(ourInstance.message);
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
