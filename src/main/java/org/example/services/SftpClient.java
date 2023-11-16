package org.example.services;


import com.jcraft.jsch.*;

import java.util.Properties;
import java.util.Vector;

/**
 * A simple SFTP client using JSCH http://www.jcraft.com/jsch/
 */
public final class SftpClient {
    private final String      host;
    private final int         port;
    private final String      username;
    private final JSch        jsch;
    private       ChannelSftp channel;
    private       Session     session;

    /**
     * @param host     remote host
     * @param port     remote port
     * @param username remote username
     */
    public SftpClient(String host, int port, String username) {
        this.host     = host;
        this.port     = port;
        this.username = username;
        jsch          = new JSch();
    }

    /**
     * Use default port 22
     *
     * @param host     remote host
     * @param username username on host
     */
    public SftpClient(String host, String username) {
        this(host, 22, username);
    }

    /**
     * Authenticate with remote using password
     *
     * @param password password of remote
     * @throws JSchException If there is problem with credentials or connection
     */
    public void authPassword(String password) throws JSchException {
        session = jsch.getSession(username, host, port);
        //disable known hosts checking
        //if you want to set knows hosts file You can set with jsch.setKnownHosts("path to known hosts file");
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setPassword(password);
        session.connect();
        channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
    }

    /**
     * Upload a file to remote
     *
     * @param localPath  full path of location file
     * @param remotePath full path of remote file
     * @throws JSchException If there is any problem with connection
     * @throws SftpException If there is any problem with uploading file permissions etc
     */
    public void uploadFile(String localPath, String remotePath) throws JSchException, SftpException {
        System.out.printf("Uploading [%s] to [%s]...%n", localPath, remotePath);
        if (channel == null) {
            throw new IllegalArgumentException("Connection is not available");
        }
        //"SFTP attempt"
        channel.put(localPath, remotePath);
        //SFTP success"
    }

    /**
     * Download a file from remote
     *
     * @param remotePath full path of remote file
     * @param localPath  full path of where to save file locally
     * @throws SftpException If there is any problem with downloading file related permissions etc
     */
    public void downloadFile(String remotePath, String localPath) throws SftpException {
        System.out.printf("Downloading [%s] to [%s]...%n", remotePath, localPath);
        if (channel == null) {
            throw new IllegalArgumentException("Connection is not available");
        }
        channel.get(remotePath, localPath);
    }

    /**
     * Delete a file on remote
     *
     * @param remoteFile full path of remote file
     * @throws SftpException If there is any problem with deleting file related to permissions etc
     */
    public void delete(String remoteFile) throws SftpException {
        System.out.printf("Deleting [%s]...%n", remoteFile);
        if (channel == null) {
            throw new IllegalArgumentException("Connection is not available");
        }
        channel.rm(remoteFile);
    }

    /**
     * Disconnect from remote
     */
    public void close() {
        if (channel != null) {
            channel.exit();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}
