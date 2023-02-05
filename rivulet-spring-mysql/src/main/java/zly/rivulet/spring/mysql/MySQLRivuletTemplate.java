package zly.rivulet.spring.mysql;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import zly.rivulet.mysql.MySQLRivulet;
import zly.rivulet.spring.mysql.connection.ConnectionTransactionSynchronization;
import zly.rivulet.spring.mysql.connection.SQLRivuletConnectionHolder;
import zly.rivulet.spring.base.scan.listener.RivuletTemplateCreatedEvent;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class MySQLRivuletTemplate extends MySQLRivulet implements ApplicationEventPublisherAware, InitializingBean, BeanNameAware {

    private final MySQLRivuletTemplateManager rivuletManager;

    private ApplicationEventPublisher applicationEventPublisher;

    private String beanName;

    public MySQLRivuletTemplate(MySQLRivuletTemplateManager rivuletManager) {
        super(rivuletManager);
        this.rivuletManager = rivuletManager;
    }

    @Override
    protected Connection useConnection() {
        // 先从事务管理器中获取
        SQLRivuletConnectionHolder holder = (SQLRivuletConnectionHolder) TransactionSynchronizationManager.getResource(this);
        if (holder != null && holder.isSynchronizedWithTransaction()) {
            holder.requested();
            return holder.getConnection();
        }

        // 事物管理器中没有，新创建一个connection
        DataSource dataSource = rivuletManager.getDataSource();
        Connection connection = DataSourceUtils.getConnection(dataSource);
        boolean autoCommit;
        try {
            autoCommit = connection.getAutoCommit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        boolean isConnectionTransactional = DataSourceUtils.isConnectionTransactional(connection, dataSource);

        // 注册到事务管理器中
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            holder = new SQLRivuletConnectionHolder(connection, isConnectionTransactional, autoCommit);
            TransactionSynchronizationManager.bindResource(this, holder);
            ConnectionTransactionSynchronization transactionSynchronization = new ConnectionTransactionSynchronization(holder, dataSource, this);
            TransactionSynchronizationManager.registerSynchronization(transactionSynchronization);
        }
        return connection;
    }

    @Override
    protected void close() {
        throw new UnsupportedOperationException("事物操作已经托管给事务管理器了");
    }

    @Override
    public void commit() {
        throw new UnsupportedOperationException("事物操作已经托管给事务管理器了");
    }

    @Override
    public void rollback() {
        throw new UnsupportedOperationException("事物操作已经托管给事务管理器了");
    }

    public MySQLRivuletTemplateManager getMySQLRivuletTemplateManager() {
        return rivuletManager;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void afterPropertiesSet() {
        applicationEventPublisher.publishEvent(new RivuletTemplateCreatedEvent(this, beanName));
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
}
