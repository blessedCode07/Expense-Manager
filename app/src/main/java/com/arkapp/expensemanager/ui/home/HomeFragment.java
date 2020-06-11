package com.arkapp.expensemanager.ui.home;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.arkapp.expensemanager.R;
import com.arkapp.expensemanager.data.models.Expense;
import com.arkapp.expensemanager.data.models.ExpenseType;
import com.arkapp.expensemanager.data.repository.PrefRepository;
import com.arkapp.expensemanager.databinding.FragmentHomeBinding;
import com.arkapp.expensemanager.utils.PieValueFormatter;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static androidx.navigation.fragment.NavHostFragment.findNavController;
import static com.arkapp.expensemanager.utils.CommonUtils.convertExpenseType;
import static com.arkapp.expensemanager.utils.CommonUtils.filterCurrentMonth;
import static com.arkapp.expensemanager.utils.CommonUtils.filterPreviousMonth;
import static com.arkapp.expensemanager.utils.Constants.ENTERED_USER_NAME;
import static com.arkapp.expensemanager.utils.Constants.EXPENSE_FOOD_BEVERAGES;
import static com.arkapp.expensemanager.utils.Constants.EXPENSE_GENERAL_MAINTENANCE;
import static com.arkapp.expensemanager.utils.Constants.EXPENSE_GROCERIES;
import static com.arkapp.expensemanager.utils.Constants.EXPENSE_HEALTH_CARE;
import static com.arkapp.expensemanager.utils.Constants.EXPENSE_MISCELLANEOUS;
import static com.arkapp.expensemanager.utils.Constants.EXPENSE_ONLINE_SHOPPING;
import static com.arkapp.expensemanager.utils.Constants.EXPENSE_RENT;
import static com.arkapp.expensemanager.utils.Constants.getAllExpenseType;
import static com.arkapp.expensemanager.utils.ViewUtilsKt.getColorRes;
import static com.arkapp.expensemanager.utils.ViewUtilsKt.getDrawableRes;

public class HomeFragment extends Fragment implements ExpenseListener {

    private PrefRepository prefRepository;
    private FragmentHomeBinding binding;
    private ArrayList<Expense> currentMonthExpenses;
    private ArrayList<Expense> previousMonthExpenses;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        prefRepository = new PrefRepository(getContext());
        addUserName();
        binding = FragmentHomeBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.pieChart.animateXY(1500, 1500);
        binding.pieChart.getLegend().setWordWrapEnabled(true);
        binding.pieChart.getLegend().setTextSize(16);
        binding.pieChart.setHoleColor(getColorRes(getContext(), R.color.transparent));
        binding.pieChart.getDescription().setEnabled(false);

        binding.btAddExpense.setOnClickListener(v -> findNavController(HomeFragment.this).navigate(R.id.action_homeFragment_to_addExpenseFragment));

        initMonthButtons();
    }

    @Override
    public void onExpenseFetched(List<Expense> data) {
        currentMonthExpenses = filterCurrentMonth(data);
        previousMonthExpenses = filterPreviousMonth(data);

        setPieData(currentMonthExpenses);
    }

    @Override
    public void onError() {

    }

    private void initMonthButtons() {
        binding.btCurrentMonth.setOnClickListener(v -> {
            binding.btCurrentMonth.setBackground(getDrawableRes(getContext(), R.drawable.bg_unselected_start));
            binding.btPreviousMonth.setBackground(getDrawableRes(getContext(), R.drawable.bg_selected_end));

            binding.btCurrentMonth.setTextColor(getColorRes(getContext(), R.color.black));
            binding.btPreviousMonth.setTextColor(getColorRes(getContext(), R.color.white));

            binding.btCurrentMonth.setTypeface(null, Typeface.BOLD);
            binding.btPreviousMonth.setTypeface(null, Typeface.NORMAL);

            setPieData(currentMonthExpenses);
        });

        binding.btPreviousMonth.setOnClickListener(v -> {
            binding.btCurrentMonth.setBackground(getDrawableRes(getContext(), R.drawable.bg_selected_start));
            binding.btPreviousMonth.setBackground(getDrawableRes(getContext(), R.drawable.bg_unselected_end));

            binding.btCurrentMonth.setTextColor(getColorRes(getContext(), R.color.white));
            binding.btPreviousMonth.setTextColor(getColorRes(getContext(), R.color.black));

            binding.btCurrentMonth.setTypeface(null, Typeface.NORMAL);
            binding.btPreviousMonth.setTypeface(null, Typeface.BOLD);

            setPieData(previousMonthExpenses);
        });
    }

    private void setPieData(List<Expense> data) {
        binding.pieChart.clear();

        ArrayList<PieEntry> chartData = new ArrayList<>();
        ArrayList<LegendEntry> legendEntries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        float rent = 0f, groceries = 0f, shopping = 0f, maintenance = 0f, health = 0f, food = 0f, other = 0f;

        for (Expense expense : data) {
            ExpenseType type = convertExpenseType(expense.getExpenseType());
            switch (type.getName()) {
                case EXPENSE_RENT:
                    rent += Double.parseDouble(expense.getExpenseValue());
                    break;
                case EXPENSE_GROCERIES:
                    groceries += Double.parseDouble(expense.getExpenseValue());
                    break;
                case EXPENSE_ONLINE_SHOPPING:
                    shopping += Double.parseDouble(expense.getExpenseValue());
                    break;
                case EXPENSE_GENERAL_MAINTENANCE:
                    maintenance += Double.parseDouble(expense.getExpenseValue());
                    break;
                case EXPENSE_HEALTH_CARE:
                    health += Double.parseDouble(expense.getExpenseValue());
                    break;
                case EXPENSE_FOOD_BEVERAGES:
                    food += Double.parseDouble(expense.getExpenseValue());
                    break;
                case EXPENSE_MISCELLANEOUS:
                    other += Double.parseDouble(expense.getExpenseValue());
                    break;
            }
        }

        for (ExpenseType expenseType : getAllExpenseType()) {
            switch (expenseType.getName()) {
                case EXPENSE_RENT:
                    if (rent > 0) {
                        chartData.add(new PieEntry(rent, expenseType.getName()));
                        colors.add(getColorRes(getContext(), expenseType.getColor()));
                    }
                    legendEntries.add(new LegendEntry(
                            expenseType.getName(),
                            Legend.LegendForm.CIRCLE,
                            18f, 18f,
                            null,
                            getColorRes(getContext(), expenseType.getColor())));
                    break;
                case EXPENSE_GROCERIES:
                    if (groceries > 0) {
                        chartData.add(new PieEntry(groceries, expenseType.getName()));
                        colors.add(getColorRes(getContext(), expenseType.getColor()));
                    }
                    legendEntries.add(new LegendEntry(
                            expenseType.getName(),
                            Legend.LegendForm.CIRCLE,
                            18f, 18f,
                            null,
                            getColorRes(getContext(), expenseType.getColor())));
                    break;
                case EXPENSE_ONLINE_SHOPPING:
                    if (shopping > 0) {
                        chartData.add(new PieEntry(shopping, expenseType.getName()));
                        colors.add(getColorRes(getContext(), expenseType.getColor()));
                    }
                    legendEntries.add(new LegendEntry(
                            expenseType.getName(),
                            Legend.LegendForm.CIRCLE,
                            18f, 18f,
                            null,
                            getColorRes(getContext(), expenseType.getColor())));
                    break;
                case EXPENSE_GENERAL_MAINTENANCE:
                    if (maintenance > 0) {
                        chartData.add(new PieEntry(maintenance, expenseType.getName()));
                        colors.add(getColorRes(getContext(), expenseType.getColor()));
                    }
                    legendEntries.add(new LegendEntry(
                            expenseType.getName(),
                            Legend.LegendForm.CIRCLE,
                            18f, 18f,
                            null,
                            getColorRes(getContext(), expenseType.getColor())));
                    break;
                case EXPENSE_HEALTH_CARE:
                    if (health > 0) {
                        chartData.add(new PieEntry(health, expenseType.getName()));
                        colors.add(getColorRes(getContext(), expenseType.getColor()));
                    }
                    legendEntries.add(new LegendEntry(
                            expenseType.getName(),
                            Legend.LegendForm.CIRCLE,
                            18f, 18f,
                            null,
                            getColorRes(getContext(), expenseType.getColor())));
                    break;
                case EXPENSE_FOOD_BEVERAGES:
                    if (food > 0) {
                        chartData.add(new PieEntry(food, expenseType.getName()));
                        colors.add(getColorRes(getContext(), expenseType.getColor()));
                    }
                    legendEntries.add(new LegendEntry(
                            expenseType.getName(),
                            Legend.LegendForm.CIRCLE,
                            18f, 18f,
                            null,
                            getColorRes(getContext(), expenseType.getColor())));
                    break;
                case EXPENSE_MISCELLANEOUS:
                    if (other > 0) {
                        chartData.add(new PieEntry(other, expenseType.getName()));
                        colors.add(getColorRes(getContext(), expenseType.getColor()));
                    }
                    legendEntries.add(new LegendEntry(
                            expenseType.getName(),
                            Legend.LegendForm.CIRCLE,
                            18f, 18f,
                            null,
                            getColorRes(getContext(), expenseType.getColor())));
                    break;
            }
        }

        PieDataSet dataSet = new PieDataSet(chartData, "Expense this month");
        dataSet.setValueFormatter(new PieValueFormatter());
        dataSet.setColors(colors);

        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(18f);

        binding.pieChart.getLegend().setCustom(legendEntries);
        binding.pieChart.setData(pieData);

        binding.tvTotal.setText(String.format("Total - $%s", rent + groceries + shopping + maintenance + health + food + other));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (prefRepository.getCurrentUser() != null)
            new GetAllExpenseTask(getActivity(), prefRepository, this).execute();
    }

    //Used for showing the edit icon in the toolbar
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        requireActivity().getMenuInflater().inflate(R.menu.menu_toolbar, menu);
    }

    //Toolbar options click listener
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == R.id.addExpense) {
            findNavController(this).navigate(R.id.action_homeFragment_to_addExpenseFragment);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void addUserName() {
        if (ENTERED_USER_NAME.length() > 0) {
            new CheckLoggedInUserAsyncTask(requireActivity(), prefRepository, this).execute();
        }

    }
}