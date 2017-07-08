package com.qsy.qsyapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
        private ActionBarDrawerToggle toggle;
        private DrawerLayout drawer;
        private Toolbar toolbar;
        private NavigationView navigationView;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
                toolbar = (Toolbar) findViewById(R.id.toolbar);
                // usamos toolbar como action bar en este caso, esto va a permitir
                // dibujar el iconito de 3 rayas horizontales
                setSupportActionBar(toolbar);

                drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                toggle = new ActionBarDrawerToggle(
                        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

                drawer.setDrawerListener(toggle);
                toggle.syncState();

                navigationView = (NavigationView) findViewById(R.id.nav_view);
                navigationView.setNavigationItemSelectedListener(this);
        }

        @Override
        public void onBackPressed() {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                        drawer.closeDrawer(GravityCompat.START);
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

                if (id == R.id.nav_item_uno) {
                        // Por ahi aca vamos a querer cambiar a un Activity nueva
                        // O simplemente cambiar el layout, ni idea
                } else if (id == R.id.nav_item_dos) {

                }

                drawer.closeDrawer(GravityCompat.START);
                return true;
        }
}
