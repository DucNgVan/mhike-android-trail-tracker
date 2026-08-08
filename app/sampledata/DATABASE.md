# Database Design

## Hikes

-   id (PK)
-   name
-   location
-   date
-   parking
-   length
-   difficulty
-   description
-   customField1
-   customField2

## Observations

-   id (PK)
-   hikeId (FK)
-   observation
-   time
-   comments

Relationship: Hike (1) ---- (\*) Observation
