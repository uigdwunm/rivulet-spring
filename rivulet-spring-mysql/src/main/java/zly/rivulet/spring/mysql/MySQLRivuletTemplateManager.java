package zly.rivulet.spring.mysql;

import zly.rivulet.base.Rivulet;
import zly.rivulet.base.convertor.ResultConvertor;
import zly.rivulet.base.convertor.StatementConvertor;
import zly.rivulet.mysql.MySQLRivuletManager;
import zly.rivulet.mysql.MySQLRivuletProperties;
import zly.rivulet.spring.mysql.support.JsonType;

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

    public void supportJsonType(StatementConvertor<JsonType> statementConvertor, ResultConvertor<String, JsonType> resultConvertor) {
        // 注册json类型的转换器，
        convertorManager.registerSuperClassConvertor(statementConvertor, resultConvertor);
    }
}
