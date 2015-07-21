package org.openmrs.module.emrmonitor.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.loader.custom.Return;
import org.openmrs.api.context.Context;
public class ExtraSystemInformation {

	private static final long serialVersionUID = 1L;
	
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private String totalPhyisycalMemory;

	private String freePhyisycalMemory;

	private String availablePhyisycalMemory;

	private String usedPhyisycalMemory;
	 
	private SessionFactory factory;
	
	public SessionFactory getFactory() {
		return factory;
	}

	public void setFactory(SessionFactory factory) {
		this.factory = factory;
	}


	File[] roots = File.listRoots();
    

	public String getTotalPhyisycalMemory() {
		
		for (File root : roots) {
			totalPhyisycalMemory=root.getTotalSpace()/(1024*1024*1024)+" GiB";
		}
		
		return totalPhyisycalMemory;
	}

	public void setTotalPhyisycalMemory(String totalPhyisycalMemory) {
		this.totalPhyisycalMemory = totalPhyisycalMemory;
	}

	public String getFreePhyisycalMemory() {
		
		for (File root : roots) {
			freePhyisycalMemory=root.getFreeSpace()/(1024*1024*1024)+" GiB";
		}
		
		return freePhyisycalMemory;
	}

	public void setFreePhyisycalMemory(String freePhyisycalMemory) {
		this.freePhyisycalMemory = freePhyisycalMemory;
	}

	public String getAvailablePhyisycalMemory() {
		
		for (File root : roots) {
			availablePhyisycalMemory=root.getUsableSpace()/(1024*1024*1024)+" GiB";
		}
		
		return availablePhyisycalMemory;
	}

	public void setAvailablePhyisycalMemory(String availablePhyisycalMemory) {
		this.availablePhyisycalMemory = availablePhyisycalMemory;
	}

	public String getUsedPhyisycalMemory() {
		
		for (File root : roots) {
			usedPhyisycalMemory=(root.getTotalSpace()-root.getFreeSpace())/(1024*1024*1024)+" GiB";
		}
		
		return usedPhyisycalMemory;
	}

	public void setUsedPhyisycalMemory(String usedPhyisycalMemory) {
		this.usedPhyisycalMemory = usedPhyisycalMemory;
	}
public String getVersion(String command) {
	String s=null;
	Process p;
	try {
	    p = Runtime.getRuntime().exec(command);
	    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
	    p.waitFor();
	    s = br.readLine();
	    p.destroy();
	    return s;    
	} catch (Exception e) {			
	}		
	return "";
}


public String checkConnection() {
	
	String[] addresses=Context.getAdministrationService().getGlobalProperty("emrmonitor.productionServer.ips").split("|");
    for(String addr:addresses){
    	InetAddress inet;
    	try {
    		inet = InetAddress.getByName(addr);
			if(inet.isReachable(5000))
				return "Connected";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    		
    }		
		return "Not Connected";
}

public double getCPULoadAverage() {

    OperatingSystemMXBean osBean=(OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	return osBean.getSystemLoadAverage();
}
public String getCPUInfo(String startsWith) {

	String s;
	Process p;
	try {
	    p = Runtime.getRuntime().exec("lscpu");
	    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
	    //s = br.readLine();
	    while ((s=br.readLine())!=null) {
	    	if(s.startsWith(startsWith))
	    		return s.split(":")[1].trim();
	        System.out.println(s);
		}
	        
	} catch (Exception e) {}
	return "";
	}



	public Map<String, Map<String, String>> getExtraSystemInformation(){
		
		Map<String, Map<String, String>> extraSystemInformation = new HashMap<String, Map<String,String>>();
		
		extraSystemInformation.put("SystemInfo.title.hardDriverInformation", new LinkedHashMap<String, String>() {
        	
        private static final long serialVersionUID = 1L;
			
			{
				put("SystemInfo.hardDriverInformation.totalMemory", getTotalPhyisycalMemory());
				put("SystemInfo.hardDriverInformation.freeMemory", getFreePhyisycalMemory());
				put("SystemInfo.hardDriverInformation.availableMemory", getAvailablePhyisycalMemory());
				put("SystemInfo.hardDriverInformation.usedMemory", getUsedPhyisycalMemory());				
			}
		});
        
        
		extraSystemInformation.put("SystemInfo.title.emrPatientsDataInformation", new LinkedHashMap<String, String>() {

            private static final long serialVersionUID = 1L;

            {
                
             	put("SystemInfo.emrDataInformation.numberOfOrders", ""+Context.getService(EmrMonitorService.class).getOpenmrsData().get("orders"));
             	put("SystemInfo.emrDataInformation.numberOfPatients", ""+Context.getService(EmrMonitorService.class).getOpenmrsData().get("patients"));
             	put("SystemInfo.emrDataInformation.numberOfEncounters", ""+Context.getService(EmrMonitorService.class).getOpenmrsData().get("encounters"));
             	put("SystemInfo.emrDataInformation.numberOfObservations", ""+Context.getService(EmrMonitorService.class).getOpenmrsData().get("observations"));
                
            }
        });
		
		
		extraSystemInformation.put("SystemInfo.title.emrSyncDataInformation", new LinkedHashMap<String, String>() {

            private static final long serialVersionUID = 1L;

            {                
             	put("SystemInfo.emrSyncDataInformation.pendingRecords", ""+Context.getService(EmrMonitorService.class).getOpenmrsData().get("pendingRecords"));
             	put("SystemInfo.emrSyncDataInformation.failedRecord", ""+Context.getService(EmrMonitorService.class).getOpenmrsData().get("failedRecord"));
             	      
            }
        });
		
		extraSystemInformation.put("SystemInfo.title.softwareVersionInformation", new LinkedHashMap<String, String>() {

            private static final long serialVersionUID = 1L;

            {                
            	put("SystemInfo.softwareVersionInformation.ubuntu", ""+getVersion("lsb_release --release").split(":")[1].trim());
            	put("SystemInfo.softwareVersionInformation.mysql", ""+Context.getService(EmrMonitorService.class).getOpenmrsData().get("mysqlVersion").split("-")[0]);
             	put("SystemInfo.softwareVersionInformation.tomcat", ""+getVersion("java -cp "+Context.getAdministrationService().getGlobalProperty("emrmonitor.tomcatPath")+"/lib/catalina.jar org.apache.catalina.util.ServerInfo").split(":")[1].trim());
             	put("SystemInfo.softwareVersionInformation.Firefox", ""+getVersion("firefox -version"));
             	put("SystemInfo.softwareVersionInformation.Chrome", ""+getVersion("google-chrome -version"));
             	
            }
        });
		
		extraSystemInformation.put("SystemInfo.title.connectionInformation", new LinkedHashMap<String, String>() {

            private static final long serialVersionUID = 1L;
            
            
            {                
            	put("SystemInfo.connectionInformation.connectedToTheServer", ""+checkConnection());
            	
            }
        });		
		extraSystemInformation.put("SystemInfo.title.cpuInformation", new LinkedHashMap<String, String>() {

            private static final long serialVersionUID = 1L;
            
            
            {                
            	put("SystemInfo.connectionInformation.cpuLoadAverage", ""+getCPULoadAverage());
            	put("SystemInfo.connectionInformation.architecture", ""+getCPUInfo("Architecture:"));
            	put("SystemInfo.connectionInformation.cpuopmodes", ""+getCPUInfo("CPU op-mode(s):"));
            	put("SystemInfo.connectionInformation.numberOfCPU", ""+getCPUInfo("CPU(s):"));
            	put("SystemInfo.connectionInformation.cpusize", ""+getCPUInfo("CPU MHz"));
            	
            }
        });		
				
		
		return extraSystemInformation;		
	}
	
}