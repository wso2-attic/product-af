package org.wso2.carbon.appfactory.deployers;


import javax.activation.DataHandler;

public class UploadItem {
    private DataHandler dataHandler;
    private String fileName;

    public DataHandler getDataHandler() {
        return dataHandler;
    }

    public void setDataHandler(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
