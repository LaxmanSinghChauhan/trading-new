# Quick Start Guide

## 🚀 Quick Start (5 minutes)

### Option 1: Docker (Recommended)

```bash
# Clone and navigate to project
cd kite-algo-trading

# Start with Docker Compose
docker-compose up -d

# Access the dashboard
open http://localhost:8080/index.html
```

### Option 2: Local Development

```bash
# Prerequisites: Java 17, Maven 3.6+, PostgreSQL 12+

# Start PostgreSQL
# Create database: kite_trading

# Run the application
./start.sh

# Or manually:
mvn spring-boot:run
```

## 📱 First Steps

1. **Access Dashboard**: Open `http://localhost:8080/index.html`

2. **Place Test Order**:
   - Enter symbol (e.g., RELIANCE)
   - Select BUY/SELL
   - Set quantity
   - Click "Place Order"

3. **Run Backtest**:
   - Enter symbol and days
   - Click "Run Backtest"
   - View results

4. **Monitor Performance**:
   - Watch real-time statistics
   - View recent orders and trades
   - Check P&L and win rate

## 🔧 Configuration

Edit `src/main/resources/application.properties`:

```properties
# Enable/disable paper trading
trading.paper-trading=true

# Set default quantity
trading.default-quantity=1

# Configure strategy parameters
strategy.sudden-movement.threshold=2.0
strategy.execution.interval=60000
```

## 📊 Key Features

- ✅ Real-time market data
- ✅ Order management
- ✅ Sudden movement detection
- ✅ Backtesting engine
- ✅ Interactive dashboard
- ✅ Paper trading mode
- ✅ PostgreSQL persistence

## 🆘 Troubleshooting

**Port 8080 already in use?**
```bash
# Change port in application.properties
server.port=8081
```

**Database connection failed?**
```bash
# Check PostgreSQL is running
pg_isready

# Verify database exists
psql -l | grep kite_trading
```

**Build failed?**
```bash
# Clean and rebuild
mvn clean install
```

## 📚 Next Steps

- Read the full [README.md](README.md)
- Explore API endpoints
- Configure trading strategies
- Set up Kite Connect for live trading
- Customize strategy parameters

## ⚠️ Important Notes

- Start with paper trading mode
- Test strategies thoroughly
- Monitor performance metrics
- Use proper risk management
- Keep API credentials secure

---

**Need help? Check the full README.md for detailed documentation.**
