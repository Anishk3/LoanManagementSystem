package main;

import dao.ILoanRepositoryImpl;
import entity.CarLoan;
import entity.Customer;
import entity.HomeLoan;
import entity.Loan;

public class MainModule {   
	public static void main(String[] args) {
	Customer customer = new Customer(3);
	Loan loan = new Loan(7,customer, 500000, 7, 240, "homeLoan", "Pending");
	ILoanRepositoryImpl ILoanRepository = new ILoanRepositoryImpl();
	System.out.println("You have an Interest of "+ ILoanRepository.calculateInterest(2));
	System.out.println("You have an EMI of "+ILoanRepository.calculateEMI(3));
	ILoanRepository.loanStatus(2);
	ILoanRepository.loanRepayment(3, 700);
	ILoanRepository.getAllLoan();
	ILoanRepository.applyLoan(loan);
	
}
}
