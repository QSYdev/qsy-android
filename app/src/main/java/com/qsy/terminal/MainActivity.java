package com.qsy.terminal;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.qsy.terminal.fragments.libterminal.LibterminalFragment;
import com.qsy.terminal.fragments.executors.PlayerExecutorFragment;
import com.qsy.terminal.services.LibterminalService;
import com.qsy.terminal.utils.QSYUtils;

import libterminal.patterns.observer.Event;
import libterminal.patterns.observer.EventListener;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
	EventListener {
	private ActionBarDrawerToggle mToggle;
	private DrawerLayout mDrawer;
	private Toolbar mToolbar;
	private NavigationView mNavigationView;

	private MainActivityConnection mConnection = new MainActivityConnection();
	private LibterminalService libterminalService;
	private LibterminalFragment mLibterminalFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);

		mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		mToggle = new ActionBarDrawerToggle(
			this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

		mDrawer.setDrawerListener(mToggle);
		mToggle.syncState();

		mNavigationView = (NavigationView) findViewById(R.id.nav_view);
		mNavigationView.setNavigationItemSelectedListener(this);

		final Intent intent = new Intent(MainActivity.this, LibterminalService.class);
		intent.setAction("on");
		startService(intent);
		bindService(intent, mConnection, 0);

		FragmentManager fm = getSupportFragmentManager();
		mLibterminalFragment = new LibterminalFragment();
		fm.beginTransaction().replace(R.id.content_main, mLibterminalFragment).commit();
	}

	@Override
	protected void onStart() {
		super.onStart();
		QSYUtils.checkWifiEnabled(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (libterminalService != null) {
			try {
				libterminalService.getTerminal().stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Intent intent = new Intent(MainActivity.this, LibterminalService.class);
			intent.setAction("off");
			startService(intent);

			unbindService(mConnection);
		}
	}

	@Override
	public void onBackPressed() {
		if (mDrawer.isDrawerOpen(GravityCompat.START)) {
			mDrawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		FragmentManager fm = getSupportFragmentManager();
		if (id == R.id.action_nodes) {
			mLibterminalFragment = LibterminalFragment.newInstance(libterminalService);
			fm.beginTransaction().replace(R.id.content_main,
				mLibterminalFragment).commit();
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		int id = item.getItemId();
		FragmentManager fm = getSupportFragmentManager();

		switch (id) {
			case R.id.nav_custom_executor:
				break;
			case R.id.nav_player_executor:
				if (libterminalService == null) {
					Toast.makeText(MainActivity.this,
						"No esta la terminal enlazada",
						Toast.LENGTH_SHORT).show();
					break;
				}
				if(!libterminalService.getTerminal().isUp()) {
					Toast.makeText(getApplicationContext(),
						getString(R.string.libterminal_not_up),
						Toast.LENGTH_LONG).show();
					break;
				}
				PlayerExecutorFragment playerExecutorFragment =
					PlayerExecutorFragment.newInstance(libterminalService);

				fm.beginTransaction().replace(R.id.content_main,
					playerExecutorFragment).commit();
				break;
		}

		mDrawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	public void receiveEvent(final Event event) {
	}


	private final class MainActivityConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(final ComponentName componentName, final IBinder iBinder) {
			LibterminalService.LocalBinder binder = (LibterminalService.LocalBinder) iBinder;
			libterminalService = (LibterminalService) binder.getService();
			mLibterminalFragment.setLibterminalService(libterminalService);
			Toast.makeText(MainActivity.this, "Se ha enlazado con la terminal", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onServiceDisconnected(final ComponentName componentName) {
			try {
				libterminalService.getTerminal().stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
			libterminalService = null;
			Toast.makeText(MainActivity.this, "Se ha desenlazado de la terminal", Toast.LENGTH_LONG).show();
		}
	}
}
