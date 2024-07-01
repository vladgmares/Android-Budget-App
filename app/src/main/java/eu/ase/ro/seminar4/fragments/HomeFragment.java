package eu.ase.ro.seminar4.fragments;


import static android.app.Activity.RESULT_OK;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;



import java.util.ArrayList;
import java.util.List;

import eu.ase.ro.seminar4.AddExpenseActivity;
import eu.ase.ro.seminar4.MainActivity;
import eu.ase.ro.seminar4.R;
import eu.ase.ro.seminar4.database.Expense;
import eu.ase.ro.seminar4.database.ExpenseService;
import eu.ase.ro.seminar4.network.Callback;
import eu.ase.ro.seminar4.util.ExpenseAdapter;

public class HomeFragment extends Fragment {
    public static final String UPDATED_POSITION = "updated_position";
    private static final String EXPENSES_KEY = "EXPENSES_KEY";
    public static final String ADD_BUDGET_KEY = "ADD_BUDGET_KEY";
    public static final String UPDATE_ACTION = "update_action";
    public static final String ACTION_KEY = "action_key";
    private ArrayList<Expense> expenses;
    private ListView lvExpenses;
    private Double incomeValue;
    private Double balanceValue;
    private Double expensesValue;

    private ActivityResultLauncher<Intent> updateExpenseLauncher;
    private ExpenseService expenseService;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(ArrayList<Expense> expenses, Double incomeValue){
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(EXPENSES_KEY, expenses);
        args.putDouble(ADD_BUDGET_KEY, incomeValue);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // citim parametrii din bundle setat in newInstance
            expenses = getArguments().getParcelableArrayList(EXPENSES_KEY);
            incomeValue = getArguments().getDouble(ADD_BUDGET_KEY, 0.0);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initComponents(view);
        return view;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initComponents(View view){
        lvExpenses = view.findViewById(R.id.home_lv_expenses);
        // daca sisop decide sa reconstr fragmentul, etc... punem if(getContext...)
        if(getContext() != null) {
            expenseService = new ExpenseService(getContext());
            //getCOntext() este referinta catre activitatea de care apartine fragmentul
            // echivalent cu this din MainActivity
            ExpenseAdapter adapter = new ExpenseAdapter(getContext().getApplicationContext(),
                R.layout.lv_expense_view, expenses, getLayoutInflater());
            lvExpenses.setAdapter(adapter);
            updateFields(view);
            updateExpenseLauncher = getUpdateExpenseLauncher();
            lvExpenses.setOnItemClickListener(getItemClickEvent());
            lvExpenses.setOnItemLongClickListener(getItemLongClickListener());

            initSearchView(view);
        }
    }

    private void initSearchView(View view) {
        SearchView searchView = view.findViewById(R.id.home_sv_search_category);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // cand utilizatorul incepe sa scrie text in SV
                expenseService.getExpensesByCategory(newText.toUpperCase(), getExpensesByCategoryCallback());
                return false;
            }
        });
    }

    private AdapterView.OnItemLongClickListener getItemLongClickListener() {
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // stergere din DB
                alertDeleteExpense(position);
                return true;
            }
        };
    }


    // --------------- metode SQLite ---------------

    private Callback<List<Expense>> getExpensesByCategoryCallback(){
        return new Callback<List<Expense>>() {
            @Override
            public List<Expense> runResultOnUiThread(List<Expense> result) {

                if(result!= null){
                    List<Expense> filteredExpenses = result;
                    ExpenseAdapter adapter = new ExpenseAdapter(getContext().getApplicationContext(),
                            R.layout.lv_expense_view, filteredExpenses, getLayoutInflater());
                    lvExpenses.setAdapter(adapter);
                    return null;
                }
                return null;
            }
        };
    }
    private Callback<Boolean> deleteExpenseCallback(int position) {
        return new Callback<Boolean>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public List<Expense> runResultOnUiThread(Boolean result) {
                if(result){
                    //stergere obiect in colectie + notificare adaptor
                    expenses.remove(position);
                    notifyAdapter();
                }
                return null;
            }
        };
    }

    private Callback<Expense> updateExpenseCallback() {
        return new Callback<Expense>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public List<Expense> runResultOnUiThread(Expense result) {
                if (result == null) {
                    return null;
                }
                for(Expense expense: expenses){
                    if(expense.getId() == result.getId()){
                        expense.setDate(result.getDate());
                        expense.setAmount(result.getAmount());
                        expense.setDate(result.getDate());
                        expense.setCategory(result.getCategory());
                    }
                }
                //notificare adapter
                notifyAdapter();
                return null;
            }
        };
    }

    // ------------------------------------------------------



    public void alertDeleteExpense(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setMessage("Delete the selected expense");
        builder.setTitle("Are you sure?");
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.main_alert_possitive_answ, (DialogInterface.OnClickListener) (dialog, which) -> {
            expenseService.delete(expenses.get(position), deleteExpenseCallback(position));
        });
        builder.setNegativeButton(R.string.main_alert_neg_answ, (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private AdapterView.OnItemClickListener getItemClickEvent() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext().getApplicationContext(), AddExpenseActivity.class);
                intent.putExtra(AddExpenseActivity.ADD_EXPENSE_KEY, expenses.get(position));
                intent.putExtra(ACTION_KEY, UPDATE_ACTION);
                intent.putExtra(UPDATED_POSITION, position);
//                Log.i("PozitieSelectata", String.valueOf(position));
//                Log.i("Expense plecat:" , expenses.get(position).toString());
                updateExpenseLauncher.launch(intent);
            }
        };
    }

    private ActivityResultLauncher<Intent> getUpdateExpenseLauncher(){
        ActivityResultCallback<ActivityResult> callback = getUpdateExpenseActivityResultCallback();
        return registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), callback);
    }

    private ActivityResultCallback<ActivityResult> getUpdateExpenseActivityResultCallback() {
        return new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK && result.getData() != null){
                    Expense expense = result.getData().getParcelableExtra(AddExpenseActivity
                            .ADD_EXPENSE_KEY);
//                    Log.i("AdusDupaUpdate", expense.toString() + expense.getId());
                    // actualizare informatii in db
                    if (UPDATE_ACTION.equals(result.getData().getStringExtra(ACTION_KEY))) {
                        int position = result.getData().getIntExtra(UPDATED_POSITION, 0);
                        expenseService.update(expense, updateExpenseCallback());
                    }
                }
            }
        };
    }

    // --------------- Metode pentru populare comp viz -------------
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateFields(View view){
        initialIncomeValue(view);
        populateExpensesValue(view);
        initialTotalBalanceValue(view);
        computeTotalBalanceValue(view, incomeValue, expensesValue);
    }

    private void initialTotalBalanceValue(View view) {
        TextView textView = view.findViewById(R.id.bal_tv_total_balance_value);
        String value = getContext().getString(R.string.row_amount_template, "+",
                String.valueOf(balanceValue));
        populateTextViewContent(textView, value);
    }
    private void initialIncomeValue(View view) {
        TextView textView = view.findViewById(R.id.bal_tv_income_value);
        String value = getContext().getString(R.string.row_amount_template, "+",
                String.format("%.2f", incomeValue));
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.row_expense_green));
        if(incomeValue > 0){
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.row_expense_green));
        }
        populateTextViewContent(textView, value);
    }
    private void computeTotalBalanceValue(View view, Double incomeValue, Double expensesValue){
        TextView textView = view.findViewById(R.id.bal_tv_total_balance_value);
        Double result = incomeValue - expensesValue;
        if (result > 0) {
            String value = getContext().getString(R.string.row_amount_template, "+",
                   String.format("%.2f", result));
            populateTextViewContent(textView, value);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.row_expense_green));
        } else {
            String value = getContext().getString(R.string.row_amount_template, "",
                    String.format("%.2f", result));
            populateTextViewContent(textView, value);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.row_expense_red));
        }
        balanceValue = result;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void populateExpensesValue(View view) {
        TextView textView = view.findViewById(R.id.bal_tv_expenses_value);
        Double value = expenses.stream().mapToDouble(expense -> expense.getAmount() != null ?
                    expense.getAmount() : 0.0).sum();
        String result = getContext().getString(R.string.row_amount_template, "",
                String.format("%.2f", value));
        populateTextViewContent(textView, result);
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.row_expense_red));
        expensesValue = value;
    }

    private void populateTextViewContent(TextView textView, String value){
        if(value != null && !value.isEmpty()){
            textView.setText(value);
        } else {
            textView.setText(R.string.expense_adapter_default_value);
        }
    }
    // ------------------------------------------------------------

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void notifyAdapter() {
        if(lvExpenses == null){
            lvExpenses = getView().findViewById(R.id.home_lv_expenses);
            ExpenseAdapter adapter = new ExpenseAdapter(getContext().getApplicationContext(),
                    R.layout.lv_expense_view, expenses, getLayoutInflater());
            lvExpenses.setAdapter(adapter);
        }
        ArrayAdapter<Expense> adapter = (ArrayAdapter<Expense>) lvExpenses.getAdapter();
        populateExpensesValue(getView());
        adapter.notifyDataSetChanged();
        computeTotalBalanceValue(getView(), incomeValue, expensesValue);
    }

}