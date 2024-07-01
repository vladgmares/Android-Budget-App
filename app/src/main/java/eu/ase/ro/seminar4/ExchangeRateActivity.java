package eu.ase.ro.seminar4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.concurrent.Callable;

import eu.ase.ro.seminar4.database.Expense;
import eu.ase.ro.seminar4.network.AsyncTaskRunner;
import eu.ase.ro.seminar4.network.Callback;
import eu.ase.ro.seminar4.network.HttpManager;

public class ExchangeRateActivity extends AppCompatActivity {
    public static final String CURRENCY_URL = "https://api.npoint.io/d722476676f9863b9954";
    private final AsyncTaskRunner asyncTaskRunner = new AsyncTaskRunner();


    private Intent intent;
    private TextInputEditText tietAmount;
    private AppCompatButton btnCompute;
    private static Double RATE_DOLLAR = 4.66;
    private static Double RATE_EURO = 4.97;
    private static Double RATE_POUNDS = 5.72;

    private TextView tvResult;

    private Spinner spnCurrency;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_rate);
        initComponents();
        loadCurrencyFromUrl();
    }

    private void loadCurrencyFromUrl() {
        Callable<String> asyncOperation = new HttpManager(CURRENCY_URL);
        Callback<String> mainThreadOperation = getMainThreadOperation();
        asyncTaskRunner.executeAsync(asyncOperation, mainThreadOperation);
    }

    private Callback<String> getMainThreadOperation() {
        return new Callback<String>() {
            @Override
            public List<Expense> runResultOnUiThread(String result) {
                //suntem in ExpenseActivity
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                Log.i("ExchangeRateActivity", result);
                return null;
            }
        };
    }

    private void initComponents() {
        intent = getIntent();

        tietAmount = findViewById(R.id.exchange_tiet_amount_value);
        btnCompute = findViewById(R.id.exchange_btn_compute);
        btnCompute.setOnClickListener(computeExchange());

        tvResult = findViewById(R.id.exchange_tv_result_value);

        spnCurrency = findViewById(R.id.exchange_spn_currencies);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.exchange_currency_values, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spnCurrency.setAdapter(adapter);
    }

    private View.OnClickListener computeExchange() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView = findViewById(R.id.exchange_tv_result_value);
                if(tietAmount.getText() != null && Double.parseDouble(tietAmount.getText().toString()) > 0){
                    String selCurrency = spnCurrency.getSelectedItem().toString();
                    Double result = null;
                    switch (selCurrency){
                        case "DOLLAR $":
                            result = Double.parseDouble(tietAmount.getText().toString()) * RATE_DOLLAR;
                            break;
                        case "EURO €":
                            result = Double.parseDouble(tietAmount.getText().toString()) * RATE_EURO;
                            break;
                        case "BRITISH POUNDS £":
                            result = Double.parseDouble(tietAmount.getText().toString()) * RATE_POUNDS;
                            break;
                    }
                    textView.setText(String.format("%.2f", result));
                } else {
                    Toast.makeText(getApplicationContext(), "Enter possitive amount to convert!", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }
}