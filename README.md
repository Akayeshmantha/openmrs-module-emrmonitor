## OpenMRS EMR Monitor Module

### Description
OpenMRS module that can be used to monitor various metrics over time, and to share these metrics with a central server

### OpenMRS Wiki
[EMR Monitor Module](https://wiki.openmrs.org/display/projects/EMR+Monitor+Module)

### Overview

#### Server
An __EmrMonitorServer__ represents a particular OpenMRS instance that is available for monitoring.  There are two types of servers:
* LOCAL:  The OpenMRS instance that this module is installed into
* CHILD:  A remote OpenMRS instance that has registered itself into this server and submits reports of it's metrics on a regular basis

The expected usage is that, within a network of servers, one server would be designated as the "parent", and the rest would be designated as "child".
Each of the child servers would report their metrics up to the parent.

#### Report
An __EmrMonitorReport__ encapsulates a particular collection of metrics that are generated at a particular time for a particular server.
All metrics are associated with a Report and a Report is also the unit of transmission between a child and a parent server.

#### Report Metric
An __EmrMonitorReportMetric__ is a particular data element that is obtained on a particular server at a particular point in time.  Metrics are generated
by a particular instance of a __MetricProducer__.  Although these can be invoked on demand, they are typically run in the context of producing a periodic
__EmrMonitorReport__ for a particular server.

#### Metric Producer
A __MetricProducer__ is a component registered with Spring that implements the MetricProducer interface and returns a Map<String, String> of metrics for a
given server as of that particular execution time.  Implementations can extend the emrmonitor module to capture their own custom metrics by adding their own
__MetricProducer__ implementations in a module.  There are several built-in metric producers that run by default.

An implementation can disable any of these metric producers explicitly by adding their namespace (comma-separated) to the global property named __emrmonitor.disabledMetricProducers__

### Workflow

1. A "LOCAL" __EmrMonitorServer__ server is automatically created on a given OpenMRS instance.
2. A scheduled task executes once per day, and generates a daily __EmrMonitorReport__ for the LOCAL server.  This process iterates across all __MetricProducer__ components and
   adds an __EmrMonitorReportMetric__ to the report for all generated metrics.
3. A history of these reports is available for viewing on the local server
4. If this server has a "parent" configured (via OpenMRS runtime properties), another scheduled task executes every few minutes to check for any __EmrMonitorReport__ that has
   been generated but not transmitted to the parent.  An __EmrMonitorReport__ can have one of the following statuses:
   * LOCAL_ONLY:  No parent is configured, do not transmit
   * WAITING_TO_SEND:  A parent is configured, but the Report has not yet been sent successfully to the parent (only found on a child server)
   * SENT:  A parent is configured and the report has successfully been sent and recieved by the parent (only found on a child server)
   * RECEIVED:  A report was successfully received on this server after it was submitted by a child (only found on a parent server)

### Rest Interface

The module provides a REST API for managing servers and reports.

- http://localhost:8080/openmrs/ws/rest/v1/emrmonitor/server
- http://localhost:8080/openmrs/ws/rest/v1/emrmonitor/report

### TODO

* UI
- Get compare page working
- Show history by date for a given server and bring up any date
- Visualize metrics in a tree hierarchy, based on dot notation
- Clean up javascript and CSS as needed

* Do not sync these tables

* ConfigurableMetricProducer: would load xml/other files from somewhere in the .OpenMRS directory, parse these, and allow for executing things like:
- SQL queries
- Runtime commands on the underlying O/S
- Groovy scripts
}

* Task to clean up history of log files or reports (if desired, to save space)

* Add last connection time to the server table, and a task with web service for hitting it from child more frequently than daily

* ReportConfig:
- trigger_type (cron, startup, context_refresh, event)
- trigger_config (used by the type)
- template (simple template that allows for text replacement of metrics by key)
- transmission_url
- transmission_username
- transmission_password
(can we use key-based authentication here instead?)

* Document REST interface above

* Service Methods:
- List<EmrMonitorReport> getEmrMonitorReports(EmrMonitorReportQuery);
- Map<EmrMonitorServer, EmrMonitorReport> getLatestEmrMonitorReports();
- EmrMonitorReport getLatestEmrMonitorReport(EmrMonitorServer);
- EmrMonitorReportMetric getEmrMonitorReportMetrics(EmrMonitorReportMetricQuery);