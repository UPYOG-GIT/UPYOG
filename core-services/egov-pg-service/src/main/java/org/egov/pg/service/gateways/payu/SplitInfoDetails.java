import java.util.List;

public class SplitInfoDetails {
    private String splitStatus;
    private List<SplitSegment> splitSegments;

    public String getSplitStatus() { return splitStatus; }
    public void setSplitStatus(String splitStatus) { this.splitStatus = splitStatus; }

    public List<SplitSegment> getSplitSegments() { return splitSegments; }
    public void setSplitSegments(List<SplitSegment> splitSegments) { this.splitSegments = splitSegments; }
}

class SplitSegment {
    private String merchantKey;
    private double amount;
    private double subvention_amount;
    private String txnId;
    private double additional_charges;
    private double transaction_fee;

    // Getters and Setters
    public String getMerchantKey() { return merchantKey; }
    public void setMerchantKey(String merchantKey) { this.merchantKey = merchantKey; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getSubvention_amount() { return subvention_amount; }
    public void setSubvention_amount(double subvention_amount) { this.subvention_amount = subvention_amount; }

    public String getTxnId() { return txnId; }
    public void setTxnId(String txnId) { this.txnId = txnId; }

    public double getAdditional_charges() { return additional_charges; }
    public void setAdditional_charges(double additional_charges) { this.additional_charges = additional_charges; }

    public double getTransaction_fee() { return transaction_fee; }
    public void setTransaction_fee(double transaction_fee) { this.transaction_fee = transaction_fee; }
}