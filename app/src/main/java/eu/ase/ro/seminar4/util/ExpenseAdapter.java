package eu.ase.ro.seminar4.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.List;

import eu.ase.ro.seminar4.R;
import eu.ase.ro.seminar4.database.Expense;

public class ExpenseAdapter extends ArrayAdapter {

    private Context context;
    private int resource;
    private List<Expense> expenses;
    private LayoutInflater inflater;

    public ExpenseAdapter(@NonNull Context context, int resource, @NonNull List objects,
                          LayoutInflater inflater) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.expenses = objects;
        this.inflater = inflater;
    }

    // getView face conv intre un obj Expense la resource (position reprezinta index din arr de
    // expenses
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = inflater.inflate(resource, parent, false);
        Expense expense = expenses.get(position); // capturam fiecare elem din lista
        if(expense == null){
            return view;
        }
        addExpenseLabel(view, expense.getCategory());
        addExpenseAmount(view, expense.getAmount());
        addExpenseDetails(view, expense.getDetails());
        return view;
    }

    private void addExpenseDetails(View view, String details) {
        TextView textView = view.findViewById(R.id.row_tv_expense_details);
        String value = context.getString(R.string.row_details_template, details);
        populateTextViewContent(textView, value);
    }

    private void addExpenseAmount(View view, Double amount) {
        TextView textView = view.findViewById(R.id.row_expense_amount);
        if(amount <= 0){
            String value = context.getString(R.string.row_amount_template, "+",
                    String.valueOf(amount));
            populateTextViewContent(textView, value);
            textView.setTextColor(ContextCompat.getColor(context, R.color.row_expense_green));
        } else {
            String value = context.getString(R.string.row_amount_template, "-",
                    String.valueOf(amount));
            populateTextViewContent(textView, value);
            textView.setTextColor(ContextCompat.getColor(context, R.color.row_expense_red));
        }
    }

    private void addExpenseLabel(View view, String category) {
        TextView textView = view.findViewById(R.id.row_tv_expense_label);
        populateTextViewContent(textView, category);
    }
    private void populateTextViewContent(TextView textView, String value){
        if(value != null && !value.isEmpty()){
            textView.setText(value);
        } else {
            textView.setText(R.string.expense_adapter_default_value);
        }
    }
}
