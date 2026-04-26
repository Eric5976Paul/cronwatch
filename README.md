# cronwatch

A daemon that monitors cron job execution times and sends alerts when jobs exceed expected durations.

## Installation

```bash
mvn clean install
java -jar target/cronwatch.jar
```

## Usage

Define your monitored jobs in `cronwatch.yml`:

```yaml
jobs:
  - name: daily-backup
    schedule: "0 2 * * *"
    max_duration: 300s
    alert: email
  - name: data-sync
    schedule: "*/15 * * * *"
    max_duration: 60s
    alert: slack
```

Start the daemon:

```bash
cronwatch --config cronwatch.yml
```

When a job exceeds its expected duration, cronwatch fires an alert to the configured channel with the job name, start time, and elapsed duration.

## Configuration

| Field          | Description                              |
|----------------|------------------------------------------|
| `name`         | Unique identifier for the cron job       |
| `schedule`     | Cron expression for expected run time    |
| `max_duration` | Maximum allowed execution time           |
| `alert`        | Alert channel (`email`, `slack`, `webhook`) |

## Requirements

- Java 17+
- Maven 3.8+

## License

This project is licensed under the [MIT License](LICENSE).