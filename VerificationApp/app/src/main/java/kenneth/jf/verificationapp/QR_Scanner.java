package kenneth.jf.verificationapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kenneth.jf.verificationapp.R;

import static android.R.attr.key;
import static android.app.Activity.RESULT_OK;

import static kenneth.jf.verificationapp.R.id.discountView;
import static kenneth.jf.verificationapp.R.id.scanner;
import static kenneth.jf.verificationapp.R.layout.qr_scanning;


public class QR_Scanner extends Fragment {
    //POWER SAVER
    private static String QRresult;
    private Long eventId;
    private String qruuid;
    private String categoryNameRedeemed;
    TextView discountView;
    Map<String,Integer> mapz;
    String dText = "";

    View myView;
    private RestTemplate restTemplate = ConnectionInformation.getInstance().getRestTemplate();
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private String url = ConnectionInformation.getInstance().getUrl();
    FragmentManager fragmentManager = getFragmentManager();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(qr_scanning, container, false);
        String qrcode = getActivity().getIntent().getStringExtra("qrcode");
        System.out.println("get something" + qrcode);
        eventId = Long.valueOf(qrcode);
        Button scanner = (Button) myView.findViewById(R.id.scanner);
        discountView = ( TextView) myView.findViewById(R.id.discountView);
        discountView.setTextSize(17);
        scanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanQR(v);
            }
        });
         mapz = new HashMap<>();

        return myView;
    }

    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            //showDialog(getActivity(), "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    //alert dialog for downloadDialog
    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                qruuid = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                //Toast toast = Toast.makeText(this.getActivity(), "Content:" + discountCode + " Format:" + format, Toast.LENGTH_LONG);
                //toast.show();

                try {
                    dText = discountView.getText().toString();
                    new viewDiscount().execute().get();
                    if ( dText.equals("Not verified")) {
                        discountView.setText(dText);
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Error")
                                .setContentText("Ticket cannnot be verified!")
                                .show();
                    }
                    else{
                        discountView.setText(dText);
                        new SweetAlertDialog(getContext(),SweetAlertDialog.SUCCESS_TYPE).setTitleText("Success!")
                                .setContentText(dText)
                                .show();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }


            }
        }
    }

    private class viewDiscount extends AsyncTask<Void, Void, String> {


        protected String doInBackground(Void... params) {
            Log.d("TAG", "DO IN BACKGROUND");
            try {
                JSONObject request = new JSONObject();
                request.put("eventId",eventId);
                request.put("code", qruuid);
                HttpEntity<String> request2 = new HttpEntity<String>(request.toString(), ConnectionInformation.getInstance().getHeaders());
                String url2 = "https://" + url + "/redeemTicket";

                Log.d("TAG", "BEFORE VERIFYING" + restTemplate.getMessageConverters().toString());
                Log.d("TAG", request2.toString());
                // Log.d("TAG",request2.getBody());
                ResponseEntity<String> responseEntity = restTemplate.exchange(url2, HttpMethod.POST, request2, String.class);

                if (responseEntity.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                    boolean ok = false;
                    dText = ("Not verified");
                    //Store number of redeemed tickets into android storage. //Select from list of events to verify from?
                } else {
                    categoryNameRedeemed = responseEntity.getBody();
/*                    Integer inter = mapz.get(categoryNameRedeemed);
                    if ( inter != null){
                        inter++;
                        mapz.put(categoryNameRedeemed,inter);
                    }
                    for (String key : mapz.keySet()) {
                        if ( dText.indexOf(key) != -1 ){

                        }
                    }*/
                    dText = (categoryNameRedeemed + " ticket redeemed!");
                }
                //  discountObj = responseEntity.getBody();
                //   System.out.println("RESPONSE ENTITY: " + responseEntity.getBody().getDiscountMessage());


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




