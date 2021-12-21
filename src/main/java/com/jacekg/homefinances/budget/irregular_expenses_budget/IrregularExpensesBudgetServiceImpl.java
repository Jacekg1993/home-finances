package com.jacekg.homefinances.budget.irregular_expenses_budget;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jacekg.homefinances.budget.BudgetUtilities;
import com.jacekg.homefinances.budget.monthly_budget.MonthlyBudget;
import com.jacekg.homefinances.budget.monthly_budget.MonthlyBudgetDTO;
import com.jacekg.homefinances.budget.monthly_budget.MonthlyBudgetDoesntExistsException;
import com.jacekg.homefinances.budget.monthly_budget.MonthlyBudgetRepository;
import com.jacekg.homefinances.budget.monthly_budget.MonthlyBudgetService;
import com.jacekg.homefinances.expenses.IrregularExpenseRepository;
import com.jacekg.homefinances.expenses.model.ConstantExpense;
import com.jacekg.homefinances.expenses.model.Expense;
import com.jacekg.homefinances.expenses.model.IrregularExpense;
import com.jacekg.homefinances.user.User;
import com.jacekg.homefinances.user.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class IrregularExpensesBudgetServiceImpl implements IrregularExpensesBudgetService {
	
	private MonthlyBudgetService monthlyBudgetService;
	
	private UserRepository userRepository;
	
	private IrregularExpensesBudgetRepository irregularExpensesBudgetRepository;
	
	private IrregularExpenseRepository irregularExpenseRepository;
	
	private MonthlyBudgetRepository monthlyBudgetRepository;
	
	private ModelMapper modelMapper;
	
	@Override
	@Transactional
	public IrregularExpensesBudgetDTO findByUserIdAndDate(Long userId, LocalDate date) {
		
		IrregularExpensesBudget irregularExpensesBudget 
			= irregularExpensesBudgetRepository.findByUserIdAndDate(userId, date);
		
		if (irregularExpensesBudget == null) {
			return null;
		} else {
			return modelMapper.map(irregularExpensesBudget, IrregularExpensesBudgetDTO.class);
		}
	}
	
	@Override
	@Transactional
	public IrregularExpensesBudgetDTO save(IrregularExpensesBudgetDTO irregularExpensesBudgetDTO) {
		
		if (findByUserIdAndDate(irregularExpensesBudgetDTO.getUserId(), 
				irregularExpensesBudgetDTO.getDate()) != null) {
			throw new IrregularExpensesBudgetAlreadyExistsException
				("Budżet wydatków nieregularnych na dany rok istnieje!");
		}
		
		List<IrregularExpense> irregularExpenses = new ArrayList<IrregularExpense>();
		
		IrregularExpensesBudget irregularExpensesBudget 
			= modelMapper.map(irregularExpensesBudgetDTO, IrregularExpensesBudget.class);
		
		User user = userRepository.findByUserId(irregularExpensesBudgetDTO.getUserId());
		
		irregularExpensesBudget.setUser(user);
		irregularExpensesBudget.setIrregularExpenses(irregularExpenses);
		
		return modelMapper
				.map(irregularExpensesBudgetRepository.save(irregularExpensesBudget),
						IrregularExpensesBudgetDTO.class);
	}

	@Override
	@Transactional
	public List<IrregularExpensesBudgetDTO> findAllByUserId(Long userId) {
		
		return irregularExpensesBudgetRepository.findAllByUserId(userId)
				.stream()
				.map(budget -> modelMapper.map(budget, IrregularExpensesBudgetDTO.class))
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public IrregularExpensesBudgetDTO update(IrregularExpensesBudgetDTO irregularExpensesBudgetDTO) {

		IrregularExpensesBudget irregularExpensesBudget = modelMapper.map(irregularExpensesBudgetDTO,
				IrregularExpensesBudget.class);

		User user = userRepository.findByUserId(irregularExpensesBudgetDTO.getUserId());
		irregularExpensesBudget.setUser(user);

		List<IrregularExpense> currentIrregularExpenses = irregularExpenseRepository
				.findAllByIrregularExpensesBudgetId(irregularExpensesBudget.getId());
		List<IrregularExpense> updatedIrregularExpenses = irregularExpensesBudget.getIrregularExpenses();

		irregularExpensesBudget.setIrregularExpenses
			(BudgetUtilities.removeDuplicatedExpenses(currentIrregularExpenses, updatedIrregularExpenses));
		
		irregularExpensesBudget.setAnnualExpensesSum
			(BudgetUtilities.calculateExpenses(irregularExpensesBudget.getIrregularExpenses()));
		
		irregularExpensesBudget.setNecessaryMonthlySavings
			(BudgetUtilities.calculateNecessaryMonthlySavings(irregularExpensesBudget.getAnnualExpensesSum()));
		
		addIrregularExpenseToRecentMonthlyBudget(irregularExpensesBudget);
		
		return modelMapper.map(irregularExpensesBudgetRepository.save(irregularExpensesBudget), 
				IrregularExpensesBudgetDTO.class);
	}
	
	@Transactional
	private void addIrregularExpenseToRecentMonthlyBudget
		(IrregularExpensesBudget irregularExpensesBudget) {
		
		LocalDate date = LocalDate.now().withDayOfMonth(1); 
		
		System.out.println("find monthlyBudget");
		MonthlyBudget monthlyBudget 
			= monthlyBudgetRepository.findByUserIdAndDate
				(irregularExpensesBudget.getUser().getId(), date);
		
		if (monthlyBudget != null) {
			
			System.out.println("get expenses");
			List<ConstantExpense> constantExpenses = monthlyBudget.getConstantExpenses();
			monthlyBudget.getOneTimeExpenses();
			
			System.out.println("get irregular");
			ConstantExpense irregularExpense = constantExpenses
					.stream()
					.filter(expense -> expense.getName().equals("wydatki nieregularne"))
					.findFirst()
					.orElse(null);
			
			System.out.println("is true: " + irregularExpense);
			
			if (irregularExpense == null) {
				
				ConstantExpense newIrregularExpense = new ConstantExpense(
						"wydatki nieregularne",
						irregularExpensesBudget.getNecessaryMonthlySavings(),
						0);
				
				constantExpenses.add(newIrregularExpense);
			} else if (irregularExpense != null) {
				
				irregularExpense.setPlannedAmount(irregularExpensesBudget.getNecessaryMonthlySavings());
				constantExpenses.add(irregularExpense);
			}
			
			monthlyBudget.setConstantExpenses(constantExpenses);
			
			monthlyBudget.setFinalBalance(BudgetUtilities.calculateFinalBalance
					(monthlyBudget.getConstantExpenses(),
							monthlyBudget.getOneTimeExpenses(), 
							monthlyBudget.getPreviousMonthEarnings()));
			
			System.out.println("update MonthlyBudget");
			monthlyBudgetRepository.save(monthlyBudget);
		}
	}

	@Override
	@Transactional
	public void deleteByDate(LocalDate monthlyBudgetDate, Long userId) {
		
		IrregularExpensesBudget irregularExpensesBudget 
			= irregularExpensesBudgetRepository.findByUserIdAndDate(userId, monthlyBudgetDate);
		
		if (irregularExpensesBudget == null) {
			throw new IrregularExpensesBudgetDoesntExistsException
				("Irregular expenses budget with this date doesn't exists!");
		}
		
		irregularExpensesBudgetRepository.delete(irregularExpensesBudget);
	}
}







