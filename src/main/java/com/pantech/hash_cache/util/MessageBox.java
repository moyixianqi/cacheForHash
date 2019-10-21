package com.pantech.hash_cache.util;


public class MessageBox<T> extends Message {
    private T data;

    public MessageBox() {
        super();
    }

    public MessageBox(int status, String message) {
        super(status, message);
    }

    public MessageBox(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public MessageBox setData(T data) {
        this.data = data;
        return this;
    }

    public boolean hasData() {
        return this.data != null;
    }

    public static MessageBox send(Object data, Object... params) {
        int status = STATUS_OK;
        String message = MESSAGE_OK;
        boolean getStatus = false;
        boolean getMsg = false;
        for (Object param: params) {
            if (param instanceof Integer && !getStatus) {
                status = (Integer)param;
                getStatus = true;
            } else if (param instanceof String && !getMsg) {
                message = (String)param;
                getMsg = true;
            }
            if (getStatus && getMsg) {
                break;
            }
        }

        MessageBox mb =  new MessageBox(status, message);
        return mb.setData(data);
    }

}
