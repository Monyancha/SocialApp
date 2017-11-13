package com.socioboard.t_board_pro;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.socioboard.t_board_pro.adapters.SelectAccountAdapter;
import com.socioboard.t_board_pro.util.Const;
import com.socioboard.t_board_pro.util.MainSingleTon;
import com.socioboard.t_board_pro.util.ModelUserDatas;
import com.socioboard.t_board_pro.util.SchTweetModel;
import com.socioboard.t_board_pro.util.TboardproLocalData;
import com.socioboard.t_board_pro.util.TweetSchedullerReceiver;
import com.socioboard.tboardpro.R;

import java.util.ArrayList;
import java.util.Calendar;

public class SchedulleComposeActivity extends ActionBarActivity {

	Button tweetButton;
	EditText edttext;
	String tweetString;
	TboardproLocalData tbDAta;
	ArrayList<ModelUserDatas> navDrawerItems;
	TextView textViewCount;
	TextView textViewDate;
	TextView textViewTime;
	AlarmManager alarmManagers;
	TimePicker timePicker;
	DatePicker datePicker;
	CheckBox chkBox;
	ImageView imgimageView1Cal;
	ImageView imageView2Time, imageViewAddUsers;

	public SparseBooleanArray sparseBooleanArray;

	int count = 0;
	int year;
	int month;
	int day;

	@Override
	protected void onDestroy() {

		super.onDestroy();

		MainSingleTon.insertedText = "";

		MainSingleTon.in_reply_to_status_id = "";

		MainSingleTon.retweet_to_status_id = "";

		myprint("* * * * SchedulleComposeActivity * * * * * * On destroyed * * * * *");

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.schdulle_compose);

		alarmManagers = (AlarmManager) getApplicationContext()
				.getSystemService(getApplicationContext().ALARM_SERVICE);

		textViewCount = (TextView) findViewById(R.id.textView1Counted);

		imgimageView1Cal = (ImageView) findViewById(R.id.imageView1Cal);

		imageViewAddUsers = (ImageView) findViewById(R.id.imageViewAddUsers);

		imageView2Time = (ImageView) findViewById(R.id.imageView2Time);

		textViewDate = (TextView) findViewById(R.id.textView1date);

		textViewTime = (TextView) findViewById(R.id.textView1time);

		chkBox = (CheckBox) findViewById(R.id.checkBox1);

		tbDAta = new TboardproLocalData(getApplicationContext());

		navDrawerItems = tbDAta.getAllUsersDataArlist();

		sparseBooleanArray = new SparseBooleanArray(navDrawerItems.size());

		for (int i = 0; i < navDrawerItems.size(); ++i) {

			sparseBooleanArray.put(i, false);

			if (navDrawerItems.get(i).getUserid()
					.contains(MainSingleTon.currentUserModel.getUserid())) {

				sparseBooleanArray.put(i, true);

				count++;
			}
		}

		textViewCount.setText("Selected : " + count);

		myprint(navDrawerItems);

		imageViewAddUsers.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				openSelectDialog();

			}
		});

		tweetButton = (Button) findViewById(R.id.button1);

		edttext = (EditText) findViewById(R.id.editText1);

		if (!MainSingleTon.retweet_to_status_id.isEmpty()) {

			edttext.setVisibility(View.GONE);

		} else {

			edttext.append(MainSingleTon.insertedText);

		}

		tweetButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				schedulleThisTweet();

			}
		});

		imgimageView1Cal.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Calendar c = Calendar.getInstance();
				final int cyear = c.get(Calendar.YEAR);
				final int cmonth = c.get(Calendar.MONTH);
				final int cday = c.get(Calendar.DAY_OF_MONTH);

				runOnUiThread(new Runnable() {

					public void run() {

						new DatePickerDialog(SchedulleComposeActivity.this,
								pickerListener, cyear, cmonth, cday).show();

					}
				});

			}
		});

		textViewDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Calendar c = Calendar.getInstance();
				final int cyear = c.get(Calendar.YEAR);
				final int cmonth = c.get(Calendar.MONTH);
				final int cday = c.get(Calendar.DAY_OF_MONTH);

				runOnUiThread(new Runnable() {

					public void run() {

						new DatePickerDialog(SchedulleComposeActivity.this,
								pickerListener, cyear, cmonth, cday).show();

					}
				});

			}
		});

		textViewTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Calendar c = Calendar.getInstance();

				final int currenthour = c.get(Calendar.HOUR_OF_DAY);

				final int currentminute = c.get(Calendar.MINUTE);

				runOnUiThread(new Runnable() {

					public void run() {

						TimePickerDialog tdialog = new TimePickerDialog(
								SchedulleComposeActivity.this, timelistner,
								currenthour, currentminute, false);

						tdialog.show();

					}
				});

			}
		});
		imageView2Time.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Calendar c = Calendar.getInstance();

				final int currenthour = c.get(Calendar.HOUR_OF_DAY);

				final int currentminute = c.get(Calendar.MINUTE);

				runOnUiThread(new Runnable() {

					public void run() {

						TimePickerDialog tdialog = new TimePickerDialog(
								SchedulleComposeActivity.this, timelistner,
								currenthour, currentminute, false);

						tdialog.show();

					}
				});

			}
		});

		int actionBarTitleId = getResources().getIdentifier("action_bar_title",
				"id", "android");

		if (actionBarTitleId > 0) {

			TextView title = (TextView) findViewById(actionBarTitleId);

			if (title != null) {

				title.setTextColor(Color.WHITE);

				title.setText("Schedule");

			}
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		getSupportActionBar().setDisplayShowHomeEnabled(true);

		getSupportActionBar().setBackgroundDrawable(
				new ColorDrawable(getResources().getColor(R.color.myBlue)));

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	protected void openSelectDialog() {

		final Dialog dialog;

		dialog = new Dialog(SchedulleComposeActivity.this);

		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		dialog.setContentView(R.layout.select_user_dialog);

		dialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

		Window window = dialog.getWindow();

		lp.copyFrom(window.getAttributes());

		lp.width = WindowManager.LayoutParams.MATCH_PARENT;

		lp.height = WindowManager.LayoutParams.MATCH_PARENT;

		window.setAttributes(lp);

		dialog.setCancelable(true);

		ListView listView = (ListView) dialog
				.findViewById(R.id.listView1select);

		final SelectAccountAdapter selectAccountAdapter;

		selectAccountAdapter = new SelectAccountAdapter(navDrawerItems,
				getApplicationContext(), sparseBooleanArray);

		listView.setAdapter(selectAccountAdapter);

		Button buttonDone, cancelbtn;

		cancelbtn = (Button) dialog.findViewById(R.id.cancelbtn);

		cancelbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				dialog.cancel();

			}
		});

		buttonDone = (Button) dialog.findViewById(R.id.button1);

		buttonDone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				sparseBooleanArray = selectAccountAdapter.sparseBooleanArray;

				count = selectAccountAdapter.count;

				myprint("buttonCancel");

				dialog.cancel();

			}
		});

		new Handler().post(new Runnable() {

			@Override
			public void run() {

				dialog.show();

			}
		});

		dialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {

				count = 0;

				for (int i = 0; i < navDrawerItems.size(); ++i) {

					if (sparseBooleanArray.get(i)) {
						++count;
						myprint("dialog.setOnCancel"
								+ sparseBooleanArray.get(i));
					}

				}

				textViewCount.setText("" + count);
			}
		});
	}

	protected void schedulleThisTweet() {

		tweetString = edttext.getText().toString();

		if (count == 0) {

			myToastS("Select User first!");

			return;
		}

		if (!MainSingleTon.retweet_to_status_id.isEmpty()) {

		} else {

			if (tweetString.length() == 0 || tweetString.trim().length()==0) {

				myToastS("Text can Not be Empty");

				return;
			}

		}

		// check everything filled by user or not

		Calendar calendar = Calendar.getInstance();

		if (datePicker != null && timePicker != null) {

			calendar.set(datePicker.getYear(), datePicker.getMonth(),
					datePicker.getDayOfMonth(), timePicker.getCurrentHour(),
					timePicker.getCurrentMinute(), 0);

			long startTime = calendar.getTimeInMillis();

			if (startTime > System.currentTimeMillis()) {

				for (int i = 0; i < navDrawerItems.size(); i++) {

					if (sparseBooleanArray.get(i)) {

						long tweetTime = startTime + i * 5000;

						myprint(i + " time = " + tweetTime);

						SchTweetModel schTweetModel = new SchTweetModel(
								navDrawerItems.get(i).getUserid(), tweetString,
								tweetTime);

						myprint(schTweetModel);

						if (!MainSingleTon.in_reply_to_status_id.isEmpty()) {

							myprint("************ in_reply_to_status_id "
									+ MainSingleTon.in_reply_to_status_id);

							String newTweetString = "in_reply_to_status_id=@@"
									+ MainSingleTon.in_reply_to_status_id
									+ "@@" + tweetString;

							schTweetModel.setTweet(newTweetString);

						} else if (!MainSingleTon.retweet_to_status_id
								.isEmpty()) {

							String newTweetString = "retweet_to_status_id=@@"
									+ MainSingleTon.retweet_to_status_id + "@@"
									+ MainSingleTon.insertedText;

							schTweetModel.setTweet(newTweetString);

							myprint("*********** retweet_to_status_id "
									+ MainSingleTon.retweet_to_status_id);

						} else {

							myprint(" Normal Composing tweet ");

						}

						tbDAta.addNewSchedulledTweet(schTweetModel);

						setAlarmThisTweet(schTweetModel);

					}

				}

				myToastS("Tweet Schedulled !");

				MainActivity.isNeedToRefreshDrawer = true;

				finish();

			} else {

				myToastS("picked date should be more than current date");

			}

		} else {

			myToastS("please select desire date or time");
		}
	}

	void myToastS(final String toastMsg) {

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), toastMsg,
						Toast.LENGTH_SHORT).show();

			}
		});
	}

	void myToastL(final String toastMsg) {

		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				Toast.makeText(getApplicationContext(), toastMsg,
						Toast.LENGTH_LONG).show();

			}
		});
	}

	public void myprint(Object msg) {

		System.out.println(msg.toString());

	}

	void setAlarmThisTweet(SchTweetModel schTweetModel) {

		// **************************************

		Intent myIntent = new Intent(SchedulleComposeActivity.this,
				TweetSchedullerReceiver.class);

		myIntent.putExtra(Const.RES_CODE, schTweetModel.getTweetId());

		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				SchedulleComposeActivity.this, schTweetModel.getTweetId(),
				myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		alarmManagers.set(AlarmManager.RTC_WAKEUP,
				schTweetModel.getTweettime(), pendingIntent);

		// **************************************

	}

	private DatePickerDialog.OnDateSetListener pickerListener = new DatePickerDialog.OnDateSetListener() {

		// when dialog box is closed, below method will be called.

		@Override
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {

			datePicker = view;

			year = selectedYear;
			month = selectedMonth;
			day = selectedDay;

			// Show selected date
			textViewDate.setText(new StringBuilder().append(month + 1)
					.append("-").append(day).append("-").append(year)
					.append(" "));

		}

	};

	private TimePickerDialog.OnTimeSetListener timelistner = new OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

			timePicker = view;

			textViewTime.setText(hourOfDay + ":" + minute);

		}
	};

}
