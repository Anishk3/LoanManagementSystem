package dao;

import entity.Loan;

public interface ILoanRepository {
		
		    void applyLoan(Loan loan)  ;
		    double calculateInterest(int loanId);
		    void loanStatus(int loanId) ;
		    double calculateEMI(int loanId);
		    void loanRepayment(int loanId, double amount) ;
		    void getAllLoan();
		    void getLoanId(int loanId) ;
		}
