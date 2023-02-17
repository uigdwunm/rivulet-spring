# rivulet-spring
[rivulet本体项目](https://github.com/uigdwunm/rivulet)
## 说明文档
1. [spl语句映射规则](https://github.com/uigdwunm/rivulet/blob/main/docs/cn/%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E/sql%E8%AF%AD%E5%8F%A5%E6%98%A0%E5%B0%84%E8%A7%84%E5%88%99.md)
2. [扩展点](https://github.com/uigdwunm/rivulet/blob/main/docs/cn/%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E/%E6%89%A9%E5%B1%95%E7%82%B9.md)
## 快速开始（spring接入）
### 1、引入包
>
>&lt;dependency>\
>&ensp;&ensp;&ensp;&ensp;&lt;groupId>io.github.uigdwunm&lt;/groupId>\
>&ensp;&ensp;&ensp;&ensp;&lt;artifactId>rivulet-spring-mysql&lt;/artifactId>\
>&ensp;&ensp;&ensp;&ensp;&lt;version>0.0.9-PREVIEW&lt;/version>\
>&lt;/dependency>

### 2、配置Bean
```java
// 注册成一个springBean
@Configuration
// 这里配置mapper及desc所在包目录
@RivuletMetaScan("com.example")
public class DataSourceConfig {
    
  /**
   * @param dataSource 传入连接池
   * @return 返回MySQLRivuletTemplateManager,管理器
   **/
  @Bean
  public MySQLRivuletTemplateManager mySQLRivuletTemplateManager(DataSource dataSource) {
    return new MySQLRivuletTemplateManager(new MySQLRivuletProperties(), dataSource);
  }

  /**
   * @param rivuletTemplateManager 传入上面的管理器
   * @return 返回MySQLRivuletTemplate，真正执行都是通过这个来操作的
   **/
  @Bean
  public MySQLRivuletTemplate mySQLRivuletTemplate(MySQLRivuletTemplateManager rivuletTemplateManager) {
    return new MySQLRivuletTemplate(rivuletTemplateManager);
  }
}
```
### 3、映射表对象
``` sql
# 简单建一个表演示下
create table t_person (
    id       bigint auto_increment primary key,
    birthday date        not null comment '出生日期',
    name     varchar(64) not null comment '姓名',
    extra    json        null comment '额外信息'
);
```
映射上面的表模型对象
```java
/**
 * @SQLTable 标识SQL表，表名
 * 需要详细映射表的各种内容，必须有get、set方法
 * 
 * 虽然表映射有点麻烦，但是留存在代码里一份，查表信息时就不用再去找建表语句了，一劳永逸。
 * 以后也会考虑推出检查表元信息的能力，如果检查发现与数据库表信息不符，会做出提示。
 * 现在的信息还不够详细，以后加上索引等信息，还能实时的分析走索引情况。
 **/
@SQLTable("t_person")
public class PersonDO {

  /**
   * @PrimaryKey 标识主键
   * @SqlColumn 标识是个列字段，名称可以不填的话，会映射为字段名
   * @MySQLBigInt 标识数据库类型，ddl语句里是什么这里就是什么
   **/
  @PrimaryKey
  @SqlColumn
  @MySQLBigInt
  private Long id;

  @SqlColumn("birthday")
  @MySQLDate
  private LocalDate birthday;

  @SqlColumn
  @MySQLVarchar(length = 64)
  private String name;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public LocalDate getBirthday() {
    return birthday;
  }

  public void setBirthday(LocalDate birthday) {
    this.birthday = birthday;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
```

### 4、查询语句
```java
/**
 * 查询语句必须在@RivuletMetaScan("com.example")声明扫描的包内
 **/
public class RivuletPersonDesc {
  /**
   * 编写查询语句
   * 单独声明一个方法，注解@RivuletDesc，带上标识的key
   * 然后用静态代码描述出，sql语句
   * 具体描述方式后面会详细介绍，但是下面示例能看出来了和手写的sql语句是高度相似的，学习成本极低。
   **/
  @RivuletDesc("queryById")
  public WholeDesc queryPerson() {
    return SQLQueryBuilder.query(PersonDO.class, PersonDO.class)
            .select()
            .where(Condition.Equal.of(PersonDO::getId, Param.of(Long.class, "id")))
            .build();
  }
}
```

### 5、声明mapper
```java
/**
 * 查询语句必须在@RivuletMetaScan("com.example")声明扫描的包内
 * 必须声明@RivuletMapper注解才能识别到
 **/
@RivuletMapper
public interface RivuletPersonMapper {

  /**
   * @MapperKey对应查询语句时声明的@RivuletDesc的key
   **/
  @MapperKey("queryById")
  PersonDO findById(long id);
}
```

### 6、完成
然后就可以用spring获取Mapper对象进行调用了，和mybatis类似
