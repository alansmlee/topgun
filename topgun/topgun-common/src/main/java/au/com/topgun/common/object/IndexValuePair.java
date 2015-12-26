package au.com.topgun.common.object;

public class IndexValuePair extends KeyValuePair<Integer, String> {
    public IndexValuePair(int index, String value) {
        super(index, value);
    }
    public int getIndex() {
        return this.getKey();
    }
}
