package com.jacekg.homefinances.budget;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/budget")
public class BudgetRestController {
	
	@PostMapping("/monthly-budgets")
	public MonthlyBudget addMonthlyBudget(@RequestBody MonthlyBudgetDTO monthlyBudgetDTO) {
		
		MonthlyBudget monthlyBudget = new MonthlyBudget();
		monthlyBudget.setFinalBalance(1000);
		
		if (true) {
			throw new BudgetAlreadyExistsException("Budżet na dany miesiąc istnieje!");
		}
		
		return monthlyBudget;
	}
	
}
