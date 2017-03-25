package com.creative.litcircle.alertbanner;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;


public class AlertDialogForAnything {

	public AlertDialogForAnything(){
		
	}
	
	public static void showAlertDialogWhenComplte(Context context, String title, String message, Boolean status) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		 
		alertDialog.setTitle(title);

		alertDialog.setMessage(message);
		
		alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            
            }
        });
 
        alertDialog.show();
	}

	public static void showAlertDialogWithoutTitle(Context context, String message, Boolean status) {
		if(context !=null) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
			alertDialog.setMessage(message);
			alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();

				}
			});
			alertDialog.show();
		}
	}

	public static void showAlertDialogForceUpdate(final Context context, String titleText, String msg, String buttonText, final String appURL) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		alertDialog.setTitle(titleText);
		alertDialog.setMessage(msg);
		alertDialog.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (appURL.length() > 0) {
					final String appPackageName = context.getPackageName();
					try {
						context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
					} catch (android.content.ActivityNotFoundException anfe) {
						context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
					}
				}
			}
		});
		alertDialog.show();
	}

	public static void showAlertDialogForceUpdateFromDropBox(final Context context, String titleText, String msg, String buttonText, final String appURL) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		alertDialog.setTitle(titleText);
		alertDialog.setMessage(msg);
		alertDialog.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (appURL.length() > 0) {

					String urlString=appURL;
					Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(urlString));
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setPackage("com.android.chrome");
					try {
						context.startActivity(intent);
					} catch (ActivityNotFoundException ex) {
						// Chrome browser presumably not installed so allow user to choose instead
						intent.setPackage(null);
						context.startActivity(intent);
					}

				}
			}
		});
		alertDialog.show();
	}


}
