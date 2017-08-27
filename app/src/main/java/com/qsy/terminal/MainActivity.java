package com.qsy.terminal;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.qsy.terminal.executors.PlayerExecutorFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
	private ActionBarDrawerToggle mToggle;
	private DrawerLayout mDrawer;
	private Toolbar mToolbar;
	private NavigationView mNavigationView;

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
				fm.beginTransaction().replace(R.id.content_main, new PlayerExecutorFragment()).commit();
				break;
		}

		mDrawer.closeDrawer(GravityCompat.START);
		return true;
	}
}
