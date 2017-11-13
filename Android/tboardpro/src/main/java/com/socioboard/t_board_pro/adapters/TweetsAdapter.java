package com.socioboard.t_board_pro.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.socioboard.t_board_pro.AnyUserProfileDialog;
import com.socioboard.t_board_pro.MainActivity;
import com.socioboard.t_board_pro.SchedulleComposeActivity;
import com.socioboard.t_board_pro.dialog.ShowTweetComposeDialog;
import com.socioboard.t_board_pro.lazylist.ImageLoader;
import com.socioboard.t_board_pro.twitterapi.TwitterPostRequestFollow;
import com.socioboard.t_board_pro.twitterapi.TwitterPostRequestPerams;
import com.socioboard.t_board_pro.twitterapi.TwitterPostRequestUnFollow;
import com.socioboard.t_board_pro.twitterapi.TwitterRequestCallBack;
import com.socioboard.t_board_pro.util.Const;
import com.socioboard.t_board_pro.util.MainSingleTon;
import com.socioboard.t_board_pro.util.ModelUserDatas;
import com.socioboard.t_board_pro.util.TboardproLocalData;
import com.socioboard.t_board_pro.util.TmpCallback;
import com.socioboard.t_board_pro.util.TweetModel;
import com.socioboard.tboardpro.R;
import com.squareup.picasso.Picasso;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TweetsAdapter extends BaseAdapter {

	private Context context;
	public ArrayList<TweetModel> tweetModels;
	public ImageLoader imageLoader;
	public Context activity;
	public static Handler handler = new Handler();
	public Handler handler2 = new Handler();
	public ProgressDialog progressDialog;

	TboardproLocalData tboardproLocalData;

	ArrayList<String> sendingids;

	ArrayList<String> sentIds;

	ArrayList<ModelUserDatas> navDrawerItems;

	Dialog dialogopenSelect;

	public TweetsAdapter(ArrayList<TweetModel> tweetModels, Context activity) {

		this.context = activity;
		this.activity = activity;
		this.tweetModels = tweetModels;

		imageLoader = new ImageLoader(context);
		progressDialog = new ProgressDialog(activity);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(true);

		tboardproLocalData = new TboardproLocalData(activity);

		sendingids = tboardproLocalData.getAllSendingIDs(MainSingleTon.currentUserModel.getUserid());

		sentIds = tboardproLocalData.getAllSentIDs(MainSingleTon.currentUserModel.getUserid());

		dialogopenSelect = new Dialog(context);

		dialogopenSelect.requestWindowFeature(Window.FEATURE_NO_TITLE);

		dialogopenSelect.setContentView(R.layout.select_user_dialog);

		dialogopenSelect.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

		Window window = dialogopenSelect.getWindow();

		lp.copyFrom(window.getAttributes());

		lp.width = WindowManager.LayoutParams.MATCH_PARENT;

		lp.height = WindowManager.LayoutParams.MATCH_PARENT;

		window.setAttributes(lp);

		dialogopenSelect.setCancelable(true);

	}

	@Override
	public int getCount() {
		return tweetModels.size();
	}

	@Override
	public TweetModel getItem(int position) {
		return tweetModels.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		if (convertView == null) {

			LayoutInflater mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			convertView = mInflater.inflate(R.layout.single_tweet, parent, false);

		}

		final TweetModel tweetModel = getItem(position);

		ImageView profilePic = (ImageView) convertView.findViewById(R.id.profile_pic);

		RelativeLayout relTweetImage = (RelativeLayout) convertView.findViewById(R.id.relTweetImage);

		RelativeLayout SingetweetRelative = (RelativeLayout) convertView.findViewById(R.id.SingetweetRelative);

		if (tweetModel.getMediaImagerUrl().isEmpty()) {

			System.out.println("*****************getMediaImagerUrl Empty ");

			relTweetImage.setVisibility(View.GONE);

		} else {

			System.out.println("******** " + tweetModel.getMediaImagerUrl());

			relTweetImage.setVisibility(View.VISIBLE);

			ImageView imageView1Tweet = (ImageView) convertView.findViewById(R.id.imageView1Tweet);

			Picasso.with(context).load(tweetModel.getMediaImagerUrl()).into(imageView1Tweet);
			//imageLoader.DisplayImage(tweetModel.getMediaImagerUrl(), imageView1Tweet);

		}

		final Button buttonFollow = (Button) convertView.findViewById(R.id.buttonFollow);

		final Button buttonUnfollow = (Button) convertView.findViewById(R.id.buttonUnfollow);

		if (tweetModel.isFollowing()) {

			buttonFollow.setVisibility(View.INVISIBLE);

			buttonUnfollow.setVisibility(View.VISIBLE);

		} else {

			buttonFollow.setVisibility(View.VISIBLE);

			buttonUnfollow.setVisibility(View.INVISIBLE);

		}

		if (MainSingleTon.currentUserModel.getUserid().contains(tweetModel.getUserID())) {

			buttonUnfollow.setVisibility(View.INVISIBLE);
			buttonFollow.setVisibility(View.INVISIBLE);

		}

		buttonFollow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (MainSingleTon.isNeedTOstopFollowing) {

					showToast("You have Exceeded Follow Limit Today");

				} else {

					myprint("buttonFollow " + getItem(position));

					buttonFollow.setVisibility(View.INVISIBLE);

					buttonUnfollow.setVisibility(View.VISIBLE);

					getItem(position).setFollowing(true);

					TwitterPostRequestFollow twitterPostRequestFollow = new TwitterPostRequestFollow(
							MainSingleTon.currentUserModel, new TwitterRequestCallBack() {

						@Override
						public void onSuccess(JSONObject jsonObject) {

						}

						@Override
						public void onSuccess(String jsonResult) {

							addDMStatus(tweetModel.getUserID());

							myprint("buttonFollow onSuccess");

							++MainSingleTon.followingCount;

							MainSingleTon.toFollowingModelsIDs.add(tweetModel.getUserID());

							MainActivity.isNeedToRefreshDrawer = true;

						}

						@Override
						public void onFailure(Exception e) {

							myprint("buttonFollow onFailure" + e);

							getItem(position).setFollowing(false);

						}

					});

					twitterPostRequestFollow.executeThisRequest(tweetModel.getUserID());
				}
			}
		});

		buttonUnfollow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				myprint("buttonUnfollow " + getItem(position));

				if (MainSingleTon.isNeedTOstopFollowing) {

					showToast("You have Exceeded Follow Limit Today");

				} else {

					buttonUnfollow.setVisibility(View.INVISIBLE);

					buttonFollow.setVisibility(View.VISIBLE);

					getItem(position).setFollowing(false);

					TwitterPostRequestUnFollow twitterPostRequestUnFollow = new TwitterPostRequestUnFollow(
							MainSingleTon.currentUserModel, new TwitterRequestCallBack() {

						@Override
						public void onSuccess(JSONObject jsonObject) {

						}

						@Override
						public void onSuccess(String jsonResult) {

							myprint("buttonUnfollow onSuccess");

							--MainSingleTon.followingCount;

							MainSingleTon.toFollowingModelsIDs.remove(tweetModel.getUserID());

							MainActivity.isNeedToRefreshDrawer = true;

						}

						@Override
						public void onFailure(Exception e) {

							myprint("buttonUnfollow onFailure" + e);

							getItem(position).setFollowing(true);

						}

					});

					twitterPostRequestUnFollow.executeThisRequest(tweetModel.getUserID());
				}
			}
		});

		final ImageView imageViewFav;

		imageViewFav = (ImageView) convertView.findViewById(R.id.imageView1Fav);

		ImageView imageViewReply = (ImageView) convertView.findViewById(R.id.imageView1REply);

		final ImageView imageView2Retweet = (ImageView) convertView.findViewById(R.id.imageView1Retweet);

		profilePic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String newUser = tweetModel.getUserName();

				newUser = newUser.replace("@", "");

				if (newUser.contains(MainSingleTon.currentUserModel.getUsername())) {

					return;
				}

				AnyUserProfileDialog anyUserProfile = new AnyUserProfileDialog(activity, newUser,
						tweetModel.getUserID());

			}
		});

		TextView userName = (TextView) convertView.findViewById(R.id.usersName);

		userName.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				handler.post(new Runnable() {

					@Override
					public void run() {

						String newUser = tweetModel.getUserName();

						newUser = newUser.replace("@", "");

						if (newUser.contains(MainSingleTon.currentUserModel.getUsername())) {
							return;
						}

						AnyUserProfileDialog anyUserProfile = new AnyUserProfileDialog(activity, (newUser),
								tweetModel.getUserID());

					}
				});

			}
		});

		TextView favCount = (TextView) convertView.findViewById(R.id.tweetViewfavs);

		TextView retweetCount = (TextView) convertView.findViewById(R.id.tweetViewRetweet);

		TextView fullname = (TextView) convertView.findViewById(R.id.usersFullName);

		TextView tweettime = (TextView) convertView.findViewById(R.id.textView1Time);

		TextView tweetView = (TextView) convertView.findViewById(R.id.tweetView);

		if (tweetModel.isRetweeted()) {

			imageView2Retweet.setImageResource(R.drawable.ic_action_rt_on_focused);

		} else {

			imageView2Retweet.setImageResource(R.drawable.ic_action_rt_off_focused);
		}

		if (tweetModel.isIsfavourated()) {

			imageViewFav.setImageResource(R.drawable.ic_action_fave_on_focused);

		} else {

			imageViewFav.setImageResource(R.drawable.ic_action_fave_off_focused);

		}

		userName.setText(tweetModel.getUserName());

		fullname.setText(tweetModel.getFullName());

		retweetCount.setText("" + tweetModel.getRetweetCount());

		favCount.setText("" + tweetModel.getFavCount());

		// "Wed Aug 29 17:12:58 +0000 2012"

		String[] timeArray = tweetModel.getTweetTime().split(" ");

		String month = monthInt(timeArray[1]), day = timeArray[2];

		String year = timeArray[5];

		String[] timeArrayHMS = timeArray[3].split(":");

		String hr, min, sec;

		hr = timeArrayHMS[0];
		min = timeArrayHMS[1];
		sec = timeArrayHMS[2];

		if (month.length() == 1) {
			month = "0" + month;
		}

		if (day.length() == 1) {
			day = "0" + day;
		}

		if (hr.length() == 1) {
			hr = "0" + hr;
		}

		if (min.length() == 1) {
			min = "0" + min;
		}

		if (sec.length() == 1) {
			sec = "0" + min;
		}

		// String format = "yyyy/MM/dd HH:mm:ss";

		String dateInUtc = year + "/" + month + "/" + day + " " + hr + ":" + min + ":" + sec;

		String dateAndTime = UTCtoLocalPrettyTime(tweetModel.getTweetTime());

		System.out.println("UTCtoLocalPrettyTime  " + dateAndTime);

		tweettime.setText(dateAndTime);

		imageLoader.DisplayImage(tweetModel.getUserImagerUrl(), profilePic);

//		if (tweetModel.getMediaImagerUrl().isEmpty())
//		{
////				profilePic.setVisibility(View.GONE);
//		}else {
//
//			//Picasso.with(context).load(tweetModel.getUserImagerUrl()).noPlaceholder().into(profilePic);
//
//		}

		tweetView.setMovementMethod(LinkMovementMethod.getInstance());

		tweetView.setText(tweetModel.getTweeet_str());

		imageView2Retweet.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (tweetModel.isRetweeted()) {

					return;
				}

				myprint(" imageView2Retweet");

				progressDialog.setMessage("Wait...");

				showProgress();

				TwitterPostRequestPerams postPeramsRequest = new TwitterPostRequestPerams(
						MainSingleTon.currentUserModel, new TwitterRequestCallBack() {

					@Override
					public void onSuccess(JSONObject jsonObject) {

						myprint("");

					}

					@Override
					public void onSuccess(String jsonResult) {

						getItem(position).setRetweeted(true);

						long count = getItem(position).getRetweetCount();

						++count;

						MainSingleTon.tweetsCount++;

						MainActivity.isNeedToRefreshDrawer = true;

						getItem(position).setRetweetCount(count);

						handler.post(new Runnable() {

							@Override
							public void run() {

								progressDialog.cancel();

								imageView2Retweet.setImageResource(R.drawable.ic_action_rt_on_focused);
								notifyDataSetChanged();
							}
						});

						myprint("onSuccess " + jsonResult);

					}

					@Override
					public void onFailure(Exception e) {

						myprint("onFailure ");

						progressDialog.cancel();

						showToast("Failed!");

					}

				});

				String url = MainSingleTon.reTweeting + tweetModel.getTweetId() + ".json";

				List<BasicNameValuePair> peramPairs = new ArrayList<BasicNameValuePair>();

				peramPairs.add(new BasicNameValuePair("id", tweetModel.getTweetId()));

				postPeramsRequest.executeThisRequest(url, peramPairs);

			}
		});

		imageViewReply.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				myprint(" imageViewReply");

				handler2.post(new Runnable() {

					@Override
					public void run() {

						String mentions = tweetModel.getUserName();

						Pattern pattern = Pattern.compile("@\\s*(\\w+)");

						String input = tweetModel.getTweeet_str();

						Matcher m = pattern.matcher(input);

						while (m.find()) {

							mentions = mentions + " " + m.group();

							System.out.println("" + m.group());

						}

						ShowTweetComposeDialog tweetComposeDialog = new ShowTweetComposeDialog(activity, mentions,
								handler);
						tweetComposeDialog.showThis();

					}
				});

			}
		});

		imageViewFav.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View view) {

				progressDialog.setMessage("Just a moment...");

				showProgress();

				myprint(" imageViewFav");

				String url;

				TwitterPostRequestPerams postPeramsRequest = new TwitterPostRequestPerams(
						MainSingleTon.currentUserModel, new TwitterRequestCallBack() {

					@Override
					public void onSuccess(JSONObject jsonObject) {

						myprint("");

					}

					@Override
					public void onSuccess(String jsonResult) {

						myprint("onSuccess " + jsonResult);

						handler.post(new Runnable() {

							@Override
							public void run() {

								long count = getItem(position).getFavCount();

								progressDialog.cancel();

								if (tweetModel.isIsfavourated()) {

									imageViewFav.setImageResource(R.drawable.ic_action_fave_off_focused);

									getItem(position).setIsfavourated(false);

									--count;

									MainSingleTon.favoritesCount--;

									MainActivity.isNeedToRefreshDrawer = true;

								} else {

									imageViewFav.setImageResource(R.drawable.ic_action_fave_on_focused);

									getItem(position).setIsfavourated(true);

									MainSingleTon.favoritesCount++;

									MainActivity.isNeedToRefreshDrawer = true;

									++count;

								}

								getItem(position).setFavCount(count);

								notifyDataSetChanged();

							}
						});

					}

					@Override
					public void onFailure(Exception e) {
						// TODO Auto-generated method stub

						myprint("onFailure ");

						handler.post(new Runnable() {

							@Override
							public void run() {

								if (progressDialog.isShowing()) {
									progressDialog.dismiss();
								}

								Toast.makeText(activity, "Failed", Toast.LENGTH_LONG).show();
							}
						});
					}
				});

				List<BasicNameValuePair> peramPairs = new ArrayList<BasicNameValuePair>();

				peramPairs.add(new BasicNameValuePair(Const.id, tweetModel.getTweetId()));

				if (tweetModel.isIsfavourated()) {

					url = MainSingleTon.favouritesDestroy;

				} else {

					url = MainSingleTon.favouritesCreate;

				}

				postPeramsRequest.executeThisRequest(url, peramPairs);

			}
		});

		final ImageView imageViewReplySchedule, imageView1MultiRetweet, imageView1MultiFav, imageView1RetweetSchedule;

		imageViewReplySchedule = (ImageView) convertView.findViewById(R.id.imageViewReplySchedule);

		imageView1MultiRetweet = (ImageView) convertView.findViewById(R.id.imageView1MultiRetweet);

		imageView1MultiFav = (ImageView) convertView.findViewById(R.id.imageView1MultiFav);

		imageView1RetweetSchedule = (ImageView) convertView.findViewById(R.id.imageView1RetweetSchedule);

		imageViewReplySchedule.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				myprint(" imageViewReply");

				String mentions = tweetModel.getUserName();

				Pattern pattern = Pattern.compile("@\\s*(\\w+)");

				String input = tweetModel.getTweeet_str();

				Matcher m = pattern.matcher(input);

				while (m.find()) {

					mentions = mentions + " " + m.group();

					System.out.println("" + m.group());

				}

				MainSingleTon.insertedText = mentions;

				MainSingleTon.in_reply_to_status_id = tweetModel.getTweetId();

				Intent intent = new Intent(context, SchedulleComposeActivity.class);

				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				context.startActivity(intent);
			}
		});

		imageView1RetweetSchedule.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				myprint(" imageView1RetweetSchedule");

				MainSingleTon.retweet_to_status_id = tweetModel.getTweetId();

				MainSingleTon.insertedText = tweetModel.getTweeet_str();

				Intent intent = new Intent(context, SchedulleComposeActivity.class);

				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				context.startActivity(intent);
			}
		});

		imageView1MultiRetweet.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				myprint(" imageView2Retweet");

				openSelectDialog(new TmpCallback() {

					@Override
					public void onsuccess() {

						// TODO Auto-generated method stub

						Thread thread = new Thread(new Runnable() {

							@Override
							public void run() {

								for (int i = 0; i < navDrawerItems.size(); i++) {

									if (sparseBooleanArray.get(i)) {

										TwitterPostRequestPerams postPeramsRequest = new TwitterPostRequestPerams(
												navDrawerItems.get(i), new TwitterRequestCallBack() {

											@Override
											public void onSuccess(JSONObject jsonObject) {

												myprint("");

											}

											@Override
											public void onSuccess(String jsonResult) {

												myprint("onSuccess " + jsonResult);

											}

											@Override
											public void onFailure(Exception e) {

												myprint("onFailure ");

											}

										});

										String url = MainSingleTon.reTweeting + tweetModel.getTweetId() + ".json";

										List<BasicNameValuePair> peramPairs = new ArrayList<BasicNameValuePair>();

										peramPairs.add(new BasicNameValuePair("id", tweetModel.getTweetId()));

										postPeramsRequest.executeThisRequest(url, peramPairs);

										try {
											Thread.sleep(1000);
										} catch (InterruptedException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}

									} else {

										myprint("* * * * * * * Not Selected navDrawerItems.item * * * * *"
												+ navDrawerItems.get(i));

									}
								}
							}
						});

						thread.start();

					}

					@Override
					public void onsuccess(Object... params) {
						// TODO Auto-generated method stub

					}
				});

			}
		});

		imageView1MultiFav.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				openSelectDialog(new TmpCallback() {

					@Override
					public void onsuccess() {

						// TODO Auto-generated method stub

						Thread thread = new Thread(new Runnable() {

							@Override
							public void run() {

								for (int i = 0; i < navDrawerItems.size(); i++) {

									if (sparseBooleanArray.get(i)) {

										String url;

										TwitterPostRequestPerams postPeramsRequest = new TwitterPostRequestPerams(
												navDrawerItems.get(i), new TwitterRequestCallBack() {

											@Override
											public void onSuccess(JSONObject jsonObject) {

												myprint("");

											}

											@Override
											public void onSuccess(String jsonResult) {

												myprint("onSuccess " + jsonResult);

											}

											@Override
											public void onFailure(Exception e) {

												// TODO Auto-generated
												// method
												// stub

												myprint("onFailure");

											}
										});

										List<BasicNameValuePair> peramPairs = new ArrayList<BasicNameValuePair>();

										peramPairs.add(new BasicNameValuePair(Const.id, tweetModel.getTweetId()));

										url = MainSingleTon.favouritesCreate;

										postPeramsRequest.executeThisRequest(url, peramPairs);

										try {
											Thread.sleep(1000);
										} catch (InterruptedException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}

									} else {

										myprint("* * * * * * * Not Selected navDrawerItems.item * * * * *"
												+ navDrawerItems.get(i));

									}
								}
							}
						});

						thread.start();

					}

					@Override
					public void onsuccess(Object... params) {
						// TODO Auto-generated method stub

					}
				});
			}
		});

		if (position != 0 && position % 10 == 0) {

			SingetweetRelative.setVisibility(View.GONE);

		} else {

			SingetweetRelative.setVisibility(View.VISIBLE);
		}

		return convertView;
	}

	SparseBooleanArray sparseBooleanArray;

	int count;

	protected void openSelectDialog(final TmpCallback tmpCallback) {

		handler2.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				ListView listView = (ListView) dialogopenSelect.findViewById(R.id.listView1select);

				final SelectAccountAdapter selectAccountAdapter;

				navDrawerItems = tboardproLocalData.getAllUsersDataArlist();

				sparseBooleanArray = new SparseBooleanArray(navDrawerItems.size());

				for (int i = 0; i < navDrawerItems.size(); ++i) {

					sparseBooleanArray.put(i, false);

					if (navDrawerItems.get(i).getUserid().contains(MainSingleTon.currentUserModel.getUserid())) {

						sparseBooleanArray.put(i, true);

					}
				}

				selectAccountAdapter = new SelectAccountAdapter(navDrawerItems, context, sparseBooleanArray);

				listView.setAdapter(selectAccountAdapter);

				Button buttonDone, cancelbtn;

				buttonDone = (Button) dialogopenSelect.findViewById(R.id.button1);

				buttonDone.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						sparseBooleanArray = selectAccountAdapter.sparseBooleanArray;

						count = selectAccountAdapter.count;

						myprint("buttonCancel");

						dialogopenSelect.cancel();

						tmpCallback.onsuccess();

					}
				});

				cancelbtn = (Button) dialogopenSelect.findViewById(R.id.cancelbtn);

				cancelbtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						dialogopenSelect.cancel();

					}
				});

				new Handler().post(new Runnable() {

					@Override
					public void run() {

						dialogopenSelect.show();

					}
				});

				dialogopenSelect.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {

						count = 0;

						for (int i = 0; i < navDrawerItems.size(); ++i) {

							if (sparseBooleanArray.get(i)) {
								++count;
								myprint("dialog.setOnCancel" + sparseBooleanArray.get(i));
							}

						}

					}
				});
			}

		});
	}

	public static String getPrettyTime(long milis) {

		// System.out.println("**********************");

		// System.out.println(android.text.format.DateUtils
		// .getRelativeTimeSpanString(milis));

		// System.out.println("**********************");

		return android.text.format.DateUtils.getRelativeTimeSpanString(milis).toString();
	}

	public void myprint(Object msg) {

		System.out.println(msg.toString());

	}

	String extractImageUrl(String tweet) {

		String url = null;

		int start = tweet.indexOf("https://pbs");

		if (start < 0) {
			return url;
		}

		String started = tweet.substring(start);

		int end = started.indexOf(" ");

		if (end < 0) {
			url = started.substring(0, started.length() - 1);
		} else {
			url = started.substring(0, end);
		}

		myprint("extractImageUrl  " + url);

		return url;
	}

	void showToast(final String msg) {

		handler.post(new Runnable() {

			@Override
			public void run() {

				Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
			}
		});

	}

	private void addDMStatus(final String Userid) {

		if (MainSingleTon.autoDmfirstime.contains("yes")) {

			handler.post(new Runnable() {

				@Override
				public void run() {

					new AlertDialog.Builder(activity).setTitle("Direct Message")
							.setMessage("A thanks message will be sent to those users. who are following you back!")
							.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {

							Editor editor = context.getSharedPreferences("twtboardpro", Context.MODE_PRIVATE).edit();

							editor.putString("autoDmfirstime", "no");

							editor.putBoolean("autodm", true);

							MainSingleTon.autodm = true;

							MainSingleTon.autoDmfirstime = "no";

							editor.commit();

							addDMStatus(Userid);
						}
					}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							Editor editor = context.getSharedPreferences("twtboardpro", Context.MODE_PRIVATE).edit();

							editor.putString("autoDmfirstime", "no");

							editor.putBoolean("autodm", false);

							editor.commit();

							MainSingleTon.autoDmfirstime = "no";

							MainSingleTon.autodm = false;

						}

					}).setIcon(R.drawable.ic_launcher).show();
				}
			});

		} else {

			myprint("Not to diss[ay fvnefisdfnvko nvkjn");

			if (MainSingleTon.autodm) {

				if (sendingids.contains(Userid) || sentIds.contains(Userid)) {

				} else {

					tboardproLocalData.addNewDMsendingId(Userid, MainSingleTon.currentUserModel.getUserid());

					sendingids.add(Userid);

				}

			} else {

			}
		}
	}

	void showProgress() {

		handler2.post(new Runnable() {

			@Override
			public void run() {

				progressDialog.show();

			}
		});
	}

	void hideProgress() {

		handler2.post(new Runnable() {

			@Override
			public void run() {

				progressDialog.cancel();

			}
		});
	}

	public String monthInt(String month) {

		// System.out.print("Enter month's number: ");

		int monthNumber;

		switch (month.toLowerCase()) {
		case "Jan":
			monthNumber = 1;
			break;
		case "Feb":
			monthNumber = 2;
			break;
		case "Mar":
			monthNumber = 3;
			break;
		case "Apr":
			monthNumber = 4;
			break;
		case "May":
			monthNumber = 5;
			break;
		case "Jun":
			monthNumber = 6;
			break;
		case "Jly":
			monthNumber = 7;
			break;
		case "Aug":
			monthNumber = 8;
			break;
		case "Sep":
			monthNumber = 9;
			break;
		case "Oct":
			monthNumber = 10;
			break;
		case "Nov":
			monthNumber = 11;
			break;
		case "Dec":
			monthNumber = 12;
			break;
		default:
			monthNumber = 1;
			break;
		}

		if (monthNumber < 10) {

			return "0" + monthNumber;

		} else {

			return "" + monthNumber;

		}
	}

	public String UTCtoLocalPrettyTime(String dateInUtc) {

		long currentmilis = System.currentTimeMillis();

		String twitterFormat = "EEE MMM dd HH:mm:ss zzzz yyyy";

		String format = "yyyy/MM/dd HH:mm:ss";

		SimpleDateFormat sdf = new SimpleDateFormat(format);

		SimpleDateFormat sdfTwitterFormat = new SimpleDateFormat(twitterFormat);

		SimpleDateFormat sdf2sameDay = new SimpleDateFormat("hh:mm a");

		SimpleDateFormat sdf2ThisYear = new SimpleDateFormat("dd MMM hh:mm a");

		SimpleDateFormat sdf2LAstYear = new SimpleDateFormat("dd MMM yyyy hh:mm a");

		Date date = null;

		long preparedTime = 0;

		try {

			date = sdfTwitterFormat.parse(dateInUtc);

			preparedTime = date.getTime();

			// System.out.println("preparedTime " + preparedTime);

			// System.out.println("currentmilis " + currentmilis);

		} catch (ParseException e) {

			e.printStackTrace();
		}

		if (date == null) {

			return dateInUtc;

		} else {

			Calendar calendar = Calendar.getInstance();

			calendar.setTimeInMillis(preparedTime);

			System.out.println("preparedTime " + preparedTime);

			System.out.println("currentmilis " + currentmilis);

			if (preparedTime > currentmilis) {

				return sdf.format(calendar.getTime());

			} else if (preparedTime < currentmilis && preparedTime > (currentmilis - 60000)) {

				return "Just Now";

			} else if (preparedTime < currentmilis && preparedTime > (currentmilis - 60 * 60 * 1000)) {

				long minsOver = currentmilis - preparedTime;

				return "" + minsOver / 60000L + " mins ago";

			} else if (preparedTime > getMorningStartDayStamp(currentmilis) && preparedTime < currentmilis) {

				return sdf2sameDay.format(calendar.getTime());

			} else if (preparedTime > getYearStartDayStamp(currentmilis)) {

				return sdf2ThisYear.format(calendar.getTime());

			} else {

				return sdf2LAstYear.format(calendar.getTime());

			}

		}

	}

	public static long getMorningStartDayStamp(long originalStamp) {

		System.out.println("**  getMorningStartDayStamp  **** ");

		long morningStamp = 0;

		Calendar calendar = Calendar.getInstance();

		calendar.setTimeInMillis(originalStamp);

		String dd, mm, yyyy;

		dd = "" + calendar.get(Calendar.DAY_OF_MONTH);

		if (dd.length() == 1) {
			dd = "0" + dd;
		}

		mm = "" + (calendar.get(Calendar.MONTH) + 1);

		if (mm.length() == 1) {
			mm = "0" + mm;
		}

		yyyy = "" + calendar.get(Calendar.YEAR);

		String yymmdd = yyyy + "/" + mm + "/" + dd;

		System.out.println("yymmdd " + yymmdd);

		String local_time = yymmdd + " 00:00:00";

		String format = "yyyy/MM/dd HH:mm:ss";

		SimpleDateFormat sdf = new SimpleDateFormat(format);

		Date date;

		try {

			date = sdf.parse(local_time.trim());

			// Convert Local Time to UTC (Works Fine)
			sdf.setTimeZone(TimeZone.getDefault());

			Date gmtTime = new Date(sdf.format(date));

			morningStamp = gmtTime.getTime();

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("morningStamp = " + morningStamp);

		return morningStamp;

	}

	public static long getYearStartDayStamp(long originalStamp) {

		System.out.println("**  getMorningStartDayStamp  **** ");

		long YearStart = 0;

		Calendar calendar = Calendar.getInstance();

		calendar.setTimeInMillis(originalStamp);

		String yyyy;

		yyyy = "" + calendar.get(Calendar.YEAR);

		String yymmdd = yyyy + "/01/01";

		System.out.println("yymmdd " + yymmdd);

		String local_time = yymmdd + " 00:00:00";

		String format = "yyyy/MM/dd HH:mm:ss";

		SimpleDateFormat sdf = new SimpleDateFormat(format);

		Date date;

		try {

			date = sdf.parse(local_time.trim());

			// Convert Local Time to UTC (Works Fine)
			sdf.setTimeZone(TimeZone.getDefault());

			Date gmtTime = new Date(sdf.format(date));

			YearStart = gmtTime.getTime();

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("YearStart = " + YearStart);

		return YearStart;

	}

	private static final int SECOND_MILLIS = 1000;
	private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
	private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
	private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

	public static String getTimeAgo(long time) {
		if (time < 1000000000000L) {
			// if timestamp given in seconds, convert to millis
			time *= 1000;
		}

		long now = System.currentTimeMillis();
		if (time > now || time <= 0) {
			return null;
		}

		// TODO: localize
		final long diff = now - time;
		if (diff < MINUTE_MILLIS) {
			return "just now";
		} else if (diff < 2 * MINUTE_MILLIS) {
			return "a minute ago";
		} else if (diff < 50 * MINUTE_MILLIS) {
			return diff / MINUTE_MILLIS + " minutes ago";
		} else if (diff < 90 * MINUTE_MILLIS) {
			return "an hour ago";
		} else if (diff < 24 * HOUR_MILLIS) {
			return diff / HOUR_MILLIS + " hours ago";
		} else if (diff < 48 * HOUR_MILLIS) {
			return "yesterday";
		} else {
			return diff / DAY_MILLIS + " days ago";
		}
	}

}
