package kenneth.jf.verificationapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static kenneth.jf.verificationapp.R.id.purchasedTixList;
import static kenneth.jf.verificationapp.R.id.viewTicketList;
import static kenneth.jf.verificationapp.R.layout.test;


public class dashboard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //POWER SAVER
    private static String QRresult;
    private static final String TAG = "SIA APP";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 1;
    private RestTemplate restTemplate = ConnectionInformation.getInstance().getRestTemplate();
    private String url = ConnectionInformation.getInstance().getUrl();
    public static final String PUBLISHABLE_KEY = "pk_test_zeyJXfY34INxorNSshxu83Q7";
    FragmentManager fragmentManager = getFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        fragmentManager.beginTransaction().replace(R.id.contentFrame, new viewTicketList()).commit();
        Toast.makeText(this, "Scan For Discount Using QR Scanner", Toast.LENGTH_LONG).show();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Nothing here yet!!!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("key2").equals("showQRcode")) {
                //from paymentSummary
                fragmentManager.beginTransaction().replace(R.id.contentFrame, new QR_Scanner()).commit();
            }
        }
    }

    //  DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    // drawer.closeDrawer(GravityCompat.START);
    // return true;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.home) {
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new viewTicketList()).commit();
            Toast.makeText(this, "Back Home", Toast.LENGTH_LONG).show();


            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            new doLogout().execute();

            Intent intent = new Intent(this, login.class);
            this.startActivity(intent);
        }


        return super.onOptionsItemSelected(item);


    /*    switch (item.getItemId()) {
            case R.id.home:
                return false; //The fragment will take care of it
            default:
                break;
        }*/
    }


    Bundle result = null;

    public void saveData(int id, Bundle data) {
        // based on the id you'll know which fragment is trying to save data(see below)
        // the Bundle will hold the data
        result = new Bundle(data);

    }

    public Bundle getSavedData() {
        // here you'll save the data previously retrieved from the fragments and
        // return it in a Bundle
        return result;
    }

    //paypal
    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private class doLogout extends AsyncTask<Void, Void, String> {

        protected String doInBackground(Void... params) {
            Log.d("TAG", "DO IN BACKGROUND");
            try {

                HttpEntity<String> request2 = new HttpEntity<String>(ConnectionInformation.getInstance().getHeaders());
                String url2 = "https://" + url + "/logout";
                Log.d("TAGTOSTRING ", request2.toString());
                ResponseEntity<Object> responseEntity = restTemplate.exchange(url2, HttpMethod.POST, request2, Object.class);
                Log.d("TAGGGGGGGGREQUEST", responseEntity.getStatusCode().toString());
                if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                    ConnectionInformation.getInstance().setIsAuthenticated(false);
                    Log.d("TAG", "Logged out inside async");
                }

            } catch (Exception e) {
                Log.e("TAG", e.getMessage(), e);
            }

            return null;
        }


        protected void onPostExecute(String greeting) {

            Log.d("TAG", "DO POST EXECUTE");
            if (ConnectionInformation.getInstance().getAuthenticated()) {
                Log.d("TAG", "SERVER LOG OUT DID NOT WORK");
            } else {
                Log.d("TAG", "LOG OUT ON SERVER OK");
            }
        }

    }

}
