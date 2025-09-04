# Personal Task Management API (Java Spring Boot)

## **Overview**
This is a backend API for a personal task management application that supports offline-first functionality. Users can create, update, delete tasks while offline, and sync changes when they reconnect. This project is implemented in **Java Spring Boot** with **SQLite** as the database.

---

## **Tech Stack**
- **Backend:** Java 17, Spring Boot 3  
- **Database:** SQLite  
- **JSON Processing:** Jackson  
- **Version Control:** Git + GitHub  
- **Build Tool:** Maven  

---

## **Project Features**

1. **Task Management Endpoints**  
   - **GET /api/tasks** – Get all non-deleted tasks  
   - **GET /api/tasks/{id}** – Get a specific task  
   - **POST /api/tasks** – Create a new task  
   - **PUT /api/tasks/{id}** – Update a task  
   - **DELETE /api/tasks/{id}** – Soft delete a task  

2. **Offline Sync**  
   - Tasks created, updated, or deleted while offline are queued in a **SyncQueue**.  
   - Sync uses a **last-write-wins** strategy based on `updatedAt`.  
   - Sync queue supports **batch processing** with a configurable batch size (`50` by default).  
   - Failed sync operations are retried up to 3 times.  

3. **Sync API Endpoints**  
   - **POST /api/sync** – Trigger sync for all pending items  
   - **GET /api/status** – Check sync status and pending queue count  
   - **POST /api/batch** – Batch sync for server processing  

4. **Health Check**  
   - **GET /api/health** – Check server status  

---

## **Data Model**

**Task Entity**

| Field           | Type       | Description                                   |
|-----------------|------------|-----------------------------------------------|
| id              | String     | Unique UUID                                   |
| title           | String     | Task title (required)                         |
| description     | String     | Optional task description                     |
| completed       | boolean    | Task completion status                        |
| createdAt       | LocalDateTime | Task creation timestamp                    |
| updatedAt       | LocalDateTime | Last update timestamp                      |
| isDeleted       | boolean    | Soft delete flag                              |
| syncStatus      | String     |pending/synced/error                           |
| serverId        | String     | Server-assigned ID after sync                 |
| lastSyncedAt    | LocalDateTime | Last successful sync timestamp             |

**SyncQueue Entity**

| Field          | Type       | Description                       |
|----------------|------------|-----------------------------------|
| id             | Long       | Auto-generated ID                 |
| taskId         | String     | ID of the task being synced       |
| operationType  | String     | `create` / `update` / `delete`    |
| taskData       | String     | JSON snapshot of the task         |
| retryAttempts  | int        | Number of retry attempts          |
| status         | String     | `pending` / `error`               |
| createdAt      | LocalDateTime | Timestamp when added to queue  |

---

## **Request & Response Examples**

### **1. Create Task**
**Request:**
json
POST /api/tasks
{
  "title": "New task",
  "description": "Optional description"
}
Response (201):
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "New task",
  "description": "Optional description",
  "completed": false,
  "createdAt": "2025-09-04T10:00:00",
  "updatedAt": "2025-09-04T10:00:00",
  "isDeleted": false,
  "syncStatus": "pending",
  "serverId": null,
  "lastSyncedAt": null
}
2. Get All Tasks

Request:

GET /api/tasks

Response:

[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "New task",
    "description": "Optional description",
    "completed": false,
    "createdAt": "2025-09-04T10:00:00",
    "updatedAt": "2025-09-04T10:00:00",
    "isDeleted": false,
    "syncStatus": "pending",
    "serverId": null,
    "lastSyncedAt": null
  }
]
3. Update Task

Request:
PUT /api/tasks/550e8400-e29b-41d4-a716-446655440000
{
  "title": "Updated task",
  "description": "Updated description",
  "completed": true
}
Response:
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Updated task",
  "description": "Updated description",
  "completed": true,
  "createdAt": "2025-09-04T10:00:00",
  "updatedAt": "2025-09-04T11:00:00",
  "isDeleted": false,
  "syncStatus": "pending",
  "serverId": null,
  "lastSyncedAt": null
}
4. Delete Task

Request:

DELETE /api/tasks/550e8400-e29b-41d4-a716-446655440000


Response: 204 No Content

5. Sync Tasks

Request:

POST /api/sync


Response:

{
  "success": true,
  "synced_items": 1,
  "failed_items": 0,
  "errors": []
}

6. Check Sync Status

Request:

GET /api/status


Response:

{
  "pending_sync_count": 0,
  "last_sync_timestamp": "2025-09-04T11:00:00",
  "is_online": true,
  "sync_queue_size": 0
}

How to Run Locally

Clone your private fork:

git clone https://github.com/Asad-code79/backend-interview-challenge.git
cd backend-interview-challenge/Task/Task


Build and run the Spring Boot app:

mvn clean install
mvn spring-boot:run


API is available at:

http://localhost:8080/api

Assumptions

Offline tasks are always queued in SyncQueue.

Conflict resolution uses last-write-wins based on updatedAt.

Batch sync size defaults to 50 and can be configured in application.properties.

No authentication required for simplicity.

Challenges

Mapping LocalDateTime to JSON required Jackson JSR310 module.

Designing a proper sync queue mechanism to handle retries and errors.
