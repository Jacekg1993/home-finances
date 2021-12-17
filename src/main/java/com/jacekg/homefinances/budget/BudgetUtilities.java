package com.jacekg.homefinances.budget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jacekg.homefinances.expenses.model.ConstantExpense;
import com.jacekg.homefinances.expenses.model.Expense;

public class BudgetUtilities {
	
	public static <T extends Expense> List<T> removeDuplicatedExpenses(List<T> currentExpenses,
			List<T> updatedExpenses) {
		
		System.out.println("currentExpense: " + currentExpenses);
		System.out.println("updatedExpense: " + updatedExpenses);
		
		T currentExpense;
		T updatedExpense;

		for (int i = 0; i < updatedExpenses.size(); i++) {

			updatedExpense = updatedExpenses.get(i);
			System.out.println("updated:" + updatedExpense + " id: " + updatedExpense.getId());
			
			for (int j = 0; j < currentExpenses.size(); j++) {

				currentExpense = currentExpenses.get(j);
				System.out.println("current: " + currentExpense + " id: " + currentExpense.getId());
				
//				if (updatedExpense.getId() == null) {
//					System.out.println("jest null");
//				}
				
				if ((updatedExpense.getId() == null 
						|| !(updatedExpense.getId().equals((currentExpense.getId()))))
						&& (updatedExpense.getName().equals(currentExpense.getName()))) {
					System.out.println("removed:" + updatedExpenses.get(i));
					updatedExpenses.remove(i);
				}
			}
		}

		return updatedExpenses;
	}
	
	public static <T> List<Long> findExpensesIdsToRemove(
			Collection<T> currentExpenses, Collection<T> updatedExpenses) {
		
//		System.out.println("currentExpense: " + currentExpenses);
//		System.out.println("updatedExpense: " + updatedExpenses);
		
		List<Long> constantExpensesIdToRemove = new ArrayList<>();

		for (T expense : currentExpenses) {

			T searchedExpense = updatedExpenses.stream()
					.filter(updatedExpense -> ((Expense) expense).getName()
							.equals(((Expense) updatedExpense).getName()))
					.findFirst().orElse(null);

			if (searchedExpense == null) {
				constantExpensesIdToRemove.add(((Expense) expense).getId());
			}
		}

		return constantExpensesIdToRemove;
	}
	
}
