package zly.rivulet.spring.mysql;

import zly.rivulet.base.Rivulet;
import zly.rivulet.mysql.MySQLRivuletManager;
import zly.rivulet.mysql.MySQLRivuletProperties;

import javax.sql.DataSource;

public class MySQLRivuletTemplateManager extends MySQLRivuletManager {

    private final DataSource dataSource;

    public MySQLRivuletTemplateManager(MySQLRivuletProperties configProperties, DataSource dataSource) {
        super(configProperties);
        this.dataSource = dataSource;
    }

    @Override
    public Rivulet getRivulet() {
        throw new UnsupportedOperationException("spring中不要从这获取RivuletTemplate");
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
