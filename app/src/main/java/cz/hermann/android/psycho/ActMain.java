package cz.hermann.android.psycho;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActMain extends Activity 
{
	private static final int REQ_SETUP = 1;

	private ToneGenerator sound = null;
	private TextView tvNextNo;
	private TextView tvTime;
	private TextView tvFinishInfo;
	private Timer timer;

	private Setup setup = new Setup();
	private List<Button> arrBtn = new ArrayList<>();
	
	private int nextNo;
	private long testTime, startTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);
		
		tvNextNo = findViewById(R.id.tvNextNo);
		tvTime   = findViewById(R.id.tvTime);
		tvFinishInfo 	 = findViewById(R.id.tvFinishInfo);
		findViewById(R.id.btnNewGame).setOnClickListener(onBtnClick);
		findViewById(R.id.btnShuffle).setOnClickListener(onBtnClick);
		findViewById(R.id.btnHelp).setOnClickListener(onBtnClick);

		init((Object[])getLastNonConfigurationInstance());

    	setVolumeControlStream(AudioManager.STREAM_MUSIC);
    	sound = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
		
/* check paied version
        String a1 = context.getPackageName();
        String a2 = a1 + ".LicenseKey";
        PackageManager pm = context.getPackageManager();
        int xsig = pm.checkSignatures(a1, a2);
        boolean koupilsito = (xsig >= 0);
 */		
	}
//	public boolean isUnlocked()
//    {
//        String mainAppPackage = "com.rachunek.android.braincalc";
//        String keyPackage = "com.rachunek.android.braincalckey";
//        return getPackageManager().checkSignatures(mainAppPackage, keyPackage) == PackageManager.SIGNATURE_MATCH);
//    }	
	@Override
	public Object onRetainNonConfigurationInstance() 
	{
    	final Object[] retainData = new Object[arrBtn.size()+2];
    	for(int i = 0; i<arrBtn.size(); i++)
    		retainData[i] = arrBtn.get(i).getTag();
    	retainData[arrBtn.size()] = nextNo;
    	retainData[arrBtn.size()+1] = testTime;
    	
    	return retainData;
	}
	@Override
	protected void onPause() 
	{
		super.onPause();
		stopTimer();	
	}
	@Override
	protected void onResume() 
	{
		super.onResume();
		if ( nextNo > 1 && nextNo < arrBtn.size() )
			startTimer(testTime);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.act_main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch ( item.getItemId() )
		{
		case R.id.menu_newgame: 
			resetGame();
			break;
		case R.id.menu_shuffle:  
			shuffle(); 
			break;
		case R.id.menu_setup: 
			stopTimer(); 
			startActivityForResult(new Intent(getApplicationContext(), ActSetup.class), REQ_SETUP); 
			break;
		case R.id.menu_help:
			showHelp();
			break;
		default: 
			return false;
		}
		
		return true;
	}
	
	private View.OnClickListener onBtnClick = new View.OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			switch ( v.getId() )
			{
			case R.id.btnNewGame: resetGame(); break;
			case R.id.btnShuffle: shuffle(); break;
			case R.id.btnHelp:	  showHelp(); break;
			}
			
		}
	};
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if ( requestCode == REQ_SETUP )
			init();
	}
	private View.OnClickListener onPlayClick = new View.OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			if ( v.getTag() instanceof Integer )
			{
				int pressValue = (int)v.getTag(); // ((Integer)v.getTag()).intValue();
				if ( pressValue == nextNo )
				{
					if ( setup.playSound )
					{
						if ( nextNo<arrBtn.size() )
							sound.startTone(ToneGenerator.TONE_PROP_BEEP);
						else
							sound.startTone(ToneGenerator.TONE_CDMA_LOW_SS, 150);//.TONE_CDMA_ABBR_ALERT);
					}
					switch ( setup.gameMode )
					{
					case Setup.MODE_HIDEPASSED: v.setVisibility(View.INVISIBLE); break;
					case Setup.MODE_CRAZY: 	    if ( nextNo < arrBtn.size() ) shuffle(2*arrBtn.size()); break;
					}
					
					if ( pressValue == 1 )
						startTimer(0); 

					if ( ++nextNo > arrBtn.size() )
						stopTimer();
					
					updateStatusWnd();
				}
				else if ( setup.playSound && nextNo <= arrBtn.size() )
					sound.startTone(ToneGenerator.TONE_SUP_ERROR, 50);
					
			}
		}
	};
	private Handler hUpdateTime = new Handler(new Callback() 
	{
		@Override
        public boolean handleMessage(Message msg) 
        {
			if ( timer != null )
			{
				testTime = (System.currentTimeMillis()-startTime);
				
				updateStatusWnd();
			}
			return true;
        }
    });
	private void startTimer(long timeBeforeStart)
	{
		startTime = System.currentTimeMillis()-timeBeforeStart;
		
		timer = new Timer();
		timer.schedule(new TimerTask() {
	        public void run() { hUpdateTime.sendEmptyMessage(0); } 
	        }, 0, 40);
	}
	private void stopTimer()
	{
		if ( timer != null )
		{
			testTime = (System.currentTimeMillis()-startTime);
			
			timer.cancel();
			timer = null;
		}
	}
	private void init()
	{
		init(null);
	}
	private void init(Object[] retainData)
	{
		int prevHeight = setup.boardHeight;
		int boardWidth  = setup.boardWidth;
		
		setup.load(this);
		
		if ( prevHeight != setup.boardHeight || boardWidth != setup.boardWidth )
		{
			Integer value;
			Button btn; 
			LinearLayout row;
			LinearLayout tbl = findViewById(R.id.tblPlay);
			tbl.removeAllViews();
			arrBtn.clear();
			
			boolean useRetainData = (retainData != null && retainData.length-2 == setup.boardHeight*setup.boardWidth);  
			Integer[] arrDefValues = setup.getDefaultValues(); 
			for( int y = 0; y < setup.boardHeight; y++ )
			{
				row = new LinearLayout(this);
				row.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 100/setup.boardHeight));
				row.setOrientation(LinearLayout.HORIZONTAL);
				for( int x = 0; x < setup.boardWidth; x++ )
				{
					if ( useRetainData )			 value = (Integer)retainData[arrBtn.size()];
					else if ( arrDefValues != null ) value = arrDefValues[arrBtn.size()];
					else							 value = arrBtn.size()+1;
					
					btn = new Button(this);
					btn.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 100/setup.boardWidth));
					
					btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36-(setup.boardWidth-4)*6);
					btn.setText(String.format("%d", value));
					btn.setTag(value);
					btn.setMaxLines(1);
					btn.setOnClickListener(onPlayClick);
					btn.setBackgroundResource(R.drawable.btn_key);
					arrBtn.add(btn);

					row.addView(btn);
				}
	    		tbl.addView(row);
			}
			
			if ( !useRetainData && arrDefValues == null )
				shuffle();

			if ( useRetainData )
			{
				nextNo    = (Integer)(retainData[retainData.length-2]);
				testTime  = (Long)(retainData[retainData.length-1]);
				
				updateStatusWnd();
			}
			else
				resetGame();
		}

		if ( nextNo > 1  ) 
		{
			for(int i = 0; i<arrBtn.size(); i++)	// show/hide passed button after either setup or configuration (screen rotation) change
				if ( (int)arrBtn.get(i).getTag() < nextNo )
					arrBtn.get(i).setVisibility((setup.gameMode==Setup.MODE_HIDEPASSED)?View.INVISIBLE:View.VISIBLE);
		}
		
		tvFinishInfo.setVisibility((nextNo<=arrBtn.size())?View.INVISIBLE:View.VISIBLE);
	}
	private void resetGame()
	{
		stopTimer();
		
//		Toast.makeText(this, getString(R.string.gameStartBy1), Toast.LENGTH_SHORT).show();
		
		tvFinishInfo.setVisibility(View.INVISIBLE);
		
		nextNo = 1;
		testTime = 0;
		
		for(int i = 0; i<arrBtn.size(); i++)
			arrBtn.get(i).setVisibility(View.VISIBLE);
		
		updateStatusWnd();
	}
	private void shuffle() 
	{
		shuffle(0);
	}
	private void shuffle(int iterationCnt)
	{
		if ( iterationCnt <= 0 || iterationCnt > 10*arrBtn.size() )
			iterationCnt = 4*arrBtn.size();
		
		Random randInt = new Random();
		
		Button btn1, btn2;
		Integer value;
		for(int i = 0; i<iterationCnt; i++)
		{
			btn1 = arrBtn.get(randInt.nextInt(arrBtn.size()));
			btn2 = arrBtn.get(randInt.nextInt(arrBtn.size()));
			if ( btn1 != btn2 && btn1.getVisibility() == View.VISIBLE && btn2.getVisibility() == View.VISIBLE )
			{
				value = (Integer)(btn1.getTag());
				btn1.setTag(btn2.getTag());
				btn2.setTag(value);
			}
		}
		
		for(int i = 0; i<arrBtn.size(); i++)
			arrBtn.get(i).setText(String.format("%d", (int)arrBtn.get(i).getTag()));
		
		if ( nextNo > arrBtn.size() ) 
			resetGame();
	}
	private void showHelp()
	{
		stopTimer(); 

		AlertDialog dlg = (new AlertDialog.Builder(this))
			.setTitle(R.string.app_name)
			.setView(getLayoutInflater().inflate(R.layout.dlg_help, null))	//			.setMessage(R.string.help_content)
			.setCancelable(true)
			.setNeutralButton("OK", null)
			.create();
		dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) { 
				if ( nextNo > 1 && nextNo < arrBtn.size() )
					startTimer(testTime);
			}
		});
		dlg.show();
	}
	private void updateStatusWnd()
	{
		if ( nextNo <= arrBtn.size() )
			tvNextNo.setText(String.format("%d", nextNo));
		else
		{
			tvNextNo.setText(" - - ");
			tvFinishInfo.setText(R.string.gameOver);
			tvFinishInfo.setVisibility(View.VISIBLE);
		}

		int min = (int)(testTime/60000);
		int sec = (int)((testTime-min*60000)/1000);
		int ms  = (int)((testTime-min*60000-sec*1000)/100);
		tvTime.setText(String.format("%02d:%02d:%01d", min, sec, ms));
	}
}
