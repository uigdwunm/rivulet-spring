package zly.rivulet.spring.mysql.connection;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import zly.rivulet.sql.SQLRivulet;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionTransactionSynchronization implements TransactionSynchronization {
    private final SQLRivuletConnectionHolder holder;

    private final DataSource dataSource;

    private final SQLRivulet sqlRivuletTemplate;

    private boolean holderActive = true;

    public ConnectionTransactionSynchronization(
        SQLRivuletConnectionHolder holder,
        DataSource dataSource,
        SQLRivulet sqlRivuletTemplate
    ) {
        this.holder = holder;
        this.dataSource = dataSource;
        this.sqlRivuletTemplate = sqlRivuletTemplate;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void suspend() {
        if (this.holderActive) {
            TransactionSynchronizationManager.unbindResource(this.sqlRivuletTemplate);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resume() {
        if (this.holderActive) {
            TransactionSynchronizationManager.bindResource(this.sqlRivuletTemplate, this.holder);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeCommit(boolean readOnly) {
        if (TransactionSynchronizationManager.isActualTransactionActive() && !holder.isConnectionTransactional() && !holder.isAutoCommit()) {
            try {
                this.holder.getConnection().commit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeCompletion() {
        // Issue #18 Close SqlSession and deregister it now
        // because afterCompletion may be called from a different thread
        if (!this.holder.isOpen()) {
            TransactionSynchronizationManager.unbindResource(sqlRivuletTemplate);
            this.holderActive = false;
            Connection connection = holder.getConnection();
            DataSourceUtils.releaseConnection(connection, this.dataSource);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterCompletion(int status) {
        if (this.holderActive) {
            TransactionSynchronizationManager.unbindResourceIfPossible(sqlRivuletTemplate);
            this.holderActive = false;
            Connection connection = holder.getConnection();
            DataSourceUtils.releaseConnection(connection, this.dataSource);
        }
        this.holder.reset();
    }
}
