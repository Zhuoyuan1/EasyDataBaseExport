package com.easydatabaseexport.enums;

/**
 * 模式
 *
 * <p>
 * 导入模式
 */
public enum ImportMode {
    /**
     * 追加：添加记录到配置
     */
    ADD,
    /**
     * 更新：更新目标和源记录相符的记录
     */
    UPDATE,
    /**
     * 追加或更新：如果目标存在相同记录，更新它。否则，添加它
     */
    ADD_OR_UPDATE,
    /**
     * 复制：删除目标全部记录，并从源重新导入
     */
    COPY;

    public ImportMode getByName(String name) {
        for (ImportMode mode : values()) {
            if (mode.name().equals(name)) {
                return mode;
            }
        }
        return null;
    }
}
