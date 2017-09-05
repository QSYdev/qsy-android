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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.qsy.terminal.fragments.LibterminalFragment;
import com.qsy.terminal.fragments.PlayerExecutorFragment;
import com.qsy.terminal.services.LibterminalService;

import java.io.IOException;

import libterminal.lib.node.Node;
import libterminal.patterns.observer.Event;
import libterminal.patterns.observer.EventListener;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, EventListener {
	private ActionBarDrawerToggle mToggle;
	private DrawerLayout mDrawer;
	private Toolbar mToolbar;
	private NavigationView mNavigationView;
	private Button mStartServiceButton;
	private Button mStopServiceButton;
	private Button mSearchNodesButton;

	private MyConnection mConnection = new MyConnection();
	private LibterminalService libterminalService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mToolbar = findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);

		mDrawer = findViewById(R.id.drawer_layout);
		mToggle = new ActionBarDrawerToggle(
				this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

		mDrawer.setDrawerListener(mToggle);
		mToggle.syncState();

		mNavigationView = findViewById(R.id.nav_view);
		mNavigationView.setNavigationItemSelectedListener(this);

	}

	public void startServiceListener(View view) {
		final Intent intent = new Intent(MainActivity.this, LibterminalService.class);
		intent.setAction("on");
		startService(intent);
		bindService(intent, mConnection, 0);
	}

	public void stopServiceListener(View view) {
		final Intent intent = new Intent(MainActivity.this, LibterminalService.class);
		intent.setAction("off");
		startService(intent);
		if (libterminalService != null) {
			//TODO main.close();
		}

	}

	public void searchNodesListener(View view) {
		if (libterminalService != null) {
			libterminalService.getTerminal().searchNodes();
		} else {
			Toast.makeText(MainActivity.this, "Aun no se ha enlazado con la terminal", Toast.LENGTH_LONG).show();
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
		if (id == R.id.action_settings) {
			return true;
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
				if(libterminalService == null) {
					Toast.makeText(MainActivity.this,
						"No esta la terminal enlazada",
						Toast.LENGTH_SHORT);
					break;
				}
				fm.beginTransaction().replace(R.id.content_main,
					new PlayerExecutorFragment()).commit();
				break;
			case R.id.nav_libterminal:
				LibterminalFragment libterminalFragment = new LibterminalFragment(this);
				fm.beginTransaction().replace(R.id.content_main,
					libterminalFragment).commit();
		}

		mDrawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	public void receiveEvent(final Event event) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(event.getEventType() == Event.EventType.newNode) {
					Node node = (Node) event.getContent();
					Toast.makeText(getApplicationContext(),
						"Se conecto el nodo "+node.getNodeId(),
						Toast.LENGTH_SHORT);
				}
			}
		});
	}

	private final class MyConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(final ComponentName componentName, final IBinder iBinder) {
			LibterminalService.LocalBinder binder = (LibterminalService.LocalBinder) iBinder;
			libterminalService = (LibterminalService) binder.getService();
			libterminalService.getTerminal().suscribeToTerminalEvents(MainActivity.this);
			Toast.makeText(MainActivity.this, "Se ha enlazado con la terminal", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onServiceDisconnected(final ComponentName componentName) {
//			libterminalService.getTerminal().removeListener(MainActivity.this);
//			main = null;
//			Toast.makeText(MainActivity.this, "Se ha desenlazado de la terminal", Toast.LENGTH_LONG).show();
		}
	}
}
