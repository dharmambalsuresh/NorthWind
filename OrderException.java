public class OrderException extends Exception {
    private int OrderReferenceNumber = 0;
    public OrderException(String Exception) {
        super(Exception);
        this.OrderReferenceNumber = 0;
    }

    public OrderException(String Exception, int referenceNumber) {
        super(Exception);
        this.OrderReferenceNumber = referenceNumber;
    }
}