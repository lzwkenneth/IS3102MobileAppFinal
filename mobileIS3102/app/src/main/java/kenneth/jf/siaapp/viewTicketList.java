package kenneth.jf.siaapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.AdapterView.OnItemSelectedListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.paypal.android.sdk.cy.i;
import static kenneth.jf.siaapp.R.id.textView;
import static kenneth.jf.siaapp.R.id.ticketName;
import static kenneth.jf.siaapp.R.layout.ticket_list;


/**
 * View ticket list from Ticket.xml
 */

public class viewTicketList extends Fragment {
    View myView;
    private RestTemplate restTemplate = ConnectionInformation.getInstance().getRestTemplate();
    private String url = ConnectionInformation.getInstance().getUrl();
    ArrayList<Ticket> TicketList = new ArrayList<Ticket>();
    ArrayList<Ticket> ticketSelected = new ArrayList<>();
    FragmentManager fragmentManager = getFragmentManager();
    String checkTixResponse;
    int counter = 0;
    String spinnerSelected;
    ArrayList<String> spinList;
    ProgressDialog progressDialog;
    SweetAlertDialog errorAlert;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(ticket_list, container, false);

        progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Retrieving the Tickets...");
        progressDialog.show();

        new viewTicketList.viewAllTickets().execute();
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                    }
                }, 700);


        myView = inflater.inflate(ticket_list, container, false);
        Button proceedToCheckOut = (Button) myView.findViewById(R.id.proceedToCheckOut);
        proceedToCheckOut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                double sum = 0.0;
                Spinner spin = (Spinner) myView.findViewById(R.id.spinnerTicketList);

                spinList = new ArrayList<>();
                TicketList = dataAdapter.getTicketListOut();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < TicketList.size(); i++) {
                    /*if ( TicketList.get(i).getNumTix() == 0){
                        continue;
                    }*/
                    //spinnerSelected =
                    //  System.out.println("kennnnnnnn" + dataAdapter.toString());
                    //  System.out.println("kennnnnnnn" + dataAdapter.getSpinnerItem(i).toString());
                    // System.out.println("kennnnnnnn" + dataAdapter.getView);
                    //   spinnerSelected = dataAdapter.getSpinnerItem(i);
                    //  spinList.add(spinnerSelected);
                    // System.out.println("DEBUG! " + spinnerSelected.toString());
                    //TicketList.get(i).setNumTix(Integer.valueOf(spinnerSelected));
                    //ticketSelected.add(TicketList.get(i));
                    System.out.println(TicketList.get(i).getName() + " " + TicketList.get(i).getNumTix());
                    if ( TicketList.get(i).getNumTix() != 0) {
                        sb.append(TicketList.get(i).getNumTix() + " " + TicketList.get(i).getName() + " ( $" + TicketList.get(i).getPrice() + " )");
                        sb.append("\n");
                    }
                    sum += (Double.valueOf(TicketList.get(i).getPrice() * TicketList.get(i).getNumTix()));

                }

                if (sum < 0.01) {
                    new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE).setTitleText("Error!")
                            .setContentText("You have not selected any tickets!")
                            .show();

                    return;
                }

                //NEED TO CHECK IF TICKETSSSSSSS are available
                try {
                    System.out.println("REACHED CHECKING OF TICKETS");
                    new checkTix().execute().get();
                    System.out.println("CHECK TICKET RESPONSE: " + checkTixResponse);
                    if (checkTixResponse.equals("\"\"")) {
                        System.out.println("OK VERIFIED");
                    } else {
                        String[] arr = checkTixResponse.split("\\.");
                        arr[0] = arr[0].substring(1);

                        //showDialog(getActivity(), "Insufficient Tickets Left", checkTixResponse, "Yes", "No").show();
                        showDialog(getActivity(), "Insufficient Tickets Left", arr[0], "Continue", "Back To Events List").show();
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(getActivity(), dashboard.class);
                intent.putExtra("key2", "ticketSum");
                intent.putExtra("price", String.valueOf(sum));
                intent.putExtra("summary",sb.toString() + "\n\n\n" + "Total - $SGD " + sum);
                System.err.println("ARRAYLIST: " + TicketList.size());
                startActivity(intent);
                ConnectionInformation.getInstance().setTicketList(TicketList);
                ConnectionInformation.getInstance().setNumList(spinList);


                System.out.println("TOTAL PRICE IS      " + sum);
                Toast.makeText(getActivity(), "Checking out..." + sum, Toast.LENGTH_SHORT).show();


                // intent.putParcelableArrayListExtra();

            }
        });


        return myView;
    }

    private AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                //Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                //Intent intent = new Intent(Intent.ACTION_VIEW, uri);

            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                goBackEventlist();
            }
        });
        return downloadDialog.show();
    }

    private void goBackEventlist() {
        Intent intent = new Intent(getActivity(), dashboard.class);
        intent.putExtra("key2", "goToEventList");
        startActivity(intent);
    }


    viewTicketList.MyCustomAdapter dataAdapter = null;


    private class viewAllTickets extends AsyncTask<Void, Void, String> {


        protected String doInBackground(Void... params) {
            Log.d("TAG", "DO IN BACKGROUND");
            try {
                JSONObject request = new JSONObject();
                //ticket ID
                String value = getActivity().getIntent().getStringExtra("eventId");
                request.put("eventId", value);
                System.out.println("event ID OF VIEW AN Ticket in ticketShowInfo: " + value);


                HttpEntity<String> request2 = new HttpEntity<String>(request.toString(), ConnectionInformation.getInstance().getHeaders());
                Log.d("TAGGGGGGGGREQUEST", ConnectionInformation.getInstance().getHeaders().getAccept().toString());
                String url2 = "https://" + url + "/tixViewEventCat";

                Log.d("TAG", "BEFORE VERIFYING" + restTemplate.getMessageConverters().toString());
                Log.d("TAG", request2.toString());
                // Log.d("TAG",request2.getBody());
                ResponseEntity<TicketListObject[]> responseEntity = restTemplate.exchange(url2, HttpMethod.POST, request2, TicketListObject[].class);
                for (TicketListObject t : responseEntity.getBody()) {

                }
                for (TicketListObject m : responseEntity.getBody()) {
                    Ticket e = new Ticket();
                    e.setName(m.getTicketName());
                    e.setCode(m.getId());
                    e.setPrice(m.getPrice());
                    e.setSelected(false);
                    TicketList.add(e);
                    //return list
                    Log.d("loopforticketlistobject", m.toString());
                }

                Collections.sort(TicketList, new Comparator<Ticket>() {
                    public int compare(Ticket s1, Ticket s2) {
                        System.out.println(s1.getName());
                        return (s1.getName().compareTo(s2.getName()));
                    }
                });
            } catch (Exception e) {
                Log.e("TAG", e.getMessage(), e);
            }

            return null;
        }


        protected void onPostExecute(String greeting) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            displayListView();
            progressDialog.dismiss();

            Log.d("TAG", "DO POST EXECUTE");
        }

    }

    //Ticket LISTING

    private void displayListView() {
        //Array list of Tickets

        //create an ArrayAdaptar from the String Array
        // TicketList = list;
        System.out.println("Size: " + TicketList.size());
        dataAdapter = new viewTicketList.MyCustomAdapter(this.getActivity(), R.layout.ticket_info, TicketList);
        ListView listView = (ListView) myView.findViewById(R.id.listViewTicket);
        TextView txv = (TextView) myView.findViewById(R.id.emptyv);
        listView.setEmptyView(txv);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);
        Collections.sort(TicketList, new Comparator<Ticket>() {
            public int compare(Ticket s1, Ticket s2) {
                System.out.println(s1.getName());
                return (s1.getName().compareTo(s2.getName()));
            }
        });
        dataAdapter.notifyDataSetChanged();


        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("i is " + i);
                System.out.println("l is " + l);
                System.out.println("FINAL" + view.toString());
                System.out.println("FINALFUCKINGLY" + view.getTag().toString());
                System.out.println("FINAL2" + adapterView.getTag().toString());
                System.out.println("FINAL3" + adapterView.getSelectedItem().toString());


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                System.out.println("FUCK YOU");
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                    /*for(Spinner s : spinners){
                        // s.getSelectedItem()
                    }*/

                // When clicked, show a toast with the TextView text

                Ticket Ticket = (Ticket) parent.getItemAtPosition(position);

                Toast.makeText(getActivity(),
                        "Clicked on Row: " + Ticket.getName(),
                        Toast.LENGTH_LONG).show();
            }
        });

    }

    private class MyCustomAdapter extends ArrayAdapter<Ticket> {

        private ArrayList<Ticket> TicketList;
        Set<ViewHolder> holderz = new HashSet<>();
        viewTicketList.MyCustomAdapter.ViewHolder holder;


        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<Ticket> TicketList) {
            super(context, textViewResourceId, TicketList);


            this.TicketList = new ArrayList<>();
            // System.out.println("jinfuckran");
            for (Ticket tix : TicketList) {
                this.TicketList.add(tix);
            }
            Collections.sort(this.TicketList, new Comparator<Ticket>() {
                public int compare(Ticket s1, Ticket s2) {
                    System.out.println(s1.getName());
                    return (s1.getName().compareTo(s2.getName()));
                }
            });
            System.out.println("jinfuck" + this.TicketList.size());

            // System.out.println("jinfuckranagain");
        }

        private class ViewHolder {
            TextView ticketName;
            TextView code2;
            // CheckBox name;
            Spinner spinner;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            Log.v("ConvertView", String.valueOf(position));
            final Ticket Ticket = TicketList.get(position);
            System.out.println("DEBUG" + Ticket.getName() + "  " + Ticket.getNumTix() + Ticket.toString());

            if (convertView == null) {
                System.out.println("POS" + "    " + position + "   " + parent.getChildCount() + "    " + parent.toString());
                counter++;
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.ticket_info, null);
                holder = new viewTicketList.MyCustomAdapter.ViewHolder();
                holder.ticketName = (TextView) convertView.findViewById(ticketName);
                holder.ticketName.setTextSize(18);
                holder.ticketName.setTypeface(null, Typeface.BOLD);
                holder.code2 = (TextView) convertView.findViewById(R.id.code2);
                holder.code2.setTextSize(17);
                // holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
                holder.spinner = (Spinner) convertView.findViewById(R.id.spinnerTicketList);
                holder.spinner.setDropDownWidth(155);


                try {
                    Field popup = Spinner.class.getDeclaredField("mPopup");
                    popup.setAccessible(true);

                    // Get private mPopup member variable and try cast to ListPopupWindow
                    android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(holder.spinner);

                    // Set popupWindow height to 500px
                    popupWindow.setHeight(1500);
                } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
                    // silently fail...
                }


                //  holder.spinner.setTag(Integer.valueOf(holder.ticketName.toString()));
                //Ticket ttt = TicketList.get(position);
                holder.spinner.setTag(position);
                System.out.println("TTTTTT" + holder.ticketName.getText().toString() + "PPP");
                if (holder.ticketName != null && holder.ticketName.getText() != null && holder.ticketName.getText().toString() != null
                        && holder.spinner != null && holder.spinner.getSelectedItem() != null) {
                    System.out.println("adding holder:" + holder.ticketName.getText().toString() + "T  " + holder.toString());
                    holderz.add(holder);
                }

                //change to spinner
                convertView.setTag(holder);

                /*holder.name.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v ;
                        Ticket Ticket = (Ticket) cb.getTag();
                        Toast.makeText(getActivity(),
                                "Clicked on Checkbox: " + cb.getText() +
                                        " is " + cb.isChecked(),
                                Toast.LENGTH_LONG).show();
                        Ticket.setSelected(cb.isChecked());

                        //retrieve Ticket Details From Backend

                    }
                });
*/

            } else {
                holder = (viewTicketList.MyCustomAdapter.ViewHolder) convertView.getTag();

               /* holder.spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        System.out.println(holder.spinner.getSelectedItemPosition() + " "+ holder.spinner.getSelectedItemId() + " "  +"ASSSSSIGN" + holder.spinner.getSelectedItem() + " to " + Ticket.getName().toString());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });*/

                // System.out.println(holder.spinner.getOnItemSelectedListener().toString());
                Map<Ticket, Boolean> fuckthisshit = new HashMap<>();
                for (Ticket t : TicketList) {
                    fuckthisshit.put(t, true);
                }
                for (ViewHolder hh : holderz) {
                    ////  System.out.println("ticket is" + hh.ticketName.toString());
                    // System.out.println("ticket is" + hh.ticketName.getText().toString());
                    // System.out.println("thig is " + hh.spinner.getSelectedItem().toString());
                    System.out.println("==================");

                    for (Ticket tix : TicketList) {
                        //if ( tix.getName().equals(hh.ticketName.getText().toString())&& !hh.spinner.getSelectedItem().toString().equals("0")){
                        if (tix.getName().equals(hh.ticketName.getText().toString())) {
                            if (fuckthisshit.get(tix)) {//always have duplicate
                                System.out.println("Assign" + hh.ticketName.getText().toString() + " with number of " + hh.spinner.getSelectedItem().toString() + "CurrentPS" + position);
                                tix.setNumTix(Integer.valueOf(hh.spinner.getSelectedItem().toString()));
                                if (Integer.valueOf(hh.spinner.getSelectedItem().toString()) > 0) {
                                    //System.out.println("assigned the value of " + Integer.valueOf(hh.spinner.getSelectedItem().toString()) + " and not assigning anymore");
                                    // assign = false;
                                    fuckthisshit.put(tix, false);
                                }
                                // assigned = Integer.valueOf(hh.spinner.getSelectedItem().toString());
                            }
                        }
                    }
                }

                //this.TicketList.get(position).setNumTix(Integer.valueOf(holder.spinner.getSelectedItem().toString()));

            }


            holder.ticketName.setText(Ticket.getName());
            holder.code2.setText(" ( $" + String.valueOf(Ticket.getPrice()) + " )");
            //holder.name.setChecked(Ticket.isSelected());
            holder.code2.setTag(Ticket);

            return convertView;

        }

        public ArrayList<Ticket> getTicketListOut() {
            return this.TicketList;
        }

        public String getSpinnerItem(int position) {
            System.out.println("---------------------");
            System.out.println("---------------------");
            this.holder.code2.getTag();
            //  System.out.println(this.holder.spinner.getTag(position).getSelectedItem().toString());

            System.out.println("---------------------");
            System.out.println("---------------------");
            // System.out.println("DEBUG1" + holder.spinner.getAdapter().getItem(position).toString());
            //ken//
            // System.out.println("kendebug" + holderz.get(position).spinner.getSelectedItem().toString());
            // return holderz.get(position).spinner.getSelectedItem().toString();
            return "10";
            // return holder.spinner.getItemAtPosition(position).toString();
        }


    }

  /*  private void checkButtonClick() {

        Button myButton = (Button) myView.findViewById(R.id.findSelected1);
        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                StringBuffer responseText = new StringBuffer();
                responseText.append("The following were selected...\n");
                //this list shows the Tickets that are selected
                ArrayList<Ticket> TicketList = dataAdapter.TicketList;
                for(int i=0;i<TicketList.size();i++){
                    Ticket Ticket = TicketList.get(i);

                    if(Ticket.isSelected()){
                        responseText.append("\n" + Ticket.getName());
                    }
                }
                Toast.makeText(getActivity(),
                        responseText, Toast.LENGTH_LONG).show();
            }
        });
    }*/


    private class checkTix extends AsyncTask<Void, Void, String> {

        protected String doInBackground(Void... params) {
            Log.d("TAG", "DO IN BACKGROUND CHECKING TICKET AMOUNT");
            try {
                JSONArray jar = new JSONArray();

                for (int i = 0; i < TicketList.size(); i++) {
                    JSONObject obj = new JSONObject();
                    obj.put("categoryId", TicketList.get(i).getCode());
                    obj.put("numTickets", TicketList.get(i).getNumTix());
                    System.out.println("CODE: " + TicketList.get(i).getCode() + "NUM TIX: " + TicketList.get(i).getNumTix());
                    jar.put(obj);
                }

                /*obj.put("categoryId", 1);
                obj.put("numTickets", 100000);
                jar.put(obj);
                JSONObject obj2 = new JSONObject();
                obj2.put("categoryId", 2);
                obj2.put("numTickets", 100000);
                jar.put(obj2);*/

                HttpEntity<String> request2 = new HttpEntity<String>(jar.toString(), ConnectionInformation.getInstance().getHeaders());
                Log.d("TAGGGGGGGGREQUEST", ConnectionInformation.getInstance().getHeaders().getAccept().toString());
                String url2 = "https://" + url + "/tixCheckTix";

                Log.d("TAG", "BEFORE VERIFYING" + restTemplate.getMessageConverters().toString());
                Log.d("TAG", request2.toString());
                // Log.d("TAG",request2.getBody());
                ResponseEntity<String> responseEntity = restTemplate.exchange(url2, HttpMethod.POST, request2, String.class);

                checkTixResponse = responseEntity.getBody().toString();


            } catch (Exception e) {
                Log.e("TAG", e.getMessage(), e);
            }

            return null;
        }


        protected void onPostExecute(String greeting) {
            Log.d("TAG", "DO POST EXECUTE");
        }

    }
}
