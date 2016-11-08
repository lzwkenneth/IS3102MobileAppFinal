package kenneth.jf.verificationapp;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import kenneth.jf.verificationapp.ConnectionInformation;
import kenneth.jf.verificationapp.Event;
import kenneth.jf.verificationapp.EventListObject;
import kenneth.jf.verificationapp.R;
import kenneth.jf.verificationapp.dashboard;

import static android.view.View.GONE;
import static kenneth.jf.verificationapp.R.id.emptytv;
import static kenneth.jf.verificationapp.R.layout.test;


/**
 * Created by User on 28/10/2016.
 */


// After payment is confirmed and the tickets are supposed to show the qr codes from the backend
public class viewTicketList extends Fragment {
    View myView;
    ListView lv;
    ArrayList<Event> list = new ArrayList<>();
    ProgressDialog progressDialog;
    private RestTemplate restTemplate = ConnectionInformation.getInstance().getRestTemplate();
    private String url = ConnectionInformation.getInstance().getUrl();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.purchased_ticketlist, container, false);
        progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Retrieving the Tickets...");
        progressDialog.show();
        try {
            new viewAllEvents().execute().get();
         /*   for(int i=0;i<list.size();i++){
                System.err.println("LISTVIEW: "+ list.size());
            }*/

            lv = (ListView) myView.findViewById(R.id.purchasedTixList);
            TextView fuckJF = (TextView) myView.findViewById(emptytv);
            if (list.size() > 0) {
                fuckJF.setVisibility(GONE);
            }
            // TicketObject[] arr = (TicketObject[]) list.toArray();
            System.out.println("LIST SIZE " + list.size());
            lv.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, list));
            lv.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Event o = (Event) lv.getItemAtPosition(position);
                    Long qr = o.getCode();
                    Intent intent = new Intent(getActivity(), dashboard.class);
                    intent.putExtra("key2", "showQRcode");
                    intent.putExtra("qrcode", String.valueOf(qr));
                    startActivity(intent);
                }
            });

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return myView;
    }

    private class viewAllEvents extends AsyncTask<Void, Void, String> {


        protected String doInBackground(Void... params) {
            Log.d("TAG", "DO IN BACKGROUND");
            try {

                HttpEntity<String> request2 = new HttpEntity<String>(ConnectionInformation.getInstance().getHeaders());
                Log.d("TAGGGGGGGGREQUEST", ConnectionInformation.getInstance().getHeaders().getAccept().toString());
                //CHANGE
                String url2 = "https://" + url + "/tixVerifyAllEvents";

                Log.d("TAG", "BEFORE VERIFYING" + restTemplate.getMessageConverters().toString());
                Log.d("TAG", request2.toString());
                // Log.d("TAG",request2.getBody());
                ResponseEntity<EventListObject[]> responseEntity = restTemplate.exchange(url2, HttpMethod.GET, request2, EventListObject[].class);
                Date newDate = new Date();
                for (EventListObject m : responseEntity.getBody()) {
                    Event e = new Event();
                    e.setName(m.getEventName());
                    e.setCode(m.getId());
                    e.setSelected(false);
                    list.add(e);
                    //return list
                    Log.d("loopforeventlistobject", m.toString());
                }


            } catch (Exception e) {
                Log.e("TAG", e.getMessage(), e);
            }

            return null;
        }

        protected void onPostExecute(String greeting) {
            progressDialog.dismiss();
        }
    }


}
