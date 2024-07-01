package eu.ase.ro.seminar4.database;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.concurrent.Callable;

import eu.ase.ro.seminar4.network.AsyncTaskRunner;
import eu.ase.ro.seminar4.network.Callback;

public class ExpenseService {
    private final ExpenseDao expenseDao;
    private final AsyncTaskRunner asyncTaskRunner;

    public ExpenseService(Context context){
        this.expenseDao = DatabaseManager.getInstance(context).getExpenseDao();
        asyncTaskRunner = new AsyncTaskRunner();
    }

    public void insert(Expense expense, Callback<Expense> activityThread){
        Callable<Expense> insertOperation = new Callable<Expense>() {
            @Override
            public Expense call() {
                //alt thread
                if(expense == null || expense.getId() > 0){
                    return null;
                }
                long id = expenseDao.insert(expense);
                if(id < 0){
                    return null;
                }
                expense.setId(id);
                return expense;
            }
        };

        asyncTaskRunner.executeAsync(insertOperation, activityThread);
    }

    public void getAll(Callback<List<Expense>> activityThread){
        Callable<List<Expense>> getAllOperation = new Callable<List<Expense>>() {
            @Override
            public List<Expense> call() {
                return expenseDao.getAll();
            }
        };
        asyncTaskRunner.executeAsync(getAllOperation, activityThread);
    }

    public void update(Expense expense, Callback<Expense> activityThread){
        Callable<Expense> updateOperation = new Callable<Expense>() {
            @Override
            public Expense call() {
                if(expense == null || expense.getId() <= 0){
                    return null;
                }
                int count = expenseDao.update(expense);
                if(count <= 0 ){
                    return null;
                }
                return expense;
            }
        };
        asyncTaskRunner.executeAsync(updateOperation, activityThread);
    }

    public void delete(Expense expense, Callback<Boolean> activityThread){
        Callable<Boolean> deleteOperation = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                if(expense == null || expense.getId() <= 0){
                    return false;
                }
                int count = expenseDao.delete(expense);
                return count > 0;
            }
        };
        asyncTaskRunner.executeAsync(deleteOperation, activityThread);
    }




    public void deleteAll(Callback<Boolean> activityThread) {
        Callable<Boolean> deleteAllOperation = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    expenseDao.deleteAll(); // Perform deletion of all expenses
                    return true; // Deletion successful
                } catch (Exception e) {
                    Log.e("ExpenseService", "Error deleting all expenses: " + e.getMessage());
                    return false; // Deletion failed
                }
            }
        };
        asyncTaskRunner.executeAsync(deleteAllOperation, activityThread);
    }

    public void getExpensesByCategory(String category, Callback<List<Expense>> activityThread) {
        Callable<List<Expense>> getExpensesOperation = new Callable<List<Expense>>() {
            @Override
            public List<Expense> call() {
                return expenseDao.getExpensesByCategory(category);
            }
        };
        asyncTaskRunner.executeAsync(getExpensesOperation, activityThread);
    }
}
