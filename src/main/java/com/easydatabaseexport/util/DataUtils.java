package com.easydatabaseexport.util;

import com.easydatabaseexport.entities.TableParameter;

import java.util.List;

/**
 * DataUtils
 *
 * @author lzy
 * @date 2021/2/28 15:13
 **/
public class DataUtils {
    public DataUtils() {
    }

    public static <T> Object[][] toArray(List<T> data) {
        //data.get(0).toString().split(",").length相信大家应该可以理解的，这个是二维数组第二维的大小，如果不填写，则会报空指针的错误；
        //如果填写具体值 的话，就不能起到动态加载的作用了，这个值不应该是固定的，应该是可变的。
        //动态分配一个二维数组的
        if (data.size() == 0) {
            return null;
        }
        Object[][] o = new Object[data.size()][data.get(0).toString().split(",").length];
        for (int i = 0; i < data.size(); i++) {
            TableParameter medicine = (TableParameter) data.get(i);
            o[i][0] = medicine.getColumnName();
            o[i][1] = medicine.getColumnType();
            o[i][2] = medicine.getLength();
            o[i][3] = medicine.getIsNullAble();
            o[i][4] = medicine.getColumnDefault();
            o[i][5] = medicine.getDecimalPlaces();
            o[i][6] = medicine.getColumnComment();
        }
        return o;
    }
}
