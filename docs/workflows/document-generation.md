# 1. Document Generation Workflow

## BPMN Flow

```text
Start Event
    |
    v
Generate Document
    |
    v
Publish Kafka Event
    |
    v
Complete Workflow
```

### Workflow Description
- User selects a profile and target job.
- workflow-service starts a Camunda process instance.
- ai-generation-service generates the requested document.
- Kafka event is published to document.generated.
- document-service consumes the event.
- Generated markdown document is stored in MinIO.
- Metadata is saved to PostgreSQL.

### Job Types
|Job Type|Description|
|---|---|
|generate-document|AI document generation|
|publish-document-event|Kafka event publishing|
### Kafka Topics
|Topic|Purpose|
|---|---|
|document.generated|Generated document events|

# 2. BPMN documentation