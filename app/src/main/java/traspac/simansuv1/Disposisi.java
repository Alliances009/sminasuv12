package traspac.simansuv1;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Disposisi extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {

    private AutoCompleteTextView txtKepada;
    private TextView pengirim,nosurat,jam,sifat,tanggal,idTindakan,idTujuan,txtNamaFile,txtSizeFile;
    private String dispengirim,disnosurat,disjam,dissifat,distanggal,kepada,surat_id,remitten,isi;
    private EditText etRemitten,etIsi;
    private JSONArray result_tujuan_disposisi,result_tindakan_disposisi;
    private ArrayList<String> tujuan_disposisi,tindakan_disposisi;
    private Spinner cboTindakan,spinner_tindakan;
    private DatePickerDialog dateDialog;
    private SimpleDateFormat dateFormat;
    private ImageButton btnSave,btnUpload;
    private int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disposisi);

        pengirim = (TextView) findViewById(R.id.txtPengirim);
        nosurat = (TextView) findViewById(R.id.txtNomorSurat);
        jam  = (TextView) findViewById(R.id.txtJam);
        tanggal = (TextView) findViewById(R.id.txtTanggal);
        sifat = (TextView) findViewById(R.id.txtSifatSurat);
        idTindakan = (TextView) findViewById(R.id.idTindakan);
        idTujuan = (TextView) findViewById(R.id.idTujuan);
        txtNamaFile = (TextView) findViewById(R.id.txtNamaFile);
        txtSizeFile = (TextView) findViewById(R.id.txtFileSize);
        txtKepada = (AutoCompleteTextView) findViewById(R.id.etKepada);
        cboTindakan = (Spinner) findViewById(R.id.cboTindakan);
        etRemitten = (EditText) findViewById(R.id.etRemitten);
        etIsi = (EditText) findViewById(R.id.etIsi);
        btnSave = (ImageButton) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(this);

        btnUpload = (ImageButton) findViewById(R.id.btn_upload);
        btnUpload.setOnClickListener(this);

        Intent intent = getIntent();

        dispengirim = intent.getStringExtra(Config.TAG_DARI);
        disnosurat = intent.getStringExtra(Config.TAG_NOSURAT);
        dissifat = intent.getStringExtra(Config.TAG_SIFAT);
        disjam = intent.getStringExtra(Config.TAG_JAM);
        distanggal = intent.getStringExtra(Config.TAG_TANGGAL);
        distanggal = intent.getStringExtra(Config.TAG_TANGGAL);
        kepada = intent.getStringExtra(Config.TAG_KEPADA);

        txtKepada.setText(kepada);
        pengirim.setText(dispengirim);
        nosurat.setText(disnosurat);
        sifat.setText(dissifat);
        jam.setText(disjam);
        tanggal.setText(distanggal);

        autocompleteTujuanDisposisi();

        spinnerTindakanDisposisi();


        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        setRemitten();

        cboTindakan.setOnItemSelectedListener(this);

        surat_id = "1";
    }


    private void setRemitten() {
        etRemitten.setOnClickListener(this);
        Calendar newCalendar = Calendar.getInstance();
        dateDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar tanggal = Calendar.getInstance();
                        tanggal.set(year,monthOfYear,dayOfMonth);
                        etRemitten.setText(dateFormat.format(tanggal.getTime()));
                    }

                },
                newCalendar.get(Calendar.YEAR),
                newCalendar.get(Calendar.MONTH),
                newCalendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private void spinnerTindakanDisposisi()
    {
        tindakan_disposisi = new ArrayList<String>();
        getDataTindakanDisposisi();
    }

    private void getDataTindakanDisposisi()
    {
        StringRequest request = new StringRequest(Config.URL_GET_TINDAKAN_DISPO,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("Data", "Response: "+response);
                        JSONObject json = null;
                        try {
                            json = new JSONObject(response);
                            result_tindakan_disposisi = json.getJSONArray(Config.TAG_JSON_ARRAY);

                            showDataTindakanDisposisi(result_tindakan_disposisi);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("Data", "onResponse2: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Data", "onResponse3: " + error.getMessage());
                    }
                }
        );
        RequestQueue rq = Volley.newRequestQueue(getApplicationContext());
        rq.add(request);
    }

    private void showDataTindakanDisposisi(JSONArray data)
    {
        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject json = data.getJSONObject(i);
                tindakan_disposisi.add(json.getString(Config.TAG_TINDAKAN));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cboTindakan.setAdapter(new ArrayAdapter<String>(getApplicationContext(),R.layout.list_data,tindakan_disposisi));
    }

    private void autocompleteTujuanDisposisi()
    {
        tujuan_disposisi = new ArrayList<String>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.list_data,R.id.txtList,tujuan_disposisi);
        txtKepada.setAdapter(adapter);
        txtKepada.setOnItemClickListener(this);
        getDataTujuanDisposisi();
    }

    private void getDataTujuanDisposisi()
    {
        StringRequest request = new StringRequest(Config.URL_GET_TUJUAN_DISPO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject json = null;
                        try {
                            json = new JSONObject(response);
                            result_tujuan_disposisi = json.getJSONArray(Config.TAG_JSON_ARRAY);
                            showDataTujuanDisposisi(result_tujuan_disposisi);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );

        RequestQueue rq = Volley.newRequestQueue(getApplicationContext());
        rq.add(request);

    }

    private void showDataTujuanDisposisi(JSONArray data)
    {
        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject json = data.getJSONObject(i);
                String tujuan = json.getString(Config.TAG_JABATAN);
                tujuan_disposisi.add(tujuan);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("file/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            String name = "";
            String size = "";
            Log.d("Data",filePath.toString());
            name = filePath.getLastPathSegment();
            int ext = name.indexOf(".");
            if (ext == -1) {
                Cursor cursor =
                        getContentResolver().query(filePath, null, null, null, null);
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                cursor.moveToFirst();
                name = cursor.getString(nameIndex);
                size = cursor.getString(sizeIndex);
            }
            txtNamaFile.setText(name);
            txtSizeFile.setText(size);
        }
    }

    @Override
    public void onClick(View v)
    {
        if (v == etRemitten) {
            dateDialog.show();
        } else if (v == btnUpload) {
            showFileChooser();
        } else if (v == btnSave) {
            simpanDisposisi();
        }
    }

    private void simpanDisposisi()
    {
        kepada = txtKepada.getText().toString();
        isi = etIsi.getText().toString();
        remitten = etRemitten.getText().toString();


        if (TextUtils.isEmpty(kepada)){
            txtKepada.setError("Harap isi");
        } else if (TextUtils.isEmpty(isi)){
            etIsi.setError("Harap isi");
        } else if (TextUtils.isEmpty(remitten)){
            etRemitten.setError("Harap isi");
        } else {
            StringRequest request = new StringRequest(Request.Method.POST, Config.URL_SIMPAN_DISPOSISI,
                    new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            Log.d("Simpan", response);
                            try {
                                JSONObject json = new JSONObject(response);
                                String success = json.getString(Config.LOGIN_SUCCESS);
                                if (success.equals("true")) {
                                    Config.alertView("Success", "Simpan disposisi", Disposisi.this);
                                /*Intent intent = new Intent(getApplicationContext(),MainMenu.class);
                                startActivity(intent);*/
                                } else {
                                    Config.alertView("Failed", "Simpan disposisi", Disposisi.this);
                                }

                            } catch (JSONException e) {
                                Log.e("Simpan", "JSON " + e.getMessage());
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    }
            ) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    //headers.put("Content-Type","text/html");
                    return headers;
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put(Config.TAG_V_DISPOSISI_ID, "1");
                    params.put(Config.TAG_V_USER_ID_PEMBUAT, "1");
                    params.put(Config.TAG_V_ENTRY_SURAT_ID, surat_id);
                    params.put(Config.TAG_V_KEPADA, kepada);
                    params.put(Config.TAG_V_ISI, isi);
                    params.put(Config.TAG_V_TANGGAL_REMITEN, remitten);
                    params.put(Config.TAG_V_TINDAKAN, cboTindakan.getSelectedItem().toString());
                    params.put(Config.TAG_V_USER_ID_TUJUAN, idTujuan.getText().toString());
                    params.put(Config.TAG_V_FILE_ORIGINAL, "1");
                    params.put(Config.TAG_V_FILE_RENAME, "1");
                    params.put(Config.TAG_V_FILE_SIZE, "1");
                    Log.d("Data", params.toString());
                    return params;
                }


            };

        RequestQueue rq = Volley.newRequestQueue(Disposisi.this);
        rq.add(request);
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        JSONObject data = null;
        try {
            data = (JSONObject) result_tindakan_disposisi.get(position);
            String id_tindakan = data.getString("id");
            idTindakan.setText(id_tindakan);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        JSONObject data = null;
        try {
            data = (JSONObject) result_tujuan_disposisi.get(position);
            String id_tindakan = data.getString("userid");
            idTujuan.setText(id_tindakan);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
