# Data-Access-Manager_Data-Query
Data Access Manager &amp; Data Query for AFarCloud

Data Access Manager & Data Query (DAM & DQ) is designed to provide access to the repositories of the AFarCLoud project. However, it can be used for the management of other repositories with the only need to change the properties file and identify the new endpoint for data injection and retribution.


The component structure is designed for near real-time data management. The nature of the data with which the component works is characterised by three dimensions, spatial, temporal and semantic model (Data Model Schemas presented in 10.5281/zenodo.7263254). In this way, detailed information is provided for each measurement according to the following questions: When was the measurement or observation taken? Where is the device that performed the measurement geolocated? What information does the measurement in question provide?
As the component is developed in the framework of a precision agriculture project, it works with a specific data model for this scenario. The data structure for sensor and device observations, the state vectors of autonomous and semi-autonomous vehicles and collars installed on the neck of livestock are modelled. However, the component presents a simple adaptability for the acceptance of other data models. 


In order to provide an agile response to data injection and retribution requests, the DAM & DQ component provides access to both SQL (relational) and NoSQL (non-relational) databases at the same time. The purpose of maintaining two different database structures is to ensure the storage and possible retribution of historical data from time series oriented NoSQL databases. On the contrary, in the SQL database, only a small table with the latest measurements provided by each device will be stored, allowing an agile retribution of the current status or measurements collected by the devices in the scenario.


Following figure depicts the simplified structure of the different access points offered by the DAM & DQ and their interaction with the databases.


![DAM estructura](https://user-images.githubusercontent.com/60104587/198829875-532b6c6e-88d3-4d4d-9df4-ac0125592f28.png)


DAM & DQ component offers two types of interfaces or protocols for its operation. On the one hand, the REST interface is offered from which any type of data injection or gathering request can be made through the HTTP protocol. On the other hand, the Thrift interface allows communication with the component through the deployment of a client in the component, device or application that wishes to establish communication. The purpose of offering both interfaces for the use of the various functionalities offered by the component is to integrate with various equipment with different capabilities and resources. Both protocols offer the possibility of using different programming languages for the implementation of the client, without the need to adapt to the language used in the DAM & DQ.
For writing or injecting measurements through the component, one of the Json models described in the schemas contained in the program must be used. Among these schemas, the model for measurements or observations captured by sensors or devices, in an aggregated or simplified form, is described. Also included is the model for observations captured by collars mounted on the neck of livestock. Finally, the necessary schemas for the vehicle state vector data model are included. For a telemetry to be accepted in the component injection request, it has to be validated against one of the schemas. It will then be processed and converted to the specific format of the database to which it is intended to be written. 


The DQ functionality described by the component offers the possibility of data distribution through multiple clauses, conditions and filters on the repositories. The following table provides a list of the different queries offered by the component and the filters that allow the adaptation of the retribution of specific data:


![tabla](https://user-images.githubusercontent.com/60104587/198830010-f3513a31-e016-498f-9eb0-22b6a29415f7.png)


## Source Code:

.
├── Dockerfile
├── Executable.BAT
├── java
│   └── com
│       └── db
│           └── influxdb
│               └── Testing
│                   └── Test.java
├── lisenses
│   ├── AGPL-3.0
│   ├── Apache-2.0
│   └── License
├── pom.xml
└── src
    ├── SSL
    │   └── Info.txt
    ├── log
    └── main
        ├── java
        │   └── nrdb
        │       ├── afarcloud
        │       │   └── nrdb
        │       │       ├── config
        │       │       │   ├── AFC_DMA_Params.properties
        │       │       │   ├── AFC_DMA_log4j.xml
        │       │       │   ├── Constants.java
        │       │       │   └── log4j.dtd
        │       │       ├── dam
        │       │       │   ├── InfluxDataAccess.java
        │       │       │   ├── MySQLDataAccess.java
        │       │       │   ├── NumResult.java
        │       │       │   ├── SerializationToCSV.java
        │       │       │   ├── SerializationToCollarData.java
        │       │       │   ├── SerializationToJSON.java
        │       │       │   ├── SerializationToJSONConstants.java
        │       │       │   ├── SerializationToObject.java
        │       │       │   ├── SerializationToSensorData.java
        │       │       │   └── SerializationToStateVector.java
        │       │       ├── export
        │       │       │   ├── MySQLExportCollar.java
        │       │       │   └── MySQLExportOM.java
        │       │       ├── services
        │       │       │   ├── rest
        │       │       │   │   ├── DataLayer.java
        │       │       │   │   ├── GetCollarTelemetry.java
        │       │       │   │   ├── GetData.java
        │       │       │   │   ├── GetLogs.java
        │       │       │   │   ├── GetMeasurements.java
        │       │       │   │   ├── GetObservationsByCollar.java
        │       │       │   │   ├── GetObservationsBySensor.java
        │       │       │   │   ├── GetRegionTelemetry.java
        │       │       │   │   ├── GetSensorTelemetry.java
        │       │       │   │   ├── GetStateVector.java
        │       │       │   │   ├── GetStateVectorsByVehicle.java
        │       │       │   │   ├── Main.java
        │       │       │   │   ├── PostDataManagerApi.java
        │       │       │   │   ├── Schema.java
        │       │       │   │   ├── TelemetryExtractor.java
        │       │       │   │   ├── ValidationUtils.java
        │       │       │   │   └── store
        │       │       │   │       ├── JSONParser.java
        │       │       │   │       ├── JSONParserCollar.java
        │       │       │   │       ├── JSONParserInterface.java
        │       │       │   │       ├── JSONParserOM.java
        │       │       │   │       ├── JSONParserRegion.java
        │       │       │   │       ├── JSONParserUtils.java
        │       │       │   │       ├── JSONParserVehicle.java
        │       │       │   │       ├── MySQLLoader.java
        │       │       │   │       ├── MySQLLoaderCollar.java
        │       │       │   │       ├── MySQLLoaderOM.java
        │       │       │   │       ├── NRDBJSONException.java
        │       │       │   │       └── test
        │       │       │   │           ├── TestJSONParser.java
        │       │       │   │           ├── TestJSONParserCollar.java
        │       │       │   │           ├── TestJSONParserOM.java
        │       │       │   │           ├── TestJSONParserRegion.java
        │       │       │   │           ├── TestJSONParserVehicle.java
        │       │       │   │           └── TestMySQLLoaderOM.java
        │       │       │   └── thrift
        │       │       │       ├── SemanticQueryAsyncHandler.java
        │       │       │       ├── SemanticQueryServiceHandler.java
        │       │       │       ├── TelemetryExtractor.java
        │       │       │       ├── TelemetryExtractorMySQL.java
        │       │       │       └── test
        │       │       │           ├── TestQueryCollarData.java
        │       │       │           ├── TestQueryCollarDataMySQL.java
        │       │       │           ├── TestQuerySensorData.java
        │       │       │           ├── TestQuerySensorDataMySQL.java
        │       │       │           └── TestQueryVehicleStateVector.java
        │       │       └── util
        │       │           ├── DataTypes.java
        │       │           ├── GeoHashProximity.java
        │       │           ├── GlobalParameters.java
        │       │           ├── LogFile.java
        │       │           ├── SerializeToGeoJSON.java
        │       │           └── test
        │       │               └── TestGeoHash.java
        │       └── com
        │           └── afarcloud
        │               └── thrift
        │                   ├── Alarm.java
        │                   ├── AlgorithmStatus.java
        │                   ├── Battery.java
        │                   ├── CollarData.java
        │                   ├── Command.java
        │                   ├── CommandType.java
        │                   ├── DssAlgorithm.java
        │                   ├── DssService.java
        │                   ├── Equipment.java
        │                   ├── EquipmentType.java
        │                   ├── Mission.java
        │                   ├── MissionManagerService.java
        │                   ├── MissionTag.java
        │                   ├── MmtService.java
        │                   ├── MobileMmtService.java
        │                   ├── ObservationType.java
        │                   ├── Orientation.java
        │                   ├── PlannerService.java
        │                   ├── Position.java
        │                   ├── Region.java
        │                   ├── SemanticQueryService.java
        │                   ├── SensorData.java
        │                   ├── SensorType.java
        │                   ├── StateVector.java
        │                   ├── Task.java
        │                   ├── TaskCommandStatus.java
        │                   ├── TaskRegionType.java
        │                   ├── TaskTemplate.java
        │                   ├── TaskType.java
        │                   ├── Vehicle.java
        │                   └── VehicleType.java
        └── resources
            ├── config
            │   ├── AFC_DMA_Params.properties
            │   ├── AFC_DMA_log4j.xml
            │   ├── Constants.java
            │   └── log4j.dtd
            ├── docker-compose.yml
            ├── localSchemas
            │   ├── Json esquemas
            ├── schemas
            │   ├── CollarListSchema.json
            │   ├── CollarSchema.json
            │   ├── Definitions.json
            │   ├── GatewayListSchema.json
            │   ├── GetAssetsbyLocationSchema.json
            │   ├── MultiSensorListSchema.json
            │   ├── RegionListSchema.json
            │   ├── RegionSchema.json
            │   ├── SensorListSchema_Complete.json
            │   ├── SensorSchema_Complete.json
            │   ├── StateVectorSchema_global.json
            │   └── schemas.properties
            └── swagger-ui


