import java.util.concurrent.atomic.*;

public class Statistics {
    private final AtomicInteger newClients = new AtomicInteger(0);
    private final AtomicInteger requests = new AtomicInteger(0);
    private final AtomicInteger addOps = new AtomicInteger(0);
    private final AtomicInteger subOps = new AtomicInteger(0);
    private final AtomicInteger mulOps = new AtomicInteger(0);
    private final AtomicInteger divOps = new AtomicInteger(0);
    private final AtomicInteger errors = new AtomicInteger(0);
    private final AtomicLong sum = new AtomicLong(0);

    public void reset() {
        newClients.set(0);
        requests.set(0);
        addOps.set(0);
        subOps.set(0);
        mulOps.set(0);
        divOps.set(0);
        errors.set(0);
        sum.set(0);
    }

    public void incrementClients() { newClients.incrementAndGet(); }
    public void incrementRequests() { requests.incrementAndGet(); }
    public void incrementAdd() { addOps.incrementAndGet(); }
    public void incrementSub() { subOps.incrementAndGet(); }
    public void incrementMul() { mulOps.incrementAndGet(); }
    public void incrementDiv() { divOps.incrementAndGet(); }
    public void incrementErrors() { errors.incrementAndGet(); }
    public void addToSum(long value) { sum.addAndGet(value); }

    public int getClients() { return newClients.get(); }
    public int getRequests() { return requests.get(); }
    public int getAddOps() { return addOps.get(); }
    public int getSubOps() { return subOps.get(); }
    public int getMulOps() { return mulOps.get(); }
    public int getDivOps() { return divOps.get(); }
    public int getErrors() { return errors.get(); }
    public long getSum() { return sum.get(); }

}