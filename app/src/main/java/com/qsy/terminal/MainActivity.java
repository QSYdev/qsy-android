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

import com.qsy.terminal.fragments.DebugFragment;
import com.qsy.terminal.fragments.executors.CustomExecutorFragment;
import com.qsy.terminal.fragments.libterminal.LibterminalFragment;
import com.qsy.terminal.fragments.executors.PlayerExecutorFragment;
import com.qsy.terminal.services.LibterminalService;
import com.qsy.terminal.utils.QSYUtils;

import java.util.ArrayList;

import libterminal.lib.node.Node;
import libterminal.patterns.observer.Event;
import libterminal.patterns.observer.EventListener;
import libterminal.patterns.visitor.EventHandler;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
	EventListener, DebugFragment.OnFragmentInteractionListener, LibterminalFragment.OnFragmentInteractionListener {
	private ActionBarDrawerToggle mToggle;
	private DrawerLayout mDrawer;
	private Toolbar mToolbar;
	private NavigationView mNavigationView;

	private MainActivityConnection mConnection = new MainActivityConnection();
	private LibterminalService libterminalService;
	private LibterminalFragment mLibterminalFragment;

	private ArrayList<Node> mNodes = new ArrayList<Node>();

	private final EventHandler eventHandler = new InternalEventHandler();

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
			libterminalService.getTerminal().removeListener(this);

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

		MenuItem menuItem = menu.findItem(R.id.nodes_counter);
		if((libterminalService != null) && (libterminalService.getTerminal().isUp())) {
			menuItem.setTitle(Integer.valueOf(libterminalService.getTerminal().connectedNodesAmount()).toString());
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		int id = item.getItemId();
		FragmentManager fm = getSupportFragmentManager();

		switch (id) {
			case R.id.node_configuration:
				if (libterminalService == null) {
					mLibterminalFragment = new LibterminalFragment();
					fm.beginTransaction().replace(R.id.content_main, mLibterminalFragment).
						addToBackStack("LibterminalFragment").commit();
					break;
				}
				mLibterminalFragment = LibterminalFragment.newInstance(libterminalService.getTerminal());
				fm.beginTransaction().replace(R.id.content_main, mLibterminalFragment).
					addToBackStack("LibterminalFragment").commit();
				break;
			case R.id.nav_custom_executor:
				if (libterminalService == null) {
					Toast.makeText(MainActivity.this,
						"No esta la terminal enlazada",
						Toast.LENGTH_SHORT).show();
					break;
				}
				if (!libterminalService.getTerminal().isUp()) {
					Toast.makeText(getApplicationContext(),
						getString(R.string.libterminal_not_up),
						Toast.LENGTH_LONG).show();
					break;
				}
				CustomExecutorFragment cef = CustomExecutorFragment.newInstance(
					libterminalService.getTerminal());
				fm.beginTransaction().replace(R.id.content_main,
					cef).addToBackStack("CustomExecutorFragment").commit();
				break;
			case R.id.nav_player_executor:
				if (libterminalService == null) {
					Toast.makeText(MainActivity.this,
						"No esta la terminal enlazada",
						Toast.LENGTH_SHORT).show();
					break;
				}
				if (!libterminalService.getTerminal().isUp()) {
					Toast.makeText(getApplicationContext(),
						getString(R.string.libterminal_not_up),
						Toast.LENGTH_LONG).show();
					break;
				}
				PlayerExecutorFragment playerExecutorFragment =
					PlayerExecutorFragment.newInstance(libterminalService.getTerminal());

				fm.beginTransaction().replace(R.id.content_main,
					playerExecutorFragment).addToBackStack("PlayerExecutorFragment").commit();
				break;
			case R.id.debug:
				if (libterminalService == null) {
					Toast.makeText(MainActivity.this,
						"No esta la terminal enlazada",
						Toast.LENGTH_SHORT).show();
					break;
				}
				if (!libterminalService.getTerminal().isUp()) {
					Toast.makeText(getApplicationContext(),
						getString(R.string.libterminal_not_up),
						Toast.LENGTH_LONG).show();
					break;
				}
				DebugFragment df = DebugFragment.newInstance(libterminalService.getTerminal());
				fm.beginTransaction().replace(R.id.content_main,
					df).addToBackStack("DebugFragment").commit();
				break;
		}

		mDrawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	public void receiveEvent(final Event event) {
		event.acceptHandler(eventHandler);
	}

	@Override
	public void terminalOff() {
		mNodes = new ArrayList<Node>();
		supportInvalidateOptionsMenu();
	}

	private final class InternalEventHandler extends EventHandler {

		@Override
		public void handle(final Event.NewNodeEvent newNodeEvent) {
			super.handle(newNodeEvent);
			final Node newNode = newNodeEvent.getNode();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mNodes.add(newNode);
					supportInvalidateOptionsMenu();
				}
			});
		}

		@Override
		public void handle(final Event.DisconnectedNodeEvent disconnectedNodeEvent) {
			super.handle(disconnectedNodeEvent);
			final Node disconnectedNode = disconnectedNodeEvent.getNode();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mNodes.remove(disconnectedNode);
					supportInvalidateOptionsMenu();
				}
			});
		}
	}

	public ArrayList<Node> getNodes() {
		return mNodes;
	}

	private final class MainActivityConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(final ComponentName componentName, final IBinder iBinder) {
			LibterminalService.LocalBinder binder = (LibterminalService.LocalBinder) iBinder;
			libterminalService = (LibterminalService) binder.getService();
			mLibterminalFragment.setTerminalAPI(libterminalService.getTerminal());
			libterminalService.getTerminal().addListener(MainActivity.this);
		}

		@Override
		public void onServiceDisconnected(final ComponentName componentName) {
			try {
				libterminalService.getTerminal().stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
			libterminalService.getTerminal().removeListener(MainActivity.this);
			libterminalService = null;
		}
	}
}
