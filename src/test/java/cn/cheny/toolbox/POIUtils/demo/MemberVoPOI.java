package cn.cheny.toolbox.POIUtils.demo;

import cn.cheny.toolbox.POIUtils.annotation.ExcelCell;
import cn.cheny.toolbox.POIUtils.annotation.ExcelData;
import cn.cheny.toolbox.POIUtils.annotation.ExcelHead;

import java.io.Serializable;

import static cn.cheny.toolbox.POIUtils.annotation.ExcelData.SwitchType.COLUMN_NUM;

/**
 * Created by hboxs011 on 2017/8/21.
 */
@ExcelHead(headTitle = "这是一个标题")
public class MemberVoPOI implements Serializable {

    private static final long serialVersionUID = 5312112449334866041L;

    @ExcelData(column = 0, type = COLUMN_NUM)
    @ExcelCell(name = "微信微信微信")
    private String wechat;

    @ExcelData(column = 1, type = COLUMN_NUM)
    @ExcelCell(name = "姓名姓名姓名")
    private String name;

    @ExcelData(column = 2, type = COLUMN_NUM)
    @ExcelCell(name = "地址地址地址")
    private String address;

    @ExcelData(column = 3, type = COLUMN_NUM)
    @ExcelCell(name = "电话号码")
    private String phone;

    public String getWechat() {
        return wechat;
    }

    public void setWechat(String wechat) {
        this.wechat = wechat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "MemberVoPOI{" +
                "wechat='" + wechat + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
