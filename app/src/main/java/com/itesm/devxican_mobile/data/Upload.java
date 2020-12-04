package com.itesm.devxican_mobile.data;

public class Upload {

    public String url, name;

    public Upload() {

    }

    public Upload(String name, String url) {
        if(name.trim().equals("")){
            name = "Anonymous";
        }

        this.name = name;
        this.url = url;
    }

}
