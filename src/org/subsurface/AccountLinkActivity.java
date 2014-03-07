package org.subsurface;

import java.util.ArrayList;

import org.subsurface.controller.UserController;
import org.subsurface.ws.WsClient;
import org.subsurface.ws.WsException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * Activity for account selection.
 * 
 * @author Aurelien PRALONG
 * 
 */
public class AccountLinkActivity extends SherlockListActivity {

	private static final String TAG = "AccountLinkActivity";
	AlertDialog levelDialog;
	String[] googleAccountsArray;

	private final WsClient wsClient = new WsClient();

	private void createAccount(final String email) {
		final ProgressDialog waitDialog = ProgressDialog.show(
				AccountLinkActivity.this, "", getString(R.string.dialog_wait),
				true, true);
		new AsyncTask<Void, Void, Integer>() {
			@Override
			protected Integer doInBackground(Void... params) {
				Integer message = R.string.error_generic;
				try {
					String user = wsClient.createUser(
							UserController.instance.getBaseUrl(), email);
					if (user != null) {
						UserController.instance.setUser(user);
						startActivity(new Intent(AccountLinkActivity.this,
								HomeActivity.class));
						AccountLinkActivity.this.finish();
						message = R.string.success_user_creation;
					}
				} catch (WsException e) {
					message = e.getCode();
				} catch (Exception e) {
					Log.d(TAG, "Could not create user", e);
				}
				return message;
			}

			@Override
			protected void onPostExecute(Integer result) {
				waitDialog.dismiss();
				Toast.makeText(AccountLinkActivity.this, result,
						Toast.LENGTH_SHORT).show();
			}
		}.execute();
	}

	private void retrieveAccount(final String email) {
		final ProgressDialog waitDialog = ProgressDialog.show(
				AccountLinkActivity.this, "", getString(R.string.dialog_wait),
				true, true);
		new AsyncTask<Void, Void, Integer>() {
			@Override
			protected Integer doInBackground(Void... params) {
				Integer message = R.string.error_generic;
				try {
					wsClient.resendUser(UserController.instance.getBaseUrl(),
							email);
					message = R.string.success_user_retrieval;
				} catch (WsException e) {
					message = e.getCode();
				} catch (Exception e) {
					Log.d(TAG, "Could not retrieve user", e);
				}
				return message;
			}

			@Override
			protected void onPostExecute(Integer result) {
				waitDialog.dismiss();
				Toast.makeText(AccountLinkActivity.this, result,
						Toast.LENGTH_SHORT).show();
			}
		}.execute();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		UserController.instance.setContext(this);
		setContentView(R.layout.login_choices);
		setListAdapter(new ArrayAdapter<String>(this,
				R.layout.login_choice_item, android.R.id.text1, getResources()
						.getStringArray(R.array.account_link_choices)));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.general, menu);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// Link to account
		final EditText edit = new EditText(this);
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(edit);
		builder.setNegativeButton(android.R.string.cancel, null);
		if (position == 0) { // Creation
			AccountManager am = AccountManager.get(this);
			Account[] accounts = am.getAccounts();
			ArrayList<String> googleAccounts = new ArrayList<String>();

			for (Account ac : accounts) {
				String acname = ac.name;
				if (ac.type.equals("com.google")) {
					googleAccounts.add(acname);
					Log.d("emails", acname);
				}
			}
			 googleAccounts.add("Other");

			googleAccountsArray = new String[googleAccounts.size()];
			googleAccountsArray = googleAccounts.toArray(googleAccountsArray);
			

			  final AlertDialog.Builder builders = new AlertDialog.Builder(this);
              builders.setTitle("Select The Difficulty Level");
              builders.setSingleChoiceItems(googleAccountsArray, -1, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int item) {
                 
                  if(item == googleAccountsArray.length -1)
                  {
          		    
          		    edit.setHint(getString(R.string.hint_email));
          			edit.setInputType(InputType.TYPE_CLASS_TEXT
          					| InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
          			builder.setTitle(
          					getString(R.string.account_link_create))
          					.setPositiveButton(
          							android.R.string.ok,
          							new DialogInterface.OnClickListener() {
          								@Override
          								public void onClick(
          										DialogInterface dialog,
          										int which) {
          									createAccount(edit
          											.getText()
          											.toString());
          								}
          							}).create().show();
                  }
                  else{
                	  createAccount(googleAccountsArray[item]);
                	  Log.d("selected" , googleAccountsArray[item]);
                  }
                 
                
                  levelDialog.dismiss();    
                  }
              });
              levelDialog = builders.create();
              levelDialog.show();




		} else if (position == 1) {
			edit.setHint(getString(R.string.hint_email));
			edit.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
			builder.setTitle(getString(R.string.account_link_retrieve))
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									retrieveAccount(edit.getText().toString());
								}
							}).create().show();
		} else if (position == 2) { // Set ID
			edit.setHint(getString(R.string.hint_id));
			edit.setInputType(InputType.TYPE_CLASS_TEXT);
			builder.setTitle(getString(R.string.account_link_existing))
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									UserController.instance.setUser(edit
											.getText().toString());
									startActivity(new Intent(
											AccountLinkActivity.this,
											HomeActivity.class));
									AccountLinkActivity.this.finish();
								}
							}).create().show();
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.menu_settings) { // Settings
			startActivity(new Intent(this, Preferences.class));
			return true;
		}
		return false;
	}
}
