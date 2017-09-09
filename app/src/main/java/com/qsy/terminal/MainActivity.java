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

import com.qsy.terminal.fragments.libterminal.LibterminalFragment;
import com.qsy.terminal.fragments.executors.PlayerExecutorFragment;
import com.qsy.terminal.services.LibterminalService;

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
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);

		mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		mToggle = new ActionBarDrawerToggle(
			this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

		mDrawer.setDrawerListener(mToggle);
		mToggle.syncState();

		mNavigationView = (NavigationView) findViewById(R.id.nav_view);
		mNavigationView.setNavigationItemSelectedListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (libterminalService != null) {
			try {
				libterminalService.getTerminal().stop();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			unbindService(mConnection);
		}
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
		// TODO: aca hay un pequeño problema. Llamar a stop en la terminal suele tardar mas
		// de 500ms. El problema es que estamos adentro de un listener, y por las reglas de
		// android no se puede hacer operaciones(dentro de los listeners y esas cosas) de mas
		// de 500ms ya que traba a el thread de la UI. Se podria usar un background task
		// que se encargue de cerrar todos los threads y terminar la terminal, pero nose si
		// es lo mas apropiado.
		if (libterminalService != null) {
			try {
				libterminalService.getTerminal().stop();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public void searchNodesListener(View view) {
		if (libterminalService != null) {
			libterminalService.getTerminal().startNodesSearch();
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
				if (libterminalService == null) {
					Toast.makeText(MainActivity.this,
						"No esta la terminal enlazada",
						Toast.LENGTH_SHORT).show();
					break;
				}
				PlayerExecutorFragment playerExecutorFragment = new PlayerExecutorFragment(libterminalService);
				fm.beginTransaction().replace(R.id.content_main,
					playerExecutorFragment).commit();
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
				switch (event.getEventType()) {
					case newNode:
						Node newNode = (Node) event.getContent();
						Toast.makeText(
							getApplicationContext(),
							getString(R.string.newNode, newNode.getNodeId()),
							Toast.LENGTH_SHORT)
							.show();
						break;
					case disconnectedNode:
						Node disconnectedNode = (Node) event.getContent();
						Toast.makeText(
							getApplicationContext(),
							getString(R.string.disconnectedNode, disconnectedNode.getNodeId()),
							Toast.LENGTH_SHORT)
							.show();

				}
			}
		});
	}

	private final class MyConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(final ComponentName componentName, final IBinder iBinder) {
			LibterminalService.LocalBinder binder = (LibterminalService.LocalBinder) iBinder;
			libterminalService = (LibterminalService) binder.getService();
			libterminalService.getTerminal().addListener(MainActivity.this);
			Toast.makeText(MainActivity.this, "Se ha enlazado con la terminal", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onServiceDisconnected(final ComponentName componentName) {
			try {
				libterminalService.getTerminal().stop();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			libterminalService.getTerminal().removeListener(MainActivity.this);
			libterminalService = null;
			Toast.makeText(MainActivity.this, "Se ha desenlazado de la terminal", Toast.LENGTH_LONG).show();
		}
	}
}
