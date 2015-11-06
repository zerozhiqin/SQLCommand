package dev.zhiqin.sqlcommand.sample;


import dev.zhiqin.sqlcommand.annotation.Column;
import dev.zhiqin.sqlcommand.annotation.PrimaryKey;
import dev.zhiqin.sqlcommand.annotation.Table;

@Table("sample_table")
public class TestTable {

    public TestTable() {
    }

    public TestTable(String type, String shopId, String intro, long saveTime, String kee) {
        this.type = type;
        this.shopId = shopId;
        this.intro = intro;
        this.saveTime = saveTime;
        this.kee = kee;
    }

    @PrimaryKey
    public long _id;

    @Column("type")
    public String type;

    @Column("shopId")
    public String shopId;

    @Column("intro")
    public String intro;

    @Column("saveTime")
    public long saveTime;

    public String kee;

    @Override
    public String toString() {
        return "TestTable{" +
                "_id=" + _id +
                ", type='" + type + '\'' +
                ", shopId='" + shopId + '\'' +
                ", intro='" + intro + '\'' +
                ", saveTime=" + saveTime +
                '}';
    }
}
