package com.triplaud;

import com.triplaud.maps.UploadObject;

import greendroid.app.GDApplication;

public class MyApplication extends GDApplication {
    private UploadObject obj;

    public UploadObject getObj() {
        return obj;
    }

    public void setObj(UploadObject obj) {
        this.obj = obj;
    }

}
