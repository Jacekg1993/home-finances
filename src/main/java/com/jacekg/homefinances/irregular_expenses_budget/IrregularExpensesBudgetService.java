package com.jacekg.homefinances.irregular_expenses_budget;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;

public interface IrregularExpensesBudgetService {

	IrregularExpensesBudgetDTO save(IrregularExpensesBudgetDTO irregularExpensesBudgetDTO);

	IrregularExpensesBudgetDTO findByUserIdAndDate(Long userId, LocalDate date);

	List<IrregularExpensesBudgetDTO> findAllByUserId(Long userId);

	IrregularExpensesBudgetDTO update(IrregularExpensesBudgetDTO irregularExpensesBudgetDTO);
}
