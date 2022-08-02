package com.easydatabaseexport.entities;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.swing.ImageIcon;
import java.awt.Component;

/**
 * ErrorMsg
 *
 * @author lzy
 * @date 2021/7/13 10:45
 **/
@Data
@Accessors(chain = true)
public class ErrorMsg {
    private Component parentComponent;
    private Object message;
    private String title;
    private int messageType;
    private ImageIcon imgIcon;
}
