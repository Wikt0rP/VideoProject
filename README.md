# VideoAPI21

A simple video upload and playback application built with Spring Boot and vanilla JavaScript.

## Requirements

To run this project, you need the following tools installed:

*   **Java 21**
*   **Maven**
*   **PostgreSQL** (database)
*   **Kafka** (for background video processing)
*   **FFmpeg** (for thumbnail generation and video processing)

## Configuration

Before running the application, you must set the following environment variables:

| Variable | Description |
| :--- | :--- |
| `DB_NAME` | PostgreSQL database name |
| `DB_USERNAME` | Database user |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | Secret key for signing JWT tokens |
| `FFMPEG_PATH` | Path to the FFmpeg executable |

Also, ensure that Kafka is available at `host.docker.internal:9092` (or update `application.properties`).

## Features

*   User registration and login (JWT).
*   Video file upload.
*   Video playback.
*   HLS format support (background processing via Kafka).
