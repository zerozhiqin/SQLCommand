#usage

bean

```Java

@Table("table")
public class SampleSqlBean {
	@PrimaryKey("ID")
	public int id;
	
	@Column("colA")
	public String colOne;
	
	@Column("colB")
	public String colTwo;
}

```

sql

```Java

// create table
SQL.create(SampleSqlBean.class);

// drop table
SQL.drop(SampleSqlBean.class);

// read
List<SampleSqlBean> result = SQL.select(SampleSqlBean.class).execute();

// with arguments
List<SampleSqlBean> result = SQL.select(SampleSqlBean.class).where("id = ?", 1024).execute();

// update by bean's primaryKey
SQL.update(bean).execute();

....


```
