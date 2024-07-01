package eu.ase.ro.seminar4.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.ase.ro.seminar4.database.Expense;

public class ExpensesJsonParser {
    public static final String DATE = "Date";
    public static final String CATEGORY = "Category";
    public static final String AMOUNT = "Amount";
    public static final String DETAILS = "Details";

    public static List<Expense> fromJson(String json){
        try {
            JSONArray array = new JSONArray(json);
            return readExpensesFromJson(array);
        } catch (JSONException e) {
            Log.e("ExpensesJsonParser", "Could not get the JSON data");
        }
        return new ArrayList<>();
    }

    private static List<Expense> readExpensesFromJson(JSONArray array) throws JSONException {
        List<Expense> results = new ArrayList<>();
        for (int i = 0; i < array.length(); i++){
            JSONObject object = array.getJSONObject(i);
            if(object.has("Type")){
                JSONObject typeExpenses = object.getJSONObject("Type");
                JSONArray expensesArr = typeExpenses.getJSONArray("Expenses");
                for (int j = 0; j < expensesArr.length(); j++){
                    JSONObject expenseObj = expensesArr.getJSONObject(j);
                    Expense expense = readExpenseFromJsonObject(expenseObj);
                    results.add(expense);
                }
            }
        }
        return results;
    }

    private static Expense readExpenseFromJsonObject(JSONObject expenseObj) throws JSONException {
        Date date = DateConverter.fromString(expenseObj.getString(DATE));
        String category = expenseObj.getString(CATEGORY);
        Double amount = expenseObj.getDouble(AMOUNT);
        String details = expenseObj.getString(DETAILS);
        return new Expense(date, category.toUpperCase(), amount, details);
    }
}
