import java.util.ArrayList;
import java.util.List;

/**
 * Created by lanev_000 on 4.04.2016.
 */
public class Globals {
    private List<Integer> openedConnections = new ArrayList<Integer>();
    private int connectionLimit = 10;
    private int minConnection = 1337;

    public int getMaxConnection() {
        return minConnection + connectionLimit;
    }
    public int getMinConnection() {
        return minConnection;
    }

    public List<Integer> getOpenedConnections() {
        return openedConnections;
    }

    public int getConnectionLimit() {
        return connectionLimit;
    }

    public void setOpenedConnections(List<Integer> openedConnections) {
        this.openedConnections = openedConnections;
    }

    public void setConnectionLimit(int connectionLimit) {
        this.connectionLimit = connectionLimit;
    }
}
