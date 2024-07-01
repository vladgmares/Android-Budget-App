package eu.ase.ro.seminar4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class AddBudgetActivity extends AppCompatActivity {
    public static final String ADD_BUDGET_KEY = "ADD_BUDGET_KEY";
    private Intent intent;
    Double budValue;

    private AppCompatButton btnSave;
    private TextInputEditText tietBudgetValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget);
        initComponents();
    }

    private void initComponents() {
        intent = getIntent();

        tietBudgetValue = findViewById(R.id.budget_tiet_budget_value);
        btnSave = findViewById(R.id.budget_btn_save);
        btnSave.setOnClickListener(getSaveListener());
    }

    private View.OnClickListener getSaveListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValid()){
                    Log.i("AddBudgetActivity", String.valueOf(budValue));
//                    Toast.makeText(getApplicationContext(), budValue.toString(), Toast.LENGTH_LONG).show();
                    intent.putExtra(ADD_BUDGET_KEY, budValue);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        };
    }

    private boolean isValid() {
        if(tietBudgetValue.getText() == null || Double.parseDouble(tietBudgetValue.getText().toString()) < 0){
            Toast.makeText(getApplicationContext(), "Provide some details about the expense.", Toast.LENGTH_SHORT).show();
            return false;
        }
        budValue = Double.parseDouble(tietBudgetValue.getText().toString());
        return true;
    }
}

