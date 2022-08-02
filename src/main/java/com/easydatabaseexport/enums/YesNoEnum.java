package com.easydatabaseexport.enums;

/**
 * 是否枚举
 *
 * @author lzy
 */
public enum YesNoEnum {
    //是
    YES_1("1", "是"),
    //否
    NO_0("0", "否");

    private final String value;
    private final String label;

    private YesNoEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return this.value;
    }

    public String getLabel() {
        return this.label;
    }

    public static YesNoEnum valueOfCode(String value) {
        for (YesNoEnum enumEntity : values()) {
            if (enumEntity.getValue().equals(value)) {
                return enumEntity;
            }
        }
        return null;
    }

    public static boolean isValidValue(String value) {
        return null != valueOfCode(value);
    }
}
