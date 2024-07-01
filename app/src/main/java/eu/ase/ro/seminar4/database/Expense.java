package eu.ase.ro.seminar4.database;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

import eu.ase.ro.seminar4.util.DateConverter;

@Entity(tableName = "expenses")
public class Expense implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "expense_date")
    private Date date;
    @ColumnInfo(name = "category")
    private String category;
    @ColumnInfo(name = "amount")
    private Double amount;
    @ColumnInfo(name = "details")
    private String details;


    public Expense(long id, Date date, String category, Double amount, String details) {
        this.id = id;
        this.date = date;
        this.category = category;
        this.amount = amount;
        this.details = details;
    }

    @Ignore
    public Expense(Date date, String category, Double amount, String details) {
        this.date = date;
        this.category = category;
        this.amount = amount;
        this.details = details;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", date=" + date +
                ", category='" + category + '\'' +
                ", amount=" + amount +
                ", details='" + details + '\'' +
                '}';
    }

    private Expense(Parcel source){
        id = source.readLong();
        date = DateConverter.fromString(source.readString());
        category = source.readString();
        amount = source.readDouble();
        details = source.readString();
    }

    public static Creator<Expense> CREATOR = new Creator<Expense>() {
        @Override
        public Expense createFromParcel(Parcel source) {
            return new Expense(source);
        }

        @Override
        public Expense[] newArray(int size) {
            return new Expense[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(DateConverter.fromDate(date));
        dest.writeString(category);
        dest.writeDouble(amount);
        dest.writeString(details);
    }
}
