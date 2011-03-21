
package org.doubango.imsdroid.Services.Impl;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.Main;
import org.doubango.imsdroid.R;
import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Screens.IBaseScreen;
import org.doubango.imsdroid.Screens.ScreenHome;
import org.doubango.imsdroid.Services.IScreenService;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

public class ScreenService extends BaseService implements IScreenService {
private final static String TAG = ScreenService.class.getCanonicalName();
	
	private int mLastScreensIndex = -1; // ring cursor
	private final String[] mLastScreens =  new String[]{ // ring
    		null,
    		null,
    		null,
    		null
	};
	
	@Override
	public boolean start() {
		Log.d(TAG, "starting...");
		return true;
	}

	@Override
	public boolean stop() {
		Log.d(TAG, "stopping...");
		return true;
	}

	@Override
	public boolean back() {
		String screen;
		
		// no screen in the stack
		if(mLastScreensIndex < 0){
			return true;
		}
		
		// zero is special case
		if(mLastScreensIndex == 0){
			if((screen = mLastScreens[mLastScreens.length-1]) == null){
				// goto home
				return show(ScreenHome.class);
			}
			else{
				return this.show(screen);
			}
		}
		// all other cases
		screen = mLastScreens[mLastScreensIndex-1];
		mLastScreens[mLastScreensIndex-1] = null;
		mLastScreensIndex--;
		if(screen == null || !show(screen)){
			return show(ScreenHome.class);
		}
		
		return true;
	}

	@Override
	public boolean bringToFront(int action, String[]... args) {
		Intent intent = new Intent(IMSDroid.getContext(), Main.class);
		try{
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP  | Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("action", action);
			for(String[] arg : args){
				if(arg.length != 2){
					continue;
				}
				intent.putExtra(arg[0], arg[1]);
			}
			IMSDroid.getContext().startActivity(intent);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean bringToFront(String[]... args) {
		return this.bringToFront(Main.ACTION_NONE);
	}

	@Override
	public boolean show(Class<? extends Activity> cls, String id) {
		final Main mainActivity = ServiceManager.getMainActivity();
		
		String screen_id = (id == null) ? cls.getCanonicalName() : id;
		Intent intent = new Intent(mainActivity, cls);
		intent.putExtra("id", screen_id);
		final Window window = mainActivity.getLocalActivityManager().startActivity(screen_id, intent);
		if(window != null){
			View view = mainActivity.getLocalActivityManager().startActivity(screen_id, intent).getDecorView();
			
			LinearLayout layout = (LinearLayout) mainActivity.findViewById(R.id.main_linearLayout_principal);
			layout.removeAllViews();
			layout.addView(view);
			
			// add to stack
			this.mLastScreens[(++this.mLastScreensIndex % this.mLastScreens.length)] = screen_id;
			this.mLastScreensIndex %= this.mLastScreens.length;
			return true;
		}
		return false;
	}

	@Override
	public boolean show(Class<? extends Activity> cls) {
		return this.show(cls, null);
	}

	@Override
	public boolean show(String id) {
		final  Activity screen = (Activity)ServiceManager.getMainActivity().getLocalActivityManager().getActivity(id);
		if (screen == null) {
			Log.e(TAG, String.format(
					"Failed to retrieve the Screen with id=%s", id));
			return false;
		} else {
			return this.show(screen.getClass(), id);
		}
	}

	@Override
	public void runOnUiThread(Runnable r) {
		if(ServiceManager.getMainActivity() != null){
			ServiceManager.getMainActivity().runOnUiThread(r);
		}
		else{
			Log.e(this.getClass().getCanonicalName(), "No Main activity");
		}
	}

	@Override
	public boolean destroy(String id) {
		return (ServiceManager.getMainActivity().getLocalActivityManager().destroyActivity(id, true) != null);
	}

	@Override
	public void setProgressInfoText(String text) {
	}

	@Override
	public IBaseScreen getCurrentScreen() {
		return (IBaseScreen)ServiceManager.getMainActivity().getLocalActivityManager().getCurrentActivity();
	}

	@Override
	public IBaseScreen getScreen(String id) {
		return (IBaseScreen)ServiceManager.getMainActivity().getLocalActivityManager().getActivity(id);
	}
}