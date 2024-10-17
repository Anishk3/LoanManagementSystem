package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import entity.CarLoan;
import entity.HomeLoan;
import entity.Loan;
import util.DatabaseConnection;

public class ILoanRepositoryImpl implements ILoanRepository {

    @Override
    public void applyLoan(Loan loan) {
        Scanner sc = new Scanner(System.in);

        // Initially setting loan status to "Pending"
        loan.setLoanStatus("Pending");

        System.out.println("Loan Details:");
        System.out.println("Loan ID: " + loan.getLoanId());
        System.out.println("Customer ID: " + loan.getCustomer().getCustomerId());
        System.out.println("Principal Amount: " + loan.getPrincipalAmount());
        System.out.println("Interest Rate: " + loan.getInterestRate());
        System.out.println("Loan Term: " + loan.getLoanTerm() + " months");
        System.out.println("Loan Type: " + loan.getLoanType());
        System.out.println("Loan Status: " + loan.getLoanStatus());

        // Get confirmation from the user
        System.out.println("Do you want to proceed with this loan application? (Yes/No): ");
        String confirmation = sc.nextLine().trim();

        if (confirmation.equalsIgnoreCase("Yes")) {
            // Proceed to store loan details in the database
            try (Connection con = DatabaseConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO loan (loanId, customerId, principalAmount, interestRate, loanTerm, loanType, loanStatus) VALUES (?, ?, ?, ?, ?, ?, ?)"
                );

                ps.setInt(1, loan.getLoanId());
                ps.setInt(2, loan.getCustomer().getCustomerId());
                ps.setInt(3, loan.getPrincipalAmount());
                ps.setInt(4, loan.getInterestRate());
                ps.setInt(5, loan.getLoanTerm());
                ps.setString(6, loan.getLoanType());
                ps.setString(7, loan.getLoanStatus());

                int rowsAffected = ps.executeUpdate();
                System.out.println(rowsAffected + " record(s) updated.");

                // Handle specific loan types (homeLoan, carLoan)
                if ("homeLoan".equalsIgnoreCase(loan.getLoanType())) {
                    System.out.println("Enter the property address: ");
                    String address = sc.nextLine();

                    System.out.println("Enter the property value: ");
                    int propVal = sc.nextInt();

                    int homeLoanId = 0;
                    try (Statement stmt = con.createStatement()) {
                        ResultSet rs = stmt.executeQuery("SELECT homeLoanId FROM homeLoan ORDER BY homeLoanId DESC LIMIT 1");
                        if (rs.next()) {
                            homeLoanId = rs.getInt(1) + 1;
                        }
                    }

                    HomeLoan homeLoan = new HomeLoan(homeLoanId, loan.getLoanId(), loan.getCustomer(), loan.getPrincipalAmount(),
                            loan.getInterestRate(), loan.getLoanTerm(), loan.getLoanType(), loan.getLoanStatus(), address, propVal);

                    PreparedStatement ps1 = con.prepareStatement(
                        "INSERT INTO homeLoan (homeLoanId, loanId, propertyAddress, propertyValue) VALUES (?, ?, ?, ?)"
                    );

                    ps1.setInt(1, homeLoan.getHomeLoanId());
                    ps1.setInt(2, homeLoan.getLoanId());
                    ps1.setString(3, homeLoan.getPropertyAddress());
                    ps1.setInt(4, homeLoan.getPropertyValue());

                    int rowsHomeLoan = ps1.executeUpdate();
                    System.out.println(rowsHomeLoan + " home loan record(s) updated.");
                } else if ("carLoan".equalsIgnoreCase(loan.getLoanType())) {
                    System.out.println("Enter the car model: ");
                    String model = sc.next();

                    System.out.println("Enter the car value: ");
                    int carVal = sc.nextInt();

                    int carLoanId = 0;
                    try (Statement stmt = con.createStatement()) {
                        ResultSet rs = stmt.executeQuery("SELECT carLoanId FROM carLoan ORDER BY carLoanId DESC LIMIT 1");
                        if (rs.next()) {
                            carLoanId = rs.getInt(1) + 1;
                        }
                    }

                    CarLoan carLoan = new CarLoan(carLoanId, loan.getLoanId(), loan.getCustomer(), loan.getPrincipalAmount(),
                            loan.getInterestRate(), loan.getLoanTerm(), loan.getLoanType(), loan.getLoanStatus(), model, carVal);

                    PreparedStatement ps2 = con.prepareStatement(
                        "INSERT INTO carLoan (carLoanId, loanId, carModel, carValue) VALUES (?, ?, ?, ?)"
                    );

                    ps2.setInt(1, carLoan.getCarLoanId());
                    ps2.setInt(2, carLoan.getLoanId());
                    ps2.setString(3, carLoan.getCarModel());
                    ps2.setInt(4, carLoan.getCarValue());

                    int rowsCarLoan = ps2.executeUpdate();
                    System.out.println(rowsCarLoan + " car loan record(s) updated.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Error applying loan in the database.");
            }
        } else {
            // Loan application discarded
            System.out.println("Loan application discarded.");
        }
    }

    @Override
    public double calculateInterest(int i){
        try (Connection con = DatabaseConnection.getConnection();
             Statement stmt = con.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT principalAmount, interestRate, loanTerm FROM loan WHERE loanId = " + i);
            if (rs.next()) {
                int principalAmount = rs.getInt("principalAmount");
                int interestRate = rs.getInt("interestRate");
                int loanTerm = rs.getInt("loanTerm");

                return (principalAmount * interestRate * loanTerm) / 12.0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error calculating interest.");
        }
        return 0;
    }

    @Override
    public void loanStatus(int loanId){
        try (Connection con = DatabaseConnection.getConnection();
             Statement stmt = con.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT creditScore FROM customer WHERE customerId = (SELECT customerId FROM loan WHERE loanId = " + loanId + ")");
            if (rs.next()) {
                int creditScore = rs.getInt("creditScore");

                if (creditScore > 650) {
                    System.out.println("Your Loan is approved as your credit score is more than 650");
                } else {
                    System.out.println("Sorry!! Your Loan has not approved as your credit score is less than 650");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error fetching loan status.");
        }
    }

    @Override
    public double calculateEMI(int loanId){
        try (Connection con = DatabaseConnection.getConnection();
             Statement stmt = con.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT principalAmount, interestRate, loanTerm FROM loan WHERE loanId = " + loanId);
            if (rs.next()) {
                int principalAmount = rs.getInt("principalAmount");
                int interestRate = rs.getInt("interestRate");
                int loanTerm = rs.getInt("loanTerm");

                double monthlyInterestRate = (double) interestRate / 12 / 100;
                return (principalAmount * monthlyInterestRate * Math.pow(1 + monthlyInterestRate, loanTerm)) /
                        (Math.pow(1 + monthlyInterestRate, loanTerm) - 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error calculating EMI.");
        }
        return 0;
    }

    @Override
    public void loanRepayment(int loanId, double amount){
        double emi = calculateEMI(loanId);
        if (emi > amount) {
            System.out.println("Amount is too low. Please increase the amount.");
        } else {
            double noOfEmi = amount / emi;
            System.out.println("With this amount, you can pay " + noOfEmi + " months of EMI.");
        }
    }

    @Override
    public void getAllLoan() {
        try (Connection con = DatabaseConnection.getConnection();
             Statement stmt = con.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT c.name, l.loanId, l.customerId, l.principalAmount, l.interestRate, l.loanTerm, l.loanType, l.loanStatus " +
                    "FROM loan l LEFT JOIN customer c ON l.customerId = c.customerId");

            while (rs.next()) {
                System.out.println("Customer Name: " + rs.getString(1) +
                                   ",\n Loan ID: " + rs.getInt(2) +
                                   ",\n Customer ID: " + rs.getInt(3) +
                                   ",\n Principal Amount: " + rs.getInt(4) +
                                   ",\n Interest Rate: " + rs.getInt(5) +
                                   ",\n Loan Term: " + rs.getInt(6) +
                                   ",\n Loan Type: " + rs.getString(7) +
                                   ",\n Loan Status: " + rs.getString(8)+"\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error fetching loan details.");
        }
    }

    @Override
    public void getLoanId(int loanId){
        try (Connection con = DatabaseConnection.getConnection();
             Statement stmt = con.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT * FROM loan WHERE loanId = " + loanId);
            if (rs.next()) {
                System.out.println("Loan ID: " + rs.getInt(1) +
                                   ", Customer ID: " + rs.getInt(2) +
                                   ", Principal Amount: " + rs.getInt(3) +
                                   ", Interest Rate: " + rs.getInt(4) +
                                   ", Loan Term: " + rs.getInt(5) +
                                   ", Loan Type: " + rs.getString(6) +
                                   ", Loan Status: " + rs.getString(7));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error fetching loan details by ID.");
        }
    }
}
