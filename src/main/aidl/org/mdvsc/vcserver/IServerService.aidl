package org.mdvsc.vcserver;

interface IServerService {
    void startServer();
    void stopServer();
    void setListenPort(int port);
    void setWorkPath(String path);
    void modifyAdminPassword(String password);
    void modifyUserPassword(String user, String password);
    boolean verifyAdminPassword(String password);
    boolean isServerStarted();
    int getListenPort();
    String getServerUrl();
    String getWorkPath();
}

