package au.com.topgun.common.object;

public class MatchTracker<T> {

    public enum STATUS {
        NOT_MATCHED,
        MATCHED
    };
    
    private STATUS status = STATUS.NOT_MATCHED;
    public final T obj;
    
    public MatchTracker(T obj) {
        this.obj = obj;
    }
    
    public T getObj() {
        return obj;
    }
    
    public boolean isMatched() {
        return STATUS.MATCHED == status;
    }
    
    public void setMatched() {
        this.status = STATUS.MATCHED;
    }
}
