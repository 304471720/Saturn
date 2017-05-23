/**
 * 
 */
package com.vip.saturn.job.console.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vip.saturn.job.sharding.listener.AbstractConnectionListener;
import org.apache.curator.framework.CuratorFramework;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author chembo.huang
 *
 */
public class ZkCluster implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String zkAlias;
	
	private String zkAddr;

	private String digest;
	
	private boolean offline = false;
	
	@JsonIgnore
	private transient  CuratorFramework curatorFramework;

	@JsonIgnore
	private transient AbstractConnectionListener connectionListener;
	
	@JsonIgnore
	private transient ArrayList<RegistryCenterConfiguration> regCenterConfList = new ArrayList<>();

	public ZkCluster() {
	}

	public String getZkAlias() {
		return zkAlias;
	}

	public void setZkAlias(String zkAlias) {
		this.zkAlias = zkAlias;
	}

	public String getZkAddr() {
		return zkAddr;
	}

	public void setZkAddr(String zkAddr) {
		this.zkAddr = zkAddr;
	}

	public String getDigest() {
		return digest;
	}

	public void setDigest(String digest) {
		this.digest = digest;
	}

	public boolean isOffline() {
		return offline;
	}

	public void setOffline(boolean offline) {
		this.offline = offline;
	}

	public CuratorFramework getCuratorFramework() {
		return curatorFramework;
	}

	public void setCuratorFramework(CuratorFramework curatorFramework) {
		this.curatorFramework = curatorFramework;
	}

	public AbstractConnectionListener getConnectionListener() {
		return connectionListener;
	}

	public void setConnectionListener(AbstractConnectionListener connectionListener) {
		this.connectionListener = connectionListener;
	}

	public ArrayList<RegistryCenterConfiguration> getRegCenterConfList() {
		return regCenterConfList;
	}

	public void setRegCenterConfList(ArrayList<RegistryCenterConfiguration> regCenterConfList) {
		this.regCenterConfList = regCenterConfList;
	}
}
