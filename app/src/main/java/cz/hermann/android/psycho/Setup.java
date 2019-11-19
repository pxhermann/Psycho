package cz.hermann.android.psycho;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


class Setup
{
	private static final int MODE_STANDARD = 1;	// these values must he the same as values in resource array 'modeValue'
	static final int MODE_HIDEPASSED = 2;
	static final int MODE_CRAZY = 3;

	boolean playSound = true;
	int boardWidth = -1;
	int boardHeight = -1;
	int gameMode = MODE_STANDARD;

	private static final Integer[] arrDefault4 = {
		 9, 11,  7, 16,
		 3,  1,  4,  6,
		15,  5, 10, 12,
		14,  8, 13,  2 };
//	private static final Integer[] arrDefault5 = {
//		 6,  1, 18, 22, 14,
//		12, 10, 15,  3, 25,
//		 2, 20,  5, 23, 13,
//		16, 21,  8, 11,  7,
//		 9,  4, 17, 19, 24 };
	private static final Integer[] arrDefault5 = {
		18,  7, 24, 20,  5,
		 1, 25,  4,  6, 23,
		12, 10, 21,  3, 16,
		19, 22, 15,  9,  8,
		17,  2, 13, 11, 14 };
	private static final Integer[] arrDefault6 = {
		25, 13, 10, 30,  9, 22,
		 5, 19,  2, 14, 31,  3,
		 4, 12, 24,  1, 23, 32,
		 7, 35, 33, 36,  8, 29,
		17, 28, 21,  6, 11, 15,
		16, 26, 27, 34, 18, 20 };
	private static final Integer[] arrDefault7 = {
		 3, 25,  2, 22, 48, 49, 17,
		20, 42, 10,  1, 23,  7, 26,
		40, 16, 29, 37, 45, 38, 14,
		11, 28, 32, 30, 47, 31,  6,
		39, 36, 19, 24, 18, 34,  4,
		 8, 41, 15,  9, 21, 46, 27,
		44, 13, 35, 12,  5, 33, 43 };
	
	void load(Context context)
	{
		SharedPreferences setup = PreferenceManager.getDefaultSharedPreferences(context);
		playSound = setup.getBoolean(context.getString(R.string.keySound), true);

		try {  boardWidth = Integer.parseInt(setup.getString(context.getString(R.string.keySize), "5")); }
		catch(Exception ex) { boardWidth = 5; }
		
		try {  gameMode = Integer.parseInt(setup.getString(context.getString(R.string.keyMode), "1")); }
		catch(Exception ex) { gameMode = MODE_STANDARD; }
		
		// ensure correct size
		if ( boardWidth < 4 ) 		boardWidth = 4;
		else if ( boardWidth > 7 ) boardWidth = 7;
		
		boardHeight = boardWidth;
		// 
		if ( gameMode < 1 || gameMode > 3 )
			gameMode = MODE_STANDARD;
	}
	
	Integer[] getDefaultValues()
	{
		int boardSize = boardHeight*boardWidth;
		if ( boardSize == arrDefault4.length ) return arrDefault4;
		else if ( boardSize == arrDefault5.length )	return arrDefault5;
		else if ( boardSize == arrDefault6.length )	return arrDefault6;
		else if ( boardSize == arrDefault7.length )	return arrDefault7;
		
		return null;
	}
}
