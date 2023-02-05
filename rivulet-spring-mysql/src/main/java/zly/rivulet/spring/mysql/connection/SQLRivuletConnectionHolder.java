package zly.rivulet.spring.mysql.connection;

import org.springframework.transaction.support.ResourceHolderSupport;

import java.sql.Connection;

public class SQLRivuletConnectionHolder extends ResourceHolderSupport {

    private final Connection connection;

    private final boolean isConnectionTransactional;

    private final boolean autoCommit;

    public SQLRivuletConnectionHolder(Connection connection, boolean isConnectionTransactional, boolean autoCommit) {
        this.connection = connection;
        this.isConnectionTransactional = isConnectionTransactional;
        this.autoCommit = autoCommit;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public boolean isConnectionTransactional() {
        return isConnectionTransactional;
    }
}
