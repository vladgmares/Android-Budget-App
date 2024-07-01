package eu.ase.ro.seminar4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Date;

import eu.ase.ro.seminar4.database.Expense;
import eu.ase.ro.seminar4.util.DateConverter;

public class AddExpenseActivity extends AppCompatActivity {


    public static final String ADD_EXPENSE_KEY = "ADD_EXPENSE_KEY";
    private Intent intent;
    private Expense expense;

    private AppCompatButton btnSave;
    private TextInputEditText tietDate;
    private TextInputEditText tietAmount;
    private TextInputEditText tietDetails;
    private Spinner spnCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);
        initComponents();
        if(intent.hasExtra(ADD_EXPENSE_KEY)){
            createViewsFromExpense();
        }
    }


    private void initComponents() {
        intent = getIntent();

        btnSave = findViewById(R.id.add_btn_save);
        btnSave.setOnClickListener(getSaveListener());

        tietDate = findViewById(R.id.add_tiet_expense_date);
        tietAmount = findViewById(R.id.add_tiet_expense_amount);
        tietDetails = findViewById(R.id.add_tiet_expense_details);

        spnCategory = findViewById(R.id.add_spinner_expense_category);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.add_category_values, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spnCategory.setAdapter(adapter);

    }

    private void createViewsFromExpense() {
        expense = (Expense) intent.getParcelableExtra(ADD_EXPENSE_KEY);
        Log.i("AddExpenseActivity", "Expense adus din intent: " + expense.toString());
        if(expense == null){
            return;
        }
        tietDetails.setText(expense.getDetails());
        tietDate.setText(DateConverter.fromDate(expense.getDate()));
        tietAmount.setText(String.valueOf(expense.getAmount()));
        spinnerSelection();
    }

    private void spinnerSelection() {
        ArrayAdapter adapter = (ArrayAdapter) spnCategory.getAdapter();
        for(int i = 0; i < adapter.getCount(); i++){
            String item = (String) adapter.getItem(i);
            if(item.equals(expense.getCategory())){
                spnCategory.setSelection(i);
                break;
            }
        }
    }

    private View.OnClickListener getSaveListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValid()){
                    Expense expense = buildExpense();
                    Log.i("AddExpenseAcitvity", expense.toString());
                    intent.putExtra(ADD_EXPENSE_KEY, expense);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        };
    }

    private Expense buildExpense() {
        Date date = DateConverter.fromString(tietDate.getText().toString().trim());
        Double amount = Double.parseDouble(tietAmount.getText().toString().trim());
        String details = tietDetails.getText().toString().trim();
        String category = spnCategory.getSelectedItem().toString();
        if(expense == null) {
            return new Expense(date, category, amount, details);
        } else {
            // operatie de modificare update
            expense.setDetails(details);
            expense.setAmount(amount);
            expense.setDate(date);
            expense.setCategory(category);
            Log.i("ExpenseUpdatat", expense.toString());
            return expense;
        }
    }

    private boolean isValid(){
        if(tietDate.getText() == null || DateConverter.fromString(tietDate.getText().toString().trim()) == null){
            Toast.makeText(getApplicationContext(), "Invalid date. Accepted format dd/MM/yyy", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(tietAmount.getText() == null) {
            Toast.makeText(getApplicationContext(), "Invalid amount. Enter possitive amount.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(tietDetails.getText() == null || tietDetails.getText().toString().trim().length() < 2) {
            Toast.makeText(getApplicationContext(), "Provide some details about the expense.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}