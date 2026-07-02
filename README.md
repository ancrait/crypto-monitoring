# Crypto Monitoring

A microservices-based crypto price monitoring system with Telegram bot notifications. Tracks BTC, ETH, SOL prices in real-time and sends alerts when your target price is hit.

## Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Binance API    │────▶│  Price Fetcher   │────▶│   PostgreSQL    │
└─────────────────┘     └────────┬─────────┘     └─────────────────┘
                                 │
                          Redis Pub/Sub
                                 │
          ┌──────────────────────┼──────────────────────┐
          ▼                      ▼                      ▼
┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
│  Price Alert    │   │  Telegram Bot   │   │   Redis Cache   │
│  Service        │   │  Service        │   └─────────────────┘
└────────┬────────┘   └────────┬────────┘
         │                     │
         └─────────────────────┘
              HTTP / REST
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| **price-fetcher** | 8081 | Fetches prices from Binance API every 5s, stores in PostgreSQL, publishes to Redis |
| **price-alert** | 8082 | Manages user alerts, listens for price updates, checks thresholds |
| **telegram-bot** | 8083 | Handles Telegram commands, receives alert notifications |
| **postgres** | 5433 | Stores price history and user alerts |
| **redis** | 6379 | Pub/Sub messaging + cache |

## Tech Stack

- **Java 21** + **Spring Boot 3.5**
- **PostgreSQL 16** — price history & alerts
- **Redis 7** — pub/sub (real-time notifications) + cache
- **Telegram Bots API** — user interaction
- **Binance API** — price data
- **Docker Compose** — container orchestration

## Flow

```
1. Price Fetcher polls Binance every 5s
       │
2. Publishes price to Redis channel "prices:updates"
       │
3. Price Alert listener checks user thresholds
       │
4. If triggered: publishes to Redis channel "alerts:notification"
       │
5. Telegram Bot listener receives notification
       │
6. Bot sends message to user in Telegram
```

## Quick Start (Docker)

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- Telegram Bot Token from [@BotFather](https://t.me/BotFather)

### Setup

```bash
# Clone the repository
git clone https://github.com/ancrait/crypto-monitoring.git
cd crypto-monitoring

# Create .env file with your bot token
cat > .env << EOF
DB_HOST=localhost
DB_PORT=5432
DB_NAME=crypto_monitoring
DB_USERNAME=crypto
DB_PASSWORD=crypto123
REDIS_HOST=localhost
REDIS_PORT=6379
BOT_TOKEN=YOUR_BOT_TOKEN_HERE
ALERT_SERVICE_HOST=price-alert
EOF

# start all services
docker compose -f docker/docker-compose.yml up --build
```

### Stop

```bash
docker compose -f docker/docker-compose.yml down
```

## Telegram Bot Commands

Start a chat with your bot on Telegram and use these commands:

| Command | Example | Description |
|---------|---------|-------------|
| `/start` | `/start` | Welcome message and command list |
| `/track` | `/track BTC 100000` | Create alert (default ABOVE) |
| `/track` | `/track ETH 5000 below` | Create alert BELOW price |
| `/list` | `/list` | Show all your alerts |
| `/get` | `/get BTC 100000 above` | Check specific alert |
| `/update` | `/update BTC 95000 100000 above` | Change target price/direction |
| `/untrack` | `/untrack BTC 100000` | Delete alert |
| `/pause` | `/pause BTC 100000` | Temporarily disable alert |
| `/resume` | `/resume BTC 100000` | Re-enable paused alert |
| `/commands` | `/commands` | Detailed help |

## API Endpoints

### Alert Service (port 8082)

| Method | URL | Description |
|--------|-----|-------------|
| `POST` | `/api/alerts` | Create alert |
| `GET` | `/api/alerts/{chatId}` | List all alerts for user |
| `GET` | `/api/alerts/{chatId}/{symbol}/{targetPrice}` | Get specific alert |
| `PUT` | `/api/alerts/{chatId}/{symbol}/{targetPrice}` | Update alert |
| `DELETE` | `/api/alerts/{chatId}/{symbol}/{targetPrice}` | Delete alert |
| `PATCH` | `/api/alerts/{chatId}/{symbol}/{targetPrice}/toggle` | Toggle pause/resume |

## Project Structure

```
crypto-monitoring/
├── docker/
│   ├── docker-compose.yml
│   ├── Dockerfile.price-fetcher
│   ├── Dockerfile.price-alert
│   └── Dockerfile.telegram-bot
├── price-fetcher/          # Spring Boot — price polling
├── price-alert/            # Spring Boot — alert management
├── telegram-bot/           # Spring Boot — bot + notifications
├── .env                    # environment variables (not committed)
└── .gitignore
```
