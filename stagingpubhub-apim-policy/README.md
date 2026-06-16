# Azure APIM Policy

## CPPI Results API
The Results API will offer external parties subscribed to CPPI i.e. Non Police Prosecutor, the ability to call 
the Common Platform to retrieve results of cases they have submitted to the platform.

Results API properties
### Query parameters:
 - startdate, in the format YYYY-MM-DD
 - enddate, in the format YYYY-MM-DD

### Headers:
 - CJSOUCODE, Organisation unit code which is set from client certificate
 - Authorization, Contains shared secret

### APIM inbound policy will redirect the client request to stagingpubhub query API as below
```
/stagingpubhub-query-api/query/api/rest/stagingpubhub/v1/results/<oucode>/?startDate=<startdate>&endDate=<enddate>
```

### APIM outbound policy will intercept the response and checks following
 - result code is 200 and no results found for the prosecutor, then updates the status code to 204

## Test instructions

### Azure [Portal](https://portal.azure.com/)

#### _Api Management service_ **spnl-apim-int** 
The policy file [CPPIGetResultsAPIMPolicy.txt](config/CPPIGetResultsAPIMPolicy.txt) exists in the following location:
```
APIs -> CPPI -> v3 -> GET Results -> Inbound processing -> base
```

The **Test** tab at the top will allow you to test the policy file. 
1. Fill in **query parameters** which are
 - startdate in the format YYYY-MM-DD e.g. 2021-02-24
 - enddate in the format YYYY-MM-DD e.g. 2021-02-24

2. Fill in **Headers** which are 
 - CJSOUCODE e.g. 007WZ31 
 - Authorization e.g. key TVL-secret

3. Select **Apply product scope** from dropdown to be _Starter_

4. Check the _Bypass CORS proxy_ checkbox and hit send.

The **HTTP response -> Message** tab will return the status code.

The **HTTP response -> Trace** tab will explain the outcome of policy expressions and http requests the call had made.

