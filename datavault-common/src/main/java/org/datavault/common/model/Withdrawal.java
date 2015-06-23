package org.datavault.common.model;

public class Withdrawal {
    
    String withdrawalPath;
    
    // Additional properties might go here - e.g. format
    
    public Withdrawal() {};

    public String getWithdrawalPath() {
        return withdrawalPath;
    }

    public void setWithdrawalPath(String withdrawalPath) {
        this.withdrawalPath = withdrawalPath;
    }
}
