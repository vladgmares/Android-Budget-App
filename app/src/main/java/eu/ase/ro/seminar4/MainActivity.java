package eu.ase.ro.seminar4;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import eu.ase.ro.seminar4.database.ExpenseService;
import eu.ase.ro.seminar4.fragments.ContactFragment;
import eu.ase.ro.seminar4.fragments.HomeFragment;
import eu.ase.ro.seminar4.database.Expense;
import eu.ase.ro.seminar4.network.AsyncTaskRunner;
import eu.ase.ro.seminar4.network.Callback;
import eu.ase.ro.seminar4.network.HttpManager;
import eu.ase.ro.seminar4.util.ExpensesJsonParser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAnalytics mFirebaseAnalytics;
    public static final String EXPENSES_URL = "https://api.npoint.io/47dc2c384c1703bd5b27";
    private final AsyncTaskRunner asyncTaskRunner = new AsyncTaskRunner();


    public static final String SHARED_PREFERENCES = "SHARED_PREFERENCES";
    public static final String EXPENSES_LIST = "EXPENSES_LIST";
    public static final String BUDGET_VALUE = "BUDGET_VALUE";


    private Double budgetValue = 0.0;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FloatingActionButton fabAddExpense;


    private ActivityResultLauncher<Intent> newExpenseLauncher;
    private ActivityResultLauncher<Intent> newBudgetLauncher;

    private List<Expense> expenses = new ArrayList<>();


    private Fragment currentFragment;

    private ExpenseService expenseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();
        // initializare expenseService
        expenseService = new ExpenseService(getApplicationContext());

        if(savedInstanceState == null){
            // e null doar daca am intrat prima data.
            // La rotirea dispozitivului mobil, nu mai este null
            currentFragment = HomeFragment.newInstance((ArrayList<Expense>)expenses, (Double) budgetValue);
            openFragment();
            navigationView.setCheckedItem(R.id.main_nav_home);
            //getAll() din db
            expenseService.getAll(getAllExpensesCallback());
        }
    }



    private void loadExpensesFromUrl() {
        Callable<String> asyncOperation = new HttpManager(EXPENSES_URL);
        Callback<String> mainThreadOperation = getMainThreadOperation();
        asyncTaskRunner.executeAsync(asyncOperation, mainThreadOperation);
    }

    private Callback<String> getMainThreadOperation() {
        return new Callback<String>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public List<Expense> runResultOnUiThread(String result) {
                //suntem in MainActivity aici
//                expenses.addAll(ExpensesJsonParser.fromJson(result));
                List<Expense> importedExpenses = ExpensesJsonParser.fromJson(result);
                for (Expense exp:
                     importedExpenses) {
                    expenseService.insert(exp, insertExpenseCallback());
                }
//                expenses.addAll(importedExpenses);
                ((HomeFragment)currentFragment).notifyAdapter();
                return null;
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    private void initComponents(){
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);




        configNavigation();
        loadData();
        navigationView = findViewById(R.id.nav_view);
        //selectie optiune meniu lateral
        navigationView.setNavigationItemSelectedListener(getNavigationItemSelectedListener());
        fabAddExpense = findViewById(R.id.main_fab_add_expense);
        fabAddExpense.setOnClickListener(getAddExpenseClickListener());


        newExpenseLauncher = getNewExpenseActivityLauncher();
        newBudgetLauncher = getNewBudgetActivityLauncher();
    }



    private ActivityResultLauncher<Intent> getNewBudgetActivityLauncher() {
        ActivityResultCallback<ActivityResult> callback = getAddBudgetCallback();
        return registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), callback);
    }

    private ActivityResultCallback<ActivityResult> getAddBudgetCallback() {
        return new ActivityResultCallback<ActivityResult>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK && result.getData() != null){
                    budgetValue = result.getData().getDoubleExtra(AddBudgetActivity.ADD_BUDGET_KEY,
                            0.0);
                    saveData();
                    if(currentFragment instanceof HomeFragment){
                        currentFragment = HomeFragment.newInstance((ArrayList<Expense>) expenses, budgetValue);
                        openFragment();
                    }
                }
            }
        };
    }


    private ActivityResultLauncher<Intent> getNewExpenseActivityLauncher() {
        ActivityResultCallback<ActivityResult> callback = getAddCallback();
        return registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), callback);
    }

    private View.OnClickListener getAddExpenseClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddExpenseActivity.class);
                newExpenseLauncher.launch(intent);
            }
        };
    }

    private ActivityResultCallback<ActivityResult> getAddCallback() {
        return new ActivityResultCallback<ActivityResult>() {
            @SuppressLint("NewApi")
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK && result.getData() != null){
                    Expense expense = result.getData().getParcelableExtra(AddExpenseActivity
                            .ADD_EXPENSE_KEY);
                    // inserarea in db
                    expenseService.insert(expense, insertExpenseCallback());

                }
            }
        };
    }

    // --------------- metode SQLite ---------------
    private Callback<Expense> insertExpenseCallback() {
        return new Callback<Expense>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public List<Expense> runResultOnUiThread(Expense expense) {
                if(expense != null){
                    expenses.add(expense);
                    saveData();
                    // notificare adapter
                    if(currentFragment instanceof HomeFragment){
                        ((HomeFragment)currentFragment).notifyAdapter();
                    }
                }
                return null;
            }
        };
    }

    private Callback<List<Expense>> getAllExpensesCallback() {
        return new Callback<List<Expense>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public List<Expense> runResultOnUiThread(List<Expense> results) {
                if(results != null){
                    expenses.clear();
                    expenses.addAll(results);
                    ((HomeFragment)currentFragment).notifyAdapter();
                }
                return results;
            }
        };
    }

    private Callback<Boolean> deleteAllExpensesCallback(){
        return new Callback<Boolean>() {
            @Override
            public List<Expense> runResultOnUiThread(Boolean result) {
                if(result){
                    expenses = new ArrayList<>();
                    budgetValue = 0.0;
                    currentFragment = HomeFragment.newInstance((ArrayList<Expense>) expenses,
                            (Double) budgetValue);
                    openFragment();
                    saveData();
                    return expenses;
                }
                return null;
            }
        };
    }

    //-----------------------------------------------



    private NavigationView.OnNavigationItemSelectedListener getNavigationItemSelectedListener() {
        return new NavigationView.OnNavigationItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.main_nav_home){
                    currentFragment = HomeFragment.newInstance((ArrayList<Expense>) expenses,
                            (Double) budgetValue);
                    openFragment();
                } else if(item.getItemId() == R.id.main_nav_contact){
                    currentFragment = new ContactFragment();
                    openFragment();
                } else if(item.getItemId() == R.id.main_nav_budget){
                    Intent intent = new Intent(getApplicationContext(), AddBudgetActivity.class);
                    newBudgetLauncher.launch(intent);
                } else if(item.getItemId() == R.id.main_nav_exchange){
                    Intent intent = new Intent(getApplicationContext(), ExchangeRateActivity.class);
                    startActivity(intent);
                } else if(item.getItemId() == R.id.main_nav_reset){
                    alertResetValues();
                    Log.i("Item id este: ", String.valueOf(item.getItemId()) + String.valueOf(R.id.main_nav_reset));
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(item.getItemId()));
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Buton Reset");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                } else if(item.getItemId() == R.id.main_nav_import){
                    loadExpensesFromUrl();
                    saveData();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        };
    }


    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
//        Gson gson = new Gson();
//        String json = gson.toJson(expenses);
//        editor.putString(EXPENSES_LIST, json);
        Float bv = Float.parseFloat(budgetValue.toString());
        editor.putFloat(BUDGET_VALUE, bv);
        editor.apply();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
//        Gson gson = new Gson();
//        String json = sharedPreferences.getString(EXPENSES_LIST, null);
//        Type type = new TypeToken<ArrayList<Expense>>() {}.getType();
        Float bv = sharedPreferences.getFloat(BUDGET_VALUE, 0);
        budgetValue = bv.doubleValue();
//        expenses = gson.fromJson(json, type);
//        if(expenses == null){
//            expenses = new ArrayList<>();
//        }
    }


    public void alertResetValues(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setMessage(R.string.main_alert_message);
        builder.setTitle(R.string.main_alert_title);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.main_alert_possitive_answ, (DialogInterface.OnClickListener) (dialog, which) -> {
            expenseService.deleteAll(deleteAllExpensesCallback());
//            expenses = new ArrayList<>();
//            budgetValue = 0.0;
//            currentFragment = HomeFragment.newInstance((ArrayList<Expense>) expenses,
//                    (Double) budgetValue);
//            openFragment();
//            saveData();
        });
        builder.setNegativeButton(R.string.main_alert_neg_answ, (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void openFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame_container, currentFragment)
                .commit();
    }

    private void configNavigation(){
        //initializare + legare DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        //initializare Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        //setam toolbar la nivelul aplicatiei
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        // legare toggle de DrawerLayout -> duce la desenare panou lateral
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }
}